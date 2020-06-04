# Mini-projet MrSudoku

Réalisation d'un Sudoku avec solveur et implémentation de fonctionnalités supplémentaires telles que :

* Sauvegarde d'une grille en cours et chargement de cette grille lors de la prochaine partie.
* Génération de grilles aléatoires (cette génération est parfois imparfaite car certaines grilles peuvent s'avérer compliquées à résoudre car il est nécessaire pour certaines cases de supposer un chiffre parmi ceux possibles pour pouvoir aboutir à la solution). Ces grilles sont réparties en niveaux (Easy - Intermediate - Hard)
* Possibilité de jouer en utilisant une grille importée. Il faut alors ajouter la grille à la fin de `./lastgrid.txt`  et de choisir `Charge Last Saved Grid` dans le menu. Cet ajout peut se faire en éditant directement ce fichier (en respectant le format qui est celui retourné par `mrsudoku.grid/grid->str`).

![MainView](./mainview.png)

![Solved](./Solved.png)

##### Remarques importantes :

* Le jeu implémenté suppose que l'on a affaire à des grilles de sudoku bien formées, ne possédant qu'une seule solution possible. Seules les grilles remplissant cette condition peuvent être résolues efficacement par le solveur implémenté. Une autre version du solveur pouvant retourner plusieurs solutions est expliquée dans `./Solver.md`.
* Lors de l'éxecution de la commande `lein run`, quelques exceptions du type `Attempting to call unbound fn ...` seront levées mais peuvent être supprimées en déplaçant les `fact` des fonctions testées en fin du fichier `mrsudoku.solver` . J'ai choisi de les garder à leur place pour faciliter la lecture du code. En travaillant directement sur le fichier, il ne devrait y avoir aucune exception ,étant donné les `(declare mk-easy-grid ...)` spécifiés en début de fichier.

### Bibliographie :

* The Joy of Clojure, 2nd Edition 2014, Michael Fogus, Chris Houser.
* Dancing Links, Donald E.Knuth, Stanford University.
* Engelberg's repository on github (spécialement le plugin `tarantella` qui implémente les dancing links de Knuth en Clojure en passant par Java). 