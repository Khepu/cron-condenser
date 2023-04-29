(ns cron-condenser.validator
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [cron-condenser.utils :refer [->byte index-of]]
   [cron-condenser.constants :refer :all])
  (:import
   [clojure.lang PersistentVector]))

(def +cron-segments+ 5)

(defn in-bounds?
  [^Integer lower ^Integer upper]
  (fn ^Boolean [^Byte value]
    (= (min (max lower value)
            (dec upper))
       value)))

(def in-minute-bounds?   (in-bounds? (:lower minute-bounds)   (:upper minute-bounds)))
(def in-hour-bounds?     (in-bounds? (:lower hour-bounds)     (:upper hour-bounds)))
(def in-day-bounds?      (in-bounds? (:lower day-bounds)      (:upper day-bounds)))
(def in-month-bounds?    (in-bounds? (:lower month-bounds)    (:upper month-bounds)))
(def in-week-day-bounds? (in-bounds? (:lower week-day-bounds) (:upper week-day-bounds)))

(defn valid-range?
  [range-str in-section-bounds?]
  (let [range-values (map ->byte (string/split range-str #"\-"))]
    (and (every? (comp not nil?) range-values)
         (= (count range-values) 2)
         (let [[start end] range-values]
           (and (< start end)
                (in-section-bounds? start)
                (in-section-bounds? end))))))

(defn valid-step?
  [step-str in-section-bounds?]
  (let [ratio (string/split step-str #"/")]
    (and (= (count ratio) 2)
         (= (first ratio) "*")
         (if-some [step-value (->byte (second ratio))]
           (in-section-bounds? step-value)
           false))))

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

(s/def :cron/day
  (s/and string?
         (s/or :all    #(= "*" %)
               :single #(if-some [value (->byte %)]
                          (in-day-bounds? value)
                          false)
               :range  #(valid-range? % in-day-bounds?)
               :step   #(valid-step? % in-day-bounds?))))

(defn valid-named-range?
  [range-str names]
  (let [range-values (string/split range-str #"\-")]
    (and (= (count range-values) 2)
         (let [[start end] (->> range-values
                                (map #(index-of % names))
                                (map inc))]
           (< start end)))))

(defn valid-named-step?
  [step-str names]
  (let [step-values (string/split step-str #"/")]
    (and (= (count step-values) 2)
         (let [[left right] step-values]
           (and (= left "*")
                (contains? names right))))))

(s/def :cron/month
  (s/and string?
         (s/or :all          #(= "*" %)
               :single       #(if-some [value (->byte %)]
                                (in-month-bounds? value)
                                false)
               :range        #(valid-range? % in-month-bounds?)
               :step         #(valid-step? % in-month-bounds?)
               :named-single (comp not nil? #(index-of % month-names))
               :named-range  #(valid-named-range? % month-names)
               :named-step   #(valid-named-step? % month-names))))

(s/def :cron/week-day
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
  [spec segment]
  (->> (string/split segment #",")
       (map #(s/valid? spec %))
       (reduce #(and %1 %2))))

(def cron-specs '(:cron/minute :cron/hour :cron/day :cron/month :cron/week-day))

(s/def :cron/expression
  (s/and string?
         #(= (count (string/split % #" ")) +cron-segments+)
         #(let [segments (string/split % #" ")]
            (->> segments
                 (map validate-segment cron-specs)
                 (every? identity)))))

(defn cron-expression?
  [^String cron]
  (s/valid? :cron/expression cron))

(defn explain-invalid
  [^String cron]
  (let [segments (string/split cron #" ")
        total-segments (count segments)]
    (if (= total-segments +cron-segments+)
      (->> (zipmap cron-specs segments)
           (reduce-kv (fn [errors cron-spec segment]
                        (if (not (s/valid? cron-spec segment))
                          (conj errors (name cron-spec))
                          errors))
                      [])
           (string/join ", ")
           (str "The following segments did not pass validation: "))
      (str "Invalid number of segments. Found " total-segments " instead of 5!"))))
