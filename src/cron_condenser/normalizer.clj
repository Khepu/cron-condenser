(ns cron-condenser.normalizer
  (:require
   [clojure.math.combinatorics :as comb]
   [cron-condenser.validator :refer [->CronExpression]])
  (:import
   [clojure.lang PersistentList]
   [cron_condenser.validator CronExpression]))

(defn ^PersistentList normalize
  [^CronExpression cron-expression]
  (->> cron-expression
       vals
       (apply comb/cartesian-product)
       (map #(apply ->CronExpression %))))
