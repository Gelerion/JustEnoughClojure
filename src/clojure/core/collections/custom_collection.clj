(ns clojure.core.collections.custom-collection
  (:import (clojure.lang Seqable Counted Indexed ILookup)
           (java.io Writer)))

; We’re going to implement a custom Pair class that holds two values we’ll refer to as a and b.
; We’d like the Pair type to work with seq, count, nth, and get

; We implement a custom data structure by using the deftype macro, which looks similar to defrecord
; but provides more features and fewer built-in similarities to maps. For example, deftypes get a
; type and constructor functions as records do, but they don’t automatically act like maps.
; With deftype, it’s our responsibility to implement the proper interfaces to act like a map if we need it.
; Types also have support for specialized features such as mutable and unsynchronized fields, which
; aren’t available in any other Clojure construct.

(deftype Pair [a b]
  Seqable
  (seq [_] (seq [a b]))

  Counted
  (count [_] 2)

  Indexed
  (nth [_ i]
    (case i
      0 a
      1 b
      (throw (IllegalArgumentException.))))
  (nth [this i _] (nth this i))

  ILookup
  (valAt [_ k _]
    (case k
      0 a
      1 b
      (throw (IllegalArgumentException.))))
  (valAt [this k] (.valAt this k nil)))

(def pair (->Pair :a :b))
(seq pair)
; (:a :b)
(count pair)
; 2
(nth pair 1)
; :b
(get pair 0)
; :a

; Custom print
(defmethod print-method Pair
  [pair ^Writer w]
  (.write w "#clojure.core.collections.custom-collection.Pair")
  (print-method (vec (seq pair)) w))

(defmethod print-dup Pair
  [pair w]
  (print-method pair w))

