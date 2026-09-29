# Simulation de transactions bancaires sécurisées — Groupe 4

Projet 4 · Java · Session normale — ICT-University, Yaoundé, Niveau 2 Cybersécurité (section francophone).
Auteur : **Soumelong Jordan Landry** (ICTU20251294).

Application console Java simulant un système bancaire simple et sécurisé : création de comptes,
authentification, dépôts/retraits/virements, historique, détection de fraude, administration,
journalisation et export de relevés.

## Fonctionnalités

| Fonctionnalité | Classe responsable |
|---|---|
| Création de compte (numéro `CM00001…`, mot de passe haché, dépôt initial) | `Banque`, `Compte` |
| Authentification (blocage après 3 échecs) | `Banque`, `Compte`, `Securite` |
| Consultation du solde | `Main`, `Compte` |
| Dépôt / retrait | `Banque` |
| Virement entre comptes | `Banque` |
| Historique des transactions | `Banque` |
| Détection de fraude (montant élevé, fréquence anormale) | `Banque` |
| Administration (liste, transactions suspectes, blocage/déblocage) | `Main`, `Banque` |
| Journalisation (`journal.log`) | `Journal` |
| Export de relevé (`releve_NUMERO.txt`) | `Banque` |

## Architecture — six classes

```
Main       Menus et saisies console. Aucune logique bancaire : appelle Banque.
Banque     Cœur du système : tableaux Compte[100] / Transaction[1000], opérations,
           analyse de fraude, administration, export.
Compte     numero, titulaire, motDePasseHache, solde, bloque, echecs.
Transaction id, type (DEPOT/RETRAIT/VIREMENT), source, destination, montant, date, suspecte, motif.
Journal    Écrit chaque événement daté dans journal.log (ajout en fin de fichier).
Securite   Hache les mots de passe avec SHA-256.
```

Choix techniques : **Java 17+**, tableaux à taille fixe (imposés par le sujet, avec compteur
d'occupation et recherche séquentielle), `SHA-256` pour les mots de passe, `LocalDateTime`
pour l'horodatage, fichiers texte (`FileWriter`/`PrintWriter`) pour le journal et les relevés.

## Règles de détection de fraude

- **Montant élevé** : opération ≥ 500 000 FCFA.
- **Fréquence anormale** : 3 opérations sortantes ou plus du même compte au cours des
  60 dernières secondes déjà enregistrées ⇒ la suivante (ex. la 4ᵉ) est marquée suspecte.

Une transaction suspecte n'est **jamais supprimée** : elle reste visible dans l'historique,
marquée avec son motif, consultable par l'administrateur.

## Installation et exécution

Prérequis : JDK 17+ et Maven 3.8+.

```bash
git clone <url-du-depot>
cd banque-securisee
mvn package
java -jar target/banque-securisee-jar-with-dependencies.jar
```

Sans Maven (compilation directe) :
```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.ictu.banque.Main
```

Les fichiers `journal.log` et `releve_*.txt` sont créés à la racine du projet, à l'endroit
où la commande `java` est exécutée.

**Code administrateur par défaut : `admin123`** (modifiable dans `Main.java`,
constante `CODE_ADMIN`).

## Tests

```bash
mvn test
```

Les tests JUnit 5 (`src/test/java`) reproduisent le jeu d'essai du rapport (section 5) :
création de comptes, authentification, retrait refusé pour solde insuffisant, virement,
4ᵉ retrait rapide marqué suspect, montant élevé marqué suspect, blocage après 3 échecs,
export de relevé, administration (liste, blocage/déblocage).

## Guide d'utilisation rapide

1. Lancer le programme → **1. Créer un compte** pour chaque utilisateur.
2. **2. Se connecter** avec le numéro (`CM00001`) et le mot de passe choisi.
3. Dans l'espace client : consulter le solde, déposer, retirer, virer, voir l'historique,
   exporter son relevé.
4. **3. Administration** (code `admin123`) : lister les comptes, voir les transactions
   suspectes, bloquer/débloquer un compte.

## Limites connues et perspectives

- Tableaux de taille fixe (100 comptes, 1000 transactions) ; données perdues à la fermeture
  (pas de persistance entre deux exécutions).
- Détection de fraude limitée à deux règles simples.
- Interface console uniquement.

Perspectives : `ArrayList`/base de données, `BCrypt`, blocage automatique des opérations
suspectes, sauvegarde des comptes entre exécutions, interface graphique JavaFX.

## Structure du dépôt

```
src/main/java/com/ictu/banque/   Code source (6 classes)
src/test/java/com/ictu/banque/   Tests JUnit 5
.github/workflows/ci.yml         Intégration continue (build + tests, Java 17 et 21)
pom.xml                          Build Maven
```
