(ns training.2-medium-exercises
  (:import (java.util Stack)))

; 1. Write a function which reverses the interleave process into x number of subsequences.
;(= (__ [1 2 3 4 5 6] 2) '((1 3 5) (2 4 6)))
;(= (__ (range 9) 3) '((0 3 6) (1 4 7) (2 5 8)))
(= ((fn [coll n]
      (loop [parts (partition-all n coll)
             result []]
        (if (empty? (first parts))
          result
          (recur (map #(drop 1 %) parts) (into result (list (map first parts))))))) (range 9) 3)
   '((0 3 6) (1 4 7) (2 5 8)))

(= (#(apply map list (partition %2 %)) (range 9) 3)
   '((0 3 6) (1 4 7) (2 5 8)))

(= (#(->> %1
          (iterate rest)
          (take %2)
          (map (partial take-nth %2))) (range 9) 3)
   '((0 3 6) (1 4 7) (2 5 8)))

(= ((fn rev-interleave [xs n]
      (reduce (partial map conj)
              (repeat [])
              (partition n xs))) (range 9) 3)
   '((0 3 6) (1 4 7) (2 5 8)))

; 2. Write a function which can rotate a sequence in either direction.
;(= (__ 2 [1 2 3 4 5]) '(3 4 5 1 2))
;(= (__ -2 [1 2 3 4 5]) '(4 5 1 2 3))
;(= (__ 6 [1 2 3 4 5]) '(2 3 4 5 1))
;(= (__ -4 '(:a :b :c)) '(:c :a :b))
(defn split-idx [n total]
  (cond
    (< n 0) (recur (+ total n) total)
    (> n total) (recur (- n total) total)
    :else n))

(= ((fn [n xs]
      (flatten (reverse (let [total (count xs)]
                          (split-at (split-idx n total) xs))))) 2 [1 2 3 4 5])
   '(2 3 4 5 1))


(= ((fn [n xs]
      (let [idx (mod n (count xs))]
        (concat (drop idx xs) (take idx xs)))) 2 [1 2 3 4 5]) '(3 4 5 1 2))

(= (#(take (count %2) (drop (mod % (count %2)) (cycle %2))) 2 [1 2 3 4 5]) '(3 4 5 1 2))

(= ((fn flip [n xs]
      (let [[x y] (split-at (mod n (count xs)) xs)] (concat y x))) 2 [1 2 3 4 5]) '(3 4 5 1 2))

; 3. Write a higher-order function which flips the order of the arguments of an input function.
;(= 3 ((__ nth) 2 [1 2 3 4 5]))
;(= true ((__ >) 7 8))
;(= 4 ((__ quot) 2 8))
;(= [1 2 3] ((__ take) [1 2 3 4 5] 3))
(= 3 (((fn [f] (fn [& args] (f (second args) (first args)))) nth) 2 [1 2 3 4 5]))
(= 3 (((fn [f] #(apply f (reverse %&))) nth) 2 [1 2 3 4 5]))
(= 3 ((#(fn [a b] (% b a)) nth) 2 [1 2 3 4 5]))

; 4. Write a function which takes a sequence consisting of items with different types and
; splits them up into a set of homogeneous sub-sequences. The internal order of each
; sub-sequence should be maintained, but the sub-sequences themselves can be returned
; in any order (this is why 'set' is used in the test cases).
;(= (set (__ [1 :a 2 :b 3 :c])) #{[1 2 3] [:a :b :c]})
;(= (set (__ [:a "foo"  "bar" :b])) #{[:a :b] ["foo" "bar"]})
;(= (set (__ [[1 2] :a [3 4] 5 6 :b])) #{[[1 2] [3 4]] [:a :b] [5 6]})
(= (set (#(map second (group-by type %)) [1 :a 2 :b 3 :c])) #{[1 2 3] [:a :b :c]})
(= (set (#(vals (group-by type %)) [1 :a 2 :b 3 :c])) #{[1 2 3] [:a :b :c]})

; 5. Write a function which returns a sequence of lists of x items each. Lists of less
; than x items should not be returned. [restrictions: partition, partition-all]
;(= (__ 3 (range 9)) '((0 1 2) (3 4 5) (6 7 8)))
;(= (__ 3 (range 8)) '((0 1 2) (3 4 5)))
((fn [n xs]
   (loop [coll xs res []]
     (if (< (count coll) n)
       res
       (recur
         (doall (drop n coll))
         (conj res (doall (take n coll))))))) 3 (range 8))

; 6. Write a function which returns a map containing the number of occurences of each
; distinct item in a sequence [restrictions: frequencies]
;(= (__ [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})
;(= (__ '([1 2] [1 3] [1 3])) {[1 2] 1, [1 3] 2})
(= (#(->>
       (group-by identity %)
       (map (fn [[k v]] [k (count v)]))
       (into {})) [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})

(= (reduce #(update-in %1 [%2] (fnil inc 0)) {} [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})
(= (#(into {} (for [[k v] (group-by identity %)] [k (count v)]))
     [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})
(= ((fn [s] (apply merge-with + (map #(assoc nil % 1) s))) [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})
(= (#(apply merge-with + (map (fn [x] {x 1}) %)) [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})

; 7. Write a function which removes the duplicates from a sequence. Order of the items must
; be maintained. [restrictions: distinct]
;(= (__ [1 2 1 3 1 2 4]) [1 2 3 4])
;(= (__ [:a :a :b :b :c :c]) [:a :b :c])
;(= (__ '([2 4] [1 2] [1 3] [1 3])) '([2 4] [1 2] [1 3]))
(= ((fn [xs] (reduce #(if (.contains %1 %2) %1 (conj %1 %2)) [] xs)) [1 2 1 3 1 2 4]) [1 2 3 4])
(= ((fn [xs] (reduce #(if (.contains %1 %2) %1 (conj %1 %2)) [] xs)) [:a :a :b :b :c :c]) [:a :b :c])

(defn contains-val?
  [coll val]
  (when (seq coll) (or (= val (first coll)) (recur (next coll) val))))

(= (reduce (fn [s e] (if (some #(= % e) s) s (conj s e))) [] [1 2 1 3 1 2 4]) [1 2 3 4])

; 8. Write a function which allows you to create function compositions.
; The parameter list should take a variable number of functions, and create a function
; that applies them from right-to-left. [restrictions: comp]
;(= [3 2 1] ((__ rest reverse) [1 2 3 4]))
;(= 5 ((__ (partial + 3) second) [1 2 3 4]))
;(= true ((__ zero? #(mod % 8) +) 3 5 7 9))
;(= "HELLO" ((__ #(.toUpperCase %) #(apply str %) take) 5 "hello world"))
(= [3 2 1]
   (((fn [& fs]
       (fn [& args]
         (reduce #(%2 %1) (apply (last fs) args) (reverse (butlast fs))))) rest reverse) [1 2 3 4]))


; 9. Take a set of functions and return a new function that takes a variable number of arguments
; and returns a sequence containing the result of applying each function left-to-right
; to the argument list. [restrictions: juxt]
;(= [21 6 1] ((__ + max min) 2 3 5 1 6 4))
;(= ["HELLO" 5] ((__ #(.toUpperCase %) count) "hello"))
(= [21 6 1] (((fn [& fs]
                (fn [& args]
                  (map #(apply % args) fs))) + max min) 2 3 5 1 6 4))


; 10. Write a function which behaves like reduce, but returns each intermediate value of the
; reduction. Your function must accept either two or three arguments, and the return sequence
; must be lazy. [restrictions: reductions]
;(= (take 5 (__ + (range))) [0 1 3 6 10])
;(= (__ conj [1] [2 3 4]) [[1] [1 2] [1 2 3] [1 2 3 4]])
;(= (last (__ * 2 [3 4 5])) (reduce * 2 [3 4 5]) 120)
(last (
        (fn reduce+
          ([f coll]
           (reduce+ f (first coll) (rest coll)))
          ([f init coll]
           (cons init (lazy-seq
                        (when-let [s (seq coll)] (reduce+ f (f init (first s)) (rest s)))))))
        * 2 [3 4 5]))

; 11. Clojure has many sequence types, which act in subtly different ways. The core functions
; typically convert them into a uniform "sequence" type and work with them that way, but it can
; be important to understand the behavioral and performance differences so that you know which kind
; is appropriate for your application.
;
; Write a function which takes a collection and returns one of :map, :set, :list, or :vector - describing
; the type of collection it was given.
; You won't be allowed to inspect their class or use the built-in predicates like list? - the point is
; to poke at them and understand their behavior.
; [restrictions: class type Class vector? sequential? list? seq? map? set? instance? getClass]
((fn [xs]
   (cond
     (reversible? xs) :vector
     (associative? xs) :map
     (= (doall (take 3 (conj xs 1 2 3))) '(3 2 1)) :list
     :else :set)) '(1 2 3))

; 12. Write a function which returns the first x number of prime numbers.
;(= (__ 2) [2 3])
;(= (__ 5) [2 3 5 7 11])
(defn primes
  ([n] (take n (primes n 2)))
  ([n prev]
   (cons prev
         (lazy-seq
           (primes n
                   ((fn next-prime [x]
                      (if (every? #(not (= (rem x %) 0)) (range 2 (inc (Math/sqrt x))))
                        x
                        (recur (inc x)))) (inc prev)))))))

; 13. Write a function which takes a function f and a variable number of maps.
; Your function should return a map that consists of the rest of the maps conj-ed
; onto the first. If a key occurs in more than one map, the mapping(s) from the
; latter (left-to-right) should be combined with the mapping in the result by
; calling (f val-in-result val-in-latter) [restrictions: merge-with]
;(= (__ * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5})
;   {:a 4, :b 6, :c 20})
;(= (__ - {1 10, 2 20} {1 3, 2 10, 3 15})
;   {1 7, 2 10, 3 15})
((fn [f & maps]
   (when (some identity maps)
     (let [merge-entry
           (fn [map entry]
             (let [k (key entry) v (val entry)]
               (if (contains? map k)
                 (assoc map k (f (get map k) v))
                 (assoc map k v))))
           do-merge
           (fn [first-map second-map]
             (reduce merge-entry first-map second-map))]
       (reduce do-merge maps))))
  * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5})

(= ((fn [op & maps]
      (let [upd-val #(fn [v] (if (nil? v) % (op v %)))
            upd #(update-in %1 [%2] (upd-val %3))
            join #(reduce-kv upd %1 %2)]
        (reduce join {} maps))) * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5})
   {:a 4, :b 6, :c 20})

(= ((fn [f & ms]
      (->> ms
           (apply concat)
           (group-by key)
           (map (fn [[k vs]] [k (reduce f (map val vs))]))
           (into {}))) * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5})
   {:a 4, :b 6, :c 20})

; 14. Write a function that splits a sentence up into a sorted list of words.
; Capitalization should not affect sort order and punctuation should be ignored.
;(= (__  "Have a nice day.") ["a" "day" "Have" "nice"])
((fn [x]
   (sort #(compare (clojure.string/lower-case %1) (clojure.string/lower-case %2))
         (#(clojure.string/split % #"\s+") (#(clojure.string/replace % #"[^A-Za-z]" " ") x))))
  "Have a nice day.")

(= (#(->> (re-seq #"\w+" %)
          (sort-by clojure.string/lower-case))
     "Have a nice day.") ["a" "day" "Have" "nice"])

; 15. Given a string of comma separated integers, write a function which returns a new
; comma separated string that only contains the numbers which are perfect squares.
;(= (__ "4,5,6,7,8,9") "4,9")
;(= (__ "15,16,25,36,37") "16,25,36")
(= ((fn [str]
      (when-let [ints (re-seq #"\d+" str)]
        (clojure.string/join "," (filter #(let [sqrt (Math/round (Math/sqrt %))]
                                            (= (int (* sqrt sqrt)) %))
                                         (map #(Integer/parseInt %) ints))))) "4,5,6,7,8,9") "4,9")

(= (#(->> (str "[" % "]")
          (read-string)
          (filter (fn [v] (let [p (int (Math/sqrt v))] (= v (* p p)))))
          (clojure.string/join ",")) "4,5,6,7,8,9") "4,9")

; 16. The trampoline function takes a function f and a variable number of parameters.
; Trampoline calls f with any parameters that were supplied. If f returns a function,
; trampoline calls that function with no arguments. This is repeated, until the return
; value is not a function, and then trampoline returns that non-function value.
; This is useful for implementing mutually recursive algorithms in a way that won't consume
; the stack.
(= [1 3 5 7 9 11]
   (letfn
     [(foo [x y] #(bar (conj x y) y))
      (bar [x y] (if (> (last x) 10)
                   x
                   #(foo x (+ 2 y))))]
     (trampoline foo [] 1)))

; 17. Reimplement the function trampoline
; (= (letfn [(triple [x] #(sub-two (* 3 x)))
;          (sub-two [x] #(stop?(- x 2)))
;          (stop? [x] (if (> x 50) x #(triple x)))]
;    (__ triple 2))
;  82)

; 17. When working with java, you often need to create an object with fieldsLikeThis, but you'd
; rather work with a hashmap that has :keys-like-this until it's time to convert. Write a
; function which takes lower-case hyphen-separated strings and converts them to camel-case
; strings.
;(= (__ "something") "something")
;(= (__ "multi-word-key") "multiWordKey")
;(= (__ "leaveMeAlone") "leaveMeAlone")
(= (#(let [[f & r] (clojure.string/split % #"-")
           up (clojure.string/join (map clojure.string/capitalize r))]
       (str f up))
     "multi-word-key") "multiWordKey")

(= (#(clojure.string/replace % #"\-." (comp clojure.string/upper-case last))
     "multi-word-key") "multiWordKey")

; 18. Given any number of sequences, each sorted from smallest to largest, find the smallest
; single number which appears in all of the sequences. The sequences may be infinite, so be
; careful to search lazily.
;(= 3 (__ [3 4 5]))
;(= 4 (__ [1 2 3 4 5 6 7] [0.5 3/2 4 19]))
;(= 7 (__ (range) (range 0 100 7/6) [2 3 5 7 11 13]))
;(= 64 (__ (map #(* % % %) (range)) ;; perfect cubes
;          (filter #(zero? (bit-and % (dec %))) (range)) ;; powers of 2
;          (iterate inc 20))) ;; at least as large as 20
((fn meth [& xss]
   (let [max (apply max (map first xss))]
     (if (apply = max (map first xss))
       max
       (loop [colls xss
              res []]
         (if (next colls)
           (recur (next colls) (conj res (drop-while #(> max %) (first colls))))
           (apply meth (conj res (drop-while #(> max %) (first colls))))
           )))))
   [1 2 3 4 5 6 7] [0.5 3/2 4 19])

(= 4 ((fn [& xxs]
        (let [[v-min v-max] (first (apply map (juxt min max) xxs))]
          (if (= v-min v-max)
            v-min
            (recur (map (fn [xs] (drop-while #(< % v-max) xs)) xxs)))))
       [1 2 3 4 5 6 7] [0.5 3/2 4 19]))

(= 4 ((letfn
        [(match? [x s] (or (= x (first s)) (and (> x (first s)) (recur x (rest s)))))
         (first-common [s1 & sn] (first (drop-while #(not-every? (partial match? %) sn) s1)))]
        first-common)
       [1 2 3 4 5 6 7] [0.5 3/2 4 19]))

; 19. When parsing a snippet of code it's often a good idea to do a sanity check to see if
; all the brackets match up. Write a function that takes in a string and returns truthy if
; all square [ ] round ( ) and curly { } brackets are properly paired and legally nested,
; or returns falsey otherwise.
;(not (__ "(start, end]"))
;(not (__ "())"))
;(__ "([]([(()){()}(()(()))(([[]]({}()))())]((((()()))))))")
;(__ "class Test {
;      public static void main(String[] args) {
;        System.out.println(\"Hello world.\");
;      }
;    }")
((fn [str]
   (let [brackets (re-seq #"\(|\)|\[|\]|\{|\}" str)
         open-to-closed {"(" ")", "[" "]", "{" "}"}
         stack (Stack.)]
     (empty?
       (reduce (fn [stack it]
                 (cond
                   (or (= "(" it) (= "[" it) (= "{" it)) (do (.push stack it) stack)
                   :else (if (and (not (empty? stack)) (= (get open-to-closed (.peek stack)) it))
                           (do (.pop stack) stack)
                           (do (.push stack it) stack))))
               stack
               brackets)))
   )
  "class Test {
     public static void main(String[] args) {
       System.out.println(\"Hello world.\");
     }
   }")

((fn [s]
   (let [p  {\( \) \[ \] \{ \}}
         b #{\( \) \[ \] \{ \}}]
     (empty?
       (reduce (fn [[h & t :as xs] x]
                 (cond
                   (= (p h) x) t
                   (b x) (conj xs x)
                   :else xs))
               () s))))
  "([]([(()){()}(()(()))(([[]]({}()))())]((((()()))))))")

; 20. Write a function which generates the power set of a given set. The power set of a set x
; is the set of all subsets of x, including the empty set and x itself.
;(= (__ #{1 :a}) #{#{1 :a} #{:a} #{} #{1}})
;(= (__ #{}) #{#{}})
;(= (__ #{1 2 3}) #{#{} #{1} #{2} #{3} #{1 2} #{1 3} #{2 3} #{1 2 3}})
;(= (count (__ (into #{} (range 10)))) 1024)
((fn perms
   [xs]
   (if (empty? xs)
     #{#{}}
     (let [val (first xs)
           res (perms (rest xs))]
       (into res (map #(conj % val) res)))))
  #{1 2 3})

(= (reduce (fn [r e] (into r (map #(conj % e) r))) #{#{}} #{1 2 3})
   #{#{} #{1} #{2} #{3} #{1 2} #{1 3} #{2 3} #{1 2 3}})

; 21. Write a function that accepts a curried function of unknown arity n. Return an equivalent function
; of n arguments.
;(= 10 ((__ (fn [a]
;             (fn [b]
;               (fn [c]
;                 (fn [d]
;                   (+ a b c d))))))
;       1 2 3 4))

(= 10 (((fn [f]
          (fn [& args] (reduce #(apply %1 (vector %2)) f args))) (fn [a]
             (fn [b]
               (fn [c]
                 (fn [d]
                   (+ a b c d))))))
       1 2 3 4))

; 22. Happy numbers are positive integers that follow a particular formula: take each individual digit,
; square it, and then sum the squares to get a new number. Repeat with the new number and eventually,
; you might get to a number whose squared sum is 1. This is a happy number. An unhappy number
; (or sad number) is one that loops endlessly. Write a function that determines if a number is happy
; or not.
;(= (__ 7) true)
;(= (__ 986543210) true)
;(= (__ 2) false)

;We can solve this problem without using extra space and that technique can be used in some other similar
;problem also. If we treat every number as a node and replacement by square sum digit as a link, then
;this problem is same as finding a loop in a linklist.
((fn happy-number?
   ([x] (happy-number? x #{}))
   ([x seen]
    (let [digits (fn [x] (map #(java.lang.Character/digit % 10) (str x)))
          square (fn [x] (* x x))
          square-sum (fn [x] (reduce + 0 (map square (digits x))))
          value (square-sum x)]
      (if (contains? seen value)
        false
        (if (= value 1)
          true
          (recur value (conj seen value)))))))
  2)


































