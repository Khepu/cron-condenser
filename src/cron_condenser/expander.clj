(ns cron-condenser.expander
  (:require
   [clojure.string :as string]
   [clojure.spec.alpha :as s]
   [cron-condenser.constants :refer :all]
   [cron-condenser.utils :refer [->byte index-of]]
   [cron-condenser.validator :refer :all]
   [cron-condenser.cron-expression :refer [map->CronExpression]])
  (:import
   [clojure.lang PersistentHashSet]
   [clojure.lang PersistentList]
   [cron_condenser.cron_expression CronExpression]))


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

;; Minute expansion

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

;; Hour expansion

(defmulti expand-hour
  (fn [[category _]]
    category))

(defmethod expand-hour :all
  [_]
  (map str
       (range (:lower hour-bounds)
              (:upper hour-bounds))))

(defmethod expand-hour :single
  [[_ value]]
  value)

(defmethod expand-hour :range
  [[_ range-str]]
  (let [[start end] (parse-range range-str)]
    (map str
         (range start
                (inc end)))))

(defmethod expand-hour :step
  [[_ step-str]]
  (let [step (parse-step step-str)]
    (map str
         (range (:lower hour-bounds)
                (:upper hour-bounds)
                step))))

;; Day expansion

(defmulti expand-day
  (fn [[category _]]
    category))

(defmethod expand-day :all
  [_]
  (map str
       (range (:lower day-bounds)
              (:upper day-bounds))))

(defmethod expand-day :single
  [[_ value]]
  value)

(defmethod expand-day :range
  [[_ range-str]]
  (let [[start end] (parse-range range-str)]
    (map str
         (range start
                (inc end)))))

(defmethod expand-day :step
  [[_ step-str]]
  (let [step (parse-step step-str)]
    (map str
         (range (:lower day-bounds)
                (:upper day-bounds)
                step))))

;; Month expansion

(defmulti expand-month
  (fn [[category _]]
    category))

(defmethod expand-month :all
  [_]
  (map str
       (range (:lower month-bounds)
              (:upper month-bounds))))

(defmethod expand-month :single
  [[_ value]]
  value)

(defmethod expand-month :range
  [[_ range-str]]
  (let [[start end] (parse-range range-str)]
    (map str
         (range start
                (inc end)))))

(defmethod expand-month :step
  [[_ step-str]]
  (let [step (parse-step step-str)]
    (map str
         (range (:lower month-bounds)
                (:upper month-bounds)
                step))))

(defmethod expand-month :named-single
  [[_ value]]
  (-> value
      (index-of month-names)
      inc))

(defmethod expand-month :named-range
  [[_ range-str]]
  (let [[lower upper] (map (comp inc #(index-of % month-names)) (string/split range-str #"\-"))]
    (map str (range lower (inc upper)))))

(defmethod expand-month :named-step
  [[_ step-str]]
  (let [step (-> step-str
                 (string/split #"/")
                 second
                 (index-of month-names)
                 inc)]
    (map str
         (range (:lower month-bounds)
                (:upper month-bounds)
                step))))

;; Week-Day expansion

(defmulti expand-week-day
  (fn [[category _]]
    category))

(defmethod expand-week-day :all
  [_]
  (map str
       (range (:lower week-day-bounds)
              (:upper week-day-bounds))))

(defmethod expand-week-day :single
  [[_ value]]
  value)

(defmethod expand-week-day :range
  [[_ range-str]]
  (let [[start end] (parse-range range-str)]
    (map str
         (range start
                (inc end)))))

(defmethod expand-week-day :step
  [[_ step-str]]
  (let [step (parse-step step-str)]
    (map str
         (range (:lower week-day-bounds)
                (:upper week-day-bounds)
                step))))

(defmethod expand-week-day :named-single
  [[_ value]]
  (-> value
      (index-of week-days)
      inc))

(defmethod expand-week-day :named-range
  [[_ range-str]]
  (let [[lower upper] (map (comp inc #(index-of % week-days)) (string/split range-str #"\-"))]
    (map str
         (range lower
                (inc upper)))))

(defmethod expand-week-day :named-step
  [[_ step-str]]
  (let [step (-> step-str
                 (string/split #"/")
                 second
                 (index-of week-days)
                 inc)]
    (map str
         (range (:lower week-day-bounds)
                (:upper week-day-bounds)
                step))))

(defn ^PersistentList segment
  [^String cron-segment]
  (string/split cron-segment #","))

(defn ^PersistentHashSet expand-segment
  [cron-segment spec expander]
  (->> cron-segment
       (map segment)
       flatten
       (map #(s/conform spec %))
       (map expander)
       flatten
       set))

(defn ^CronExpression expand
  [^CronExpression cron-expression]
  (let [{:keys [minute hour day month week-day]} cron-expression]
    (map->CronExpression {:minute   (expand-segment minute   :cron/minute   expand-minute)
                          :hour     (expand-segment hour     :cron/hour     expand-hour)
                          :day      (expand-segment day      :cron/day      expand-day)
                          :month    (expand-segment month    :cron/month    expand-month)
                          :week-day (expand-segment week-day :cron/week-day expand-week-day)})))
