(ns cron-condenser.planner
  (:require
   [clojure.set :refer [union intersection]]
   [cron-condenser.merger :refer [mergeable? merge]])
  (:import
   [clojure.lang PersistentHashSet]))

(defn intersection-over-union
  "Intersection over union is used to quantify how compatible 2 crons are. As
  the score gets closer to one between two crons, they are considered a better
  match to merge."
  [^PersistentHashSet set-a ^PersistentHashSet set-b]
  (let [ab-union (count (union set-a set-b))
        ab-intersection (count (intersection set-a set-b))]
    (/ ab-intersection ab-union)))

(defn merge-graph
  "Produces a graph of possible merges for each item in `crons` per cron segment."
  [^PersistentHashSet crons]
  (map (fn [cron]
         (dissoc (->> crons
                      (filter #(not= % cron))
                      (group-by #(mergeable? cron %)))
                 nil))
       crons))

(defn longest
  [a key-b matches-b]
  (let [current-segment-length (count matches-b)]
    (if (> current-segment-length (:length a))
      {key-b matches-b
       :length current-segment-length}
      a)))

(defn prune
  "Keep only the longest 'branch' of valid merges."
  [sub-graph]
  (dissoc (reduce-kv longest
                     {:length -1}
                     sub-graph)
          :length))
