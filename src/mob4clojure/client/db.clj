(ns mob4clojure.client.db
  (:require [mob4clojure.util :as util]
            [clojure.contrib.sql :as sql]))


(def db-spec
  {:classname   (:mob4clojure.db.classname (util/get-config))
   :subprotocol (:mob4clojure.db.subprotocol (util/get-config))
   :subname     (:mob4clojure.db.subname (util/get-config))
   :user        (:mob4clojure.db.user (util/get-config))
   :password    (:mob4clojure.db.password (util/get-config))})

(def tab-prefix
  (:mob4clojure.table.prefix (util/get-config)))

(def order-tab
  (str tab-prefix "ORDER"))

(def order-seq
  (str order-tab "_SEQ"))

(def trade-tab
  (str tab-prefix "TRADE"))

(defn- to-sql-case-map
  [m]
  (apply hash-map
         (apply concat
                (for [k (keys m)] [(util/sql-case (name k)) (k m)]))))

(defn- insert-order
  "Insert an order map and returns its ID (not ticket)."
  [ord]
  (let [[id] (sql/do-commands (str "CALL NEXTVAL('" order-seq "')"))]
    (sql/insert-records order-tab (assoc (to-sql-case-map ord) "ID" id))
    id))

(defn- select-order
  "Select an order by ID."
  [id]
  (sql/with-query-results rs
    [(str "SELECT * FROM " order-tab " WHERE ID = ?") id]
    (first rs)))

(defn- select-order-until
  "Poll an order until its OPERATION value becomes negative (means order executed)."
  [id]
  (let [ord (select-order id)]
    (if (neg? (:operation ord))
      ord
      (do
        (Thread/sleep 500)
        (recur id)))))

(defn order
  "Send order and returns its result."
  [ord]
  (sql/with-connection db-spec
    (let [id (insert-order ord)]
      (Thread/sleep 100)
      (select-order-until id))))

(defn open-trades
  "Current open trades."
  []
  (sql/with-connection db-spec
    (sql/with-query-results rs
      [(str "SELECT * FROM " trade-tab
            " WHERE CLOSE_TIME = '1970-01-01 00:00:00.0' ORDER BY TICKET")]
      (doall rs))))

(defn trade
  "Returns a trade status. Only search TRADE table for it, doesn't HISTORY table."
  [ticket]
  (sql/with-connection db-spec
    (sql/with-query-results rs
      [(str "SELECT * FROM " trade-tab " WHERE TICKET = ?") ticket]
      (first rs))))
