(ns cron-condenser.merger
  (:require
   [clojure.set :refer [union]])
  (:import
   [cron_condenser.cron_expression CronExpression]))


(defn ^Boolean equal-for?
  [^CronExpression a ^CronExpression b keywords]
  (->> keywords
       (map #(= (a %) (b %)))
       (reduce #(and %1 %2))))

(defn mergeable?
  "Two crons can be merged iff they differ in only one of the columns. Returns the
  segment to merge on or nil if they cannot be merged"
  [^CronExpression a ^CronExpression b]
  (cond (equal-for? a b '(:hour   :day  :month :week-day)) :minute
        (equal-for? a b '(:minute :day  :month :week-day)) :hour
        (equal-for? a b '(:minute :hour :month :week-day)) :day
        (equal-for? a b '(:minute :hour :day   :week-day)) :month
        (equal-for? a b '(:minute :hour :day   :month))    :week-day
        :else nil))

(defn ^CronExpression merge
  [^CronExpression a ^CronExpression b segment]
  (update-in a
             segment
             #(union % (b segment))))
