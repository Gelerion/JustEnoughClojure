(ns real.world.domain.modeling-entities)

; In Clojure, we use either maps or records to represent domain entities. Maps are generic collections
; of key-value pairs, whereas records construct a type with predefined structure for well-known fields.

; Model Earth planet with map
(def earth {:name       "Earth"
            :moons      1
            :volume     1.08321e12 ;; km^3​
            :mass       5.97219e24 ;; kg
            :aphelion   152098232 ;; km, farthest from sun
            :perihelion 147098290 ;; km, closest to sun​
            ;it’s often useful to have an entity type that can be used to drive dynamic behavior
            :type       :Planet ;; entity type​
            })
; We now have a planet instance and even a usable entity type, but we didn’t capture this structure
; (specifically the field names) in a way that’s useful to other developers on our team.

;; Model Earth planet with record
(defrecord Planet [name
                   moons
                   volume                                   ;; km^3​
                   mass                                     ;; kg
                   aphelion                                 ;; km, farthest from sun
                   perihelion                               ;; km, closest to sun​
                   ])
; Once the record structure is defined, we can use it to create many instances of the record with the same
; well-known fields. All instances of this record will have an observable type of Planet
; (in the namespace where we created it)

; - now it is very useful to define factory functions

; Positional factory function
(def make-earth
  ;expects a value for each attribute in the order specified by defrecord
  (->Planet "Earth" 1 1.08321e12 5.97219e24 152098232 147098290))

; Map factory function
(def make-map-earth
  ; The positional factory function is more concise but requires all attributes to be included in the specified
  ; order, so callers are more likely to break if the record is changed
  (map->Planet {:name "Earth"
                :moons 1
                :volume 1.08321e12
                :aphelion 152098232
                :perihelion 147098290}))

; -- Deciding between maps and records
; Maps and records both use the standard map collection functions for access and modification, but most of the
; time records are a better choice for domain entities. Records leverage features of the host platform—the
; Java Virtual Machine (JVM)—to provide better performance in several ways. Records define their type by
; creating a Java class with a field for each attribute. A record can thus take primitive type hints in its
; field definition and will create primitive fields in the underlying Java class, which provides a more
; efficient representation and faster arithmetic for numbers. The underlying Java class also provides a place
; to implement Java interfaces and Clojure protocols, placing behavior directly on the record and providing
; the fastest possible function dispatch for those cases.

; Given that records give you well-known fields, a type, factory functions, and better performance, they
; should be your first choice for domain entities. Why might we use maps instead?
;
; One specific case for which you should strongly consider maps is in public-facing APIs, whether they’re
; expected to be consumed by Java or by Clojure. In an API, it’s important to minimize the constraints on our
; callers. Requiring them to create instances of our record classes causes some of the details of those classes
; to be effectively public as well. In this case, maps with a well-known set of keys commit to less and are a
; simpler and better choice.