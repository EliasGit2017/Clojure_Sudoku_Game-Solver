(ns mrsudoku.solver
  (:use midje.sweet)
  (:require [mrsudoku.grid :as g]
            [mrsudoku.engine :as e]
            [clojure.set :as set]))

(def ^:private sudoku-grid (var-get #'g/sudoku-grid))
;;---------------------------------------------------------------------------------------------------------------------------------------
;; Functions used to generate a random grid considering the fact that the minimum number of specified digits must be at least 17 to have
;; a unique answer (for initial grids that have less than 17 specified digits, there is no configuration such as only one solution is
;; possible).
;; Naïve and slow solver

(defn possible-values
  "Given a `grid`, returns a set of grids whith the possible values in 
  the given cell at coordinates cx cy."
  [grid cx cy]
  {:pre [(<= 1 cx 9)
         (<= 1 cy 9)]}
  (let [bx (quot (dec cx) 3) by (quot (dec cy) 3)
        already-set (set/union (e/values (g/row grid cy))
                               (e/values (g/col grid cx))
                               (e/values (g/block grid (+ (* by 3) bx 1))))]
    (set/difference (set (range 1 10)) already-set)))

;; Tests
(fact
 
 (possible-values sudoku-grid 3 1) => #{1 4 2}

 (possible-values sudoku-grid 2 4) => #{1 2 5}

 (possible-values sudoku-grid 4 4) => #{7 9 5}

 (possible-values sudoku-grid 7 9) => #{1 3 6 4})

(defn assign-cell
  "Returns a set of grids where a possible value is assigned to the cell at coordinates cx cy."
  [grid cx cy]
  (if (= :empty (:status (g/cell grid cx cy)))
    (map #(g/change-cell grid cx cy {:status :set, :value %}) (possible-values grid cx cy))
    [grid]))

(defn to-solve
  "Updates the :status value in each cell of the solved `grid` to :solved.
  This function is called at the end of the bruteforce-solve function."
  [grid]
  (loop [newgrid grid cx 1 cy 1]
    (if (<= cy 9)
      (if (< cx 9)
        (recur (g/change-cell newgrid cx cy {:status :solved, :value (:value (g/cell grid cx cy))}) (inc cx) cy)
        (recur (g/change-cell newgrid cx cy {:status :solved, :value (:value (g/cell grid cx cy))}) (mod (inc cx) 9) (inc cy)))
      newgrid)))


;; bruteforce-solve is called on every cell of the grid but only the cells that are not :init are considered
;; this solution uses a signicative amount of memory compared to other solving methods.
(defn bruteforce-solve
  "Solves the sudoku `grid` using Brute force ... Can take a very long time for grids with a large.
  number of possible solutions. Complexity is in O(scary, time is relative but come on...)"
  ([grid]
   (bruteforce-solve grid (for [cx (range 1 10) cy (range 1 10)] [cx cy])))
  ([grid [cxcy & coords]]
   (if cxcy
     (let [possibles (assign-cell grid (first cxcy) (second cxcy))]
       (first (keep #(bruteforce-solve % coords) possibles)))
     (to-solve grid))))

;; Print solution
(println (g/grid->str (bruteforce-solve sudoku-grid)))
