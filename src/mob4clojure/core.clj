(ns mob4clojure.core
  (:gen-class))

(def  environment (atom nil))

(defn- get-environment-properties
  []
  (let [prop (doto (java.util.Properties.)
               (.load (.getResourceAsStream (class "") "/mob4clojure.properties")))
        keys (enumeration-seq (.keys prop))]
    (apply hash-map
           (apply concat
                  (for [k keys] [(keyword k) (.get prop k)])))))

(defn -main [& args]
  (swap! environment (fn [_] (get-environment-properties)))
  (.println *err* (format "mob4clojure.env.name: %s"
                          (:mob4clojure.env.name @environment)))
  ;; For server arguments:
  ;; http://www.h2database.com/html/tutorial.html#using_server
  (org.h2.tools.Server/main args))
