(ns cron-condenser.utils
  (:import [clojure.lang PersistentVector]))


(defn ->byte
  ^Byte [^String string]
  (try
    (Byte/parseByte string)
    (catch Exception _
      nil)))

(defn index-of
  [item ^PersistentVector vector]
  (let [index-or-sentinel (.indexOf vector item)]
    (if (>= index-or-sentinel 0)
      index-or-sentinel
      nil)))
