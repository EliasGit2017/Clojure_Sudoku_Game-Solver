(ns mrsudoku.solver
  (:use midje.sweet)
  (:require [mrsudoku.grid :as g]
            [mrsudoku.engine :as e]
            [clojure.set :as set]))

(def ^:private sudoku-grid (var-get #'g/sudoku-grid))

(declare mk-empty-grid mk-easy-grid mk-interm-grid mk-hard-grid mk-randomgrid)

;;---------------------------------------------------------------------------------------------------------------------------------------
;; Naïve solver

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

(fact
 
 (assign-cell sudoku-grid 3 1) => (list (g/change-cell sudoku-grid 3 1 (g/mk-cell :set 1))
                                    (g/change-cell sudoku-grid 3 1 (g/mk-cell :set 4))
                                    (g/change-cell sudoku-grid 3 1 (g/mk-cell :set 2)))
 
 (assign-cell sudoku-grid 4 4) => (list (g/change-cell sudoku-grid 4 4 (g/mk-cell :set 7))
                                        (g/change-cell sudoku-grid 4 4 (g/mk-cell :set 9))
                                        (g/change-cell sudoku-grid 4 4 (g/mk-cell :set 5)))
 
 (assign-cell sudoku-grid 2 4) => (list (g/change-cell sudoku-grid 2 4 (g/mk-cell :set 1))
                                        (g/change-cell sudoku-grid 2 4 (g/mk-cell :set 2))
                                        (g/change-cell sudoku-grid 2 4 (g/mk-cell :set 5))))

(defn to-solve
  "Updates the :status value in each cell of the solved `grid` to :solved.
  This function is called at the end of the bruteforce-solve function."
  [grid]
  {:pre [(= 3 (count grid))]}
  (loop [newgrid grid cx 1 cy 1]
    (if (<= cy 9)
      (if (< cx 9)
        (recur (g/change-cell newgrid cx cy (g/mk-cell :solved (g/cell-value (g/cell grid cx cy)))) (inc cx) cy)
        (recur (g/change-cell newgrid cx cy (g/mk-cell :solved (g/cell-value (g/cell grid cx cy)))) (mod (inc cx) 9) (inc cy)))
      newgrid)))

(fact

(g/reduce-grid (fn [acc cx cy cell]
                 (if (not= (:status cell) :solved)
                   (+ acc 1)
                   acc)) 0 (to-solve sudoku-grid)) => 0
 
 (g/reduce-grid (fn [acc cx cy cell]
                  (if (not= (:status cell) :solved)
                    (+ acc 1)
                    acc)) 0 (g/change-cell (to-solve sudoku-grid) 2 4 (g/mk-cell :conflict 4))) => 1)


;; bruteforce-solve is called on every cell of the grid but only the cells that are not :init are considered
;; this solution uses a signicative amount of memory compared to other solving methods.

(defn bruteforce-solve
  "Solves the sudoku `grid` using Brute force"
  ([grid]
   (bruteforce-solve grid (for [cx (range 1 10) cy (range 1 10)] [cx cy])))
  ([grid [cxcy & coords]]
   (if cxcy
     (let [possibles (assign-cell grid (first cxcy) (second cxcy))]
       (first (keep #(bruteforce-solve % coords) possibles)))
     (to-solve grid))))

(fact
 
 (e/grid-conflicts (bruteforce-solve sudoku-grid)) => {}
 
 (e/grid-conflicts (bruteforce-solve (mk-easy-grid))) => {}
 
 (e/grid-conflicts (bruteforce-solve (mk-interm-grid))) => {}
 
 (e/grid-conflicts (bruteforce-solve (mk-hard-grid))) => {})

;; Print solution
(comment (println (g/grid->str (bruteforce-solve sudoku-grid))))

;; ------------------------------- Breadth First Search Sudoku Solver ---------------------------------------------------

(defn list->grid
  [s-list]
  {:pre [(= 0 (mod (count s-list) 81))]} ;; Il peut y avoir plusieurs solutions, dans ce cas on prend la première
  (if (zero? (count s-list))
             "NO SOLUTION FOUND. Possibly 2 solutions"
    (let [blocs (partition 9 (take 81 s-list))]
    (loop [resgrid [], nb 0]
      (if (= 9 nb)
        (into [] resgrid)
        (recur (into resgrid [[(into [] (nth blocs nb)) (into [] (nth blocs (+ 1 nb))) (into [] (nth blocs (+ 2 nb)))]]) (+ nb 3)))))))

(defn breadth-solve
  "Solves the sudoku `grid` using Brute force by generating each and every grid possible and returning
   the right one"
  [grid]
  (if-let [[cx cy] (first (for [cx (range 1 10) cy (range 1 10)
                                :when (= (:status (g/cell grid cx cy)) :empty)]
                            [cx cy]))]
    (flatten (map #(breadth-solve (g/change-cell grid cx cy (g/mk-cell :set %))) (possible-values grid cx cy)))
    grid))

(defn bruteforce-breadth-solve
  "Returns the sudoku in the correct grid format after solving with breadth search."
  [grid]
  (-> grid
      breadth-solve
      list->grid
      to-solve))

(fact
 
(e/grid-conflicts (bruteforce-breadth-solve sudoku-grid)) => {}

(e/grid-conflicts (bruteforce-breadth-solve (mk-easy-grid))) => {}

;; Contrairement à la méthode de recherche en profondeur, Le temps de résolution de grilles
;; intermédiaires et difficiles peut être très long. J'ai choisi de ne pas les inclure
;; dans les test.
  
;(e/grid-conflicts (bruteforce-breadth-solve (mk-interm-grid))) => {}

;(e/grid-conflicts (bruteforce-breadth-solve (mk-hard-grid))) => {}
)

;; ----------------------------------------------------------------------------------------------------------------------------------------
;; Functions used to generate a random grid considering the fact that the minimum number of specified digits must be at least 17 to have
;; a unique answer (for initial grids that have less than 17 specified digits, there is no configuration such as only one solution is
;; possible).

(defn mk-empty-grid
  "Returns an empty sudoku grid"
  []
  (conj (into [] (repeat 3 (into [] (repeat 3 (into [] (repeat 9 (g/mk-cell)))))))))

(fact

 (g/grid->str (mk-empty-grid)) => " .   .   .   .   .   .   .   .   . \n .   .   .   .   .   .   .   .   . \n .   .   .   .   .   .   .   .   . \n .   .   .   .   .   .   .   .   . \n .   .   .   .   .   .   .   .   . \n .   .   .   .   .   .   .   .   . \n .   .   .   .   .   .   .   .   . \n .   .   .   .   .   .   .   .   . \n .   .   .   .   .   .   .   .   . "

 (g/reduce-grid (fn [acc cx cy cell]
                  (if (= (:status cell) :empty)
                    (+ acc 1)
                    acc)) 0 (mk-empty-grid)) => 81)

(defn to-init
  "Updates the :status value in each cell of the new `grid` to :init.
  This function is called for each new generated grid."
  [grid]
  (loop [newgrid grid cx 1 cy 1]
    (if (<= cy 9)
      (if (< cx 9)
        (if (not= (:status (g/cell newgrid cx cy)) :empty)
          (recur (g/change-cell newgrid cx cy (g/mk-cell (g/cell-value (g/cell newgrid cx cy)))) (inc cx) cy)
          (recur newgrid (inc cx) cy))
        (recur (if (not= (:status (g/cell newgrid cx cy)) :empty)
                 (g/change-cell newgrid cx cy (g/mk-cell (g/cell-value (g/cell newgrid cx cy))))
                 newgrid) (mod (inc cx) 9) (inc cy)))
      newgrid)))

(defn seed-grid
  "Returns a grid having `n` cells set. `n` must not exceed 10 in order to lead to a grid that can be solved"
  [grid n]
  {:pre [(<= n 10)]}
  (loop [seed grid, n n]
    (if (zero? n)
      (if (bruteforce-solve seed)
        seed
        (println "The random grid generated has no solution"))
      (let [cx (rand-nth (range 1 10)), cy (rand-nth (range 1 10)), poss (into [] (possible-values seed cx cy))]
        (recur (g/change-cell seed cx cy (g/mk-cell (rand-nth poss))) (dec n))))))

(defn mk-randomgrid
  "Returns a random sudoku grid given an empty `grid` having `n` numbers set"
  [grid n]
  (loop [rand-grid (bruteforce-solve (seed-grid grid (rand-nth (range 5 10)))), toset (- 81 n)] ;; ajouter un if-let
    (if (zero? toset)
      (to-init rand-grid)
      (let [cx (rand-nth (range 1 10)), cy (rand-nth (range 1 10))]
        (if (= (:status (g/cell rand-grid cx cy)) :solved)
          (recur (g/change-cell rand-grid cx cy (g/mk-cell)) (dec toset))
          (recur rand-grid toset))))))

(comment (println (g/grid->str (mk-randomgrid (mk-empty-grid) 40))))

(fact

 (e/grid-conflicts (mk-randomgrid (mk-empty-grid) 8)) => {}

 (e/grid-conflicts (mk-randomgrid (mk-empty-grid) 40)) => {}

 (e/grid-conflicts (mk-randomgrid (mk-empty-grid) 60)) => {}

 (= (mk-randomgrid (mk-empty-grid) 40) (mk-randomgrid (mk-empty-grid) 40)) => false)

(defn mk-easy-grid
  "Returns an easy grid made of 38 set cells"
  []
  (mk-randomgrid (mk-empty-grid) 38))

(fact

 (g/reduce-grid (fn [acc cx cy cell]
                  (if (= (:status cell) :empty)
                    (+ acc 1)
                    acc)) 0 (mk-easy-grid)) => 43

 (e/grid-conflicts (mk-easy-grid)) => {}

 (not= (mk-easy-grid) (mk-easy-grid)) => true)

(defn mk-interm-grid
  "Returns an intermediate grid made of 24 set cells"
  []
  (mk-randomgrid (mk-empty-grid) 24))

(fact

 (g/reduce-grid (fn [acc cx cy cell]
                  (if (= (:status cell) :empty)
                    (+ acc 1)
                    acc)) 0 (mk-interm-grid)) => 57

 (e/grid-conflicts (mk-interm-grid)) => {}

 (not= (mk-interm-grid) (mk-interm-grid)) => true)

(defn mk-hard-grid
  "Returns a difficult grid made of 15 set cells"
  []
  (mk-randomgrid (mk-empty-grid) 15))

(fact

 (g/reduce-grid (fn [acc cx cy cell]
                  (if (= (:status cell) :empty)
                    (+ acc 1)
                    acc)) 0 (mk-hard-grid)) => 66

 (e/grid-conflicts (mk-hard-grid)) => {}

 (not= (mk-hard-grid) (mk-hard-grid)) => true)

