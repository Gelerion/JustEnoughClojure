(ns clojure.core.concurrent.metaphysics)

; The term value is used often by Clojurists, and its specific meaning might differ from what you’re
; used to. Values are atomic in the sense that they form a single irreducible unit or component in a
; larger system; they’re indivisible, unchanging, stable entities. Numbers are values: it wouldn’t
; make sense for the number 15 to mutate into another number. When you add or subtract from 15, you
; don’t change the number 15; you just wind up with a different number. Clojure’s data structures
; are also values because they’re immutable. When you use assoc on a map, you don’t modify the
; original map; instead, you derive a new map.
;
; So a value doesn’t change, but you can apply a process to a value to produce a new value.
; For example, say we start with a value F1, and then we apply the Cuddle Zombie process to
; F1 to produce the value F2. The process then gets applied to the value F2 to produce the value
; F3, and so on.
;
; This leads to a different conception of identity. Instead of understanding identity as inherent
; to a changing object, as in OO metaphysics, Clojure metaphysics construes identity as something
; we humans impose on a succession of unchanging values produced by a process over time. We use
; names to designate identities. The name Fred is a handy way to refer to a series of individual
; states F1, F2, F3, and so on. From this viewpoint, there’s no such thing as mutable state.
; Instead, state means the value of an identity at a point in time.
;
; Rich Hickey has used the analogy of phone numbers to explain state. Alan’s phone number has
; changed 10 times, but we will always call these numbers by the same name, Alan’s phone number.
; Alan’s phone number five years ago is a different value than Alan’s phone number today, and
; both are two states of Alan’s phone number identity.
;
; This makes sense when you consider that in your programs you are dealing with information about
; the world. Rather than saying that information has changed, you would say you’ve received
; new information. At 12:00 pm on Friday, Fred the Cuddle Zombie was in a state of 50 percent decay.
; At 1:00 pm, he was 60 percent decayed. These are both facts that you can process, and the
; introduction of a new fact does not invalidate a previous fact. Even though Fred’s decay increased
; from 50 percent to 60 percent, it’s still true that at 12:00 pm he was in a state of 50 percent decay.

; --- Atoms

; To handle this sort of change, Clojure uses reference types. Reference types let you manage identities
; in Clojure. Using them, you can name an identity and retrieve its state. Let’s look at the simplest of
; these, the atom.

(def fred (atom {:cuddle-hunger-level 0 :percent-deteriorated 0}))

; This creates a new atom and binds it to the name fred. This atom refers to the value
; {:cuddle-hunger-level 0 :percent-deteriorated 0}, and you would say that that’s its current state.

; To get an atom’s current state, you dereference it. Here’s Fred’s current state:

@fred

; read state
(let [zombie-state @fred]
  (if (>= (:percent-deteriorated zombie-state) 50)
    (future (println (:cuddle-hunger-level zombie-state)))))

; To update the atom so that it refers to a new state, you use swap!
(swap! fred
       (fn [current-sate]
         (merge-with + current-sate {:cuddle-hunger-level 1
                                     :percent-deteriorated 1})))

; Note that this code doesn’t actually update fred, because we’re not using swap!
; We’re just making a normal function call to increase-cuddle-hunger-level, which returns a result.
(defn increase-cuddle-hunger-level
  [zombie-state increase-by]
  (merge-with + zombie-state {:cuddle-hunger-level increase-by}))

(increase-cuddle-hunger-level @fred 10)
(swap! fred increase-cuddle-hunger-level 10)
; or
(swap! fred update-in [:cuddle-hunger-level] + 10)

; By using atoms, you can retain past state. You can dereference an atom to retrieve State 1,
; and then update the atom, creating State 2, and still make use of State 1:
(let [num (atom 1)
      s1 @num]
  (swap! num inc)
  (println "State 1:" s1)
  (println "Current state:" @num))

; Sometimes you’ll want to update an atom without checking its current value. For example,
; you might develop a serum that sets a cuddle zombie’s hunger level and deterioration back
; to zero. For those cases, you can use the reset! function:

(reset! fred {:cuddle-hunger-level 0
              :percent-deteriorated 0})


; ---- Watches
; A watch is a function that takes four arguments: a key, the reference being watched,
; its previous state, and its new state. You can register any number of watches with
; a reference type.

