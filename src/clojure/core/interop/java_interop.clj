(ns clojure.core.interop.java-interop)

; Java Interop
; You can call methods on an object using (.methodName object). For example, because all Clojure
; strings are implemented as Java strings, you can call Java methods on them:
(.toUpperCase "string")
(.indexOf "leiningen" 3)

; You can also call static methods on classes and access classes’ static fields
(java.lang.Math/abs -3)

; All of these examples use macros that expand to use the dot special form.
(macroexpand-1 '(.toUpperCase "string")) ; => (. "string" toUpperCase)

; Creating and Mutating Objects
; You can create a new object in two ways: (new ClassName optional-args) and (ClassName. optional-args):
(new String)
(String. "hello")

; mutating
(let [stack (java.util.Stack.)]
  (.push stack "Push-it")
  stack)

; Clojure provides the doto macro, which allows you to execute multiple methods on the same object
; more succinctly:
(doto (java.util.Stack.)
  (.push "First")
  (.push "Second"))

; Importing
; (import [java.util Date Stack]
;         [java.net Proxy URI])

;(Date.)

; or in ns
;(ns pirate.talk
;  (:import [java.util Date Stack]
;           [java.net Proxy URI]))

; The Date Class
#inst "2016-09-19T20:40:02.733-00:00"

; Files and Input/Output
(let [file (java.io.File. "/")]
  (println (.exists file))
  (println (.canWrite file))
  (println (.getPath file)))

; clojure built in
; - slurp
; - spit

; The with-open macro is another convenience: it implicitly closes a resource at the end of
; its body, ensuring that you don’t accidentally tie up resources by forgetting to manually
; close the resource.

;(with-open [todo-list-rdr (clojure.java.io/reader "/tmp/hercules-todo-list")]
;  (println (first (line-seq todo-list-rdr))))

