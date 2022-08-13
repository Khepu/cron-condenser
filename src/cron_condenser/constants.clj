(ns cron-condenser.constants)

                                        ; Segment Bounds

;; Numeric bounds [lower, upper)

(def minute-bounds   {:lower 0 :upper 60})
(def hour-bounds     {:lower 0 :upper 24})
(def day-bounds      {:lower 1 :upper 32})
(def month-bounds    {:lower 1 :upper 13})
(def week-day-bounds {:lower 0 :upper  7})


;; Explicit bounds

(def month-names ["JAN"
                  "FEB"
                  "MAR"
                  "APR"
                  "MAY"
                  "JUN"
                  "JUL"
                  "AUG"
                  "SEP"
                  "OCT"
                  "NOV"
                  "DEC"])

;;; Sunday is first according to UNIX spec
(def week-days ["SUN"
                "MON"
                "TUE"
                "WED"
                "THU"
                "FRI"
                "SAT"])
