# LeitnerBox — Spécifications fonctionnelles

**Version** : 1.1 — Draft  
**Langue de développement** : Français

---

## Table des matières

1. [Bonnes pratiques de développement](#1-bonnes-pratiques-de-développement)
2. [Glossaire métier](#2-glossaire-métier)
3. [Fonctionnalités](#3-fonctionnalités)
4. [Interface utilisateur](#4-interface-utilisateur)

---

## 1. Bonnes pratiques de développement

Ces principes constituent le socle technique du projet. Toute décision d'implémentation devra s'y conformer.

### 1.1 Architecture — Clean Architecture

Séparation stricte des responsabilités en trois couches :

- **Couche Présentation** (UI / ViewModel) : affichage et gestion d'état.
- **Couche Domaine** (Use Cases, entités) : logique métier pure, sans dépendance externe.
- **Couche Données** (Repository, sources de données) : accès aux données locales ou distantes.

Le cœur métier (domaine) ne dépend d'aucune autre couche. Les dépendances sont orientées vers l'intérieur. Le pattern **MVVM** est appliqué côté UI.

### 1.2 Séparation stricte du code métier

Le code métier (règles de planification, calcul des dates de sessions, déplacement des cartes entre les boîtes, algorithme de vérification des réponses, gestion de l'état "maîtrisée") doit être **intégralement isolé dans la couche Domaine**, sous forme de Use Cases et d'entités pures.

Règles à respecter :

- Aucun Use Case ne dépend d'Android, de Room, de Compose ou de toute autre bibliothèque externe.
- Les Use Cases sont des classes Kotlin pures, testables unitairement sans émulateur.
- Un ViewModel ne contient pas de logique métier : il orchestre les Use Cases et expose l'état à l'UI.
- Un composable Compose ne contient pas de logique métier : il observe l'état et délègue les actions au ViewModel.

### 1.3 UI et gestion d'état

- **Jetpack Compose** comme framework UI déclaratif.
- Flux d'état unidirectionnel (**UDF / MVI-like**) : l'état est immuable et descend du ViewModel vers les composables.
- **State hoisting** systématique.
- Le ViewModel est la source de vérité unique pour l'état de l'écran.
- Aucune logique métier dans les composables.

### 1.4 Concurrence et asynchronisme

- Coroutines Kotlin : `suspend`, `Flow`, `StateFlow`.
- Scopes appropriés : `viewModelScope`, `lifecycleScope`.
- `GlobalScope` interdit.
- Gestion explicite des erreurs via `try/catch`, `Result` ou **sealed classes**.
- Respect de la cancellation des coroutines.

### 1.5 Injection de dépendances

- **Hilt** (basé sur Dagger) comme standard DI.
- Objectifs : découplage, testabilité, lisibilité.

### 1.6 Données et persistance

- **Repository pattern** : source de vérité unique.
- **Room** pour la persistance locale.
- Approche **offline-first** : l'app fonctionne sans réseau.

### 1.7 Qualité du code

- Kotlin idiomatique : `data class`, `sealed class`, fonctions d'extension, null safety.
- Principes **SOLID** et **KISS** : classes courtes, responsabilités claires.
- **Modularisation** par feature et par couche (`feature:*`, `core:*`, `data:*`).

### 1.8 Performance

- Éviter les recompositions inutiles dans Compose (`remember`, `derivedStateOf`, clés stables).
- **Paging** si les listes deviennent grandes.
- Profiling régulier (CPU, mémoire).

### 1.9 Sécurité

- Aucun secret stocké en clair.
- Utilisation du **Android Keystore** pour les données sensibles.
- HTTPS obligatoire.

### 1.10 Internationalisation

- Toutes les chaînes de caractères dans les fichiers `res/values/strings.xml`. Aucun texte en dur dans le code.
- Le fichier `res/values-fr/strings.xml` est la langue de développement de référence.
- L'app suit la **langue système Android**, pas un paramètre interne.
- Les traductions futures (EN, ES, etc.) n'impliquent que l'ajout de nouveaux fichiers de ressources, sans modification du code.

---

## 2. Glossaire métier

Avant de décrire les fonctionnalités, voici les définitions des termes utilisés dans tout le document.

### Carte

Unité élémentaire d'apprentissage. Une carte est composée :

- d'une **question** (recto), affichée à l'utilisateur en début de tour,
- d'une **réponse** (verso), révélée après retournement ou saisie,
- d'une option **saisieRequise** (booléen) indiquant si l'app vérifie automatiquement la réponse,
- d'un état **maîtrisée** (booléen) indiquant que la carte a quitté le circuit de révision.

### Deck

Ensemble thématique de cartes (ex : "Anglais", "Histoire", "Kotlin"). Chaque deck est indépendant et possède sa propre configuration : nombre de boîtes, intervalles de révision, règle de mauvaise réponse, ordre de présentation des cartes.

### Boîte

Niveau de maîtrise à l'intérieur d'un deck. Les cartes progressent de boîte en boîte au fil des révisions. Chaque boîte possède un **intervalle de révision** (en jours) qui détermine la fréquence à laquelle ses cartes sont présentées à l'utilisateur.

### Planning d'une boîte

Date de prochaine session associée à une boîte. Toute boîte non vide possède toujours un planning, jamais nul et jamais dans le passé. Cette date est calculée en tenant compte des **jours exclus** définis dans les paramètres globaux. Un report décale cette date au jour ouvrable suivant, ce qui garantit que le planning est toujours à aujourd'hui ou dans le futur.

### Session de révision

Séquence de cartes présentées à l'utilisateur lors d'une révision. Une session correspond à une ou plusieurs boîtes dont le planning tombe à la date du jour. Elle peut porter sur un ou plusieurs decks.

### Évaluation

Action par laquelle l'utilisateur indique si sa réponse était bonne ou mauvaise, après avoir retourné une carte ou saisi sa réponse. Le résultat d'une évaluation détermine le déplacement de la carte dans les boîtes.

### Carte maîtrisée

Carte ayant obtenu une bonne réponse alors qu'elle se trouvait dans la dernière boîte du deck. Elle quitte le circuit de révision ordinaire et entre dans un état "maîtrisée". Elle peut être révisée via le **Challenge**.

### Challenge

Session de révision spéciale portant sur les cartes maîtrisées. Elle n'impacte pas le circuit de révision ordinaire (en V1).

### Jours exclus

Jours de la semaine (ex : samedi, dimanche) configurés dans les paramètres globaux et exclus du planning de révision. Ces jours sont sautés dans tous les calculs de dates de sessions, quel que soit le deck ou la boîte concernée. Comme aucune session n'est planifiée ces jours-là, aucune notification n'est envoyée.

> **Exemple :** jours exclus : samedi et dimanche. Carte en boîte 2 (intervalle 3 jours) révisée un vendredi. La prochaine session est planifiée le mercredi (et non le lundi), samedi et dimanche étant sautés.

### Règle de mauvaise réponse

Paramètre par deck déterminant le comportement lorsqu'une réponse est incorrecte :

- **Retour en boîte 1** : la carte repart depuis le début.
- **Retour à la boîte précédente** : la carte recule d'un niveau.

### Streak

Compteur de sessions consécutives réalisées sans aucun report. Affiché sur le dashboard à titre de motivation. **Règle unique : dès qu'une session fait l'objet d'un report, le streak est remis à zéro. Tant qu'aucun report n'intervient, le streak s'incrémente à chaque session réalisée.**

---

## 3. Fonctionnalités

### Règle transversale : calcul des dates de sessions

Toute date de prochaine session est calculée en tenant compte des **jours exclus** définis dans les paramètres globaux. Les jours exclus sont systématiquement sautés dans tous les calculs de dates, quels que soient le deck, la boîte ou le contexte (planification initiale, report, recalcul après session).

---

### Fonctionnalité 1 — Import / Export de cartes et de decks (CSV)

#### Import

- L'utilisateur importe un fichier CSV depuis le stockage local ou via un partage Android.
- Format attendu : une carte par ligne, colonnes : `question`, `réponse`, `saisieRequise` (booléen).
- L'import cible un deck existant ou crée un nouveau deck.
- **Validation du fichier :** si le fichier est mal formé (colonne manquante, encodage incorrect ou non reconnu), l'import est abandonné et un message d'erreur est affiché à l'utilisateur.
- **Détection des doublons :** un doublon est défini par une **question identique** (comparaison insensible à la casse et aux accents, après normalisation). La détection s'effectue :
  - au sein du fichier CSV lui-même (deux lignes avec la même question),
  - entre le fichier et le deck cible, si le deck existe déjà. Si le deck est nouveau (créé lors de l'import), seuls les doublons internes au fichier sont détectés.
- Le fichier est parcouru en intégralité avant toute opération d'écriture.
- Si au moins un doublon est détecté, l'app affiche la liste des doublons avec leur numéro de ligne et indique que le fichier doit être corrigé avant import. Aucune carte n'est importée.
- Si aucun doublon n'est détecté, les cartes sont créées en base de données.

#### Export

- L'utilisateur exporte un deck ou l'ensemble de ses cartes en CSV.
- Colonnes exportées : `question`, `réponse`, `saisieRequise`, `boîte courante`, `état maîtrisée`, `date dernière révision`.
- La configuration du deck (nombre de boîtes, intervalles) n'est pas exportée.
- Le fichier est partagé via l'intent Android standard (email, Drive, etc.).

---

### Fonctionnalité 2 — Gestion des decks

- L'utilisateur peut créer, modifier et supprimer des decks thématiques indépendants.
- Paramètres configurables par deck :
  - Nom du deck.
  - Nombre de boîtes et intervalles de révision par boîte (voir Fonctionnalité 3).
  - Règle de mauvaise réponse : retour en boîte 1 ou retour à la boîte précédente.
  - Ordre de présentation des cartes en session : **aléatoire** (défaut), **ordre de création**, **par boîte**.

---

### Fonctionnalité 3 — Configuration du système de boîtes

- Le nombre de boîtes est paramétrable par deck.
- Chaque boîte possède un intervalle de révision paramétrable indépendamment (en jours).
- Valeurs par défaut pour un nouveau deck : **5 boîtes**, intervalles de **1, 3, 5, 7 et 14 jours**.
- **V1 : la suppression de boîtes n'est pas disponible.** L'utilisateur peut modifier les intervalles des boîtes existantes et ajouter de nouvelles boîtes, mais ne peut pas en supprimer.

---

### Fonctionnalité 4 — Gestion des cartes

- Au sein d'un deck, l'utilisateur peut créer, modifier et supprimer des cartes.
- Chaque carte comporte une question (recto) et une réponse (verso).
- Option **saisieRequise** (booléen, défaut : `false`) :
  - Si `false` : l'utilisateur retourne la carte et s'auto-évalue (Bonne réponse / Mauvaise réponse).
  - Si `true` : l'utilisateur saisit sa réponse et l'app vérifie automatiquement.

#### Vérification automatique de réponse (`saisieRequise = true`)

La comparaison n'est jamais faite sur les chaînes brutes. Une normalisation est appliquée en amont sur les deux chaînes, puis la distance de Levenshtein est calculée.

##### Étape 1 : Normalisation des chaînes

La même fonction de normalisation est appliquée à la réponse attendue (au moment de la création ou modification de la carte) et à la saisie de l'utilisateur (à la volée lors de la session) :

1. Mise en minuscules.
2. Suppression des accents et diacritiques (ex : "é" → "e", "ç" → "c").
3. Suppression des caractères spéciaux non alphanumériques (ponctuation, apostrophes, tirets…).
4. `trim` : suppression des espaces en début et fin de chaîne.
5. Compression des espaces multiples en un seul espace.

##### Stockage de la réponse normalisée

À la création ou modification d'une carte, la réponse normalisée est calculée et persistée en base (champ `reponse_normalisee`), aux côtés de la réponse brute (utilisée pour l'affichage). Si la réponse est modifiée, la réponse normalisée est recalculée et mise à jour.

##### Étape 2 : Calcul du seuil de tolérance

Le seuil est calculé à partir de la longueur de la **réponse attendue normalisée** :

```
seuil = floor(longueur_réponse_normalisée / 5)
```

| Réponse attendue (normalisée) | Longueur | Seuil |
|---|---|---|
| "fe" | 2 | 0 — correspondance exacte |
| "azote" | 5 | 1 — 1 écart toléré |
| "photosynthese" | 13 | 2 — 2 écarts tolérés |
| "distance de levenshtein" | 23 | 4 — 4 écarts tolérés |

##### Étape 3 : Comparaison

```
distance = levenshtein(réponse_normalisée, saisie_normalisée)

si distance <= seuil  →  bonne réponse
sinon                 →  mauvaise réponse
```

##### Cas particuliers

- **Réponse attendue vide** : la vérification automatique est désactivée, la carte se comporte comme si `saisieRequise = false`.
- **Saisie vide de l'utilisateur** : réponse considérée comme incorrecte sans calcul.
- **Longueur 1 à 4** : seuil = 0, correspondance exacte requise après normalisation.

---

### Fonctionnalité 5 — Session de révision quotidienne

#### Planification

- Toute boîte non vide possède toujours une date de prochaine session, jamais nulle et jamais dans le passé.
- La date est calculée à partir de la date de la dernière session + l'intervalle de la boîte, en tenant compte des jours exclus.

#### Lancement d'une session

- L'app présente à l'utilisateur les decks ayant au moins une boîte planifiée à la date du jour.
- L'utilisateur sélectionne un ou plusieurs decks pour sa session.
- L'app regroupe les cartes à réviser depuis les decks sélectionnés, en ne retenant que les boîtes dont le planning est à la date du jour.
- Si plusieurs boîtes tombent le même jour (même deck ou decks différents), l'utilisateur choisit :
  - **Fusionner** les cartes en une session unique.
  - **Conserver** des sessions distinctes et en refuser certaines.
- En cas de fusion, chaque carte conserve les règles de son deck d'origine. La prochaine date de session de chaque boîte est recalculée indépendamment à partir de la date de la session fusionnée, selon l'intervalle propre à chaque boîte, en tenant compte des jours exclus.

#### Déroulement d'une session

- Les cartes sont présentées dans l'ordre défini par le paramètre du deck (aléatoire, création, par boîte).
- Un **indicateur de progression** est affiché en permanence (ex : "Carte 4 / 12").
- Pour chaque carte :
  - La question (recto) est affichée.
  - Si `saisieRequise = false` : l'utilisateur retourne la carte (animation de retournement) et s'auto-évalue via les boutons **Bonne réponse** / **Mauvaise réponse**, ou par **swipe gestuel** (droite = bonne réponse, gauche = mauvaise réponse).
  - Si `saisieRequise = true` : l'utilisateur saisit sa réponse. L'app vérifie automatiquement et affiche le résultat.
- L'utilisateur peut **annuler la dernière évaluation** (undo) s'il s'est trompé.

#### Déplacement des cartes

- **Bonne réponse** : la carte avance dans la boîte suivante.
- **Mauvaise réponse** : selon le paramètre du deck, la carte retourne en boîte 1 ou recule d'une boîte.
- Si une carte est dans la dernière boîte et que la réponse est bonne, elle passe dans l'état **maîtrisée** et quitte le circuit de révision. Un **effet de célébration** est déclenché (animation confetti légère).

#### Résultat de session

À la fin d'une session, un écran de résultat affiche :

- Nombre de cartes révisées.
- Taux de réussite.
- Nombre de cartes nouvellement maîtrisées.
- Si le taux de réussite est **supérieur à 60 %**, un **effet de célébration** est déclenché.

#### Report de session

- L'utilisateur peut refuser une session. La date de la boîte concernée est repoussée au jour ouvrable suivant (jours exclus respectés). **Le streak est remis à zéro.**
- Le report est au **niveau de la boîte** : refuser la boîte 2 n'impacte pas la planification de la boîte 3 du même deck.
- Un report peut se répéter indéfiniment. La date de session d'une boîte non vide ne peut jamais être nulle ni dans le passé.
- En cas de collision après report (deux boîtes le même jour), la logique de fusion décrite ci-dessus s'applique.
- En session multi-deck, le refus peut s'appliquer boîte par boîte.

> **Note :** il n'est pas possible de lancer une session si aucune boîte n'a de session planifiée à la date du jour. La fonctionnalité de session extraordinaire (choix libre d'un deck et d'une boîte) est prévue en V2.

---

### Fonctionnalité 6 — Notifications de rappel

- L'app envoie une notification si des cartes sont à réviser ce jour-là.
- Paramètre configurable global : horaire auquel la notification est déclenchée.

---

### Fonctionnalité 7 — Statistiques de progression

- Répartition des cartes par deck et par boîte.
- Taux de réussite par deck et par boîte.
- Taux de maîtrise par deck.
- Historique des sessions : date, deck(s) révisés, nombre de cartes, nombre de cartes maîtrisées lors de la session, taux de réussite.

---

### Fonctionnalité 8 — Paramètres globaux

- Jours exclus du planning de révision.
- Horaire de notification.
- Thème de l'application : **clair**, **sombre**, **suivre le système**.

> **Note :** la langue de l'interface suit la langue système Android. Il n'existe pas de paramètre de langue interne à l'app.

---

### Fonctionnalité 9 — Challenge sur les cartes maîtrisées

- L'utilisateur peut lancer un challenge de révision portant sur l'ensemble de ses cartes maîtrisées, tous decks confondus.
- Le déroulement est identique à une session de révision classique, avec la particularité suivante :
  - L'**indicateur de progression** affiche le nombre de cartes maîtrisées correctement répondues sur le total de cartes du challenge (ex : "Maîtrisées 3 / 20"), et non un simple compteur de cartes vues.
- **V1 :** en cas d'échec sur une carte maîtrisée, la carte reste dans l'état "maîtrisée". Le challenge n'a aucun impact sur le circuit de révision normal.
- **V2 (prévu) :** en cas d'échec, l'app proposera à l'utilisateur de remettre la carte dans une boîte de son choix pour la réintégrer dans le circuit.
- **V1 :** la sélection porte sur toutes les cartes maîtrisées.
- **V2 (prévu) :** l'utilisateur pourra affiner la sélection (par deck, par thème, etc.).

---

## 4. Interface utilisateur

### 4.1 Navigation

La navigation principale repose sur une **Bottom Navigation Bar** à 3 entrées :

| Entrée | Icône suggérée | Destination |
|---|---|---|
| Accueil | Maison | Dashboard |
| Decks | Cartes empilées | Liste des decks |
| Paramètres | Engrenage | Paramètres globaux |

Les écrans secondaires s'empilent en navigation hiérarchique (back stack) à partir de ces trois entrées.

---

### 4.2 Liste des écrans

#### Navigation principale

| # | Écran | Description |
|---|---|---|
| 1 | **Dashboard** | Résumé du jour : boîtes à réviser, streak, raccourci vers la session du jour. |
| 2 | **Statistiques globales** | Vue d'ensemble de la progression sur tous les decks. |

#### Decks

| # | Écran | Description |
|---|---|---|
| 3 | **Liste des decks** | Tous les decks de l'utilisateur, avec indicateur de cartes à réviser aujourd'hui. |
| 4 | **Création / Édition d'un deck** | Formulaire de configuration : nom, boîtes, intervalles, règle mauvaise réponse, ordre de présentation. |
| 5 | **Détail d'un deck** | Répartition des cartes par boîte, planning des prochaines sessions, accès aux cartes, export. |

#### Cartes

| # | Écran | Description |
|---|---|---|
| 6 | **Liste des cartes d'un deck** | Toutes les cartes, filtrables par boîte et par état (maîtrisée / en cours). |
| 7 | **Création / Édition d'une carte** | Formulaire : question, réponse, option saisieRequise. |
| 8 | **Import / Export CSV** | Interface de sélection du fichier, affichage des erreurs et doublons, partage. |

#### Sessions

| # | Écran | Description |
|---|---|---|
| 9 | **Sélection de session** | Choix des decks, visualisation des boîtes du jour, option de fusion. |
| 10 | **Session de révision** | Cœur de l'app : affichage recto, retournement animé, évaluation (swipe ou bouton), indicateur de progression, undo. Réutilisé pour le Challenge. |
| 11 | **Résultat de session** | Récapitulatif : taux de réussite, cartes maîtrisées, animation de célébration si applicable. |

#### Challenge

| # | Écran | Description |
|---|---|---|
| 12 | **Lancement du challenge** | Affichage du nombre de cartes maîtrisées disponibles, bouton de démarrage. Réutilise l'écran 10 pour la session. |

**Total : 12 écrans distincts.** L'écran de session (10) est réutilisé pour la révision ordinaire et le challenge.

---

### 4.3 Animation de retournement de carte

L'animation est implémentée en Jetpack Compose avec `graphicsLayer` et `animateFloatAsState`.

- **Déclencheur** : tap sur la carte.
- **Axe** : rotation sur l'axe Y (effet carte physique).
- **Durée** : 350 ms.
- **Masquage du contenu** : le texte est masqué lorsque la rotation atteint 90° (évite l'effet miroir). Le recto s'affiche de 0° à 89°, le verso de 91° à 180°.
- **Accessibilité** : un bouton "Retourner" textuel est également disponible pour les utilisateurs qui n'utilisent pas le tap gestuel.

---

### 4.4 Swipe gestuel sur les cartes

Pendant la session de révision, après retournement :

- **Swipe vers la droite** → Bonne réponse.
- **Swipe vers la gauche** → Mauvaise réponse.
- Un indicateur visuel (couleur verte / rouge) apparaît progressivement pendant le swipe pour guider l'utilisateur.
- Les boutons **Bonne réponse** / **Mauvaise réponse** restent visibles comme alternative au swipe.

---

### 4.5 Indicateur de progression

Affiché en permanence pendant une session :

- **Session de révision ordinaire** : format "Carte X / Y" avec une barre de progression linéaire. Mis à jour à chaque évaluation. En session fusionnée multi-boîtes, Y prend en compte toutes les cartes de la session.
- **Challenge** : format "Maîtrisées X / Y" où X est le nombre de cartes maîtrisées correctement répondues depuis le début du challenge, et Y le total de cartes du challenge.

---

### 4.6 Animations de célébration

Deux déclencheurs :

1. **Maîtrise d'une carte** : animation légère de confetti déclenchée immédiatement après l'évaluation positive sur la dernière boîte.
2. **Fin de session avec taux de réussite > 60 %** : animation de célébration sur l'écran de résultat.

L'animation est non bloquante (l'utilisateur peut continuer sans l'attendre).

---

### 4.7 Thèmes clair et sombre

- Implémenté avec **Material 3** : deux `ColorScheme` distincts (clair et sombre).
- Trois options dans les paramètres : **Clair**, **Sombre**, **Suivre le système** (défaut).
- Les couleurs, typographies et espacements sont définis dans le `MaterialTheme` centralisé et ne sont jamais codés en dur dans les composables.

---

### 4.8 Tailles de police dynamiques

- L'app respecte les préférences de taille de police définies dans les paramètres système Android.
- Toutes les tailles de texte sont définies en **sp** (pas en dp).
- Les mises en page sont conçues pour accommoder des textes plus longs sans troncature ni débordement (utilisation de composants scrollables si nécessaire).

---

### 4.9 Empty states

Chaque écran susceptible d'afficher une liste vide possède un **empty state soigné** :

| Contexte | Message suggéré |
|---|---|
| Aucun deck créé | "Créez votre premier deck pour commencer à apprendre !" |
| Deck sans cartes | "Ce deck est vide. Ajoutez des cartes ou importez un fichier CSV." |
| Aucune session planifiée aujourd'hui | "Rien à réviser aujourd'hui. Profitez-en pour ajouter de nouvelles cartes !" |
| Aucune carte maîtrisée | "Pas encore de cartes maîtrisées. Continuez vos révisions !" |
| Historique vide | "Aucune session réalisée pour l'instant." |

Les empty states incluent une illustration simple et un bouton d'action contextuel (ex : "Créer un deck", "Ajouter une carte").
