(ns clojure.core.language.namespacesex)

;Technically, namespaces are objects of type clojure.lang.Namespace, and you can interact
;with them just like you can with Clojure data structures. For example, you can refer to
;the current namespace with *ns*, and you can get its name with (ns-name *ns*):

;If you want to just use the symbol itself, and not the thing it refers to, you have to quote it.
;Quoting any Clojure form tells Clojure not to evaluate it but to treat it as data. The next few
;examples show what happens when you quote a form
;inc
; => #<core$inc clojure.core$inc@30132014>
;'inc
; => inc
;(map inc [1 2])
; => (2 3)
;'(map inc [1 2])
; => (map inc [1 2])

; -- Storing Objects with def
(def great-books ["East of Eden" "The Glass Bead Game"])
; => #'user/great-books
;great-books
; => ["East of Eden" "The Glass Bead Game"]

;This code tells Clojure:
;
;Update the current namespace’s map with the association between great-books and the var.
;1. Find a free storage shelf.
;2. Store ["East of Eden" "The Glass Bead Game"] on the shelf.
;3. Write the address of the shelf on the var.
;4. Return the var (in this case, #'user/great-books).

;This process is called interning a var. You can interact with a namespace’s map of
; symbols-to-interned-vars using ns-interns. Here’s how you’d get a map of interned vars:
;(ns-interns *ns*)
; => {great-books #'user/great-books}
(get (ns-interns *ns*) 'great-books)
; => #'clojure-noob.namespacesex/great-books

;#'user/great-books is the reader form of a var. You can use #' to grab hold of
;the var corresponding to the symbol that follows; #'user/great-books lets you use
;the var associated with the symbol great-books within the user namespace. We can
;deref vars to get the objects they point to:
(deref #'clojure-noob.namespacesex/great-books)
; => ["East of Eden" "The Glass Bead Game"]

;refer
;refer gives you fine-grained control over how you refer to objects in other namespaces.
;Fire up a new REPL session and try the following. Keep in mind that it’s okay to play around
;with namespaces like this in the REPL, but you don’t want your Clojure files to look like this;
;
;user=> (in-ns 'cheese.taxonomy)
;cheese.taxonomy=> (def cheddars ["mild" "medium" "strong" "sharp" "extra sharp"])
;cheese.taxonomy=> (def bries ["Wisconsin" "Somerset" "Brie de Meaux" "Brie de Melun"])
;cheese.taxonomy=> (in-ns 'cheese.analysis)
;cheese.analysis=> (clojure.core/refer 'cheese.taxonomy)
;cheese.analysis=> bries
;; => ["Wisconsin" "Somerset" "Brie de Meaux" "Brie de Melun"]
;
;cheese.analysis=> cheddars
;; => ["mild" "medium" "strong" "sharp" "extra sharp"]

;When you call refer, you can also pass it the filters :only, :exclude, and :rename.
;As the names imply, :only and :exclude restrict which symbol/var mappings get merged
;into the current namespace’s ns-map. :rename lets you use different symbols for the
;vars being merged in. Here’s what would happen if we had modified the preceding
;example to use :only:
;
;cheese.analysis=> (clojure.core/refer 'cheese.taxonomy :only ['bries])
;(clojure.core/refer 'cheese.taxonomy :rename {'bries 'yummy-bries})


; -- alias
;Compared to refer, alias is relatively simple. All it does is let you shorten a namespace
;name for using fully qualified symbols:
;
;cheese.analysis=> (clojure.core/alias 'taxonomy 'cheese.taxonomy)
;cheese.analysis=> taxonomy/bries
;; => ["Wisconsin" "Somerset" "Brie de Meaux" "Brie de Melun"]


; --
;(ns the-divine-cheese-code.core)
;; Ensure that the SVG code is evaluated
;(require 'the-divine-cheese-code.visualization.svg)
;; Refer the namespace so that you don't have to use the
;; fully qualified name to reference svg functions
;(refer 'the-divine-cheese-code.visualization.svg)
; now we can use visualization.svg functions by name

; -- require with aliases
; (require '[the-divine-cheese-code.visualization.svg :as svg])
;is equivalent to this:
; (require 'the-divine-cheese-code.visualization.svg)
; (alias 'svg 'the-divine-cheese-code.visualization.svg)

; (require 'the-divine-cheese-code.visualization.svg)
;(refer 'the-divine-cheese-code.visualization.svg)
;is equivalent to this:
;
;(use 'the-divine-cheese-code.visualization.svg)
;You can alias a namespace with use just like you can with require. This:
;
;(require 'the-divine-cheese-code.visualization.svg)
;(refer 'the-divine-cheese-code.visualization.svg)
;(alias 'svg 'the-divine-cheese-code.visualization.svg)
;is equivalent to the code in Listing 6-2, which also shows aliased namespaces being used in function calls.
;
;(use '[the-divine-cheese-code.visualization.svg :as svg])
;(= svg/points points)
;; => true

; (use '[the-divine-cheese-code.visualization.svg :as svg :only [points]])

;-------------------------------------------------------------------
; You can also refer all symbols (notice the :all keyword):
;
;(ns the-divine-cheese-code.core
;  (:require [the-divine-cheese-code.visualization.svg :refer :all]))

