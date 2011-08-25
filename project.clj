(defproject mob4clojure "0.1.0-prerelease"
  :description "An application of MOB (MT4-ODBC Bridge) for Clojure programming language"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [com.h2database/h2 "1.3.157"]
                 [joda-time/joda-time "1.6.2"]]
  :dev-dependencies [[swank-clojure "1.3.2"]]
  :main mob4clojure.core)
