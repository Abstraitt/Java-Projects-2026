# Groupe 3 — Plateforme de quiz en ligne avec mode temps réel

Projet de programmation Java — Session normale Summer 2026, ICT University.

**Présenté par :**
- Monkam Kevin Duran — ICTU20241801
- Nassourou Ali — ICTU20251264
- Emoune Massoma Éric Joyce — ICTU20251298

## Description

Application console permettant à un enseignant de créer un quiz (questions à choix
multiples, vrai/faux ou réponse courte) et à des étudiants d'y répondre en direct, avec
un temps limite par question, calcul automatique des scores, classement trié et export
des résultats.

## Contenu du dossier

| Fichier | Description |
| --- | --- |
| `QuizPlatform.java` | Code source complet du programme |
| `Groupe3-Quiz-Presentation.pptx` | Présentation du projet |
| `Groupe3-Quiz-Rapport.docx` | Rapport détaillé (objectif, conception, utilisation) |

## Fonctionnalités principales

- Création, modification et suppression de questions
- Trois types de questions : choix multiples, vrai/faux, réponse courte
- Ordre des questions défini ou aléatoire
- Temps limite configurable par question
- Calcul automatique du score et classement trié par score décroissant
- Export des résultats au format CSV

## Compilation et exécution

```bash
javac QuizPlatform.java
java QuizPlatform
```

## Utilisation

1. Choisir l'option **5** pour charger un jeu de questions d'exemple
2. Choisir l'option **3** pour vérifier les questions chargées
3. Choisir l'option **6** pour lancer une session et répondre au quiz
4. Choisir l'option **7** pour afficher le classement
5. Choisir l'option **8** pour exporter les résultats dans `resultats.csv`

## Structures de données

Toutes les données sont stockées dans des tableaux : énoncés, types de question,
propositions de réponse, bonnes réponses, points, ainsi que les noms et scores des
étudiants ayant participé à une session.
