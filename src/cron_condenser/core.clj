(ns cron-condenser.core
  (:require
   [clojure.string :as string]
   [clojure.java.io :refer [file]]
   [clojure.tools.cli :refer [parse-opts]]
   [cron-condenser.validator :refer [cron-expression? explain-invalid]]
   [cron-condenser.expander :refer [expand]]
   [cron-condenser.planner :refer [->merge-graph least-connected-merge]]
   [cron-condenser.visualizer :refer [draw-merge-graph]]
   [cron-condenser.contractor :refer [contract]]
   [cron-condenser.cron-expression :refer [str->CronExpression CronExpression->str]])
  (:gen-class))


(def argument-options [[nil "--draw DIRECTORY" "Draw directory. If left out no visualization will be produced."
                        :validate [#(.exists (file %)) "Draw directory does not exist!"
                                   #(.isDirectory (file %)) "Path provided for `--draw` is not a directory!"]]
                       ["-v" nil "Enables error verbosity."
                        :id :verbosity]])

(defn println-err
  [& more]
  (binding [*out* *err*]
    (apply println more)))

(defn condense
  [crons draw-path]
  (let [merge-graph (->> crons
                         (map str->CronExpression)
                         (map expand)
                         set
                         ->merge-graph)]
    (loop [current-graph merge-graph
           iteration 1]
      (when draw-path
        (draw-merge-graph draw-path (str "merge-graph-" (dec iteration)) current-graph))
      (let [new-graph (least-connected-merge current-graph)]
        (if (= (count current-graph) (count new-graph))
          current-graph
          (recur new-graph (inc iteration)))))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors]} (parse-opts args argument-options)
        {:keys [valid-cron invalid-cron]} (group-by #(if (cron-expression? %)
                                                       :valid-cron
                                                       :invalid-cron)
                                                    arguments)
        invalid-cron-formatter (if (:verbosity options)
                               #(str % " - " (explain-invalid %))
                               identity)]
    (cond
      (seq invalid-cron) (println-err "The following cron expressions are invalid: \n"
                                      (->> invalid-cron
                                           (map invalid-cron-formatter)
                                           (string/join "\n")))
      (seq errors)       (run! println-err errors)
      :else              (->> (condense valid-cron (:draw options))
                              keys
                              (map contract)
                              (map CronExpression->str)
                              (string/join "\n")
                              println))))
