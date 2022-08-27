(ns cron-condenser.planner
  (:require
   [clojure.set :refer [union intersection]]
   [cron-condenser.merger :refer [mergeable? merge-crons]])
  (:import
   [clojure.lang PersistentHashSet]
   [cron_condenser.cron_expression CronExpression]))

(defn intersection-over-union
  "Intersection over union is used to quantify how compatible 2 crons are. As
  the score gets closer to one between two crons, they are considered a better
  match to merge."
  [^PersistentHashSet set-a ^PersistentHashSet set-b]
  (let [ab-union (count (union set-a set-b))
        ab-intersection (count (intersection set-a set-b))]
    (/ ab-intersection ab-union)))

(defn branch
  [^PersistentHashSet crons
   ^CronExpression cron]
  (let [crons (disj crons cron)]
    {cron (dissoc (group-by #(mergeable? cron %) crons)
                  nil)}))

(defn ->merge-graph
  "Produces a graph of possible merges for each item in `crons` per cron segment."
  #_{cron-0 {:minute [cron-1 ... cron-n]}}
  [^PersistentHashSet crons]
  (->> crons
       (map #(branch crons %))
       (apply merge)))

(defn longest
  [a key-b matches-b]
  (let [current-segment-length (count matches-b)]
    (if (> current-segment-length (:length a))
      {key-b matches-b
       :length current-segment-length}
      a)))

;;TODO Calculate lengths before-hand
(defn prune
  "Keep only the longest 'branch' of valid merges."
  [sub-graph]
  (dissoc (reduce-kv longest
                     {:length -1}
                     sub-graph)
          :length))

(defn count-connections
  [connection-map]
  (reduce-kv (fn [total-connections type connections]
               (+ total-connections (count connections)))
             0
             connection-map))

(defn least-connected-node
  [origin origin-connections node-connections]
  (let [node-connections (dissoc node-connections origin)
        connection-set (reduce union (vals origin-connections))]
    (->> connection-set
         (sort-by #(node-connections %))
         first)))

(defn least-connected-merge
  "Merges the nodes with the least connections"
  [merge-graph]
  (let [node-connections (->> merge-graph
                              (mapv (fn [[node connection-map]]
                                      [node (count-connections connection-map)]))
                              (filter #(not= (second %) 0))
                              (into {}))]
    (if (empty? node-connections)
      merge-graph
      (let [[least-connected-origin _] (reduce-kv (fn [least-connected target target-connection-count]
                                                    (if (< (second least-connected) target-connection-count)
                                                      least-connected
                                                      [target target-connection-count]))
                                                  (first node-connections)
                                                  (rest node-connections))
            least-connected-target (least-connected-node least-connected-origin
                                                         (merge-graph least-connected-origin)
                                                         node-connections)
            segment (mergeable? least-connected-origin least-connected-target)
            nodes (->> merge-graph
                       keys
                       (into #{}))]
        (-> nodes
            (disj least-connected-origin least-connected-target)
            (conj (merge-crons least-connected-origin least-connected-target segment))
            ->merge-graph)))))