; Let’s say that a zombie’s shuffle speed (measured in shuffles per hour, or SPH) is dependent on
; its hunger level and deterioration. Here’s how you’d calculate it, multiplying the cuddle hunger
; level by how whole it is:
(defn shuffle-speed
  [zombie]
  (* (:cuddle-hunger-level zombie) (- 100 (:percent-deteriorated zombie))))

; Let’s also say that you want to be alerted whenever a zombie’s shuffle speed reaches the
; dangerous level of 5,000 SPH. Otherwise, you want to be told that everything’s okay.

(defn shuffle-alert
  [key watched old-state new-state]
  (let [sph (shuffle-speed new-state)]
    (if (> sph 5000)
      (do
        (println "The zombie's SPH is now " sph)
        (println "This message brought to you by " key))
      (do
        (println "All's well with " key)
        (println "Cuddle hunger: " (:cuddle-hunger-level new-state))
        (println "Percent deteriorated: " (:percent-deteriorated new-state))
        (println "SPH: " sph)))))

(reset! fred {:cuddle-hunger-level 22 :percent-deteriorated 2})
(add-watch fred :fred-shuffle-alert shuffle-alert)

(swap! fred update-in [:percent-deteriorated] + 1)
; => All's well with  :fred-shuffle-alert
; => Cuddle hunger:  22
; => Percent deteriorated:  3
; => SPH:  2134

(swap! fred update-in [:cuddle-hunger-level] + 30)
; => Run, you fool!
; => The zombie's SPH is now 5044
; => This message brought to your courtesy of :fred-shuffle-alert



; ---- Validators
; Validators let you specify what states are allowable for a reference. For example, here’s a
; validator that you could use to ensure that a zombie’s :percent-deteriorated is between 0 and 100:
(defn percent-deteriorated-validator
  [{:keys [percent-deteriorated]}]
  (and (>= percent-deteriorated 0)
       (<= percent-deteriorated 100)))

;You can attach a validator during atom creation:
(def bobby
  (atom {:cuddle-hunger-level 0 :percent-deteriorated 0 :validator percent-deteriorated-validator}))

(swap! bobby update-in [:percent-deteriorated] + 200)
; This throws "Invalid reference state"

; ---- Ref and Alter
; There are a couple of details to note here: when you alter a ref, the change isn’t
; immediately visible outside of the current transaction. This is what lets you call
; alter on the dryer twice within a transaction without worry­ing about whether dryer
; will be read in an inconsistent state. Similarly, if you alter a ref and then deref
; it within the same transaction, the deref will return the new state.

; Here’s an example to demonstrate this idea of in-transaction state:
(def counter (ref 0))
(future
  (dosync
    (alter counter inc)
    (println @counter) ; 1
    (Thread/sleep 500)
    (alter counter inc)
    (println @counter))) ; 2

(Thread/sleep 250)
(println @counter)

; This prints 1, 0 , and 2, in that order. First, you create a ref, counter, which holds the
; number 0. Then you use future to create a new thread to run a transaction on. On the transaction
; thread, you increment the counter and print it, and the number 1 gets printed. Meanwhile,
; the main thread waits 250 milliseconds and prints the counter’s value, too. However, the
; value of counter on the main thread is still 0—the main thread is outside of the transaction
; and doesn’t have access to the transaction’s state. It’s like the transaction has its own
; private area for trying out changes to the state, and the rest of the world can’t know about
; them until the transaction is done. This is further illustrated in the transaction code: after
; it prints the first time, it increments the counter again from 1 to 2 and prints the result, 2.


; ---- commute
; commute allows you to update a ref’s state within a transaction, just like alter.
; However, its behavior at commit time is completely different. Here’s how alter behaves:
;  1. Reach outside the transaction and read the ref’s current state.
;  2. Compare the current state to the state the ref started with within the transaction.
;  3. If the two differ, make the transaction retry.
;  4. Otherwise, commit the altered ref state.

;commute, on the other hand, behaves like this at commit time:

;  1. Reach outside the transaction and read the ref’s current state.
;  2. Run the commute function again using the current state.
;  3. Commit the result.

; As you can see, commute doesn’t ever force a transaction retry. This can help improve
; performance, but it’s important that you only use commute when you’re sure that it’s not
; possible for your refs to end up in an invalid state. Let’s look at examples of safe and
; unsafe uses of commute.

(defn sleep-print-update
  [sleep-time thread-name update-fn]
  (fn [state]
    (Thread/sleep sleep-time)
    (println (str thread-name ": " state))
    (update-fn state)))

