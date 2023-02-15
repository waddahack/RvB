# RvB

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
8 tourelles différentes et 6 type d'ennemie différent !
