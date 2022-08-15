(ns cron-condenser.normalizer
  (:require
   [clojure.math.combinatorics :as comb]
   [cron-condenser.cron-expression :refer [->CronExpression]])
  (:import
   [clojure.lang PersistentList]
   [cron_condenser.cron_expression CronExpression]))

(defn ^PersistentList normalize
  [^CronExpression cron-expression]
  (->> cron-expression
       vals
       (apply comb/cartesian-product)
       (map #(apply ->CronExpression %))))