(def counter (ref 0))
(future (dosync (commute counter (sleep-print-update 100 "Thread A" inc))))
(future (dosync (commute counter (sleep-print-update 150 "Thread B" inc))))

; Here’s a timeline of what prints:
;
; Thread A: 0 | 100ms
; Thread B: 0 | 150ms
; Thread A: 0 | 200ms
; Thread B: 1 | 300ms

; If you dereference counter after the transactions run, you’ll see that the value is 2


; ---- Vars
; To recap briefly, vars are associations between symbols and objects.
; You create new vars with def

; Although vars aren’t used to manage state in the same way as atoms and refs, they do have a
; couple of concurrency tricks: you can dynamically bind them, and you can alter their roots.
; Let’s look at dynamic binding first.

; - Dynamic Binding
; Dynamic vars can be useful for creating a global name that should refer to different values
; in different contexts.
(def ^:dynamic *notification-address* "dobby@elf.org")
; Notice two important details here. First, you use ^:dynamic to signal to Clojure that a var
; is dynamic. Second, the var’s name is enclosed by asterisks. Lispers call these earmuffs,
; which is adorable. Clojure requires you to enclose the names of dynamic vars in earmuffs.
; This helps signal the var’s dynamicaltude to other programmers.

; Unlike regular vars, you can temporarily change the value of dynamic vars by using binding:
(binding [*notification-address*] "test@elf.org" *notification-address*)

(defn notify
  [message]
  (str "TO: " *notification-address* "\n"
       "MESSAGE: " message))
(notify "I fell.") ; dobby@elf.org

(binding [*notification-address* "test@elf.org"] (notify "test!"))
; => "TO: test@elf.org\nMESSAGE: test!"

; Dynamic vars are most often used to name a resource that one or more functions target.
; In this example, you can view the email address as a resource that you write to. In fact,
; Clojure comes with a ton of built-in dynamic vars for this purpose. *out*, for example,
; represents the standard output for print operations. In your program, you could
; re-bind *out* so that print statements write to a file, like so:

;(binding [*out* (clojure.java.io/writer "print-output")]
;  (println "A man who carries a cat by the tail learns something he can learn in no other way. -- Mark Twain"))
;(slurp "print-output")


; Finally, it’s possible to set! dynamic vars that have been bound. Whereas the examples you’ve
; seen so far allow you to convey information in to a function without having to pass in the
; information as an argument, set! allows you convey information out of a function without
; having to return it as an argument.

(def ^:dynamic *troll-thought* nil)
(defn troll-riddle
  [your-answer]
  (let [number "man meat"]
    (when (thread-bound? #'*troll-thought*)
      (set! *troll-thought* number))
    (if (= number your-answer)
      "TROLL: You can cross the bridge!"
      "TROLL: Time to eat you, succulent human!")))

; You use the thread-bound? function at to check that the var has been bound, and if it has,
; you set! *troll-thought* to the troll’s thought

(binding [*troll-thought* nil]
  (println (troll-riddle 2))
  (println "SUCCULENT HUMAN: Oooooh! The answer was" *troll-thought*))

; Per-Thread Binding
; One final point to note about binding: if you access a dynamically bound var from within a
; manually created thread, the var will evaluate to the original value.

;This code prints output to the REPL:

(.write *out* "prints to repl")
; => prints to repl

;The following code doesn’t print output to the REPL, because *out* is not bound to the REPL printer:
(.start (Thread. #(.write *out* "prints to standard out")))

;You can work around this by using this goofy code:

(let [out *out*]
  (.start
    (Thread. #(binding [*out* out]
                (.write *out* "prints to repl from thread")))))

;Or you can use bound-fn, which carries all the current bindings to the new thread:
(.start (Thread. (bound-fn [] (.write *out* "prints to repl from thread"))))

; Altering the Var Root
(def power-source "hair") ; root
(alter-var-root #'power-source (fn [_] "7-eleven parking lot"))
; Just like when using swap! to update an atom or alter! to update a ref, you use alter-var-root
; along with a function to update the state of a var.

; You can also temporarily alter a var’s root with with-redefs. This works similarly to binding
; except the alteration will appear in child threads. Here’s an example:
(with-redefs [*out* *out*]
  (doto (Thread. #(println "with redefs allows me to show up in the REPL"))
    .start
    .join))
















