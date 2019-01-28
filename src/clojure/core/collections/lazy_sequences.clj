(ns clojure.core.collections.lazy-sequences)

;As you saw earlier, map first calls seq on the collection you pass to it. But that’s not
;the whole story. Many functions, including map and filter, return a lazy seq. A lazy seq
;is a seq whose members aren’t computed until you try to access them. Computing a seq’s
;members is called realizing the seq. Deferring the computation until the moment it’s
;needed makes your programs more efficient, and it has the surprising benefit of allowing
;you to construct infinite sequences.

(def vampire-database
  {0 {:makes-blood-puns? false, :has-pulse? true  :name "McFishwich"}
   1 {:makes-blood-puns? false, :has-pulse? true  :name "McMackson"}
   2 {:makes-blood-puns? true,  :has-pulse? false :name "Damon Salvatore"}
   3 {:makes-blood-puns? true,  :has-pulse? true  :name "Mickey Mouse"}})

; computes every time
(defn vampire-related-details
  [social-security-number]
  (Thread/sleep 1000) ;assume there are many more in the DB
  (get vampire-database social-security-number))

; evaluates lazily
(defn mapped-vampire-related-details
  [social-security-numbers]
  (map vampire-related-details social-security-numbers))

(defn vampire?
  [record]
  (and (:makes-blood-puns? record)
       (not (:has-pulse? record))
       record))

(defn identify-vampire
  [social-security-numbers]
  (first (filter vampire? (map vampire-related-details social-security-numbers))))

;mapped-details is unrealized. Once you try to access a member of mapped-details, it will use its
;recipe to generate the element you’ve requested, and you’ll incur the one-second-per-database-lookup
;cost:

;(time (first (mapped-vampire-related-details (range 0 100))))
;"Elapsed time: 32071.224726 msecs"
;=> {:makes-blood-puns? false, :has-pulse? true, :name "McFishwich"}

;This operation took about 32 seconds. That’s much better than one million seconds, but it’s still
;31 seconds more than we would have expected. After all, you’re only trying to access the very
;first element, so it should have taken only one second.

; -----
;The reason it took 32 seconds is that Clojure chunks its lazy sequences, which just means that
;whenever Clojure has to realize an element, it preemptively realizes some of the next elements
;as well. In this example, you wanted only the very first element of mapped-details, but Clojure
;went ahead and prepared the next 31 as well. Clojure does this because it almost always results in
;better performance.

;Thankfully, lazy seq elements need to be realized only once. Accessing the first element of mapped-details
;again takes almost no time
; -----

;(time (vampire-related-details 0)) ;1 sec
;(time (def mapped-details (map vampire-related-details (range 0 100)))) ; lazy-sequences
;; lazy seq elements need to be realized only once.
;(time (first mapped-details))


;--------------------- Infinity Sequences
(concat (take 8 (repeat "na")) ["Batman!"])

; repeatedly - call the provided function to generate each element in the sequence
(take 3 (repeatedly (fn [] (rand-int 10))))

; cons - returns a new list with an element appended to the given list
(cons 0 '(2 4 6))

(defn even-numbers
  ([] (even-numbers 0))
  ([n] (cons n (lazy-seq (even-numbers (+ n 2))))))


(take 10 (even-numbers))

; reductions
; Returns a lazy seq of the intermediate values of the reduction (as
; per reduce) of coll by f, starting with init.
(reductions + [1 1 1 1]) ; (1 2 3 4)
(reductions + [1 2 3]) ; (1 3 6)
;;reductions using a init value 100
(reductions (fn [sum num] (+ sum num)) 100 [1 2 3 4 5])
;;(100 101 103 106 110 115)

;; useful for performing lazy calculations which rely on
;; previous calculations

;; e.g. Taking an infinite list of posts with some height and
;; adding an offset to each, which is the sum of all previous
;; heights
(def posts (repeat {:height 50}))
(def posts-with-offsets
  (map #(assoc %1 :offset %2)
       posts
       (reductions + 0 (map :height posts))))
(take 3 posts-with-offsets)
; ({:height 50, :offset 0} {:height 50, :offset 50} {:height 50, :offset 100})


; cycle
; Returns a lazy (infinite!) sequence of repetitions of the items in coll.
(take 5 (cycle ["a" "b"])) ; ("a" "b" "a" "b" "a")
(take 10 (cycle (range 0 3))) ; (0 1 2 0 1 2 0 1 2 0)


; keep-indexed
; Returns a lazy sequence of the non-nil results of (f index item)
(keep-indexed #(if (odd? %1) %2) [:a :b :c :d :e])

; doall
;; Nothing is printed because map returns a lazy-seq
(def foo (map println [1 2 3]))
;; doall forces the seq to be realized
(def foo (doall (map println [1 2 3]))) ; 1 2 3












