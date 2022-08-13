(ns cron-condenser.validator-test
  (:require [clojure.test :refer [is deftest]]
            [cron-condenser.validator :refer [in-bounds?]]))

(deftest test-in-bounds?
  (is (= true ((in-bounds? 0 5) 3)))
  (is (= true ((in-bounds? 0 5) 0)))
  (is (= false ((in-bounds? 0 5) 5)))
  (is (= false ((in-bounds? 0 5) -1)))
  (is (= false ((in-bounds? 4 2) -1)))
  (is (= false ((in-bounds? 4 2) 4)))
  (is (= false ((in-bounds? 4 2) 3)))
  (is (= false ((in-bounds? 4 2) 2))))
