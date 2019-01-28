(ns clojure.core.language.high-order-functions)

; ---- functions

; -- apply
; apply explodes a seqable data structure so it can be passed to a function that
; expects a rest parameter.
(max 0 1 2)
; but
(max [0 1 2]) ; returns the vector!, max returns the greatest of all the elements
; so solution is to explode arguments so they are passing as separate elements
(apply max [0 1 2])

(defn my-into
  [target additions]
  (apply conj target additions))

; -- partial
; partial takes a function and any number of arguments. It then returns a new function.
; When you call the returned function, it calls the original function with the original
; arguments you supplied it along with the new arguments
(def add10 (partial + 10))
(add10 3)

(def add-missing-elements
  (partial conj ["water" "earth" "air"]))
(add-missing-elements "adamantium")

(defn my-partial
  [partialized-fn & args]
  (fn [& more-args]
    (apply partialized-fn (into args more-args))))

(def add20 (my-partial + 20))
(add20 5)

;In general, you want to use partials when you find you’re repeating the same combination of
;function and arguments in many different contexts. This toy example shows how you could use
;partial to specialize a logger, creating a warn function:
(defn lousy-logger
  [log-level message]
  (condp = log-level
    :warn (clojure.string/lower-case message)
    :emergency (clojure.string/upper-case message)))

(def warn (partial lousy-logger :warn))
(warn "Red Light Alert")

; -- complement

(defn identify-humans
  [social-security-numbers]
  (filter #(not (vampire? %))
          (map vampire-related-details social-security-numbers)))

; Look at the first argument to filter, #(not (vampire? %)). It’s so common to want
; the complement (the negation) of a Boolean function that there’s a function, complement,
; for that:
(def not-vampire? (complement vampire?))
(defn identify-humans
  [social-security-numbers]
  (filter not-vampire?
          (map vampire-related-details social-security-numbers)))


(defn my-complement
  [fun]
  (fn [& args]
    (not (apply fun args))))

(def my-pos? (my-complement neg?))
(my-pos? 1)


; -- comp
; Clojure provides a function, comp, for creating a new function from
; the composition of any number of functions.
((comp inc *) 2 3) ; -> 2 * 3 = 6, 6 + 1 = 7
; using comp on the functions f1, f2, ... fn, creates a new function g such that g(x1, x2, ... xn)
; equals f1( f2( fn(x1, x2, ... xn))). One detail to note here is that the first function
; applied — * in the code shown here—can take any number of arguments, whereas the remaining
; functions must be able to take only one argument.

(def character
  {:name "Smooches McCutes"
   :attributes {:intelligence 10
                :strength 4
                :dexterity 5}})

(def c-int (comp :intelligence :attributes))
(def c-str (comp :strength :attributes))
(c-int character)
(c-str character) ; same as (fn [c] (:strength (:attributes c)))

;What do you do if one of the functions you want to compose needs to take more than one argument?
;You wrap it in an anonymous function. Have a look at this next snippet:
(defn spell-slots [char] (int (inc (/ (c-int char) 2))))
; achieve the sane with comp
(defn spell-slots-comp (comp int inc #(/ % 2) c-int))


; -- memoize
; Memoization lets you take advantage of referential transparency by storing the arguments passed
; to a function and the return value of the function.
(defn sleepy-identity [x] (Thread/sleep 1000) x)
(def memo-sleepy-identity (memoize sleepy-identity))


; juxt
; Takes a set of functions and returns a fn that is the juxtaposition
; of those fns.
; ((juxt a b c) x) => [(a x) (b x) (c x)]

;;; Extract values from a map, treating keywords as functions.
((juxt :a :b) {:a 1 :b 2 :c 3 :d 4}) ; [1 2]

((juxt identity name) :keyword)
;;=> [:keyword "keyword"]

(juxt identity name)
; ...is the same as:
;(fn [x] [(identity x) (name x)])

((juxt first count) "Clojure Rocks") ; [\C 13]


;iterate
(iterate inc 5)

(iterate (fn [[a b]] [b (+' a b)]) [0 1])

(def powers-of-two (iterate (partial * 2) 1))
(nth powers-of-two 10) ; 1024