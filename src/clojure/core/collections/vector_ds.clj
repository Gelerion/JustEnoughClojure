(ns clojure.core.collections.vector-ds)

; [] - Vectors (0-indexed array)
(vector "a" "b" "c")
(def array_vec [3 2 1])
(get array_vec 0) ; NOTE doesn't work with lists

; remove an item from a vector, obtaining a new vector without the item
(pop [1 2 3 4]) ; [1 2 3]
(subvec [:a :b :c :d] 1) ; [:b :c :d]
(subvec [:a :b :c :d] 1 3) ; [:b :c]

; matrix
((fn [board]
   (let [rows board ;get rows
         columns (apply map vector board) ;get columns
         diagonals [(map get board [0 1 2]) (map get board [2 1 0])]] ;get diagonals
     (println rows columns diagonals)))
  [[:x :e :o]
   [:x :o :e]
   [:o :e :x]])

(get-in [[1 2 3]
         [4 5 6]
         [7 8 9]] [0 2]) ; 3

(assoc [:a :b :c ] 1 :x) ; -> [:a :x :c]

; conj operator adds value to vector
(conj [1 2 3] 4) ; [1 2 3 4]
; BUT
(conj '(1 2 3) 4) ; (4 1 2 3)

(conj [0] [1]) ; !![0 [1]]

