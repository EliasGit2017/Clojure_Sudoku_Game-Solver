(ns mrsudoku.engine
  (:use midje.sweet)
  (:require [mrsudoku.grid :as g]))

(def ^:private sudoku-grid (var-get #'g/sudoku-grid))

(defn values
  "Return the set of values of a vector or grid `cells`."
  [cells]
  (loop [res #{}, c cells]
    (if (seq c)
      (recur (if (not= nil (g/cell-value (first c)))
               (conj res (g/cell-value (first c)))
               res) (rest c))
      res)))

(fact
 (values (g/block sudoku-grid 1)) => #{5 3 6 9 8})

(fact
 (values (g/row sudoku-grid 1)) => #{5 3 7})

(fact
 (values (g/col sudoku-grid 1)) => #{5 6 8 4 7})

(fact
 (values (g/block sudoku-grid 8)) => #{4 1 9 8})

(fact
 (values (g/row sudoku-grid 8)) => #{4 1 9 5}) ;; Le 6 a été retiré

(fact
 (values (g/col sudoku-grid 8)) => #{6 8 7})

(defn values-except
  "Return the set of values of a vector of cells, except the `except`-th one."
  [cells except]
  {:pre [(<= 1 except (count cells))]}
  (loop [res #{}, c cells, ite 1]
    (if (seq c)
      (recur (if (and (not= ite except) (not= nil (g/cell-value (first c))))
               (conj res (g/cell-value (first c)))
               res) (rest c) (inc ite))
      res)))

(fact
 (values-except (g/block sudoku-grid 1) 1) => #{3 9 6 8})

(fact
 (values-except (g/block sudoku-grid 1) 4) => #{3 9 5 8})

(defn mk-conflict [kind cx cy value]
  {:status :conflict
   :kind kind
   :value value})

(defn merge-conflict-kind
  [kind1 kind2]
  (cond
    (and (set? kind1) (set? kind2)) (clojure.set/union kind1 kind2)
    (set? kind1) (conj kind1 kind2)
    (set? kind2) (conj kind2 kind1)
    (= kind1 kind2) kind1
    :else (hash-set kind1 kind2)))

(fact
 (merge-conflict-kind :row :row) => :row)

(fact
 (merge-conflict-kind :row :block) => #{:row :block})

(fact
 (merge-conflict-kind :row #{:row :block}) => #{:row, :block})

(fact
 (merge-conflict-kind #{:row :block} :block) => #{:row, :block})

(fact
 (merge-conflict-kind #{:row :block} #{:block :col}) => #{:row :block :col})


(defn merge-conflict [conflict1 conflict2]
  (assoc conflict1 :kind (merge-conflict-kind (:kind conflict1) (:kind conflict2))))

(defn merge-conflicts [& conflicts]
  (apply (partial merge-with merge-conflict) conflicts))

(defn update-conflicts
  [conflict-kind cx cy value conflicts]
  (if-let [conflict (get conflicts [cx, cy])]
    (assoc conflicts [cx, cy] (mk-conflict (merge-conflict-kind conflict-kind (:kind conflict))
                                           cx cy value))
    (assoc conflicts [cx, cy] (mk-conflict conflict-kind cx cy value))))

(defn conflict-value [values except cell]
  (when-let [value (g/cell-value cell)]
    (when (and (not= (:status cell) :init)
               (contains? (values-except values except) value))
      value)))

(defn row-conflicts
  "Returns a map of conflicts in the specified `row` of the grid."
  [row cy]
  {:pre [(<= 1 cy (count row))]}
  (loop [res-conf {}, ite 1]
    (if (<= ite (count row))
      (if-let  [conf (conflict-value row ite (nth row (dec ite)))]
        (recur (update-conflicts :row ite cy conf res-conf) (inc ite))
        (recur res-conf (inc ite)))
      res-conf)))

(fact
 (row-conflicts (map #(g/mk-cell :set %) [1 2 3 4]) 1) => {})

(fact
 (row-conflicts (map #(g/mk-cell :set %) [1 2 3 1]) 1)
 => {[1 1] {:status :conflict, :kind :row, :value 1}
     [4 1] {:status :conflict, :kind :row, :value 1}})

(fact
 (row-conflicts [{:status :init, :value 8} {:status :empty} {:status :empty} {:status :empty} {:status :init, :value 6} {:status :set, :value 6} {:status :empty} {:status :empty} {:status :init, :value 3}] 4)
 => {[6 4] {:status :conflict, :kind :row, :value 6}})

(defn rows-conflicts [grid]
  (reduce merge-conflicts {}
          (map (fn [r] (row-conflicts (g/row grid r) r)) (range 1 10))))

(defn col-conflicts
  "Returns a map of conflicts in the specified `col` of the grid."
  [col cx]
  {:pre [(<= 1 cx (count col))]}
  (loop [res-conf {},ite 1]
    (if (<= ite (count col))
      (if-let [conf (conflict-value col ite (nth col (dec ite)))]
        (recur (update-conflicts :col cx ite conf res-conf) (inc ite))
        (recur res-conf (inc ite)))
      res-conf)))

;; Tests added
(fact
 (col-conflicts (map #(g/mk-cell :set %) [1 2 3 4 5 6 7 8 9 10]) 1) => {})

(fact
 (col-conflicts (map #(g/mk-cell :set %) [1 2 3 4 5 6 7 8 2 1]) 1) => {[1 1] {:status :conflict, :kind :col, :value 1},[1 2] {:status :conflict, :kind :col, :value 2},[1 9] {:status :conflict, :kind :col, :value 2},[1 10] {:status :conflict, :kind :col, :value 1}})

(fact
 (col-conflicts [{:status :init, :value 8} {:status :empty} {:status :empty} {:status :empty} {:status :init, :value 6} {:status :set, :value 6} {:status :empty} {:status :empty} {:status :init, :value 3}] 4)
 => {[4 6] {:status :conflict, :kind :col, :value 6}})

(fact
 (col-conflicts (g/col sudoku-grid 1) 1) => {})

(fact
 (col-conflicts (map #(g/mk-cell :set %) [1 2 3 4 5 6 7 8 2 1]) 7) => {[7 1] {:status :conflict, :kind :col, :value 1},[7 2] {:status :conflict, :kind :col, :value 2},[7 9] {:status :conflict, :kind :col, :value 2},[7 10] {:status :conflict, :kind :col, :value 1}})

(fact
 (col-conflicts (g/col (g/change-cell sudoku-grid 1 3 {:status :set, :value 5}) 1) 1) => {[1 3] {:status :conflict, :kind :col, :value 5}})

(defn cols-conflicts
  [grid] (reduce merge-conflicts {}
                 (map (fn [c] (col-conflicts (g/col grid c) c)) (range 1 10))))

(defn block-conflicts
  "Returns a map of conflicts in the specified `b`-th `block` of the grid."
  [block b]
  {:pre [(<= 1 b (count block))]}
  (let [bx (* 3 (rem (dec b) 3)), by (* 3 (quot (dec b) 3))] ;; bx by are the coordinates of the first cell of the `block` `b`
    (loop [res-conf {}, ite 1]
      (if (<= ite (count block))
        (if-let [conf (conflict-value block ite (nth block (dec ite)))]
          (recur (update-conflicts :block (inc (+ bx (rem (dec ite) 3))) (inc (+ by (quot (dec ite) 3))) conf res-conf) (inc ite))
          (recur res-conf (inc ite)))
        res-conf))))

;; Tests added
(fact
 (block-conflicts (map #(g/mk-cell :set %) [1 2 5 4 7]) 1) => {})

(fact
 (block-conflicts (map #(g/mk-cell :set %) [7 2 5 4 7]) 1) => {[2 2] {:status :conflict, :kind :block, :value 7}, [1 1] {:status :conflict, :kind :block, :value 7}})

(fact
 (block-conflicts [{:status :init, :value 5} {:status :init, :value 3} {:status :set, :value 5} {:status :init, :value 6} {:status :empty} {:status :empty} {:status :set, :value 3} {:status :init, :value 9} {:status :init, :value 8}] 1)
 => {[3 1] {:status :conflict, :kind :block, :value 5} [1 3] {:status :conflict, :kind :block, :value 3}})

(fact
 (block-conflicts (g/block (g/change-cell sudoku-grid 1 3 {:status :set, :value 3}) 1) 1) => {[1 3] {:status :conflict, :kind :block, :value 3}})

(fact
 (block-conflicts (g/block (g/change-cell sudoku-grid 5 3 {:status :set, :value 9}) 2) 2) => {[5 3] {:status :conflict, :kind :block, :value 9}})

(fact
 (block-conflicts (g/block (g/change-cell (g/change-cell sudoku-grid 2 4 {:status :set, :value 8}) 2 5 {:status :set, :value 8}) 4) 4) => {[2 4] {:status :conflict, :kind :block, :value 8} [2 5] {:status :conflict, :kind :block, :value 8}})

(defn blocks-conflicts
  [grid]
  (reduce merge-conflicts {}
          (map (fn [b] (block-conflicts (g/block grid b) b)) (range 1 10))))

(defn grid-conflicts
  "Compute all conflicts in the Sudoku grid."
  [grid]
  (merge-conflicts (rows-conflicts grid)
                   (cols-conflicts grid)
                   (blocks-conflicts grid)))


