(ns real.world.domain.constructing-entities)

; It’s useful to have a naming convention for constructor functions across your project. The Clojure community
; has no standard naming convention for constructors, but some of the most common constructor prefixes
; used are new-, make-, and map->

; Constructing with options
; Use of optional arguments can give your constructors flexibility. If you’re expecting to build entities
; in a variety of ways, optional arguments can help.
(defn fn-wth-opts [f1 f2 & opts] (str "stub"))

; Positional Destructuring for clarity
(defn make-entity [f1 f2 & [f3 f4]] (str "stub"))

; You can use this technique to define a constructor function that takes zero or more fields, prioritized
; by dependency. Let’s look at a snippet concerned with manipulating currency. Along with methods for adding
; currency (+$) and multiplying sums by a number (*$), we want a Money entity that encapsulates a money value
; in a particular currency.

; In Patterns of Enterprise Application Architecture [Fow03], Martin Fowler describes a representation of
; monetary values that avoids many of the pitfalls of using a floating-point number, while abstracting away issues
; of currency. Let’s build a Money value object in Clojure

(declare validate-same-currency)

(defrecord Currency [divisor sym desc])
(defrecord Money [amount ^Currency currency]
  Comparable (compareTo [m1 m2]
               (validate-same-currency m1 m2)
               (compare (:amount m1) (:amount m2))))

(def currencies {:usd (->Currency 100 "USD" "US Dollars")
                 :eur (->Currency 100 "EUR" "Euro")})

; flexible constructor
(defn make-money
  ([] (make-money 0))
  ([amount] (make-money amount :usd))
  ([amount currency] (->Money amount currency)))

; (make-money)
; (make-money 1)
; (make-money 5 (:eur currencies))

; Sometimes we want to create an entity representing a zero quantity or an empty container.
; You could create a function to construct a single entity in its default state
(defn new-money "$0.00 used" [] (->Money 0 :used))
(def zero-dollars (->Money 0 :usd))

; functions for adding, comparing, multiplying, and other operations
(defn- validate-same-currency
  [m1 m2]
  (or (= (:currency m1) (:currency m2))
      (throw (ex-info "Currencies do not match." {:m1 m1 :m2 m2}))))

(defn =$
  ([m1] true)
  ([m1 m2] (zero? (.compareTo m1 m2)))
  ([m1 m2 & monies] (every? zero? (map #(.compareTo m1 %) (conj monies m2)))))

(defn +$
  ([m1] m1)
  ([m1 m2]
    (validate-same-currency m1 m2)
    (->Money (+ (:amount m1) (:amount m2)) (:currency m1)))
  ([m1 m2 & monies]
    (reduce +$ m1 (conj monies m2))))

(defn *$ [m n] (->Money (* n (:amount m)) (:currency m)))

; Map Destructuring
; Often, though, it’s useful to accept optional arguments in any order. In this case, accepting a map of
; options that can be destructured is one simple solution:
(defn make-entity-opts [f1 f2 {:keys [f3 f4] :as opts}] (str "stub"))

; For example, consider extending our space simulation to also include data about the Apollo missions.
; These missions varied as to whether they were manned, had a lunar module, and so on. We can accept
; all of these options by destructuring a single map of options:

(def mission-defaults {:orbits 0 :evas 0})                  ;defaults

(defn make-mission
  [name system launched manned? opts]
  (let [{:keys [cm-name                                     ; command module
                lm-name                                     ; lunar module
                orbits
                evas]} (merge mission-defaults opts)]
    (str "stub")))

(def apollo-4 (make-mission
                "Apollo 4"
                "Saturn V"
                #inst "1967-11-09T12:00:01-00:00"
                false
                {:orbits 3}))

(defn make-mission-varargs
  [name system launched manned? & opts]
  (let [{:keys [cm-name                                     ; command module
                lm-name                                     ; lunar module
                orbits
                evas]} :or {:orbits 0 :evas 0} opts]
    (str "stub")))

(def apollo-11 (make-mission-varargs
                "Apollo 11"
                "Saturn V"
                #inst "1967-11-09T12:00:01-00:00"
                false
                :orbits 3
                :evas 1))




















