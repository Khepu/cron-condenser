(ns cron-condenser.expander
  (:require
   [clojure.string :as string]
   [cron-condenser.utils :refer [->byte]]
   [cron-condenser.constants :refer :all]))


(defn parse-range
  [^String range-str]
  (->> (string/split range-str #"\-")
       (map ->byte)))

(defn parse-step
  [^String step-str]
  (-> step-str
      (string/split #"/")
      second
      ->byte))

(defmulti expand-minute
  (fn [[category _]]
    category))

(defmethod expand-minute :all
  [_]
  (map str
       (range (:lower minute-bounds)
              (:upper minute-bounds))))

(defmethod expand-minute :single
  [[_ value]]
  value)

(defmethod expand-minute :range
  [[_ range-str]]
   (let [[start end] (parse-range range-str)]
     (map str
          (range start
                 (inc end)))))

(defmethod expand-minute :step
  [[_ step-str]]
  (let [step (parse-step step-str)]
    (map str
         (range (:lower minute-bounds)
                (:upper minute-bounds)
                step))))
