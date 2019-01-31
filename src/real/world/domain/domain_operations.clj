(ns real.world.domain.domain-operations
  (:require [real.world.domain.constructing-entities :refer :all]
            [real.world.domain.validating_entities])
  (:import [real.world.domain.validating_entities Recipe Ingredient]))

; We often need to define a function for our domain that can be applied to many different types of domain entities.
; This is particularly useful when domain entities of different types are collected together in a composite data
; structure.
;
; Object-oriented languages typically address this need via polymorphism. Polymorphism is a means of abstraction,
; allowing a domain operation to be decoupled from the types to which it can be applied. This makes your
; domain implementation more general and provides a way to extend behavior without modifying existing code.
;
; Clojure provides two features that allow the creation of generic domain operations: multimethods and protocols.
; Choosing the specific function to invoke for a generic operation is known as dispatch. Both protocols and
; multimethods can dispatch based on argument type, but only multimethods can dispatch based on argument value.
; We’ll start by looking at how type-based dispatch compares in the two approaches and follow that with a
; look at value-based dispatch and how to layer protocols.

; Problem
; Consider our recipe-manager application and the need to calculate an estimated grocery cost for each recipe.
; The cost of each recipe will be dependent on adding up the costs of all the ingredients. We want to invoke
; the same generic domain operation (“How much does it cost?”) on entities of two specific types:
; Recipe and Ingredient.

; -- Multimethod
; To implement this domain operation with multimethods, we use two forms: defmulti and defmethod.
; The defmulti form defines the name and signature of the function as well as the dispatch function.
; Each defmethod form provides a function implementation for a particular dispatch value. Invoking the
; multimethod first invokes the dispatch function to produce a dispatch value, then selects the best
; match for that value, and finally invokes that function implementation.

; We need to extend our recipe-manager domain slightly to add a Store domain entity and a function that
; can look up the cost of an ingredient in a particular grocery store.
(defrecord Store [name])
(defn cost-of [store ingredient] (str "stub"))

; First the defmulti defines the dispatch function as (class entity), which produces a dispatch value
; based on type. If we were using maps instead of records, we would instead extract a type attribute
; with (:type entity) as the dispatch function.
(defmulti cost
          (fn [entity store] (class entity)))               ; dispatch function

(defmethod cost Recipe [recipe store]
  (reduce +$ zero-dollars (map #(cost % store) (:ingredients recipe))))

(defmethod cost Ingredient [ingredient store]
  (cost-of store ingredient))


; -- Protocols
(defprotocol Cost-protocol (cost [entity store]))
(extend-protocol Cost-protocol
  Recipe
  (cost [recipe store]
    (reduce +$ zero-dollars (map #(cost % store) (:ingredients recipe))))

  Ingredient
  (cost [ingredient store]
    (cost-of store ingredient)))

; Let’s compare these two approaches to type-based dispatch. Protocols are faster than multimethods for type
; dispatch because they leverage the underlying JVM runtime optimizations for this kind of dispatch
; (this is common in Java). Protocols also have the ability to group related functions together in a single
; protocol. For these reasons, protocols are usually preferred for type-based dispatch.
;
; However, whereas protocols only support type-based dispatch on the first argument to the generic function,
; multimethods can provide value-based dispatch based on any or all of the function’s arguments. Multimethods
; and protocols both support matching based on the Java type hierarchy, but multimethods can define and use
; custom value hierarchies and declare preferences between implementations when there’s more than one matching
; value.
;
; Thus, protocols are the preferred choice for the narrow (but common) case of type-based dispatch, and
; multimethods provide greater flexibility for a broad range of other cases.

; Value based dispatch
(defmulti convert
          "Convert quantity from unit1 to unit2, matching on [unit1 unit2]"
          (fn [unit1 unit2 quantity] [unit1 unit2]))

;; lb to oz
(defmethod convert [:lb :oz] [_ _ lb] (* lb 16))

;; oz to lb
(defmethod convert [:oz :lb] [_ _ oz] (/ oz 16))

;; fallthrough
(defmethod convert :default [u1 u2 q]
  (if (= u1 u2)
    q
    (assert false (str "Unknown unit conversion from " u1 " to " u2))))

(defn ingredient+
  "Add two ingredients into a single ingredient, combining their
  quantities with unit conversion if necessary."
  [{q1 :quantity u1 :unit :as i1} {q2 :quantity u2 :unit}]
  (assoc i1 :quantity (+ q1 (convert u2 u1 q2))))