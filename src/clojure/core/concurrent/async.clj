(ns clojure.core.concurrent.async
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]))

; We used the chan function to create a channel named echo-chan. Channels communicate messages.
; You can put messages on a channel and take messages off a channel. Processes wait for the
; completion of put and take—these are the events that processes respond to. You can think of
; processes as having two rules: 1) when trying to put a message on a channel or take a message
; off of it, wait and do nothing until the put or take succeeds, and 2) when the put or take
; succeeds, continue executing.
(def echo-chan (chan))
; Everything within the go expression—called a go block—runs concurrently on a separate thread.
; Go blocks run your processes on a thread pool that contains a number of threads equal to two
; plus the number of cores on your machine, which means your program doesn’t have to create a
; new thread for each process. This often results in better performance because you avoid the
; overhead associated with creating threads.

; In this case, the process (println (<! echo-chan)) expresses “when I take a message from echo-chan,
; print it.” The process is shunted to another thread, freeing up the current thread and allowing
; you to continue interacting with the REPL.

; In the expression (<! echo-chan), <! is the take function. It listens to the channel you give
; it as an argument, and the process it belongs to waits until another process puts a message on
; the channel. When <! retrieves a value, the value is returned and the println expression is executed.
(go (println (<! echo-chan)))

; The expression (>!! echo-chan "ketchup") puts the string "ketchup" on echo-chan and returns true.
; When you put a message on a channel, the process blocks until another process takes the message.
; In this case, the REPL process didn’t have to wait at all, because there was already a process
; listening to the channel, waiting to take something off it. However, if you do the following,
; your REPL will block indefinitely:
;   (>!! (chan) "mustard")
; You’ve created a new channel and put something on it, but there’s no process listening to that channel.
; Processes don’t just wait to receive messages; they also wait for the messages they put on a
; channel to be taken.
(>!! echo-chan :ketchup)

; -- Buffering
; It’s worth noting that the previous exercise contained two processes: the one you created with go and
; the REPL process. These processes don’t have explicit knowledge of each other, and they act independently.

; buffer size
(def sized-buffer 2)
;(>!! sized-buffer "a") ;true
;(>!! sized-buffer "a")  ;true
;(>!! sized-buffer "a") ; BLOCKS

; Buffer types:
; - sliding-buffer -> drops values in a first-in, first-out fashion
; - dropping-buffer -> discards values in a last-in, first-out fashion
; Neither of these buffers will ever cause >!! to block

; Blocking and Parking
; You may have noticed that the take function <! used only one exclamation point, whereas the put function >!!
; used two. In fact, both put and take have one-exclamation-point and two-exclamation-point varieties.
; When do you use which? The simple answer is that you can use one exclamation point inside go blocks,
; but you have to use two exclamation points outside of them:
; put  - Inside go block [ >! or >!! ] Outside go block [ >!! ]
; take - Inside go block [ <! or <!! ] Outside go block [ <!! ]

; There are two varieties of waiting: parking and blocking. Blocking is the kind of waiting you’re
; familiar with: a thread stops execution until a task is complete. Usually this happens when you’re
; doing some kind of I/O operation

; Parking frees up the thread so it can keep doing work. Let’s say you have one thread and two processes,
; Process A and Process B. Process A is running on the thread and then waits for a put or take. Clojure
; moves Process A off the thread and moves Process B onto the thread. If Process B starts waiting and
; Process A’s put or take has finished, then Clojure will move Process B off the thread and put
; Process A back on it. Parking allows the instructions from multiple processes to interleave on a
; single thread, similar to the way that using multiple threads allows interleaving on a single core.
; The implementation of parking isn’t important; suffice it to say that it’s only possible within go blocks,
; and it’s only possible when you use >! and <!, or parking put and parking take. >!! and <!! are blocking put
; and blocking take.

; thread
; There are definitely times when you’ll want to use blocking instead of parking, like when your process
; will take a long time before putting or taking, and for those occasions you should use thread:
(thread (println (<!! echo-chan)))
(>!! echo-chan "mustard")
; thread acts almost exactly like future: it creates a new thread and executes a process on that thread.
; Unlike future, instead of returning an object that you can dereference, thread returns a channel.
; When thread’s process stops, the process’s return value is put on the channel that thread returns:
(let [t (thread "chili")]
  (<!! t))
