# Téléchargement :
 - [Version installable pour Windows](https://github.com/dalgwen/LesTablesDeDaniel/releases/download/1.0/LesTablesDeDaniel-setup.exe)
 - [Ou version en simple .jar](https://github.com/dalgwen/LesTablesDeDaniel/releases/download/1.0/LesTablesDeDaniel-1.0-jar-with-dependencies.jar)

# Les Tables De Daniel

## Introduction

Ce projet a été créé pour aider à rendre aléatoire le tirage de matchs entre équipes.
Comme il a été en premier lieu fait pour la belote, vous pouvez voir quelques références dans le code ainsi que quelques contraintes liées. Par exemple, l'utilisateur est capable de "forcer" un numéro de table pour une équipe. C'était une contrainte obligatoire de ce développement, car certains membres d'équipe peuvent avoir des problèmes de mobilité.

This project helps randomizing matchs between teams.
As it was primarily designed for "belote" match, you can see some reference to it and related constraints. For example, the user is able to force some table number on some team. It was mandatory in my developement because some team members can have mobility issue.

## How to use / Comment l'utiliser

Utilisez les boutons "Nouveau", "Charger" ou "Sauvegarder" pour créer, ouvrir ou sauvegarder les fichiers *.tdd (fichier de définition de joueurs). Vous pouvez alors :
 - changer les noms
 - spécifier une contrainte de table
 - éditer la table de joueur en ajoutant, supprimant, ou déplaçant les lignes grâce aux boutons sur la droite de la table.
 
Cliquez ensuite sur le bouton "C'est parti" pour obtenir les résultats dans un fichier texte ouvert dans une application tierce.

Use the New, Load, or Save button to create, load existing, or save *.tdd (players definition file)
Edit the table. You can  :
 - change username 
 - specify a table constraint
 - edit table by adding, deleting, moving line with the right button.

Then click the Go button to launch external file editor with the computed results.

## History / Histoire

Ce projet est une projet de "commande" proposé par mon beau-père. Qui s'appelle "Daniel", comme vous avez pu le deviner en lisant le titre.
C'était un projet à l'origine très simple (encore plus que maintenant) (pure ligne de commande, lecture / écriture de fichier).
Puis quelques connaissances ont manifesté de l'intérêt. J'ai donc nettoyé le code, ajouter une IHM avec JavaFX, et voilà.

This project starts as a "command" project from my father-in-law. "Daniel" as you could have guessed from the title.
It was pretty simple (pure command line and, file reading and writing).
Then some friends manifest interest. So I cleaned the code, added some GUI with JavaFX, and *voilà.*
