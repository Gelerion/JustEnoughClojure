(ns clojure.core.collections.examples)

(def food-journal
  [{:month 1 :day 1 :human 5.3 :critter 2.3}
   {:month 1 :day 2 :human 5.1 :critter 2.0}
   {:month 2 :day 1 :human 4.9 :critter 2.1}
   {:month 2 :day 2 :human 5.0 :critter 2.5}
   {:month 3 :day 1 :human 4.2 :critter 3.3}
   {:month 3 :day 2 :human 4.0 :critter 3.8}
   {:month 4 :day 1 :human 3.7 :critter 3.9}
   {:month 4 :day 2 :human 3.7 :critter 3.6}])

; you can also give map multiple collections
(map str ["a" "b" "c"] ["A" "B" "C"]) ; => ("aA" "bB" "cC")
; same as
(list (str "a" "A") (str "b" "B") (str "c" "C"))

; sequential
(defn titleize
  [topic]
  (str topic " for the Brave and True"))

(map titleize ["Hamsters" "Ragnarok"])
; => ("Hamsters for the Brave and True" "Ragnarok for the Brave and True")

(map titleize '("Empathy" "Decorating"))
; => ("Empathy for the Brave and True" "Decorating for the Brave and True")

(map titleize #{"Elbows" "Soap Carving"})
; => ("Elbows for the Brave and True" "Soap Carving for the Brave and True")

; second must be called
(map #(titleize (second %)) {:uncomfortable-thing "Winking"})
; => ("Winking for the Brave and True")


; abstractions

(empty? [])
; many seq funcs return a seq rather than the original collection, into can convert value back
(map identity {:iam "map"}) ; return vector
(into {} (map identity {:iam "map"}))

(last {:a 9 :b 98 :c 0})
(count {:a 9 :b 98 :c 0})

(butlast [3 2 1]) ;[3 2]

(concat '(1) '(2 3)) ;(1 2 3)

; Exercise
(def human-consumption   [8.1 7.3 6.6 5.0])
(def critter-consumption [0.0 0.2 0.3 1.1])
(defn unify-diet-data
  [human critter]
  {:human human :critter critter})

(map unify-diet-data human-consumption critter-consumption)

; Another fun thing you can do with map is pass it a collection of functions.
; You could use this if you wanted to perform a set of calculations on different
; collections of numbers
(def sum #(reduce + %))
(def avg #(/ (sum %) (count %)))
(defn stats
  [numbers]
  (map #(% numbers) [sum count avg]))

;(stats [3 4 10])
;(stats [80 1 44 13 6])

;additionally, Clojurists often use map to retrieve the value associated with a keyword
;from a collection of map data structures
(map :real [{:alias "Batman" :real "Bruce Wayne"}
            {:alias "Spider-Man" :real "Peter Parker"}])

; producing a new map with the same keys but with updated values
(reduce (fn [new-map [key val]]
          (assoc new-map key (inc val)))
        {}
        {:max 30 :min 10})
; reduce treats the argument {:max 30 :min 10} as a sequence of vectors, like ([:max 30] [:min 10])

; assoc takes three arguments: a map, a key, and a value
; it derives a new map from the map you give it by associating
; the given key with the given value. For example, (assoc {:a 1} :b 2) would return {:a 1 :b 2}
(assoc {} :max (inc 30) :min (inc 10))
(assoc (assoc {} :max (inc 30)) :min (inc 10))

; use reduce for filtering
(reduce (fn [new-map [key val]]
          (if (> val 4)
            (assoc new-map key val)
            new-map))
        {}
        {:human 4.1 :critter 3.9})

; take, drop, take-while, and drop-while
(take 3 [1 2 3 4 5 6 7 8 9 10])
; => (1 2 3)

(drop 3 [1 2 3 4 5 6 7 8 9 10])
; => (4 5 6 7 8 9 10)

; take-while and drop-while
; Each takes a predicate function to determine when it should stop taking or dropping
(take-while #(< (:month %) 3) food-journal)
(drop-while #(< (:month %) 3) food-journal)
; Feb + March only
(take-while #(< (:month %) 4)
            (drop-while #(< (:month %) 2) food-journal))


(filter #(< (:human %) 5) food-journal)

; note: `take-while' stops traversing the collection when the predicate is false,
; as is different from `filter'.
(take-while neg? [-2 -1 0 -1 -2 3]) ; (-2 -1)
(filter neg? [-2 -1 0 -1 -2 3]) ; (-2 -1 -1 -2)

; some - you want to know whether a collection contains any values that test true for a
; predicate function.
(some #(> (:critter %) 5) food-journal) ; nil
(some #(> (:critter %) 3) food-journal) ; true

(some #{2 7 6} [5 6 7 8]) ; 6
(some #(when (even? %) %) [5 6 7 8]) ; 6

; return entry
(some #(and (> (:critter %) 3) %) food-journal)

; sort / sort-by
(sort [3 1 2])
(sort-by count ["aaa" "c" "bb"])

; concat
(concat [1 2] [3 4])

(flatten [1 2 [3 4]])

; mapcat
; (mapcat f & colls)
(mapcat reverse [[3 2 1 0] [6 5 4] [9 8 7]])  ;(0 1 2 3 4 5 6 7 8 9)

; tree-seq
(tree-seq seq? identity '(1 2 (3 (4))))
;; ((1 2 (3 (4))) 1 2 (3 (4)) 3 (4) 4)

;; It's same as ...
(tree-seq seq? seq '(1 2 (3 (4))))
(tree-seq sequential? seq '(1 2 (3 (4))))
;; ((1 2 (3 (4))) 1 2 (3 (4)) 3 (4) 4)

;; This processing ...
(sequential? '(1 2 (3 (4)))) ;; returns true  ... -> (1 2 (3 (4))) <--- !!!
(sequential? 1)              ;; returns false ... -> 1
(sequential? 2)              ;; returns false ... -> 2
(sequential? '(3 (4)))       ;; returns true  ... -> (3 (4))       <--- !!!
(sequential? 3)              ;; returns false ... -> 3
(sequential? '(4))           ;; returns true  ... -> (4)           <--- !!!
(sequential? 4)              ;; returns false ... -> 4
;; so, #tree-seq returns...
;; ((1 2 (3 (4))) 1 2 (3 (4)) 3 (4) 4)


;; Each node is a (node-root child1 child2 ...),
;; so branch?==next and children==rest
;;
;;     A
;;    / \
;;   B   C
;;  / \  |
;; D   E F
;;
(map first (tree-seq next rest '(:A (:B (:D) (:E)) (:C (:F)))))
;;=> (:A :B :D :E :C :F)


; partition-by
; Applies f to each value in coll, splitting it each time f returns a new value.
(partition-by #(= 3 %) [1 2 3 4 5])
; ((1 2) (3) (4 5))
(partition-by identity "ABBA")
; ((\A) (\B \B) (\A))

; partition-all
; Returns a lazy sequence of lists like partition, but may include
; partitions with fewer than n items at the end.
(partition 4 [0 1 2 3 4 5 6 7 8 9])
; ((0 1 2 3) (4 5 6 7))
(partition-all 4 [0 1 2 3 4 5 6 7 8 9])
;=> ((0 1 2 3) (4 5 6 7) (8 9))

(partition-all 2 4 [0 1 2 3 4 5 6 7 8 9])
;=> ((0 1) (4 5) (8 9))


; interleave
; Returns a lazy seq of the first item in each coll, then the second etc.
; (interleave)(interleave c1)(interleave c1 c2)(interleave c1 c2 & colls)

(interleave [:a :b :c] [1 2 3])
;=> (:a 1 :b 2 :c 3)
(#(interleave %2 (repeat %1)) 0 [1 2 3]) ; (1 0 2 0 3 0)

; interpose
; Returns a lazy seq of the elements of coll separated by sep.
(interpose ", " ["one" "two" "three"]) ; ("one" ", " "two" ", " "three")

; doseq
;Repeatedly executes body (presumably for side-effects) with
;bindings and filtering as provided by "for".  Does not retain
;the head of the sequence. Returns nil.

; Multiplies every x by every y.
(doseq [x [-1 0 1]
        y [1 2 3]]
  (prn (* x y)))
; -1 -2 -3 0 0 0 1 2 3

(doseq [[x y] (map list [1 2 3] [1 2 3])] (prn (* x y)))
; (map list [1 2 3] [1 2 3])
; ((1 1) (2 2) (3 3))
; => 1 4 9

(doseq [y (range 10)]
  (dotimes [z y] (print (inc z)))
  (newline))


; split
; Returns a vector of [(take n coll) (drop n coll)]
(split-at 2 [1 2 3 4 5]) ; [(1 2) (3 4 5)]

; split-with
; Returns a vector of [(take-while pred coll) (drop-while pred coll)]
(split-with (partial >= 3) [1 2 3 4 5]) ; [(1 2 3) (4 5)]
(split-with odd? [1 3 5 6 7 9]) ; [(1 3 5) (6 7 9)]

