(ns mob4clojure.core
  (:require [mob4clojure.util :as util])
  (:import [java.util TimeZone]
           [org.joda.time DateTimeZone])
  (:gen-class))


(defn -main [& args]
  ;; Print configuration name.
  (.println *err* (format "mob4clojure.config.name: %s"
                          (:mob4clojure.config.name (util/get-config))))
  ;; Set default timezone in JDK and JodaTime as well.
  (TimeZone/setDefault (util/get-timezone))
  (DateTimeZone/setDefault (DateTimeZone/forTimeZone (util/get-timezone)))
  ;; For server arguments, see as follows:
  ;; http://www.h2database.com/html/tutorial.html#using_server
  (org.h2.tools.Server/main args))
