(ns vigiscrapper.core
  (:require [yummy.config    :refer [load-config]]
            [clj-http.client :as client]
            [clojure.tools.logging :as log])
  (:import (java.util.concurrent TimeUnit)
           (org.influxdb InfluxDB InfluxDBFactory InfluxDB$ConsistencyLevel)
           (org.influxdb.dto BatchPoints Point))
  (:gen-class))

(def types [{:key "H" :measurement "niveau"}
            {:key "Q" :measurement "débit"}])

(defn fetch
  [station timestamp type]
  (let [url (str
             "https://www.vigicrues.gouv.fr/services/observations.json/index.php?CdStationHydro="
             station
             "&GrdSerie="
             type
             "&FormatSortie=simple&_="
             timestamp)]
    (log/info "fetch" url)
    (client/get url {:as :json})))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [stations influx]} (load-config {:path (first args)})
        ts (System/currentTimeMillis)]
    (doseq [station stations]
      (doseq [type types]
        (let [serie (-> (fetch station ts (:key type))
                        :body
                        :Serie)
              _ (when-not serie
                  (throw (ex-info (str "serie" station "not found for key" (:key type))
                                  {})))
              influx-client (InfluxDBFactory/connect (:address influx)
                                                     (:username influx)
                                                     (:password influx))
              batchpoint (.build (doto
                                   (BatchPoints/database (:database influx))
                                   (.tag "nom" (:CdStationHydro serie))
                                   (.tag "code" (:LbStationHydro serie))))]
          (doseq [[time value] (:ObssHydro serie)]
            (.point batchpoint (.build (doto
                                         (Point/measurement (:measurement type))
                                         (.time time TimeUnit/MILLISECONDS)
                                         (.addField "value" (float value))))))
          (.write influx-client batchpoint))))))
