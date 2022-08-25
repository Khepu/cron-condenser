(ns cron-condenser.visualizer
  (:require
   [tangle.core :refer [graph->dot dot->image]]
   [clojure.java.io :refer [file copy]]))


(def colors {:minute   "blue"
             :hour     "red"
             :day      "green"
             :month    "yellow"
             :week-day "pink"})

(defn edge
  [edge-type origin target]
  [origin target {:color (colors edge-type)}])

(defn normalize-edges
  [edge-type origin targets]
  (mapv (partial edge edge-type origin) targets))

(defn normalize-branch
  [[cron merge-map]]
  (mapv #(normalize-edges (first %)
                          cron
                          (second %))
        merge-map))

(defn normalize-graph
  [merge-graph]
  {:nodes (vals merge-graph)
   :edges (mapv normalize-branch merge-graph)})

(defn draw-merge-graph
  [merge-graph]
  (let [{:keys [nodes edges]} (normalize-graph merge-graph)
        _ (println nodes edges)
        dot (graph->dot nodes edges {:node {:shape :circle}
                                     :node->id identity
                                     :node->descriptor identity})]
    (copy (dot->image dot "png")
          (file "resources/merge-graph.png"))))
