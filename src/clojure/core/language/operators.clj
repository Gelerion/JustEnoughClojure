(ns clojure.core.language.operators)

; do operator
; lets you wrap up multiple form in parentheses and run each of them
(if true
  (do (println "true statement") "true")
  (do (println "false stmt") "false"))

; when operator
; is like if and do but without else, returns nil if condition is false
(when true (println "Success!") "true")

; check if value is nil
(nil? 1)
(nil? nil)

; = is equality operator
(= 1 1)

; or - returns either the first truthy value or the last value
(or false nil "true")
(or (= 1 2) (= "yes" "no"))
(or nil)

; and - returns the first false value or, if no values are false, the last truthy value
(and :first :second)
(and :first nil false)

; def - bind a name to a value
(def names
  ["Denis" "Andrew" "Rafael"])

; keywords - primarily used as keys in maps,
; can be used as functions that look up the corresponding value in a data structure
(:a {:a 1 :b 2 :c 3})

; let - binds names to values and introduces a new scope
(let [x 3] x)

(def dalmatian-list
  ["Pongo" "Perdita" "Puppy 1" "Puppy 2"])

(let [dalmatians (take 2 dalmatian-list)] dalmatians)
(let [[pongo & dalmatinas] dalmatian-list] [pongo dalmatinas])


(def x 0)
(let [x 1] x) ; => 1

; do
(do
  (def x 5)
  (def y 4)
  (+ x y)
  [x y])

; into
(into [] (set [:a :a]))
; set returns #{:a}, then into returns vector [:a]

; loop
(loop [iteration 0] ; introduces a binding with an initial value
  (println (str "Iteration " iteration))
  (if (> iteration 3)
    (println "Goodbye!")
    (recur (inc iteration)))) ; allows you to call the function from within itself, passing the argument

; reg-ex
(re-find #"^left-" "left-eye")

; reduce, 0 - is optional value
(reduce + 0 [1 2 3 4])
; This is like telling Clojure to do this:   (+ (+ (+ 1 2) 3) 4)
(defn my-reduce
  ([f initial coll]
   (loop [result initial remaining coll]
     (if (empty? remaining)
       result
       (recur (f result (first remaining)) (rest remaining)))))
  ([f [head & tail]]
   (my-reduce f head tail)))

; reduce-kv
(def vector-of-maps [{:a 1 :b 2} {:a 3 :b 4}])
(defn update-map [m f]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))
(map #(update-map % inc) vector-of-maps) ;({:b 3, :a 2} {:b 5, :a 4})

; cond
; Takes a set of test/expr pairs. It evaluates each test one at a
; time.  If a test returns logical true, cond evaluates and returns
; the value of the corresponding expr and doesn't evaluate any of the
; other tests or exprs. (cond) returns nil.
(defn pos-neg-or-zero
  [n]
  (cond
    (< 0 n) "neg"
    (> 0 n) "pos"
    :else "zero"))


; re-seq
(re-seq #"[A-Z]" "HeLlO, WoRlD!") ; => ("H" "L" "O" "W" "R" "D")


; mod (! not remainder)
; The problem here is that in Python the % operator returns the modulus and in Java
; it returns the remainder. These functions give the same values for positive
; arguments, but the modulus always returns positive results for negative input,
; whereas the remainder may give negative results.
;; a % b = a - a / b * b; i.e. it's the remainder.
;; You can do (a % b + b) % b
(mod -6 5) ; 4 -> (rem (+ (rem -6 5) 5) 5)
(mod 6 5) ; 1

(rem -6 5) ; -1

; name
(name :location) ; "location"


; fnil
; Takes a function f, and returns a function that calls f, replacing
; a nil first argument to f with the supplied value x
(defn say-hello [name] (str "Hello " name))
(def say-hello-with-defaults (fnil say-hello "World"))
(say-hello-with-defaults "Sir") ; Hello Sir
(say-hello-with-defaults nil) ; Hello World

; try
(try
  (throw (Error. "I done throwed in CLJS"))
  (catch Error err "I done catched in CLJS"))

; letfn
;Using letfn allows you to create local functions that reference each other
(letfn [(twice [x]
          (* x 2))
        (six-times [y]
          (* (twice y) 3))]
  (println "Twice 15 =" (twice 15))
  (println "Six times 15 =" (six-times 15)))

(defn even2? [n]
  (letfn [(neven? [n] (if (zero? n) true (nodd? (dec n))))
          (nodd? [n] (if (zero? n) false (neven? (dec n))))]
    (neven? n)))

; dotimes
(dotimes [n 5] (println "n is" n))
;n is 0
;n is 1
;n is 2
;...