# Towser

## Setup Netbeans :

- Créer une librairie, y ajouter tous les .jar trouvable dans le dossier lib.
- Clique droit sur librairies, ajouter la librairie créée.
- Clique droit sur le projet, propriétés, librairies, ajouter la librarie dans Classpath, puis dans l'onglet Run, faire de même dans Modulepath.
- Toujours dans propriétés, dans Run cette fois-ci, ajouter la ligne suivante : 
	-Djava.library.path="lib/"
- Vous pouvez Clean & Build le projet et le lancer.



## Review rapide

C'est un petit jeu réalisé en partant de presque rien (d'une librairie).
Je voulais essayer de créer un jeu "from scratch".
Le jeu est un Tower Defense, des ennemies parcours un chemin pour attaquer votre base. Survivez le plus de round possible.
Pour le moment, le jeu ne contient que 2 tourelles et 4 types d'ennemies. Le principe du jeu étant codé et
fonctionnel je ne me suis pas (encore) acharné à créer plein de types de tourelle et d'ennemie, mais c'est
dans l'idée assez rapide. J'ai mis en place le fait que les ennemies pourront (une fois codé) attaquer les tours.