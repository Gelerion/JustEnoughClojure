(ns clojure.core.concurrent.concurrent-primitives)

; Futures
(future (Thread/sleep 4000) (println "I'll print after 4 seconds"))
; (println "I'll print immediately")

; The future function returns a reference value that you can use to request the result.
; Requesting a future’s result is called dereferencing the future, and you do it with
; either the deref function or the @ reader macro. A future’s result value is the value
; of the last expression evaluated in its body. A future’s body executes only once,
; and its value gets cached.

(let [result (future (println "this prints once") (+ 1 1))]
  (println "deref: " (deref result))
  (println "@: " @result))
;this prints once
;deref:  2
;@:  2
; Notice that the string "this prints once" indeed prints only once, even though you dereference
; the future twice. This shows that the future’s body ran only once and the result, 2, got cached.

; Dereferencing a future will block if the future hasn’t finished running, like so:
(let [result (future (Thread/sleep 3000) (+ 1 1))]
  (println "The result is: " @result)
  (println "It will be at least 3 seconds before I print"))

; Sometimes you want to place a time limit on how long to wait for a future.
(deref (future (Thread/sleep 1000) 0) 10 5)
; This code tells deref to return the value 5 if the future doesn’t return a value within 10 milliseconds

; Finally, you can interrogate a future using realized? to see if it’s done running:
(realized? (future (Thread/sleep 1000)))


; ------ Delays
; Delays allow you to define a task without having to execute it or require the result immediately. Y
; You can create a delay using delay
(def jackson-5-delay
  (delay (let [message "Just call my name and I'll be there"]
           (println "First deref: " message)
           message)))
; In this example, nothing is printed, because we haven’t yet asked the let form to be evaluated.
; You can evaluate the delay and get its result by dereferencing it or by using force. force behaves
; identically to deref in that it communicates more clearly that you’re causing a task to start
; as opposed to waiting for a task to finish:
(force jackson-5-delay)
; => First deref: Just call my name and I'll be there
; => "Just call my name and I'll be there"

; Like futures, a delay is run only once and its result is cached. Subsequent dereferencing will
; return the Jackson 5 message without printing anything:
@jackson-5-delay

; One way you can use a delay is to fire off a statement the first time one future out of a group of
; related futures finishes. For example, pretend your app uploads a set of headshots to a
; headshot-sharing site and notifies the owner as soon as the first one is up, as in the following:
(def gimli-headshots ["serious.jpg" "fun.jpg" "playful.jpg"])
(defn email-user
  [email-address]
  (println "Sending headshot notification to" email-address))
(defn upload-document
  "Needs to be implemented"
  [headshot]
  true)

(let [notify (delay (email-user "user@mail.com"))]
  (doseq [headshot gimli-headshots]
    (future (upload-document headshot) (force notify))))
; Even though (force notify) will be evaluated three times, the delay body is evaluated only once.
; Gimli will be grateful to know when the first headshot is available so he can begin tweaking it
; and sharing it.


; ------ Promise
; Promises allow you to express that you expect a result without having to define the task that
; should produce it or when that task should run. You create promises using promise and deliver
; a result to them using deliver. You obtain the result by dereferencing:
(def my-promise (promise))
(deliver my-promise (+ 1 2))
@my-promise
; => 3

(def yak-butter-international
  {:store "Yak Butter International"
   :price 90
   :smoothness 90})
(def butter-than-nothing
  {:store "Butter Than Nothing"
   :price 150
   :smoothness 83})
;; This is the butter that meets our requirements
(def baby-got-yak
  {:store "Baby Got Yak"
   :price 94
   :smoothness 99})

(defn mock-api-call
  [result]
  (Thread/sleep 1000)
  result)

