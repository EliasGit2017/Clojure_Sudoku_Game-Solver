# Solveur de Sudoku : 

Après avoir essayé plusieurs approches et les avoir comparées en fonction de leur temps d'éxecution et de leur complexité, j'ai choisi d'en présenter deux qui bien que similaires dans leur approche, aboutissent à la grille résultat avec des temps très différents.

Ma première approche a été d'utiliser un algorithme de backtracking qui consiste simplement à proposer à chaque case libre de la grille un chiffre parmi ceux qui respectent les contraintes du jeu. A chaque fois qu'une incohérence est observée, on revient en arrière et on réessaye avec une valeur différente. Cette approche bien que simple et intuitive a été inefficace pour des grilles de difficulté moyenne et pratiquement tous mes tests ont nécessité de longs temps d'éxecution. 

J'ai alors décidé d'utiliser une approche dans laquelle on recherche la solution parmi toutes les configurations possibles à chaque étape de la tentative de résolution de la grille. La taille mémoire raisonnable des grilles de sudoku et les structures de données spécifiées dans le squelette du projet permettent de manipuler les instances du problème de manière compacte.  

Ayant donc décidé d'aborder le problème sous la forme d'un problème de recherche de solution (et non de satisfaction de contraintes comme c'est le cas pour la résoltion par SAT ou par propagation de contraintes et recherche comme réalisé par Peter Norvig (lien)), j'ai tout d'abord commencé par essayer la recherche en largeur.

### Approche par recherche en largeur : `breadth-solve` 

Pour chaque cellule de la grille dont le `:status` est à `:empty` et pour chaque chiffre parmi ceux possibles et respectants les contraintes d'unicité sur la ligne, la colonne et le bloc, on recherche la solution du sudoku en essayant les différentes valeurs possibles que l'on obtient grace à la fonction `mrsudoku.solver/possible-values`.

* On récupère tout d'abord la première cellule `:empty` de la grille.
* On génère toutes les grilles possibles en remplissant la cellule considérée par les différentes valeurs possibles.
* On réalise une récursion pour chacune des grilles générées afin d'examiner toutes les possibilités.

De cette manière, on map sur toutes les grilles pouvant être générées en respectant les contraintes du jeu imposées par les cellules à `:init` de la grille de départ et les ajouts réalisés lors de la recherche.

```clojure
(defn breadth-solve
  "Solves the sudoku `grid` using Brute force by generating each and every grid possible and returning
   the right one"
  [grid]
  (if-let [[cx cy] (first (for [cx (range 1 10) cy (range 1 10)
                                :when (= (:status (g/cell grid cx cy)) :empty)]
                            [cx cy]))]
    (flatten (map #(breadth-solve (g/change-cell grid cx cy (g/mk-cell :set %))) (possible-values grid cx cy)))
    grid))
```

 

On a recours à `flatten` car lorsque les remplissages successifs de cellules aboutissent à une cellule impossible à remplir (car `mrsudoku.solver/possible-values` retourne `#{}` dans cette configuration), on obtient une série de listes vides. Ces listes vides sont obtenues lors de l'appel à map (qui permet de traiter toutes les possibilités générées) et qui dans un cas tel que celui-ci :

```clojure
(fact
    (map #(breadth-solve (g/change-cell grid cx cy (g/mk-cell :set %))) #{}) => '()) => true
```

renvoie bien une liste vide. La situation peut être modélisée par un arbre sur lequel on recherche la solution, dont la racine est la grille de départ et les noeuds n'aboutissant à aucun résultat renvoient `()`.  Le noeud non vide le plus développé correspond à la solution et on le récupère à l'aide de `flatten`. Dans le cas d'une grille possédant plusieurs solutions, on peut avoir à récupérer plusieurs noeuds solution. 

Exemple  : La grille suivante admet 3 solutions

```bash
 7   .   9   .   .   .   6   .   .                     
 .   .   5   .   4   .   .   7   . 
 1   2   8   7   .   5   4   .   3 	  solution 1 :
 5   7   .   .   3   2   9   8   .    breadth-solve 
 4   .   .   6   8   .   .   .   .    -------------> 7  [4]  9  [3] [1] [8]  6  [2] [5]
 3   8   .   .   9   1   .   .   . 				    [6] [3]  5  [2]  4  [9] [1]  7  [8]
 .   1   .   .   5   .   3   .   . 				     1   2   8   7  [6]  5   4  [9]  3 
 .   6   4   1   .   .   8   .   . 				     5   7  [1] [4]  3   2   9   8  [6]
 .   5   3   9   2   .   .   .   4 				     4  [9] [2]  6   8  [7] [5] [3] [1]
 													 3   8  [6] [5]  9   1  [2] [4] [7]
													[2]  1  [7] [8]  5  [4]  3  [6] [9]
													[9]  6   4   1  [7] [3]  8  [5] [2]
													[8]  5   3   9   2  [6] [7] [1]  4                                   
```

```bash
 7  [4]  9  [3] [1] [8]  6  [5] [2]
[6] [3]  5  [2]  4  [9] [1]  7  [8]
 1   2   8   7  [6]  5   4  [9]  3 
 5   7  [1] [4]  3   2   9   8  [6]
 4  [9] [2]  6   8  [7] [5] [3] [1]
 3   8  [6] [5]  9   1  [2] [4] [7]
[2]  1  [7] [8]  5  [4]  3  [6] [9]
[9]  6   4   1  [7] [3]  8  [2] [5]
[8]  5   3   9   2  [6] [7] [1]  4        La solution 2 est obtenue en inversant 				                                  simplement le 2 et le 5 en (8,1) et (9,1) 
 7  [4]  9  [3] [1] [8]  6  [2] [5]
[6] [3]  5  [2]  4  [9] [1]  7  [8]
 1   2   8   7  [6]  5   4  [9]  3 
 5   7  [1] [4]  3   2   9   8  [6]
 4  [9] [2]  6   8  [7] [5] [3] [1]
 3   8  [6] [5]  9   1  [2] [4] [7]
[9]  1  [7] [8]  5  [4]  3  [6] [2]
[2]  6   4   1  [7] [3]  8  [5] [9]
[8]  5   3   9   2  [6] [7] [1]  4        Solution 3
```

Dans ce cas, on prend la première solution dans la fonction `list->grid` (on peut récupérer les autres en décalant sur le résultat de `breadth-solve` à l'aide d'un drop.

```clojure
(def mg (breadth-solve (mk-easy-grid)))
(e/grid-conflicts (list->grid (take 81 mg))) => {}
(e/grid-conflicts (list->grid (take 81 (drop 81 mg)))) => {}
(e/grid-conflicts (list->grid (take 81 (drop 162 mg)))) => {}
```

Lorsqu'il n'y a plus de cellule à `:empty` dans la grille, on retourne la grille résultat sous la forme d'une liste de cellules organisées par blocs où chaque cellule est résolue. A l'aide des fonctions `list->grid` et `to-solve`, on retrouve le format spécifié dans le sujet et l'on met à jour le statut des cellules à `:solved` pour adapter l'affichage de l'application.  

```clojure
(defn bruteforce-breadth-solve
  "Returns the sudoku in the correct grid format after solving with breadth search."
  [grid]
  (-> grid
      breadth-solve
      list->grid
      to-solve))
```

##### Remarques :

Cette méthode permet de rechercher toutes les solutions possibles mais est très gourmande en mémoire dans le cas de grilles ayant un nombre importants de contraintes et de possibilités. En effet, on procédera à la génération de toutes les éventualités et on ne s'arrêtera que lorsqu'elles seront toutes examinées (qu'elles aboutissent à un résultat ou non). Contrairement à la deuxième méthode, `bruteforce-breadth-solve` est plus lente et ne permet pas de résoudre en temps satisfaisant les grilles de difficulté moyenne (avec 57 cellules à déterminer) et encore moins les grilles difficiles (avec 66 cellules vides). Son seul avantage par rapport à l'algorithme utilisé pour la recherche en profondeur est le renvoi de toutes les solutions.  

### Approche par recherche en profondeur : `bruteforce-solve`





