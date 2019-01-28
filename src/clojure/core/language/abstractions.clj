(ns clojure.core.language.abstractions)

; Multimethods
; Multimethods give you a direct, flexible way to introduce polymorphism into your code.
; Using multimethods, you associate a name with multiple implementations by defining a
; dispatching function, which produces dispatching values that are used to determine
; which method to use.

(defmulti full-moon-behavior (fn [were-creature] (:were-type were-creature)))

(defmethod full-moon-behavior :wolf
  [were-creature]
  (str (:name were-creature) " will howl and murder"))

(defmethod full-moon-behavior :simmons
  [were-creature]
  (str (:name were-creature) " will encourage people and sweat to the oldies"))

(defmethod full-moon-behavior nil
  [were-creature]
  (str (:name were-creature) " will stay at home and eat ice cream"))

(defmethod full-moon-behavior :default
  [were-creature]
  (str (:name were-creature) " will stay up all night fantasy footballing"))

(full-moon-behavior {:were-type :wolf :name "Rachel from next door"})
(full-moon-behavior {:name "Andy the baker" :were-type :simmons})

; We create the multimethod at. This tells Clojure, “Hey, create a new multimethod
; named full-moon-behavior. Whenever someone calls full-moon-behavior, run the dispatching
; function (fn [were-creature] (:were-type were-creature)) on the arguments. Use the result
; of that function, aka the dispatching value, to decide which specific method to use!”

; multi-args
(defmulti types (fn [x y] [(class x) (class y)]))
(defmethod types [java.lang.String java.lang.String]
  [x y]
  "Two strings!")

; ---- Protocols
; Approximately 93.58 percent of the time, you’ll want to dispatch to methods according to
; an argument’s type. For example, count needs to use a different method for vectors than it
; does for maps or for lists. Although it’s possible to perform type dispatch with multimethods,
; protocols are optimized for type dispatch. They’re more efficient than multimethods, and
; Clojure makes it easy for you to succinctly specify protocol implementations.

; A multimethod is just one polymorphic operation, whereas a protocol is a collection of one or
; more polymorphic operations. Protocol operations are called methods, just like multimethod
; operations. Unlike multimethods, which perform dispatch on arbitrary values returned by a
; dispatching function, protocol methods are dispatched based on the type of the first argument,
; as shown in this example:
(defprotocol psychodynamics
  "Plumb the inner depths of your data types"
  (thoughts [x] "The data type's innermost thoughts")
  (feelings-about [x] [x y] "Feelings about self or other"))
; A method signature consists of a name, an argument specification, and an optional docstring
; The first method signature is named thoughts and can take only one argument. The second is
; named feelings-about and can take one or two arguments. Protocols do have one limitation:
; the methods can’t have rest arguments.

; By defining a protocol, you’re defining an abstraction, but you haven’t yet defined how that
; abstraction is implemented. It’s like you’re reserving names for behavior (in this example,
; you’re reserving thoughts and feelings-about), but you haven’t defined what exactly the behavior
; should be. If you were to evaluate (thoughts "blorb"), you would get an exception that reads,
; “No implementation of method: thoughts of protocol: data-psychology/Psychodynamics found for
; class: java.lang.String.” Protocols dispatch on the first argument’s type, so when you call
; (thoughts "blorb"), Clojure tries to look up the implementation of the thoughts method for strings,
; and fails.

; You can fix this sorry state of affairs by extending the string data type to implement the
; Psychodynamics protocol:

(extend-type java.lang.String
  psychodynamics
  (thoughts [x] (str x " thinks, 'Truly, the character defines the data type'"))
  (feelings-about
    ([x] (str x " is longing for a simpler way of life"))
    ([x y] (str x " is envious of " y "'s simpler way of life"))))


(thoughts "blorb")
(feelings-about "schmorb")
(feelings-about "schmorb" 2)


; or

(extend-protocol psychodynamics
  java.lang.String
  (thoughts [x] "Truly, the character defines the data type")
  (feelings-about
    ([x] "longing for a simpler way of life")
    ([x y] (str "envious of " y "'s simpler way of life")))

  java.lang.Object
  (thoughts [x] "Maybe the Internet is just a vector for toxoplasmosis")
  (feelings-about
    ([x] "meh")
    ([x y] (str "meh about " y))))


; ---- Records
; Clojure allows you to create records, which are custom, maplike data types. They’re maplike in that
; they associate keys with values, you can look up their values the same way you can with maps, and
; they’re immutable like maps. They’re different in that you specify fields for records. Fields are
; slots for data; using them is like specifying which keys a data structure should have. Records are
; also different from maps in that you can extend them to implement protocols.
(defrecord werewolf [name title])

; This record’s name is WereWolf, and its two fields are name and title.
; You can create an instance of this record in three ways:
(werewolf. "David" "London Tourist")
(->werewolf "Jacob" "Lead Shirt Discarder")
(map->werewolf {:name "Lucian" :title "CEO of Melodrama"})

; You can look up record values in the same way you look up map values
(def jacob (->werewolf "Jacob" "Lead Shirt Discarder"))
(.name jacob)
(:name jacob)
(get jacob :name)

; Any function you can use on a map, you can also use on a record:
(assoc jacob :title "Lead Third Wheel")
; => #werewolf{:name "Jacob", :title "Lead Third Wheel"}
(dissoc jacob :title)
; => {:name "Jacob"} <- that's not a werewolf

; Here’s how you would extend a protocol when defining a record:
(defprotocol WereCreature
  (full-moon-behavior [x]))

(defrecord WereWolf [name title]
  WereCreature
  (full-moon-behavior [x]
    (str name " will howl and murder")))

























