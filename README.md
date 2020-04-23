# Mini-projet MrSudoku

Le jeu du Sudoku en mini-projet.


==> le fichier src/mrsudoku/engine.clj est à compléter...

==> sans oublier le solveur ...





=> Grille exemple résolue :

 5   3  [4] [6]  7  [8] [9] [1] [2]
 6  [7] [2]  1   9   5  [3] [4] [8]
[1]  9   8  [3] [4] [2] [5]  6  [7]
 8  [5] [9] [7]  6  [1] [4] [2]  3 
 4  [2] [6]  8  [5]  3  [7] [9]  1 
 7  [1] [3] [9]  2  [4] [8] [5]  6 
[9]  6  [1] [5] [3] [7]  2   8  [4]
[2] [8] [7]  4   1   9  [6] [3]  5 
[3] [4] [5] [2]  8  [6] [1]  7   9 

# TO DO
- Sudoku Solver using Backtraking and Sudoku generator
- Link to the UI and make `button` work

```python
def solve():
    global grid
    for y in range(9):
        for x in range(9):
            if grid[y][x] == 0:
                for n in range(1,10):
                    if possible(y,x,n):
                        grid[y][x] = n
                        solve()
                        grid[y][x] = 0
                return 
	print(np.matrix(grid))
    input("Other Solution (if available) ?")
```

### Notes
```clojure

(def cur-grid (atom sudoku-grid))

(comment
  (defn backtracking
    "Solves the Sudoku `grid` using a backtracking algorithm"
    [grid]
    (for [cx (range 1 10)
          cy (range 1 10)]
      (if (= (:status (g/cell @cur-grid cx cy)) :empty)
        (do (println "i see an empty case")
            (for [n (range 1 10)]
              (if (possible-value? @cur-grid cx cy n)
                (do (swap! cur-grid #(g/change-cell % cx cy {:status :set, :value n}))
                    (backtracking)
                    (comment (swap! cur-grid #(g/change-cell % cx cy {:status :empty}))))
                (println "finished"))))))))

(defn backtracking
  "Solves the Sudoku `grid` using a backtracking algorithm"
  [grid]
  (loop [my-grid grid, cx 1, cy 1]
    (if (= cy 9)
      my-grid
      (let [resg (->> (range 1 10)
                      (filter #(possible-value? my-grid cx cy %))
                      first
                      (g/mk-cell)
                      (g/change-cell my-grid cx cy))]
        (if (= cx 9)
          (recur resg 1 (inc cy))
          (recur resg (inc cx) cy))))))


;;(->> (range 1 10) (filter #(possible-value? sudoku-grid 3 1 %)) first (g/mk-cell) (g/change-cell sudoku-grid 3 1))

(println (g/grid->str (backtracking sudoku-grid)))

(comment
  (for [cx (range 1 10)
        cy (range 1 10)]
    (if (= (:status (g/cell @cur-grid cx cy)) :empty)
      (do (println "i see an empty cell")
          (for [n (range 1 10)
                :when (possible-value? @cur-grid cx cy n)]
            (do (println "i can put it in")
                (swap! cur-grid #(g/change-cell % cx cy {:status :set, :value n}))
                (backtracking)
                (swap! cur-grid #(g/change-cell % cx cy {:status :set, :value n}))))))))
```
 5   3  [4] [6]  7  [8] [9] [1] [2]
 6  [7] [2]  1   9   5  [3] [4] [8]
[1]  9   8  [3] [4] [2] [5]  6  [7]
 8  [5] [9] [7]  6  [1] [4] [2]  3 
 4  [2] [6]  8  [5]  3  [7] [9]  1 
 7  [1] [3] [9]  2  [4] [8] [5]  6 
[9]  6  [1] [5] [3] [7]  2   8  [4]
[2] [8] [7]  4   1   9  [6] [3]  5 
[3] [4] [5] [2]  8  [6] [1]  7   9 

```clojure
(fn [event]
((resolve 'mrsudoku.control/show-solved) ctrl (s/bruteforce-solve grid) grid-widget))
```