(ns cron-condenser.contractor
  (:require
   [cron-condenser.constants :refer [minute-bounds
                                     hour-bounds
                                     day-bounds
                                     month-bounds
                                     week-day-bounds]]
   [cron-condenser.cron-expression :refer [->CronExpression]])
  (:import
   [cron_condenser.cron_expression CronExpression]))


(defn contract-minute
  [minute-segment]
  (let [segment-length (count minute-segment)]
    (cond
      (= segment-length (- (:upper minute-bounds)
                           (:lower minute-bounds))) #{"*"}
      :else minute-segment)))

(defn contract-hour
  [hour-segment]
  (let [segment-length (count hour-segment)]
    (cond
      (= segment-length (- (:upper hour-bounds)
                           (:lower hour-bounds))) #{"*"}
      :else hour-segment)))

(defn contract-day
  [day-segment]
  (let [segment-length (count day-segment)]
    (cond
      (= segment-length (- (:upper day-bounds)
                           (:lower day-bounds))) #{"*"}
      :else day-segment)))

(defn contract-month
  [month-segment]
  (let [segment-length (count month-segment)]
    (cond
      (= segment-length (- (:upper month-bounds)
                           (:lower month-bounds))) #{"*"}
      :else month-segment)))

(defn contract-week-day
  [week-day-segment]
  (let [segment-length (count week-day-segment)]
    (cond
      (= segment-length (- (:upper week-day-bounds)
                           (:lower week-day-bounds))) #{"*"}
      :else week-day-segment)))

(defn contract
  ^CronExpression [^CronExpression cron]
  (->CronExpression (contract-minute   (:minute   cron))
                    (contract-hour     (:hour     cron))
                    (contract-day      (:day      cron))
                    (contract-month    (:month    cron))
                    (contract-week-day (:week-day cron))))
