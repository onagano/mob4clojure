(ns mob4clojure.util
  (:use [clojure.contrib.def :only [defvar defvar-]])
  (:import [java.util Properties Calendar Locale TimeZone SimpleTimeZone])
  (:import [org.joda.time DateTime DateTimeZone]))


;;;; Configuration

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


;;;; Helper functions for timezone and locale

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


;;;; Timezone

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

(def joda-timezones
  {:sydney    (DateTimeZone/forID "Australia/Sydney")
   :tokyo     (DateTimeZone/forID "Asia/Tokyo")
   :fxddmalta (DateTimeZone/forTimeZone fxddmalta-tz)
   :london    (DateTimeZone/forID "Europe/London")
   :utc       (DateTimeZone/forID "Etc/UTC")
   :newyork   (DateTimeZone/forID "America/New_York")
   :default   (DateTimeZone/getDefault)})

(defvar- timezone (atom nil))

(defn- init-timezone
  "Looks custom timezones first then falls back in JDK's one."
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

(defvar server-epoch
  (.toDate (DateTime. 1970 1 1 0 0 0 0 (DateTimeZone/forTimeZone (get-timezone))))
  "Returns JDK Date object at 1970-01-01 00:00:00.0 in the instance's timezone")


;;;; Identifier case conversion

(defn clojure-case
  "Returns a string which is lower-cased and replaced all '_' with '-'."
  [^String str]
  (-> str .toLowerCase (.replaceAll "_" "-")))

(defn sql-case
  "Returns a string which is upper-cased and replaced all '-' with '_'."
  [^String str]
  (-> str .toUpperCase (.replaceAll "-" "_")))
