(ns mob4clojure.client.mob)


(def operations
  {:send 1
   :close 2
   :close-by 3
   :delete 4
   :modify 5
   :dump-trades 6
   :dump-history 7
   :get-account 8
   :get-trade 9
   :copy-rates 10})

(def order-types
  {:buy 0
   :sell 1
   :buy-limit 2
   :sell-limit 3
   :buy-stop 4
   :sell-stop 5})

(def periods
  {:tick 0
   :m1 1
   :m5 5
   :m15 15
   :m30 30
   :h1 60
   :h4 240
   :d1 1440
   :w1 10080
   :mn1 43200})

(defn send-order
  ([ord sym lot]
     (send-order ord sym lot :comment ""))
  ([ord sym lot & kvs]
     (apply assoc {:operation (:send operations)
		   :type (ord order-types)
		   :symbol sym
		   :lots lot}
	    kvs)))

(defn close-trade
  [ticket]
  {:operation (:close operations)
   :ticket ticket})

(defn close-trade-by
  [ticket opposite]
  {:operation (:close-by operations)
   :ticket ticket
   :opposite-ticket opposite})

(defn delete-order
  [ticket]
  {:operation (:delete operations)
   :ticket ticket})

(defn modify-order
  [ticket & kvs]
  (apply assoc {:operation (:modify operations)
		:ticket ticket} kvs))

(defn dump-trades
  []
  {:operation (:dump-trades operations)})

(defn dump-history
  []
  {:operation (:dump-history operations)})

(defn get-account
  []
  {:operation (:get-account operations)})

(defn get-trade
  [ticket]
  {:operation (:get-trade operations)
   :ticket ticket})

(defn copy-rates
  [symbol period]
  {:operation (:copy-rates operations)
   :symbol symbol
   :type (period periods)})
