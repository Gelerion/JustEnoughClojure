(ns clojure.core.example.exercises)

; Write a function, mapset, that works like map except the return value is a set:
;(mapset inc [1 1 2 2])
(defn mapset
  "map to set"
  [fun coll]
  (set (map fun coll)))

; Create a function that’s similar to symmetrize-body-parts except that it has to work with
; weird space aliens with radial symmetry. Instead of two eyes, arms, legs, and so on, they have five.
(def alien-body-parts [{:name "head" :size 3}
                       {:name "1-eye" :size 1}
                       {:name "mouth" :size 1}
                       {:name "nose" :size 1}
                       {:name "1-arm" :size 3}
                       {:name "chest" :size 10}
                       {:name "abdomen" :size 6}
                       {:name "1-leg" :size 3}])

(defn create-part-n [n part]
  {:name (clojure.string/replace (:name part) #"^(\d)+" (str n))
   :size (:size part)})

(defn should-inc?
  [{part-name :name}]
  (clojure.string/starts-with? part-name "1"))

(defn create-n-matching-parts [n part]
  (loop [x 1 result []]
    (if (<= x n)
      (recur (inc x) (conj result (create-part-n x part)))
      result)))

(defn matching-parts [n part]
  (if (should-inc? part)
    (create-n-matching-parts n part)
    (vector part)))

(defn symmetrize-body-parts
  ([asym-parts n]
   (reduce (fn [result part]
             (into result (matching-parts n part)))
           []
           asym-parts)))

(symmetrize-body-parts alien-body-parts 4)