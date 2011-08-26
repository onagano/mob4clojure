(ns mob4clojure.util
  (:use [clojure.contrib.def :only [defvar defvar-]])
  (:import [java.util Properties Calendar Locale TimeZone SimpleTimeZone]))


(defvar- config (atom nil))

(defn- init-config
  []
  (let [prop (doto (Properties.)
               (.load (.getResourceAsStream (class "") "/mob4clojure.properties")))
        keys (enumeration-seq (.keys prop))]
    (apply hash-map
           (apply concat
                  (for [k keys] [(keyword k) (.get prop k)])))))

(defn get-config
  "Returns configuration hash-map of this instance."
  []
  (if-let [c @config]
    c
    (do (swap! config (fn [_] (init-config)))
        @config)))

(defn find-timezone
  [ptn]
  (for [tz (TimeZone/getAvailableIDs)
	:when (re-find (re-pattern (str "(?i)" ptn)) tz)] tz))

(defn find-locale
  [ptn]
  (for [lc (Locale/getAvailableLocales)
	:when (re-find (re-pattern (str "(?i)" ptn))
		       (.getDisplayName lc Locale/US))]
    [(.getDisplayName lc Locale/US) lc]))

(defvar- fxddmalta-tz
  (SimpleTimeZone. (* 2 60 60 1000) "Custom/FXDD_Malta"
		   Calendar/MARCH 8 (* -1 Calendar/SUNDAY)
		   3600000
		   Calendar/NOVEMBER 1 (* -1 Calendar/SUNDAY)
		   7200000
		   3600000)
  "Timezone used at FXDD Malta.
   This is same as 'America/New_York' except the raw offset (NY time + 7 hours).")

(defvar custom-timezones
  {(.getID fxddmalta-tz) fxddmalta-tz}
  "User defined timezones.")

(defvar- timezone (atom nil))

(defn- init-timezone
  []
  (let [tzid (:mob4clojure.mt4.timezone (get-config))
        custz (custom-timezones tzid)]
    (if custz
      custz
      (TimeZone/getTimeZone tzid))))

(defn get-timezone
  "Returns timezone object of this instance."
  []
  (if-let [tz @timezone]
    tz
    (do (swap! timezone (fn [_] (init-timezone)))
        @timezone)))
