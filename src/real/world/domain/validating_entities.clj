(ns real.world.domain.validating-entities
  (:require [schema.core :as schema]))
;https://github.com/plumatic/schema

; A number of external libraries exist to provide data description and validation support. We’ll focus
; on Prismatic’s Schema library, but you may also want to look at:
;  core.typed, clj-schema, Strucjure, or seqex

(schema/defrecord Recipe [name :- [schema/Str]
                          description :- [schema/Str]
                          ingredients :- [Ingredient]
                          steps :- [schema/Str]
                          servings :- [schema/Int]
                          ])

(schema/defrecord Ingredient [name :- [schema/Str]
                              quantity :- [schema/Int]
                              unit :- [schema/Keyword]
                              ])

; ask for explanation
(schema/explain Recipe)

(def spaghetti-tacos
  (map->Recipe
    {:name        "Spaghetti tacos"
     :description "It's spaghetti... in a taco."
     :ingredients [(->Ingredient "Spaghetti" 1 :lb)
                   (->Ingredient "Spaghetti sauce" 16 :oz)
                   (->Ingredient "Taco shell" 12 :shell)]
     :steps       ["Cook spaghetti according to box."
                   "Heat spaghetti sauce until warm."
                   "Mix spaghetti and sauce."
                   "Put spaghetti in taco shells and serve."]
     :servings    4}))

; Validate our data against the schema
(schema/check Recipe spaghetti-tacos)


; Schema also has a version of defn to specify schema shapes as input parameters and return types.
; The types are used to create a helpful docstring:
(schema/defn add-ingredients :- Recipe
  [recipe :- Recipe & ingredients :- [Ingredient]]
  (update-in recipe [:ingredients] into ingredients))