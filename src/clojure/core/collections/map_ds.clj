(ns clojure.core.collections.map-ds)

; {} - Map
(def map-example
  {:key_one "value_one"
   :key_two "value_two"})
; or impl-specific map
(hash-map :key_one "value_one")

; nesting maps
{:key_one {:nest_one "nest_one_v" :nest_two "nest_two_v"}}
(def nested_map
  {:key_one {:nest_one "nest_one_v" :nest_two "nest_two_v"}})

; retrieve values
(get map-example :key "on_nil_value")

; nested
(get-in nested_map [:key_one :nest_two])

; direct lookup
(map-example :key_two)

; add nested
(assoc-in {} [:cookie :monster :vocals] "Finntroll")
; => {:cookie {:monster {:vocals "Finntroll"}}}

(assoc-in {} [1 :connections 4] 2)
; => {1 {:connections {4 2}}}

(zipmap [:a :b] [1 2])
; => {:a 1 :b 2}

(vals {:a 1 :b 2})
; => 1 2

;merge two maps using the addition function
(merge-with + {:a 1 :b 2} {:a 9 :b 98 :c 0})

(merge {:a 1} {:a 2} {:a 3})
;;=> {:a 3}

(merge-with + {:a 1} {:a 2} {:a 3})
;;=> {:a 6}

(update-in {:a {:b 3}} [:a :b] inc) ; => {:a {:b 4}}
(update-in {:a {:b 3}} [:a :b] + 10) ; => {:a {:b 13}}

;; fnil is very useful for specifying default values when updating maps
;; For a map containing counters of keys:
(update-in {:a 1} [:a] inc) ; { :a 2 }
; (update-in {:a 1} [:b] inc) ; NPE
(update-in {:a 1} [:b] (fnil inc 0)) ; {:b 1, :a 1}

;; Another example is when map values are collections and we don't want
;; default behavior of conj with nil that produces a list
(conj nil 1)
;;=> (1)
;; I.e.
(update-in {} [:a] conj 1)
;;=> {:a (1)}

;; But say we want map values to be vectors instead:
(update-in {} [:a] (fnil conj []) 1)
;;=> {:a [1]}