(ns cron-condenser.cron-expression
  (:import
   [clojure.lang PersistentHashSet]))


(defrecord CronExpression
    [^PersistentHashSet minute
     ^PersistentHashSet hour
     ^PersistentHashSet day
     ^PersistentHashSet month
     ^PersistentHashSet week-day])
