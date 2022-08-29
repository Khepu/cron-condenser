(ns cron-condenser.visualizer
  (:require
   [clojure.java.io :refer [copy]]
   [tangle.core :refer [graph->dot dot->image]]
   [cron-condenser.cron-expression :refer [CronExpression->str]])
  (:import
   [java.nio.file Paths]
   [cron_condenser.cron_expression CronExpression]))


(def colors {:minute   "blue"
             :hour     "red"
             :day      "green"
             :month    "orange"
             :week-day "pink"})

(defn edge
  [edge-type
   ^CronExpression origin
   ^CronExpression target]
  [(CronExpression->str origin)
   (CronExpression->str target)
   {:color (colors edge-type)
    :label (name edge-type)}])

(defn normalize-edges
  [edge-type
   ^CronExpression origin
   targets]
  (mapv (partial edge edge-type origin) targets))

(defn normalize-branch
  [[^CronExpression cron merge-map]]
  (apply concat
         (mapv #(normalize-edges (first %) cron (second %)) merge-map)))

(defn deduplicate
  [edges]
  (reduce (fn [unique [origin target opts]]
            (if (some #{[origin target opts] [target origin opts]} unique)
              unique
              (conj unique [origin target opts])))
          []
          edges))

(defn normalize-graph
  [merge-graph]
  {:nodes (mapv CronExpression->str (keys merge-graph))
   :edges (deduplicate
           (apply concat
                  (mapv normalize-branch merge-graph)))})

(defn draw-merge-graph
  [directory file-name merge-graph]
  (let [{:keys [nodes edges]} (normalize-graph merge-graph)
        dot (graph->dot nodes edges {:node {:shape :rectangle}})]
    (copy (dot->image dot "png")
          (.. Paths (get directory (into-array [(str file-name ".png")])) toFile))))
