(ns training.3-hard-exercises
  )

; 1. Given a vector of integers, find the longest consecutive sub-sequence of
; increasing numbers. If two sub-sequences have the same length, use the one
; that occurs first. An increasing sub-sequence must have a length of 2
; or greater to qualify.
;(= (__ [1 0 1 2 3 0 4 5]) [0 1 2 3])
;(= (__ [5 6 1 3 2 7]) [5 6])
;(= (__ [2 3 3 4 5]) [3 4 5])
;(= (__ [7 6 5 4]) [])
(defn lis
  [xs]
  (reduce
    (fn [m c] (if (< (count m) (count c)) c m))
    ()
    (filter
      #(> (count %) 1)
      (loop [coll xs inc-seqs [] res []]
        (if (empty? coll)
          res
          (let [curr (first coll)
                next (if (empty? (rest coll)) (dec curr) (second coll))]
            (if (>= curr next)
              (recur (rest coll) [] (conj res (conj inc-seqs curr)))
              (recur (rest coll) (conj inc-seqs curr) res))))))))

(lis [1 0 1 2 3 0 4 5])
(lis [2 3 3 4 5])
(lis [7 6 5 4])


; 2. Analyze a Tic-Tac-Toe Board
; A tic-tac-toe board is represented by a two dimensional vector.
; X is represented by :x,
; O is represented by :o,
; and empty is represented by :e.
; A player wins by placing three Xs or three Os in a horizontal, vertical, or diagonal row.
; Write a function which analyzes a tic-tac-toe board and returns :x if X has won,
; :o if O has won, and nil ; if neither player has won.
;(= nil (__ [[:e :e :e]
;            [:e :e :e]
;            [:e :e :e]]))
;(= :x (__ [[:x :e :o]
;           [:x :e :e]
;           [:x :e :o]]))
;(= :o (__ [[:e :x :e]
;           [:o :o :o]
;           [:x :e :x]]))
;(= nil (__ [[:x :e :o]
;            [:x :x :e]
;            [:o :x :o]]))
;(= :x (__ [[:x :e :e]
;           [:o :x :e]
;           [:o :e :x]]))
;(= :o (__ [[:x :e :o]
;           [:x :o :e]
;           [:o :e :x]]))
;(= nil (__ [[:x :o :x]
;            [:x :o :x]
;            [:o :x :o]]))
((fn analyze-tic-tac-toe-board [board]
   (let [hor? (fn [char board]
                (if (empty? board)
                  false
                  (let [line (first board)]
                    (if (every? #(= char %) line)
                      true
                      (recur char (rest board))))))
         ver? (fn [char board]
                (hor? char (partition 3 (apply interleave board))))
         diag-lr? (fn [char board]
                    (every? #(= char %) (reduce #(conj %1 (nth %2 (count %1))) [] board)))
         diag-rl? (fn [char board]
                    (every? #(= char %) (reduce #(conj %1 (nth %2 (- 2 (count %1)))) [] board)))
         won? (fn [char] (not-every? #(= false %) (map #(% char board) [hor? ver? diag-lr? diag-rl?])))]
     ;(sc.api/spy)
     (if (won? :x) :x (when (won? :o) :o))))
 [[:x :e :e]
  [:x :o :e]
  [:o :e :x]])

; uncomment (sc.api/spy) first
; debug scope
; (sc.api/defsc 2)

((fn [board]
   (letfn [(check [[[a b c][d e f][g h i]] p]
             (or (= p a b c)
                 (= p d e f)
                 (= p g h i)
                 (= p a d g)
                 (= p b e h)
                 (= p c f i)
                 (= p a e i)
                 (= p c e g)))]
     (cond (check board :x) :x
           (check board :o) :o
           :else nil)))
 [[:x :e :o]
  [:x :o :e]
  [:o :e :x]])

((fn [board]
   (let [rows board ;get rows
         columns (apply map vector board) ;get columns
         diagonals [(map get board [0 1 2]) (map get board [2 1 0])]] ;get diagonals
     (loop [remaining (concat rows columns diagonals)] ;all potential triplets
       (if-let [item (set (first remaining))]
         (if (and (not-any? #(= :e %) item) (or (every? #(= :x %) item) (every? #(= :o %) item)));find the winner
           (first item)
           (recur (rest remaining)))
         nil))))
 [[:x :e :o]
  [:x :o :e]
  [:o :e :x]])

((fn tic-tac-toe [board]
   (let [same? (fn [sec] (if (apply = sec) (first sec) nil))
         rows (map same? board)
         cols (map same? (apply map vector board))
         diag1 (same? (map get board (range 3)))
         diag2 (same? (map get board (range 2 -1 -1)))]
     (some #{:x :o} (concat rows cols [diag1] [diag2]))))
 [[:x :e :o]
  [:x :o :e]
  [:o :e :x]])

