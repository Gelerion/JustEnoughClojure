(ns real.world.domain.modeling-relationships)

; Entities by themselves aren’t much use. Most models will need to connect entities of different types to
; create relationships in the data, like the foreign keys in a SQL database.
;
; An entity can use three main techniques to refer to another entity: nesting, identifiers, and stateful
; references. All of these techniques have analogues in other languages (and databases), but Clojure users
; prioritize the use of these techniques differently than in other languages. Languages like Java rely
; heavily on stateful references from one mutable object to another. Clojure users use the nesting and
; identifier techniques first and fall back to stateful references only in special cases.
;
; Nesting simply means including another entity directly under a parent entity. Nesting is an easy choice
; when the nested entity is part of the parent entity and will follow its life cycle.
(defrecord Recipe [name author description])
(defrecord Person [first-name last-name])

; nesting
; Now let’s consider the options we have for connecting Recipe and Person instances. If we’re interested in
; making the Recipe the centerpiece of our application and consider authors to be merely descriptive
; information about the recipe, we can nest the person underneath the recipe:
(def toast (->Recipe "Toast"
                    (->Person "Alex"  "Miller") ;;nested
                    "Crispy bread"))

; identifier
; Or you might want both Person and Recipe to be top-level entities that can each be updated in a single place.
; For example, a Recipe might have multiple authors. In this case, we may not want to rely on nesting at
; instead refer to an entity by a well-known identifier. An identifier is a simple value (usually a
; keyword, string, or number) that refers to an entity defined elsewhere. Let’s rework our data model to allow
; recipes and authors to be managed independently:
(def people {"p1" (->Person "Alex"  "Miller")})
(def recipes {"r1" (->Recipe "Toast"
                             "p1" ;;Person Id
                             "Crispy bread")})