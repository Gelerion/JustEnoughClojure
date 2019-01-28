(ns clojure.core.language.functions)

(inc 1.1)

(map inc [0 1 2 3 4])

;!! Clojure evaluates all function arguments recursively before passing them to the function.
(+ (inc 199) (/ 100 (- 7 2)))
(+ 200 (/ 100 (- 7 2))) ; evaluated "(inc 199)"
(+ 200 (/ 100 5))       ; evaluated (- 7 2)
(+ 200 20)              ; evaluated (/ 100 5)
;220 ; final evaluation

;Function definitions are composed of five main parts:
;  defn
;  Function name
;  A docstring describing the function (optional)
;  Parameters listed in brackets
;  Function body
(defn say_name
  "documentation for say_name"
  [name]
  (str "Your name is: " name))

(say_name "Denis")

; private definitions
;can not refer to it from another ns
(defn- private-def "Example" [])

; Parameters and Arity
;Clojure functions can be defined with zero or more parameters. The values you pass to functions
;are called arguments, and the arguments can be of any type. The number of parameters is the
;function’s arity.
(defn no-params
  "0-arity"
  []
  "I take no parameters!")
(defn one-param
  "1-arity"
  [x]
  (str "I take one parameter: " x))
(defn two-params
  "2-arity"
  [x y]
  (str "Two parameters: " x y))

(defn do-things
  [& args]
  (str args))
; Arity overloading - body call will be dependant on the arity
(defn multi-arity
  ;3-arity arguments and body
  ([first second third]
   (do-things first second third))
  ;2-arity args and body
  ([first second]
   (do-things first second))
  ;1-arity args and body
  ([first]
   (do-things first)))

; providing default values
(defn default-vals-example
  ([name age]
   (str name " : " age))
  ([name]
   (default-vals-example name "unknown")))

(default-vals-example "Denis")
(default-vals-example "Denis" 31)

; variable arity-functions, rest parameters are indicated by ampersand &
(defn favorite-things
  [name & things]
  (str "Hi " name " here are my favorite things: "
       (clojure.string/join ", " things)))

(favorite-things "Doreen" "gum" "shoes" "kara-te")

; Destructing, lets concisely bind names within a collection
(defn bind-first-value-to-name
  ; Notice that arg is within a vector
  [[first-collection-value]]
  first-collection-value)

(bind-first-value-to-name ["three" "two" "one"])

(defn chooser
  [[first-choice second-choice & unimportant-choices]]
  (println (str "Your first choice is: " first-choice))
  (println (str "Your second choice is: " second-choice))
  (println (str "We're ignoring the rest of your choices. "
                "Here they are in case you need to cry over them: "
                (clojure.string/join ", " unimportant-choices))))

(chooser ["Marmalade", "Handsome Jack", "Pigpen", "Aquaman"])

; Destructing maps
(defn announce-treasure-location
  [{lat :lat lng :lng}]
  ;shorter syntax
  ;[{ :keys [lat lng] }]
  (println (str "Treasure lat: " lat))
  (println (str "Treasure lng: " lng)))

(announce-treasure-location {:lat 28.22 :lng 81.33})

; accessing original map arguments with alias
(defn receive-treasure-location
  [{ :keys [lat lng] :as treasure-location}]
  (println (str "Treasure lat: " lat))
  (println (str "Treasure lng: " lng))
  (str "Steer-ship! " treasure-location))

(receive-treasure-location {:lat 28.22 :lng 81.33})

; ------------

; Anonymous functions
; (fn name [param-list] body)
; (fn [param-list] body)
; another war to declare an anonymous function
; #( body ) , % - is an argument

;; Function call
;(* 8 3)
;; Anonymous function
;#(* % 3)

(map (fn [name] (str "Hi " name))
     ["Denis" "Elena"])

(def my-multiplier (fn [x] (* x 3)))
(my-multiplier 5)

(#(* 3 8))

(map #(str "Hi, " %)
     ["Denis" "Elena"])

; args
(#(str %1 " and " %2) "You" "Me")
; %& rest param
(#(identity %&) 1 "one" "two")

; returning functions
(defn inc-maker
  [inc-by]
  #(+ % inc-by))

(def inc3 (inc-maker 3))
(inc3 7)

