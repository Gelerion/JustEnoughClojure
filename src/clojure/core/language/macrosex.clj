(ns clojure.core.language.macrosex)

(defmacro backwards
  [form]
  (reverse form))

;The backwards macro allows Clojure to successfully evaluate the expression (" backwards" " am" "I" str),
;even though it doesn’t follow Clojure’s built-in syntax rules, which require an expression’s operand
;to appear first
(backwards (" backwards" " am" "I" str)) ; works!

;read - evaluate

;We can send your program’s data structures directly to the Clojure evaluator with eval
(def addition-list (list + 1 2))
(eval addition-list)

; interact with the reader
(read-string "(+ 1 2)")
(list? (read-string "(+ 1 2)")) ; true
(read-string "#(+ 1 %)") ; (fn* [p1__423#] (+ 1 p1__423#))

; -- reader macros: ' # @

; -- Macros
;(eval (read-string "(1 + 1)"))
; => ClassCastException java.lang.Long cannot be cast to clojure.lang.IFn

; However, read-string returns a list, and you can use Clojure to reorganize that
; list into something it can successfully evaluate:
(eval
  (let [infix (read-string "(1 + 1)")]
    (list (second infix) (first infix) (last infix))))
; => 2

; This is cool, but it’s also quite clunky. That’s where macros come in. Macros give you a
; convenient way to manipulate lists before Clojure evaluates them. Macros are a lot like
; functions: they take arguments and return a value, just like a function would. They
; work on Clojure data structures, just like functions do. What makes them unique and
; powerful is the way they fit in to the evaluation process. They are executed in between
; the reader and the evaluator—so they can manipulate the data structures that the reader
; spits out and transform with those data structures before passing them to the evaluator.

(defmacro ignore-last-operand
  [function-call]
  (butlast function-call))

(ignore-last-operand (+ 1 2 10)) ; => 3
(ignore-last-operand (+ 1 2 (println "look at me!!!"))) ;work just fine

; The macro ignore-last-operand receives the list (+ 1 2 10) as its argument,
; not the value 13. This is very different from a function call, because
; function calls always evaluate all of the arguments passed in, so there is
; no possible way for a function to reach into one of its operands and alter or ignore it.
; By contrast, when you call a macro, the operands are not evaluated. In particular,
; symbols are not resolved; they are passed as symbols. Lists are not evaluated either;
; that is, the first element in the list is not called as a function, special form, or macro.
; Rather, the unevaluated list data structure is passed in.


; ---- macroexpand
; Another difference is that the data structure returned by a function is not evaluated, but the
; data structure returned by a macro is. The process of determining the return value of a
; macro is called macro expansion, and you can use the function macroexpand to see what data
; structure a macro returns before that data structure is evaluated. Note that you have to
; quote the form that you pass to macroexpand:
(macroexpand '(ignore-last-operand (+ 1 2 10)))
; => (+ 1 2)


; ---- Syntactic Abstraction and the -> Macro
(defn read-resource
  "Read a resource into a string"
  [path]
  (read-string (slurp (clojure.java.io/resource path))))

; To understand the function body, you have to find the innermost form, in this case
; (clojure.java.io/resource path), and then work your way outward from right to left
; to see how the result of each function gets passed to another function. This
; right-to-left flow is opposite of what non-Lisp programmers are used to. As you
; get used to writing in Clojure, this kind of code gets easier and easier to understand.
; But if you want to translate Clojure code so you can read it in a more familiar,
; left-to-right, top-to-bottom manner, you can use the built-in -> macro, which is
; also known as the threading or stabby macro. It lets you rewrite the preceding
; function like this:
(defn read-resorce-lr
  [path]
  (-> path
      clojure.java.io/resource
      slurp
      read-string))


; built-in macros
; when, and, or, unless
(macroexpand '(when boolean-expression
                expression-1
                expression-2
                expression-x))

; ---
(defmacro infix [[operand-x operation operand-y]] (list operation operand-x operand-y))
(infix (1 + 1))

; Distinguishing Symbols and Values
; to return symbol itself we need to escape reserved word with '

; macro body tries to get the value that the symbol let refers to, whereas what you actually
; want to do is return the let symbol itself. There are other problems, too: you’re trying
; to get the value of result, which is unbound, and you’re trying to get the value of println
; instead of returning its symbol. Here’s how you would write the macro to do what you want:
(defmacro pring-and-retrun-value
  [expression]
  (list 'let ['result expression]
        (list 'println 'result)
        'result))
; Here, you’re quoting each symbol you want to use as a symbol by prefixing it with the single
; quote character, '. This tells Clojure to turn off evaluation for whatever follows, in this
; case preventing Clojure from trying to resolve the symbols and instead just returning the symbols.

; Simple Quoting
; quote at the beginning, it returns an unevaluated data structure:
(quote (+ 1 2)) ; (+ 1 2)
(quote (cons 1 [2 3])) ; (cons 1 [2 3])

; Syntax Quoting `
; Quoting does not include a namespace if your code doesn’t include a namespace:
; '+ => +
; Write out the namespace, and it’ll be returned by normal quote
; 'clojure.core/+ => clojure.core/+
; Syntax quoting will always include the symbol’s full namespace:
; `+ => clojure.core/+

; The other difference between quoting and syntax quoting is that the latter allows you
; to unquote forms using the tilde, ~. It’s kind of like kryptonite in that way: whenever
; Superman is around kryptonite, his powers disappear. Whenever a tilde appears within a
; syntax-quoted form, the syntax quote’s power to return unevaluated, fully namespaced forms
; disappears. Here’s an example:
`(+ 1 (inc 1))
; (clojure.core/+ 1 (clojure.core/inc 1))
`(+ 1 ~(inc 1))
; (clojure.core/+ 1 2)

; Syntax quoting and unquoting allow you to create lists more clearly and concisely.
; Compare using the list function, shown first, with using syntax quoting:
(list '+ 1 (inc 1))
`(+ 1 ~(inc 1))

(defmacro code-critic-verbose
  [bad good]
  (list 'do
        (list 'println "This is bad code:" (list 'quote bad))
        (list 'println "This is good code:" (list 'quote good))))

(defmacro code-critic
  [bad good]
  `(do
     (println "This is bad code:" (quote ~bad))
     (println "This is good code:" (quote ~good))))

(code-critic (1 + 1) (+ 1 1))

; Most of the time, your macros will return lists. You can build up the list to be returned
; by using list functions or by using syntax quoting. Syntax quoting usually leads to code
; that’s clearer and more concise because it lets you create a template of the data structure
; you want to return that’s easier to parse visually. Whether you use syntax quoting or plain
; quoting, it’s important to be clear about the distinction between a symbol and the value it
; evaluates to when building up your list. And if you want your macro to return multiple
; forms for Clojure to evaluate, make sure to wrap them in a do.


; Refactoring a Macro and Unquote Splicing

; That code-critic macro in the preceding section could still use some improvement.
; Look at the duplication! The two println calls are nearly identical. Let’s clean that up.
; First, let’s create a function to generate those println lists. Functions are easier to
; think about and play with than macros, so it’s often a good idea to move macro guts
; to helper functions:
(defn criticize-code
  [criticism code]
  `(println ~criticism (quote ~code)))

(defmacro code-critic-v2
  [bad good]
  `(do
     ~(criticize-code "This is bad code:" bad)
     ~(criticize-code "This is good code:" good)))

(defmacro code-critic-not-working
  [bad good]
  `(do ~(map #(apply criticize-code %)
             [["Great squid of Madrid, this is bad code:" bad]
              ["Sweet gorilla of Manila, this is good code:" good]])))

; This is looking a little better. You’re mapping over each criticism/code pair and applying
; the criticize-code function to the pair. Let’s try to run the code:
;
; (code-critic (1 + 1) (+ 1 1))
  ; => NullPointerException

; Oh no! That didn’t work at all! What happened? The problem is that map returns a list,
; and in this case, it returned a list of println expressions. We just want the result of
; each println call, but instead, this code sticks both results in a list and then tries to
; evaluate that list.
;
; In other words, as it’s evaluating this code, Clojure gets to something
; like this:

;(do
; ((clojure.core/println "criticism" '(1 + 1))
;  (clojure.core/println "criticism" '(+ 1 1))))

; then evaluates the first println call to give us this:

;(do
; (nil
;  (clojure.core/println "criticism" '(+ 1 1))))

; and after evaluating the second println call, does this:

;(do
; (nil nil))

; This is the cause of the exception. println evaluates to nil, so we end up with
; something like (nil nil). nil isn’t callable, and we get a NullPointerException

; What an inconvenience! But as it happens, unquote splicing was invented precisely to
; handle this kind of situation. Unquote splicing is performed with ~@. If you merely unquote
; a list, this is what you get:

`(+ ~(list 1 2 3)) ; => (clojure.core/+ (1 2 3))
`(+ ~@(list 1 2 3)) ; => (clojure.core/+ 1 2 3)

; Unquote splicing unwraps a seqable data structure, placing its contents directly within
; the enclosing syntax-quoted data structure. It’s like the ~@ is a sledgehammer and whatever
; follows it is a piñata, and the result is the most terrifying and awesome party you’ve
; ever been to.

(defmacro code-critic-v3
  [{:keys [good bad]}]
  `(do ~@(map #(apply criticize-code %)
              [["Sweet lion of Zion, this is bad code:" bad]
               ["Great cow of Moscow, this is good code:" good]])))


; Variable Capture
(def message "Good job!")
(defmacro with-mischief
  [& stuff-to-do]
  `(let [message "Oh, big deal!"] ~@stuff-to-do))
; leads to exceptions:
; Exception: Can't let qualified name: user/message

; This exception is for your own good: syntax quoting is designed to prevent you
; from accidentally capturing variables within macros. If you want to introduce let
; bindings in your macro, you can use a gensym. The gensym function produces unique
; symbols on each successive call:
(gensym) ; => G__1826

(defmacro without-mischief
  [& stuff-to-do]
  (let [macro-message (gensym 'message)]
    `(let [~macro-message "Oh, big deal!"]
       ~@stuff-to-do
       (println "I still need to say: " ~macro-message))))

(without-mischief
  (println "Here's how I feel about that thing you did: " message))


; This example avoids variable capture by using gensym to create a new, unique symbol that
; then gets bound to macro-message. Within the syntax-quoted let expression, macro-message
; is unquoted, resolving to the gensym’d symbol. This gensym’d symbol is distinct from any
; symbols within stuff-to-do, so you avoid variable capture. Because this is such a common
; pattern, you can use an auto-gensym. Auto-gensyms are more concise and convenient ways
; to use gensyms:
`(just-name# just-name#)
; (just-name__1834__auto__ just-name__1834__auto__)

`(let [name# "Larry Potter"] name#)
; => (clojure.core/let [name__2872__auto__ "Larry Potter"] name__2872__auto__)

; In this example, you create an auto-gensym by appending a hash mark (or hashtag, if you must insist)
; to a symbol within a syntax-quoted list. Clojure automatically ensures that each instance of x#
; resolves to the same symbol within the same syntax-quoted list, that each instance of y# resolves
; similarly, and so on.
;
; gensym and auto-gensym are both used all the time when writing macros, and they allow you to
; avoid variable capture.

; Double Evaluation
; Another gotcha to watch out for when writing macros is double evaluation, which occurs when a
; form passed to a macro as an argument gets evaluated more than once
(defmacro report
  [to-try]
  `(if ~to-try
     (println (quote ~to-try) "was successful:" ~to-try)
     (println (quote ~to-try) "was not successful:" ~to-try)))


(defmacro report-better
  [to-try]
  `(let [result# ~to-try]
     (if result#
       (println (quote ~to-try) "was successful:" result#)
       (println (quote ~to-try) "was not successful:" result#))))



; ----------------- Validations
; Validation Functions
; To keep things simple, we’ll just worry about validating the name and email for each order.
; For our store, I’m thinking we’ll want to have those order details represented like this:

;(def order-details
;  {:name "Mitchard Blimmons"
;   :email "mitchard.blimmonsgmail.com"})

; This particular map has an invalid email address (it’s missing the @ symbol), so this
; is exactly the kind of order that our validation code should catch! Ideally, we want
; to write code that produces something like this:
;
;(validate order-details order-details-validations)
; => {:email ["Your email address doesn't look like an email address."]}

; That is, we want to be able to call a function, validate, with the data that needs
; validation and a definition for how to validate it. The result should be a map where
; each key corresponds to an invalid field, and each value is a vector of one or more
; validation messages for that field. The following two functions do the job.

(def order-details-validations
  {:name
   ["Please enter a name" not-empty]

   :email
   ["Please enter an email address" not-empty
    "Your email address doesn't look like an email address" #(or (empty? %) (re-seq #"@" %))]})

(defn error-messages-for
  "Return a seq of error messages"
  [to-validate message-validator-pairs]
  (map first (filter #(not ((second %) to-validate))
                     (partition 2 message-validator-pairs))))

(defn validate
  "Returns a map with a vector of erros for each key"
  [to-validate validations]
  (reduce (fn [errors validation]
            (let [[field-name validation-check-groups] validation
                  value (get to-validate field-name)
                  error-messages (error-messages-for value validation-check-groups)]
              (if (empty? error-messages)
                errors
                (assoc errors field-name error-messages))))
          {}
          validations))

(def order-details
  {:name "Mitchard Blimmons"
   :email "mitchard.blimmonsgmail.com"})

(validate order-details order-details-validations)

; if-valid
; With our validation code in place, we can now validate records to our hearts’ content!
; Most often, validation will look something like this:

;(let [errors (validate order-details order-details-validations)]
;  (if (empty? errors)
;    (println :success)
;    (println :failure errors)))

; The pattern is to do the following:

; Validate a record and bind the result to errors
; Check whether there were any errors
; If there were, do the success thing, here (println :success)
; Otherwise, do the failure thing, here (println :failure errors)

; Simple example

;(defn if-valid
;  [record validations success-code failure-code]
;  (let [errors (validate record validations)]
;    (if (empty? errors)
;      success-code
;      failure-code)))

; However, this wouldn’t work, because success-code and failure-code would get evaluated each time.

(defmacro if-valid
  "Handle validation more concisely"
  [to-validate validations errors-name & then-else]
  `(let [~errors-name (validate ~to-validate ~validations)]
     (if (empty? ~errors-name)
       ~@then-else)))

(macroexpand
  '(if-valid order-details order-details-validations my-error-name
             (println :success)
             (println :failure my-error-name)))

;(let*
; [my-error-name (user/validate order-details order-details-validations)]
;  (if (clojure.core/empty? my-error-name)
;   (println :success)
;   (println :failure my-error-name))


;(if-valid order-details order-details-validations errors
;          (render :success)
;          (render :failure errors))

(defn if-valid-no-macro
  [record validations success-code failure-code]
  (let [errors (validate record validations)]
    (if (empty? errors)
      success-code
      failure-code)))

; eagerly evaluated
(if-valid-no-macro order-details order-details-validations
                   (println "Success")
                   (println "Failure"))

(if-valid order-details order-details-validations my-error-name
          (println :success)
          (println :failure my-error-name))





