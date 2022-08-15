(ns cron-condenser.planner
  (:require
   [clojure.set :refer [union intersection]]))

(defn intersection-over-union
  "Intersection over union is used to quantify how compatible 2 crons are. As
  the score gets closer to one between two crons, they are considered a better
  match to merge."
  [set-a set-b]
  (let [ab-union (count (union set-a set-b))
        ab-intersection (count (intersection set-a set-b))]
    (/ ab-intersection ab-union)))
