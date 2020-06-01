# Solveur de Sudoku : 

Après avoir essayé plusieurs approches et les avoir comparées en fonction de leur temps d'éxecution et de leur complexité, j'ai choisi d'en présenter deux qui bien que similaires dans leur approche, aboutissent à la grille résultat avec des temps très différents.

Ma première approche a été d'utiliser un algorithme de backtracking qui consiste simplement à proposer à chaque case libre de la grille un chiffre parmi ceux qui respectent les contraintes du jeu. A chaque fois qu'une incohérence est observée, on revient en arrière et on réessaye avec une valeur différente. Cette approche bien que simple et intuitive a été inefficace pour des grilles de difficulté moyenne et pratiquement tous mes tests ont nécessité de longs temps d'éxecution. 

J'ai alors décidé d'utiliser une approche dans laquelle on recherche la solution parmi toutes les configurations possibles à chaque étape de la tentative de résolution de la grille. La taille mémoire raisonnable des grilles de sudoku et les structures de données spécifiées dans le squelette du projet permettent de manipuler les instances du problème de manière compacte.  

Ayant donc décidé d'aborder le problème sous la forme d'un problème de recherche de solution (et non de satisfaction de contraintes comme c'est le cas pour la résoltion par SAT ou par propagation de contraintes et recherche comme réalisé par Peter Norvig (lien)), J'ai tout d'abord commencé par essayé la recherche en largeur.

### Approche par recherche en largeur :

Pour chaque cellule de la grille dont le `:status` est à `:empty` et pour chaque chiffre parmi ceux possibles et respectants les contraintes d'unicité sur la ligne, la colonne et le bloc :

* 

 



### Approche par recherche en profondeur :





