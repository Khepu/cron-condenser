(ns cron-condenser.cron-expression
  (:require
   [clojure.string :as string])
  (:import
   [clojure.lang PersistentHashSet]))


(defrecord CronExpression
    [^PersistentHashSet minute
     ^PersistentHashSet hour
     ^PersistentHashSet day
     ^PersistentHashSet month
     ^PersistentHashSet week-day])

(defn str->CronExpression
  ^CronExpression [cron-str]
  (->> (string/split cron-str #" ")
       (map hash-set)
       (apply ->CronExpression)))

(defn CronExpression->str
  [^CronExpression cron]
  (str (string/join "," (:minute   cron)) " "
       (string/join "," (:hour     cron)) " "
       (string/join "," (:day      cron)) " "
       (string/join "," (:month    cron)) " "
       (string/join "," (:week-day cron))))

(defn CronExpression->canonical-string
  [^CronExpression cron]
  (str (string/join "," (:minute   cron)) " "
       (string/join "," (:hour     cron)) " "
       (string/join "," (:day      cron)) " "
       (string/join "," (:month    cron)) " "
       (string/join "," (:week-day cron))))
