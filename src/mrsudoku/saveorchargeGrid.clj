(ns mrsudoku.saveorchargeGrid
  (:use midje.sweet)
  (:require [mrsudoku.grid :as g]
            [mrsudoku.engine :as e]
            [clojure.set :as set]
            [clojure.java.shell :as sh]
            [mrsudoku.solver :as s]))

(def ^:private sudoku-grid (var-get #'g/sudoku-grid))
;; -----------------------------------------------------------------------------------------------------------------------------
;; Functions used to save/charge a grid given a specified file 
;; 

(defn readfile
  "Returns a sequence from a file f"
  [f]
  (with-open [rdr (clojure.java.io/reader f)]
    (doall (line-seq rdr))))

(defn write-file
  "Writes the string chain `to-write` in the file /home/elias/Documents/3I020/Projet_Sudoku/mrsudoku_19-03-2020/lastgrid.txt which will be used to save a grid"
  [to-write]
  (with-open [w (clojure.java.io/writer  "/home/elias/Documents/3I020/Projet_Sudoku/mrsudoku_19-03-2020/lastgrid.txt" :append true)]
    (.write w to-write)))

(defn save-grid
  "Saves a given `grid` to a .txt file in order to resume the game later"
  [grid]
  (write-file (str (g/grid->str grid) "\n\n")))

(save-grid (s/mk-hard-grid))

(defn parse-grids
  [lists]
  (loop [to-parse lists, grids [], mygrid []]
    (if (seq to-parse)
      (if (not= "" (first to-parse))
        (recur (rest to-parse) grids (conj mygrid (first to-parse)))
        (recur (rest to-parse) (conj grids mygrid) []))
      grids)))

(defn split-spaces
  [line]
  (clojure.string/split line #"\s"))

(defn remove-whitespaces
  [line]
  (->> line
       split-spaces
       (filter #(not (clojure.string/blank? %)))))

(defn str->vecells
  [vecgrid]
  (loop [vecgrid vecgrid, res []]
    (if (seq vecgrid)
      (recur (rest vecgrid) (into [] (concat res (into [] (remove-whitespaces (first vecgrid))))))
      res)))

(defn str->grid
  "Updates the :status value in each cell of the solved `grid` to :solved.
  This function is called at the end of the bruteforce-solve function."
  [vecgrid]
  (loop [newgrid (s/mk-empty-grid), cx 1, cy 1, vecgrid vecgrid]
    (if (<= cy 9)
      (if (< cx 9)
        (recur (g/change-cell newgrid cx cy (cond 
                                              (= "." (first vecgrid)) (g/mk-cell)
                                              (= \[ (ffirst vecgrid)) (g/mk-cell :set (Integer. (str (second (first vecgrid)))))
                                              (= \! (ffirst vecgrid)) (g/mk-cell :conflict (Integer. (str (second (first vecgrid)))))
                                              :else (g/mk-cell (Integer. (str (ffirst vecgrid)))))) (inc cx) cy (rest vecgrid))
        (recur (g/change-cell newgrid cx cy (cond
                                              (= "." (first vecgrid)) (g/mk-cell)
                                              (= \[ (ffirst vecgrid)) (g/mk-cell :set (Integer. (str (second (first vecgrid)))))
                                              (= \! (ffirst vecgrid)) (g/mk-cell :conflict (Integer. (str (second (first vecgrid)))))
                                              :else (g/mk-cell (Integer. (str (ffirst vecgrid)))))) (mod (inc cx) 9) (inc cy) (rest vecgrid)))
      newgrid)))

(defn charge-grid
  "Charges a previously saved grid from the `lastgrid.txt` file to resume a game"
  (if-let))