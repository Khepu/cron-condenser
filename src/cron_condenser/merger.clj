(ns cron-condenser.merger
  (:require
   [clojure.set :refer [union]]
   [cron-condenser.validator :refer [map->CronExpression]])
  (:import
   [cron_condenser.validator CronExpression]))


(defn ^Boolean equal-for?
  [^CronExpression a ^CronExpression b keywords]
  (->> keywords
       (map #(= (a %) (b %)))
       (reduce #(and %1 %2))))

(defn ^Boolean mergeable?
  "Two crons can be merged iff they differ in only one of the columns."
  [^CronExpression a ^CronExpression b]
  (or (equal-for? a b '(:hour   :day  :month :week-day))
      (equal-for? a b '(:minute :day  :month :week-day))
      (equal-for? a b '(:minute :hour :month :week-day))
      (equal-for? a b '(:minute :hour :day   :week-day))
      (equal-for? a b '(:minute :hour :day   :month))))

(defn ^CronExpression merge
  [^CronExpression a ^CronExpression b]
  (->> '(:minute :hour :day :month :week-day)
       (map #(vector % (union (a %) (b %))))
       (into {})
       map->CronExpression))
