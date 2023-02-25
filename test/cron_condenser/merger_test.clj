(ns cron-condenser.merger-test
  (:require
   [clojure.test :refer [is deftest]]
   [cron-condenser.expander :refer [expand]]
   [cron-condenser.merger :refer [mergeable?]]
   [cron-condenser.cron-expression :refer [str->CronExpression]]))

(deftest test-mergeable?
  (let [every-second-hour   (expand (str->CronExpression "0 */2 * * *"))
        every-second-minute (expand (str->CronExpression "*/2 * * * *"))
        every-fourth-hour   (expand (str->CronExpression "0 */4 * * *"))]
    (is (= :hour (mergeable? every-second-hour every-fourth-hour)))
    (is (nil? (mergeable? every-second-hour every-second-minute)))
    (is (nil? (mergeable? every-fourth-hour every-second-minute)))))
