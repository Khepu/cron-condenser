(ns cron-condenser.validator
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [cron-condenser.utils :refer [->byte index-of]])
  (:import [clojure.lang PersistentVector]
           [clojure.lang PersistentHashSet]))


(defn in-bounds?
  [^Integer lower ^Integer upper]
  (fn ^Boolean [^Byte value]
    (= (min (max lower value)
            (dec upper))
       value)))

(defn ^Boolean valid-range?
  [^String range-str in-section-bounds?]
  (let [range-values (->> (string/split range-str #"\-")
                          (map ->byte))]
    (and (every? (comp not nil?) range-values)
         (= (count range-values) 2)
         (let [[start end] range-values]
           (and (< start end)
                (in-section-bounds? start)
                (in-section-bounds? end))))))

(defn ^Boolean valid-step?
  [^String step-str in-section-bounds?]
  (let [ratio (string/split step-str #"/")]
    (and (= (count ratio) 2)
         (= (first ratio) "*")
         (if-some [step-value (->byte (second ratio))]
           (in-section-bounds? step-value)
           false))))

(def in-minute-bounds?   (in-bounds? 0 60))
(def in-hour-bounds?     (in-bounds? 0 24))
(def in-day-bounds?      (in-bounds? 1 32))
(def in-month-bounds?    (in-bounds? 1 12))
(def in-week-day-bounds? (in-bounds? 0  7))

(s/def :cron/minute
  (s/and string?
         (s/or :all    #(= "*" %)
               :single #(if-some [value (->byte %)]
                          (in-minute-bounds? value)
                          false)
               :range  #(valid-range? % in-minute-bounds?)
               :step   #(valid-step? % in-minute-bounds?))))

(s/def :cron/hour
  (s/and string?
         (s/or :all    #(= "*" %)
               :single #(if-some [value (->byte %)]
                          (in-hour-bounds? value)
                          false)
               :range  #(valid-range? % in-hour-bounds?)
               :step   #(valid-step? % in-hour-bounds?))))

(s/def :cron/day-of-month
  (s/and string?
         (s/or :all    #(= "*" %)
               :single #(if-some [value (->byte %)]
                          (in-day-bounds? value)
                          false)
               :range  #(valid-range? % in-day-bounds?)
               :step   #(valid-step? % in-day-bounds?))))

(defn ^Boolean valid-named-range?
  [^String range-str ^PersistentVector names]
  (let [range-values (string/split range-str #"\-")]
    (and (= (count range-values) 2)
         (let [[start end] (->> range-values
                                (map #(index-of % names))
                                (map inc))]
           (< start end)))))

(defn ^Boolean valid-named-step?
  [^String step-str ^PersistentVector names]
  (let [step-values (string/split step-str #"/")]
    (and (= (count step-values) 2)
         (let [[left right] step-values]
           (and (= left "*")
                (contains? names right))))))

(def months ["JAN" "FEB" "MAR" "APR" "MAY" "JUN" "JUL" "AUG" "SEP" "OCT" "NOV" "DEC"])

(s/def :cron/month
  (s/and string?
         (s/or :all          #(= "*" %)
               :single       #(if-some [value (->byte %)]
                                (in-month-bounds? value)
                                false)
               :range        #(valid-range? % in-month-bounds?)
               :step         #(valid-step? % in-month-bounds?)
               :named-single (comp not nil? #(index-of % months))
               :named-range  #(valid-named-range? % months)
               :named-step   #(valid-named-step? % months))))

(def week-days ["SUN" "MON" "TUE" "WED" "THU" "FRI" "SAT"])

(s/def :cron/day-of-week
  (s/and string?
         (s/or :all          #(= "*" %)
               :single       #(if-some [value (->byte %)]
                                (in-week-day-bounds? value)
                                false)
               :range        #(valid-range? % in-week-day-bounds?)
               :step         #(valid-step? % in-week-day-bounds?)
               :named-single (comp not nil? #(index-of % week-days))
               :named-range  #(valid-named-range? % week-days)
               :named-step   #(valid-named-step? % week-days))))

(defn validate-segment
  [spec ^String segment]
  (->> (string/split segment #",")
       (map #(s/valid? spec %))
       (reduce #(and %1 %2))))

(def cron-specs '(:cron/minute :cron/hour :cron/day-of-month :cron/month :cron/day-of-week))

(s/def :cron/expression
  (s/and string?
         #(= (count (string/split % #" ")) 5)
         #(let [segments (string/split % #" ")]
            (->> segments
                 (map validate-segment cron-specs)
                 (every? identity)))))

(defrecord cron-expression
    [^PersistentHashSet minute
     ^PersistentHashSet hour
     ^PersistentHashSet day
     ^PersistentHashSet month
     ^PersistentHashSet week-day])
