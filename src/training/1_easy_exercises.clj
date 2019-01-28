(ns training.1-easy-exercises)

; 1. Write a function which returns the last element in a sequence.)
;(= (__ '(5 4 3)) 3)
;(= (__ [1 2 3 4 5]) 5)
;(= (__ ["b" "c" "d"]) "d")
(= (#(first (reverse %)) '(5 4 3)) 3)
(= (#(nth % (dec (count %))) [1 2 3 4 5]) 5)
(= ((comp first reverse) ["b" "c" "d"]) "d")
(= ((fn [x] (if-let [r (next x)] (recur r) (first x))) '(5 4 3)) 3)
(= (#(-> % reverse first) ["b" "c" "d"]) "d")
(= (reduce #(identity %2) '(5 4 3)) 3)

; 2. Write a function which returns the second to last element from a sequence.
;(= (__ (list 1 2 3 4 5)) 4)
;(= (__ ["a" "b" "c"]) "b")
;(= (__ [[1 2] [3 4]]) [1 2])
(= ((comp second reverse) (list 1 2 3 4 5)) 4)
(= ((fn get-last [xs]
      (if (= (count xs) 2) (first xs) (get-last (next xs)))) (list 1 2 3 4 5)) 4)

; 3. Write a function which returns the Nth element from a sequence.
;(= (__ '(4 5 6 7) 2) 6)
;(= (__ [:a :b :c] 0) :a)
;(= (__ '([1 2] [3 4] [5 6]) 2) [5 6])
(= ((fn [coll n]
      (loop [it coll x 0] (if (= n x) (first it) (recur (rest it) (inc x ))))) '(4 5 6 7) 2) 6)
(= (#(first (drop %2 %)) '(4 5 6 7) 2) 6)
(= (.get '(4 5 6 7) 2) 6)

; 4. Write a function which returns the total number of elements in a sequence.
;(= (__ '(1 2 3 3 1)) 5)
;(= (__ "Hello World") 11)
;(= (__ [[1 2] [3 4] [5 6]]) 3)
;(= (__ '(13)) 1)
;(= (__ '(:a :b :c)) 3)
(defn elems [coll] (reduce (fn [x xs](inc x)) 0 coll))
(= (#(reduce (fn [x xs](inc x)) 0 %) '(1 2 3 3 1)) 5)

; 5. Write a function which reverses a sequence. [reverse, rseq]
; (= (__ [1 2 3 4 5]) [5 4 3 2 1])
; (= (__ (sorted-set 5 7 2 7)) '(7 5 2))
; (= (__ [[1 2][3 4][5 6]]) [[5 6][3 4][1 2]])
(= (#(reduce conj '() %) [1 2 3 4 5]) [5 4 3 2 1])
(= (apply conj () [1 2 3 4 5]) [5 4 3 2 1])
(= (into () [1 2 3 4 5]) [5 4 3 2 1])

; 6. Write a function which returns the sum of a sequence of numbers.
;(= (__ [1 2 3]) 6)
;(= (__ (list 0 -2 5 5)) 8)
;(= (__ #{4 2 1}) 7)
(= (reduce + [1 2 3]) 6)
(= (apply + [1 2 3]) 6)

; 7. Write a function which returns only the odd numbers from a sequence.
;(= (__ #{1 2 3 4 5}) '(1 3 5))
;(= (__ [4 2 1 6]) '(1))
(= (filter #(not (= (rem % 2) 0)) [4 2 1 6]) '(1))
(= (filter odd? [4 2 1 6]) '(1))

; 8. Write a function which returns the first X fibonacci numbers.
;(= (__ 6) '(1 1 2 3 5 8))
(defn fib
      ([up-to] (take up-to (fib 1 1)))
      ([x y] (cons x (lazy-seq (fib y (+ x y))))))
(= (fib 6) '(1 1 2 3 5 8))
(= (#(map (fn fib [x] (if (> x 1) (+ (fib (- x 1)) (fib (- x 2))) 1)) (range 0 %)) 6) '(1 1 2 3 5 8))

; 9. Write a function which returns true if the given sequence is a palindrome.
;(false? (__ '(1 2 3 4 5)))
;(true? (__ "racecar"))
(true? (#(= (seq %) (reverse %)) "racecar"))
#(if (< (count %1) 3) true (if (= (first %1) (last %1)) (recur (-> %1 rest butlast)) false))
(true? (#(if (instance? String %) (= % (clojure.string/reverse %)) (= % (reverse %))) "racecar"))

; 10. Write a function which flattens a sequence. [restrictions: flattern]
; (= (__ '((1 2) 3 [4 [5 6]])) '(1 2 3 4 5 6))
(= (#(loop [xs %, ys '()]
           (cond
                 (sequential? (first xs)) (recur (concat (first xs) (rest xs)) ys)
                 (empty? xs) (reverse ys)
                 :else (recur (rest xs) (cons (first xs) ys)))) '(((1 2) 3) 4 [5 6 [7 8]]))
   '(1 2 3 4 5 6 7 8))

(= ((fn iter [xs]
      (reduce (fn [acc x]
                (if (coll? x)
                  (concat acc (iter x))
                  (concat acc (cons x ()))))
              ()
              xs)) '(((1 2) 3) 4 [5 6 [7 8]]))
   '(1 2 3 4 5 6 7 8))

(= (#(filter (complement sequential?) (rest (tree-seq sequential? seq %))) '(((1 2) 3) 4 [5 6 [7 8]]))
   '(1 2 3 4 5 6 7 8))

(= ((fn flat [l] (if (coll? l) (reduce concat (map flat l)) (list l))) '(((1 2) 3) 4 [5 6 [7 8]]))
   '(1 2 3 4 5 6 7 8))

(= ((fn my-flattern [x] (if (coll? x) (mapcat my-flattern x) [x])) '(((1 2) 3) 4 [5 6 [7 8]]))
   '(1 2 3 4 5 6 7 8))

; 11. Write a function which takes a string and returns a new string containing only the capital letters.
;(= (__ "HeLlO, WoRlD!") "HLOWRD")
(= ((fn [x] (apply str (filter #(Character/isUpperCase %)  (seq x)))) "HeLlO, WoRlD!") "HLOWRD")
(= ((fn [xs] (reduce str (re-seq #"[A-Z]" xs))) "HeLlO, WoRlD!") "HLOWRD")
(= (#(clojure.string/replace % #"[^A-Z]" "") "HeLlO, WoRlD!") "HLOWRD")

; 12. Write a function which removes consecutive duplicates from a sequence.
;(= (apply str (__ "Leeeeeerrroyyy")) "Leroy")
;(= (__ [1 1 2 3 3 2 2 3]) '(1 2 3 2 3))
(= (apply str ((fn [x] (reduce #(if (= (last %1) %2) %1 (conj %1 %2)) [] x)) "Leeeeeerrroyyy")) "Leroy")

(= (apply str (#(loop [result [], input %]
                  (cond
                    (nil? input) result
                    (= (last result) (first input)) (recur result (next input))
                    :else (recur (conj result (first input)) (next input)))) "Leeeeeerrroyyy")) "Leroy")

(= (apply str (#(map first (partition-by identity %)) "Leeeeeerrroyyy")) "Leroy")

; 13. Write a function which packs consecutive duplicates into sub-lists.
; (= (__ [1 1 2 1 1 1 3 3]) '((1 1) (2) (1 1 1) (3 3)))
; (= (__ [:a :a :b :b :c]) '((:a :a) (:b :b) (:c)))
(= (partition-by identity [1 1 2 1 1 1 3 3]) '((1 1) (2) (1 1 1) (3 3)))

(= ((fn [s]
      (loop [res [] left s]
        (if (empty? left)
          res
          (recur (concat res [(take-while #(= (first left) %) left)])
                 (drop-while #(= (first left) %) left))))) [1 1 2 1 1 1 3 3])
   '((1 1) (2) (1 1 1) (3 3)))

; 14. Write a function which duplicates each element of a sequence.
;(= (__ [1 2 3]) '(1 1 2 2 3 3))
;(= (__ [[1 2] [3 4]]) '([1 2] [1 2] [3 4] [3 4]))
(= (flatten (map #(repeat 2 %) [1 2 3])) '(1 1 2 2 3 3))
(= ((fn [xs] (reduce (fn [acc x] (concat acc (take 2 (repeat x)))) [] xs)) [[1 2] [3 4]]) '([1 2] [1 2] [3 4] [3 4]))

(= (#(interleave % %) [1 2 3]) '(1 1 2 2 3 3))
(= (#(sort (into % %)) [1 2 3]) '(1 1 2 2 3 3))
(= (mapcat #(list % %) [1 2 3]) '(1 1 2 2 3 3))
(= (reduce #(conj %1 %2 %2) [] [1 2 3]) '(1 1 2 2 3 3))

; 15. Write a function which replicates each element of a sequence a variable number of times.
;(= (__ [44 33] 2) [44 44 33 33])
;(= (__ [:a :b] 4) '(:a :a :a :a :b :b :b :b))
(= ((fn [xs, times] (reduce (fn [acc x] (concat acc (take times (repeat x)))) [] xs)) [44 33] 2) [44 44 33 33])
(= ((fn [xs n] (mapcat #(repeat n %) xs)) [44 33] 2) [44 44 33 33])

; 16. Write a function which creates a list of all integers in a given range. [restrictions: range]
;(= (__ 1 4) '(1 2 3))
;(= (__ -2 2) '(-2 -1 0 1))
(= ((fn [l u] (take (- u l) (iterate inc l))) 1 4) '(1 2 3))
(= ((fn r [lo hi] (when (< lo hi) (cons lo (r (inc lo) hi)))) 1 4) '(1 2 3))
(= (#(reductions + %1 (repeat (- %2 %1 1) 1)) 1 4) '(1 2 3))
(= (#(take-while (partial > %2) (iterate inc %)) 1 4) '(1 2 3))

; 17. Write a function which takes a variable number of parameters and returns the maximum value. [restrictions: max, max-key]
;(= (__ 1 8 3 4) 8)
(= ((fn [first & args] (last (reductions (fn [max x] (if (> max x) max x)) first args))) 1 8 3 4) 8)
(= ((fn [& params] (reduce (fn [a b] (if (< a b) b a)) 0 params)) 1 8 3 4) 8)
(= (#(last (sort %&)) 1 8 3 4) 8)

; 18. Write a function which takes two sequences and returns the first item from each, then the second item
; from each, then the third, etc. [restrictions interleave]
;(= (__ [1 2 3] [:a :b :c]) '(1 :a 2 :b 3 :c))
;(= (__ [1 2 3 4] [5]) [1 5])
;(= (__ [1 2] [3 4 5 6]) '(1 3 2 4))
(= (#(flatten (map list %1 %2)) [1 2 3 4] [5]) [1 5])
(= (mapcat list [1 2 3 4] [5]) [1 5])

; 19. Write a function which separates the items of a sequence by an arbitrary value. [restrictions: interpose]
;(= (__ 0 [1 2 3]) [1 0 2 0 3])
;(= (apply str (__ ", " ["one" "two" "three"])) "one, two, three")
(= ((fn [sep coll] (butlast (mapcat #(list % sep) coll))) 0 [1 2 3]) [1 0 2 0 3])
(= (#(butlast (interleave %2 (repeat %1))) 0 [1 2 3]) [1 0 2 0 3])

; 20. Write a function which drops every Nth item from sequence
;(= (__ [1 2 3 4 5 6 7 8] 3) [1 2 4 5 7 8])
;(= (__ [:a :b :c :d :e :f] 2) [:a :c :e])
((fn [coll drop]
   (loop [i 1
          r []
          c coll]
     (if (empty? c)
       r
       (recur (inc i) (if (= (rem i drop) 0) r (conj r (first c))) (rest c))))) [1 2 3 4 5 6 7 8] 3)

(= (#(apply concat (partition-all (dec %2) %2 %1))
     [1 2 3 4 5 6 7 8] 3) [1 2 4 5 7 8])
(= (#(keep-indexed (fn [a b] (when (not= (dec %2) (mod a %2)) b)) %1)
     [1 2 3 4 5 6 7 8] 3) [1 2 4 5 7 8])

; 21. Write a function which calculates factorials.
;(= (__ 5) 120)
(defn factorial
  ([n] (last (take n (factorial 1 2))))
  ([x y] (cons x (lazy-seq (factorial (* x y) (inc y))))))
(= (factorial 5) 120)

(= (#(reduce * (range 1 (inc %))) 5) 120)
(= (#(reduce * (take-while pos? (iterate dec %))) 5) 120)
(= ((fn fac [x] (case x 0 1  (* (fac (- x 1)) x))) 5) 120)

; 22. Write a function which will split a sequence into two parts. [restrictions: split-at]
;(= (__ 3 [1 2 3 4 5 6]) [[1 2 3] [4 5 6]])
;(= (__ 2 [[1 2] [3 4] [5 6]]) [[[1 2] [3 4]] [[5 6]]])
(= (#(list (take %1 %2) (drop %1 %2)) 3 [1 2 3 4 5 6]) [[1 2 3] [4 5 6]])
(= ((juxt take drop) 3 [1 2 3 4 5 6]) [[1 2 3] [4 5 6]])

; 23. Write a function which takes a vector of keys and a vector of values and constructs a map
; from them. [restrictions: zipmap]
;(= (__ [:a :b :c] [1 2 3]) {:a 1, :b 2, :c 3})
;(= (__ [1 2 3 4] ["one" "two" "three"]) {1 "one", 2 "two", 3 "three"})
(= ((fn [k v] (into {} (map (fn [x y] [x y]) k v))) [:a :b :c] [1 2 3]) {:a 1, :b 2, :c 3})
(= (#(apply array-map (interleave %1 %2)) [:a :b :c] [1 2 3]) {:a 1, :b 2, :c 3})
(= ((fn [ks vs] (reduce merge (map (fn [k v] {k v}) ks vs))) [:a :b :c] [1 2 3]) {:a 1, :b 2, :c 3})

; 24. Given a side-effect free function f and an initial value x write a function which returns
; an infinite lazy sequence of x, (f x), (f (f x)), (f (f (f x))), etc. [restrictions: iterate]
;(= (take 5 (__ #(* 2 %) 1)) [1 2 4 8 16])
;(= (take 100 (__ inc 0)) (take 100 (range)))
;(= (take 9 (__ #(inc (mod % 3)) 1)) (take 9 (cycle [1 2 3])))
(take 5 ((fn iter [f init]
           (cons init (lazy-seq (iter f (f init))))) #(* 2 %) 1))

; 25. Given a function f and a sequence s, write a function which returns a map. The keys
; should be the values of f applied to each item in s. The value at each key should be a
; vector of corresponding items in the order they appear in s. [restrictions: group-by]
;(= (__ #(> % 5) [1 3 6 8]) {false [1 3], true [6 8]})
;(= (__ #(apply / %) [[1 2] [2 4] [4 6] [3 6]])
;   {1/2 [[1 2] [2 4] [3 6]], 2/3 [[4 6]]})
(= ((fn [f xs]
      (reduce #(let [k (f %2)]
                 (assoc %1 k (conj (get %1 k []) %2))) {} xs)) #(> % 5) [1 3 6 8])
   {false [1 3], true [6 8]})

; 26. Greatest Common Divisor
; Given two integers, write a function which returns the greatest common divisor.
;(= (__ 2 4) 2)
;(= (__ 1023 858) 33)
(= ((fn gcd [x y]
      (if (= y 0) x (recur y (rem x y)))) 1023 858) 33)