; => "chili"
; In this case, the process doesn’t wait for any events; instead, it stops immediately. Its return
; value is "chili", which gets put on the channel that’s bound to t. We take from t, returning "chili".

; The reason you should use thread instead of a go block when you’re performing a long-running task is
; so you don’t clog your thread pool. Imagine you’re running four processes that download humongous files,
; save them, and then put the file paths on a channel. While the processes are downloading files and saving
; these files, Clojure can’t park their threads. It can park the thread only at the last step, when the
; process puts the files’ paths on a channel. Therefore, if your thread pool has only four threads, all
; four threads will be used for downloading, and no other process will be allowed to run until one of
; the downloads finishes.


(defn hot-dog-machine
  []
  (let [in (chan)
        out (chan)]
    (go (<! in)
        (>! out "hot dog"))
    [in out]))

(let [[in out] (hot-dog-machine)]
  (>!! in "pocket lint")
  (<!! out))
; => "hot dog"


(defn hot-dog-machine-v2
  [hot-dog-count]
  (let [in (chan)
        out (chan)]
    (go (loop [hc hot-dog-count]
          (if (> hc 0)
            (let [input (<! in)]
              (if (= 3 input)
                (do (>! out "hot dog")
                    (recur (dec hc)))
                (do (>! out "wilted lettuce")
                    (recur hc))))
            (do (close! in)
                (close! out)))))
    [in out]))


(let [[in out] (hot-dog-machine-v2 2)]
  (>!! in "pocket lint")
  (println (<!! out))

  (>!! in 3)
  (println (<!! out))

  (>!! in 3)
  (println (<!! out))

  (>!! in 3)
  (<!! out))
; => wilted lettuce
; => hotdog
; => hotdog
; => nil


; pipelines
(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (go (>! c2 (clojure.string/upper-case (<! c1))))
  (go (>! c3 (clojure.string/reverse (<! c2))))
  (go (println (<! c3)))
  (>!! c1 "redrum"))
; => MURDER



; alts!!
; The core.async function alts!! lets you use the result of the first successful channel operation among
; a collection of operations
(defn upload
  [headshot c]
  (go (Thread/sleep (rand 100))
      (>! c headshot)))

(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (upload "serious.jpg" c1)
  (upload "fun.jpg" c2)
  (upload "sassy.jpg" c3)
  (let [[headshot channel] (alts!! [c1 c2 c3])]
    (println "Sending headshot notification for" headshot)))


; One cool aspect of alts!! is that you can give it a timeout channel, which waits the specified number of
; milliseconds and then closes.
(let [c1 (chan)]
  (upload "serious.jpg" c1)
  (let [[headshot channel] (alts!! [c1 (timeout 20)])]
    (if headshot
      (println "Sending headshot notification for" headshot)
      (println "Timed out!"))))

(let [c1 (chan)
      c2 (chan)]
  (go (<! c2)
    (let [[value channel] (alts!! [c1 [c2 "put!"]])]
      (println value)
      (= channel c2))))
; => true
; => true

; Queues
(defn append-to-file
  "Write a string to the end of a file"
  [filename s]
  (spit filename s :append true))

(defn format-quote
  "Delineate the beginning and end of a quote because it's convenient"
  [quote]
  (str "=== BEGIN QUOTE ===\n" quote "=== END QUOTE ===\n\n"))

(defn random-quote
  "Retrieve a random quote and format it"
  []
  (format-quote (slurp "http://www.braveclojure.com/random-quote")))

(defn snag-quotes
  [filename num-quotes]
  (let [c (chan)]
    (go (while true (append-to-file filename (<! c))))
    (dotimes [n num-quotes] (go (>! c (random-quote))))))


; Escape Callback Hell with Process Pipelines
(defn upper-caser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/upper-case (<! in)))))
    out))

(defn reverser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/reverse (<! in)))))
    out))

(defn printer
  [in]
  (go (while true (println (<! in)))))

(def in-chan (chan))
(def upper-caser-out (upper-caser in-chan))
(def reverser-out (reverser upper-caser-out))
(printer reverser-out)

(>!! in-chan "redrum")
; => MURDER

(>!! in-chan "repaid")
; => DIAPER



















