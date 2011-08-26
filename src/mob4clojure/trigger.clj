(ns mob4clojure.trigger
  (:import [org.h2.api Trigger])
  (:gen-class
   :name mob4clojure.GenericTrigger
   :implements [org.h2.api.Trigger]
   :state state
   :init cljinit)
  (:require [mob4clojure.util :as util]))


;; Trigger-name-keyword and called-function pairs.
;; The function should take 3 arguments: state, old-row, new-row.
(def trig-funs
  (agent {}))

(defn register-function
  [trig-kw func]
  (send trig-funs assoc trig-kw func))

(defn unregister-function
  [trig-kw]
  (send trig-funs dissoc trig-kw))


;; How to register a trigger in H2:
;; CREATE TRIGGER trigger AFTER [UPDATE|...] ON table
;;   FOR EACH ROW CALL "mob4clojure.GenericTrigger"
;; http://www.h2database.com/html/grammar.html#create_trigger

(defn -cljinit
  []
  [[] (atom {})])

(defn- column-spec
  "Returns column labels of the table."
  [conn schema tabname]
  (let [sql (format "select * from %s.%s where 1 = 2" schema tabname)]
    (with-open [stmt (.createStatement conn)]
      (with-open [rset (.executeQuery stmt sql)] 
	(let [meta (.getMetaData rset)
	      idxs (range 1 (inc (.getColumnCount meta)))
	      lbls (map #(.getColumnLabel meta %) idxs)]
	  (doall (map (comp keyword util/clojure-case) lbls)))))))

(def sql-ope-types
  {Trigger/INSERT :insert
   Trigger/UPDATE :update
   Trigger/DELETE :delete
   Trigger/SELECT :select})

(defn -init
  [this conn schema trigger table before type]
  (swap! (.state this) assoc 
	   :conn conn
	   :schema (keyword (util/clojure-case schema))
	   :trigger (keyword (util/clojure-case trigger))
	   :table (keyword (util/clojure-case table))
	   :before before
	   :type (sql-ope-types type)
	   :struct (apply create-struct (column-spec conn schema table))))

(defn -fire
  [this conn oldRow newRow]
  (let [init @(.state this)
        agnt trig-funs
        func ((:trigger init) @agnt)]
    (when func
      (let [rstr (:struct init)
            oldr (apply struct rstr oldRow)
            newr (apply struct rstr newRow)
            args [init oldr newr]]
        (send agnt (fn [m] (apply func args) m))))))

(defn -close
  [this]
  nil)

(defn -remove
  [this]
  nil)
