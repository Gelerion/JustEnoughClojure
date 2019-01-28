(ns clojure.core.collections.set-ds)

; #{} - Sets
(def set_example #{"name" 20 :key})
(hash-set 1 1 2 2)
(clojure.set/union #{:a :b :c} #{:b :c :d})

; contains? - true/false
(contains? set_example 20)
; by keyword lookup
(:a #{:b :a})