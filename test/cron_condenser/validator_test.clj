(ns cron-condenser.validator-test
  (:require [clojure.test :refer [is deftest]]
            [cron-condenser.validator :refer [in-bounds? explain-invalid]]))

(deftest test-in-bounds?
  (is (= true ((in-bounds? 0 5) 3)))
  (is (= true ((in-bounds? 0 5) 0)))
  (is (= false ((in-bounds? 0 5) 5)))
  (is (= false ((in-bounds? 0 5) -1)))
  (is (= false ((in-bounds? 4 2) -1)))
  (is (= false ((in-bounds? 4 2) 4)))
  (is (= false ((in-bounds? 4 2) 3)))
  (is (= false ((in-bounds? 4 2) 2))))

(deftest test-explain-invalid
  (let [msg "The following segments did not pass validation: "]
    ;; Invalid ranges
    (is (= (str msg "day") (explain-invalid "0 0 0 JAN 0")))
    (is (= (str msg "day, month") (explain-invalid "0 0 0 DAN 0")))
    (is (= (str msg "minute, hour, day, month, week-day") (explain-invalid "99 99 99 99 99")))
    ;; Invalid segment count
    (is (= "Invalid number of segments. Found 1 instead of 5!" (explain-invalid "")))
    (is (= "Invalid number of segments. Found 6 instead of 5!" (explain-invalid "0 0 1 JAN  0")))))
