(ns clojure.core.concurrent.examples)

; --- vars
(def ^:dynamic foo "root")

(defn print-foo
  ([] (println foo))
  ([prefix] (println (str prefix foo))))

(print-foo) ;root

(binding [foo "foo"]
  (print-foo)) ; foo

(print-foo) ; root

(defn with-new-thread [f]
  (doto
    (new Thread f)
    (.start)))

(do
  (binding [foo "threaded-foo"]
    (with-new-thread (fn [] (print-foo "inside new thread: ")))
    (print-foo "main thread, inside binding: "))
  (print-foo "main thread, outside binding: "))

;inside new thread: root !!

;main thread, inside binding: threaded-foo
;main thread, outside binding: root


; --- atom
;autonomic: (atom {:key "value"})
(def atom-holder (atom {:key "value"}))

;get value
(deref atom-holder)
@atom-holder

;update
(swap! atom-holder
       ;produce new value
       (fn func [old-state] 3))

(swap! atom-holder inc) ; 4

; inc 100 times atom value in parallel
(pmap
  (fn [_] (swap! atom-holder inc))
  (range 100))
;(5 7 6 10 8 9 16 ...
;@tom-holder -> 104, no lost updates

; --- agents
;- Manage an independent value
;- Changes through ordinary function executed asynchronously
;- Not actors: not distributed
;- Use send or send-off to dispatch

(def my-agent (agent {:name "craig-heg" :favorites []}))
(deref my-agent)
;@my-agent

(defn slow-append-favorite [val new-favorite]
  (Thread/sleep 2000) ; simulate delay
  ; add new favorite to the map
  (assoc val :favorites (conj (:favorites val) new-favorite)))

; send takes an agent and a function
; returns immediately
; dispatches only one value at a time (queue)
(do
  (send my-agent slow-append-favorite "food")
  (send my-agent slow-append-favorite "music")
  (println "Print agent value immediately after send")
  (println @my-agent) ; it hasn't changed
  (println "Sleeping for 2.5 seconds")
  (Thread/sleep 2500)
  (println @my-agent) ; only the first value has fired
  (println "Sleeping for another 2.5 seconds")
  (Thread/sleep 2500)
  (println @my-agent))

; exceptions will bubble up
; they are causes agent to enter error state
; interactions with agents in this state cause exception
; errors can be examined with agent-errors
; errors can be cleared with clear-agent-errors
(def error-agent (agent 3))
(defn modify-agent-with-error
  [current new]
  (if (= 42 new)
    (throw (Exception. "Not 42!"))
    new))

(send error-agent modify-agent-with-error 42)
; (error-agent) accessing to an agent with error state causes an exception
(agent-errors error-agent)
(clear-agent-errors error-agent) ; 3


; --- Refs
;- Allow for synchronous changes to shared state
;- Refs can only be changed within a transaction
;- Software Transactional Memory (STM) system
;- Retries are automatic
;- Therefore, there should be no side effects
;- They do compose with agents, allowing for deferred side effects
(def ref-holder (ref {:name "Crag"
                      :last "Hag"
                      :battles 2}))

;update
(assoc @ref-holder :game "HoMM 3")

; will take care of calling assoc function with old value of the ref-holder
; (commute ref-holder assoc :game "HoMM3")

;alter applies changes IMMEDIATELY within transaction
;(alter ref-holder assoc :game "HoMM3")

; must be inside transaction
(dosync ; protected by single STM transaction
  (commute ref-holder assoc :game "HoMM3"))

(defmacro with-new-thread-macro [& body]
  `(.start (Thread. (fn [] ~@body))))
;(macroexpand-1 '(with-new-thread-macro (print-foo)))

; concurrent writes
(def r (ref 0))

(with-new-thread-macro
  (dosync
    (println "tx1 initial: " @r)
    (alter r inc)
    (println "tx1 final: " @r)
    (Thread/sleep 5000)
    (println "tx1 done")))


(with-new-thread-macro
  (dosync
    (println "tx2 initial: " @r)
    (Thread/sleep 1000)
    (alter r inc)
    (println "tx2 final: " @r)
    (println "tx2 done")))

; tx1 initial:  0
; tx1 final:  1
; -- tx2 is getting restarted several times
; tx2 initial:  0
; tx2 initial:  0
; tx2 initial:  0
; tx2 initial:  0
; tx2 initial:  0
; tx1 done
; tx2 initial:  1
; tx2 final:  2
; tx2 done

; - reading without a transaction
(with-new-thread-macro
  (dotimes [_ 10] (Thread/sleep 1000)
                  (dosync (alter r inc))
                  (println "updated ref to " @r)))

(with-new-thread-macro
  (dotimes [_ 7]
    (println "ref is "@r)
    (Thread/sleep 1000)))

; ref is  2
; updated ref to  3
; ref is  3
; updated ref to  4
; ref is  4
; ref is  4
; updated ref to  5
; ref is  5
; updated ref to  6
; ref is  6
; updated ref to  7
; ref is  7
; updated ref to  8
; updated ref to  9
; updated ref to  10
; updated ref to  11

; - reading with a transaction
(dosync (ref-set r 0))

(with-new-thread-macro
  (dotimes [_ 10] (Thread/sleep 1000)
                  (dosync (alter r inc))
                  (println "updated ref to " @r)))

(with-new-thread-macro
  (println "r outside transaction is " @r)
  (dosync
    (dotimes [i 7]
      (println "iteration" i)
      (println "r is " @r)
      (Thread/sleep 1000)))
  (println "r outside transaction is " @r))

;r outside transaction is  0
;iteration 0
;r is  0
;updated ref to  1
;iteration 1
;r is  0
;updated ref to  2
;iteration 2
;r is  0
;updated ref to  3
;iteration 3
;r is  0
;updated ref to  4
;iteration 4
;r is  0
;iteration 5
;r is  0
;updated ref to  5
;iteration 6
;r is  0
;updated ref to  6
;r outside transaction is  6
;updated ref to  7
;updated ref to  8
;updated ref to  9
;updated ref to  10