(ns cron-condenser.merger
  (:require
   [clojure.set :refer [join]]
   [cron-condenser.validator :refer [map->CronExpression]])
  (:import
   [cron_condenser.validator CronExpression]))


(defn ^Boolean equal-for?
  [^CronExpression a ^CronExpression b keywords]
  (->> keywords
       (map #(= (a %) (b %)))
       (reduce #(and %1 %2))))

(defn ^Boolean mergeable?
  [^CronExpression a ^CronExpression b]
  (or (equal-for? a b '(:hour   :day  :month :week-day))
      (equal-for? a b '(:minute :day  :month :week-day))
      (equal-for? a b '(:minute :hour :month :week-day))
      (equal-for? a b '(:minute :hour :day   :week-day))
      (equal-for? a b '(:minute :hour :day   :month))))

(defn ^CronExpression merge
  [^CronExpression a ^CronExpression b]
  (->> '(:minute :hour :day :month :week-day)
       (map #(vector % (join (a %) (b %))))
       (into {})
       map->CronExpression))
