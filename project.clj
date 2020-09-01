(defproject vigiscrapper "0.1.0-SNAPSHOT"
  :description "vigiscrapper"
  :url "http://github.com/mcorbin/vigiscrapper"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.logging "1.1.0"]
                 [exoscale/yummy "0.2.6"]
                 [org.influxdb/influxdb-java "2.20"]
                 [clj-http "3.10.2"]
                 [cheshire "5.10.0"]]
  :main ^:skip-aot vigiscrapper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