(defn satisfactory?
  "If the butter meets our criteria, return the butter, else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))

(time (some (comp satisfactory? mock-api-call)
            [yak-butter-international butter-than-nothing baby-got-yak]))
; "Elapsed time: 3002.132 msecs"

; with promises
(time
  (let [butter-promise (promise)]
    (doseq [butter [yak-butter-international butter-than-nothing baby-got-yak]]
      (future (if-let [satisfactory-butter (satisfactory? (mock-api-call butter))]
                (deliver butter-promise satisfactory-butter))))
    (println "And the winner is: " @butter-promise)))

; You might be wondering what happens if none of the yak butter is satisfactory.
; If that happens, the dereference would block forever and tie up the thread.
; To avoid that, you can include a timeout:
(let [p (promise)]
  (deref p 100 "timed out"))

(let [ferengi-wisdom-promise (promise)]
  (future (println "Here's some Ferengi wisdom:" @ferengi-wisdom-promise))
  (Thread/sleep 100)
  (deliver ferengi-wisdom-promise "Whisper your way to success."))


; custom impl
(defmacro wait
  "Sleep `timeout seconds before evaluating body"
  [timeout & body]
  `(do (Thread/sleep ~timeout) ~@body))

(let [saying3 (promise)]
  (future (deliver saying3 (wait 100 "Cheerio!")))
  @(let [saying2 (promise)]
     (future (deliver saying2 (wait 400 "Pip pip!")))
     @(let [saying1 (promise)]
        (future (deliver saying1 (wait 200 "'Ello, gov'na!")))
        (println @saying1)
        saying1)
     (println @saying2)
     saying2)
  (println @saying3)
  saying3)

(defmacro enqueue
  ([q concurrent-promise-name concurrent serialized]
   `(let [~concurrent-promise-name (promise)]
      (future (deliver ~concurrent-promise-name ~concurrent))
      (deref ~q)
      ~serialized
      ~concurrent-promise-name))
  ([concurrent-promise-name concurrent serialized]
   `(enqueue (future) ~concurrent-promise-name ~concurrent ~serialized)))

(time @(-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
           (enqueue saying (wait 400 "Pip pip!") (println @saying))
           (enqueue saying (wait 100 "Cheerio!") (println @saying))))



; ---- Stateless Concurrency
; Often, though, you’ll want to concurrent-ify tasks that are completely independent of each other.
; There is no shared access to a mutable state; therefore, there are no risks to running the tasks
; concurrently and you don’t have to bother with using any of the tools I’ve just been blabbing
; on about.
; As it turns out, Clojure makes it easy for you to write code for achieving stateless concurrency.
; pmap, which gives you concurrency performance benefits virtually for free.

; map is a perfect candidate for parallelization: when you use it, all you’re doing is deriving a
; new collection from an existing collection by applying a function to each element of the existing
; collection. There’s no need to maintain state; each function application is completely independent.
; Clojure makes it easy to perform a parallel map with pmap. With pmap, Clojure handles the
; running of each application of the mapping function on a separate thread.

(def alphabet-length 26)

; Vector of chars, A-Z
(def letters (mapv (comp str char (partial + 65)) (range alphabet-length)))

(defn random-string
  "Returns a random string of specified length"
  [length]
  (apply str (take length (repeatedly #(rand-nth letters)))))

(defn random-string-list
  [list-length string-length]
  (doall (take list-length (repeatedly (partial random-string string-length)))))

(def orc-names (random-string-list 3000 7000))

(time (dorun (map clojure.string/lower-case orc-names)))
; => "Elapsed time: 270.182 msecs"

(time (dorun (pmap clojure.string/lower-case orc-names)))
; => "Elapsed time: 147.562 msecs"

; There’s always some overhead involved with creating and coordinating threads. Sometimes,
; in fact, the time taken by this overhead can dwarf the time of each function application,
; and pmap can actually take longer than map
; The solution to this problem is to increase the grain size, or the amount of work done by
; each parallelized task. In this case, the task is to apply the mapping function to one
; element of the collection. Grain size isn’t measured in any standard unit, but you’d say
; that the grain size of pmap is one by default. Increasing the grain size to two would mean
; that you’re applying the mapping function to two elements instead of one, so the thread that
; the task is on is doing more work

(def numbers [1 2 3 4 5 6 7 8 9 10])
(partition-all 3 numbers)
; => ((1 2 3) (4 5 6) (7 8 9) (10))

; Now suppose you started out with code that looked like this:
(pmap inc numbers)
; In this case, the grain size is one because each thread applies inc to an element.

(pmap (fn [number-group] (doall (map inc number-group)))
      (partition-all 3 numbers))
; ((2 3 4) (5 6 7) (8 9 10) (11))

; Now we need to ungroup the result
(apply concat
       (pmap (fn [number-group] (doall (map inc number-group)))
             (partition-all 3 numbers)))

(def orc-name-abbrevs (random-string-list 20000 300))
(time
  (dorun
    (apply concat
           (pmap (fn [name] (doall (map clojure.string/lower-case name)))
                 (partition-all 1000 orc-name-abbrevs)))))


(defn ppmap
  "Partitioned pmap, for grouping map ops together to make parallel
  overhead worthwhile"
  [grain-size f & colls]
  (apply concat
         (apply pmap
                (fn [& pgroups] (doall (apply map f pgroups)))
                (map (partial partition-all grain-size) colls))))



