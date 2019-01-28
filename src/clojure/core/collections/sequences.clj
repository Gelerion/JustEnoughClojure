(ns clojure.core.collections.sequences)

;when you call map, first, rest, or cons—it calls the seq function on the data structure
;in question to obtain a data structure that allows for first, rest, and cons

(seq '(1 2 3))
(seq [1 2 3])
(seq #{1 2 3})
; NOTE returns list of vectors
(seq {:name "Bill" :age 45})
; convert back to map
(into {} (seq {:a 1 :b 2 :c 3}))

; So, Clojure’s sequence functions use seq on their arguments. The sequence functions are
; defined in terms of the sequence abstraction, using first, rest, and cons. As long as a
; data structure implements the sequence abstraction, it can use the extensive seq library,
; which includes such superstar functions as reduce, filter, distinct, group-by, and dozens more.

















