# LeitnerBox — Plan de tests

**Version** : 1.0  
**Lien** : Roadmap v1.0, Spécifications fonctionnelles v1.1

Conventions utilisées dans ce document :

- **UT** — Test unitaire (JUnit 5 + MockK, module `:core:domain`, sans émulateur)
- **IT** — Test d'intégration (Room in-memory, DataStore in-memory, avec émulateur ou Robolectric)
- **UI** — Test d'interface (Compose Testing, avec émulateur)

---

## Phase 0 — Mise en place du projet

**Objectif :** vérifier que le squelette de l'application est correct avant d'écrire une seule ligne de logique métier.

### UI — Lancement et navigation

| ID | Description | Résultat attendu |
|---|---|---|
| P0-UI-01 | Lancer l'app sur un émulateur propre | L'app démarre sans crash |
| P0-UI-02 | Vérifier la Bottom Navigation Bar | 3 onglets visibles : Accueil, Decks, Paramètres |
| P0-UI-03 | Taper sur chaque onglet | L'écran correspondant s'affiche, l'onglet actif est mis en surbrillance |
| P0-UI-04 | Naviguer entre les onglets avec le bouton Retour | Le back stack est géré correctement (pas de sortie intempestive) |
| P0-UI-05 | Lancer l'app avec le thème système clair | Le thème clair s'applique |
| P0-UI-06 | Lancer l'app avec le thème système sombre | Le thème sombre s'applique |
| P0-UI-07 | Modifier la taille de police système à "très grande" | L'app ne crash pas, les textes s'adaptent sans débordement visible |

---

## Phase 1 — Couche Domaine

**Objectif :** valider l'intégralité de la logique métier de façon isolée, avant toute persistance ou interface.

### UT — AnswerNormalizer

| ID | Description | Résultat attendu |
|---|---|---|
| P1-UT-01 | Normaliser `"Éléphant"` | `"elephant"` |
| P1-UT-02 | Normaliser `"  Bonjour  Monde  "` | `"bonjour monde"` |
| P1-UT-03 | Normaliser `"C'est l'été !"` | `"cest lete"` |
| P1-UT-04 | Normaliser `"42"` | `"42"` |
| P1-UT-05 | Normaliser une chaîne vide `""` | `""` |
| P1-UT-06 | Normaliser `"ñoño"` | `"nono"` |
| P1-UT-07 | Normaliser `"  "` (espaces seuls) | `""` |

### UT — LevenshteinCalculator

| ID | Description | Résultat attendu |
|---|---|---|
| P1-UT-10 | `distance("chat", "chat")` | `0` |
| P1-UT-11 | `distance("chat", "chats")` | `1` |
| P1-UT-12 | `distance("azote", "azoze")` | `1` |
| P1-UT-13 | `distance("", "abc")` | `3` |
| P1-UT-14 | `distance("abc", "")` | `3` |
| P1-UT-15 | `distance("", "")` | `0` |
| P1-UT-16 | `threshold(2)` (longueur 1-4) | `0` |
| P1-UT-17 | `threshold(5)` | `1` |
| P1-UT-18 | `threshold(13)` | `2` |
| P1-UT-19 | `threshold(23)` | `4` |
| P1-UT-20 | `isMatch("azote", "azoze")` — seuil 1 | `true` |
| P1-UT-21 | `isMatch("fe", "fa")` — seuil 0 | `false` |
| P1-UT-22 | `isMatch("", "abc")` — réponse attendue vide | `false` (cas particulier : désactivation) |

### UT — NextSessionDateCalculator

| ID | Description | Résultat attendu |
|---|---|---|
| P1-UT-30 | Intervalle 1 jour, aucun jour exclu, depuis un lundi | Mardi |
| P1-UT-31 | Intervalle 3 jours, aucun jour exclu, depuis un lundi | Jeudi |
| P1-UT-32 | Intervalle 3 jours, samedi+dimanche exclus, depuis un vendredi | Mercredi suivant |
| P1-UT-33 | Intervalle 1 jour, samedi+dimanche exclus, depuis un vendredi | Lundi suivant |
| P1-UT-34 | Intervalle 1 jour, samedi+dimanche exclus, depuis un samedi | Doit lever une exception ou être impossible (samedi est un jour exclu, pas une date de départ valide) |
| P1-UT-35 | Intervalle 7 jours, tous les jours de la semaine exclus | Doit lever une exception (aucun jour ouvrable disponible) |
| P1-UT-36 | Intervalle 14 jours, samedi+dimanche exclus, depuis un lundi | Dans 14 jours ouvrables = résultat correct |

### UT — CheckAnswerUseCase

| ID | Description | Résultat attendu |
|---|---|---|
| P1-UT-40 | Carte `saisieRequise = false`, saisie quelconque | `AnswerCheckResult.AutoCheckDisabled` |
| P1-UT-41 | Carte `saisieRequise = true`, réponse attendue vide | `AnswerCheckResult.AutoCheckDisabled` |
| P1-UT-42 | Saisie vide, réponse attendue non vide | `AnswerCheckResult.Incorrect` |
| P1-UT-43 | Saisie correcte (distance = 0) | `AnswerCheckResult.Correct` |
| P1-UT-44 | Saisie avec faute de frappe dans le seuil (ex : "azoze" pour "azote") | `AnswerCheckResult.Correct` |
| P1-UT-45 | Saisie trop éloignée (hors seuil) | `AnswerCheckResult.Incorrect` |
| P1-UT-46 | Saisie avec accents différents de la réponse ("éléphant" vs "elephant") | `AnswerCheckResult.Correct` (normalisation préalable) |
| P1-UT-47 | Saisie en majuscules ("AZOTE") | `AnswerCheckResult.Correct` (normalisation préalable) |

### UT — EvaluateCardUseCase

| ID | Description | Résultat attendu |
|---|---|---|
| P1-UT-50 | Bonne réponse, carte en boîte 1, deck à 5 boîtes | Carte déplacée en boîte 2 |
| P1-UT-51 | Bonne réponse, carte en boîte 4, deck à 5 boîtes | Carte déplacée en boîte 5 |
| P1-UT-52 | Bonne réponse, carte en boîte 5 (dernière), deck à 5 boîtes | `EvaluationResult.isMastered = true`, carte quitte le circuit |
| P1-UT-53 | Mauvaise réponse, règle `BACK_TO_BOX_ONE`, carte en boîte 3 | Carte déplacée en boîte 1 |
| P1-UT-54 | Mauvaise réponse, règle `PREVIOUS_BOX`, carte en boîte 3 | Carte déplacée en boîte 2 |
| P1-UT-55 | Mauvaise réponse, règle `PREVIOUS_BOX`, carte déjà en boîte 1 | Carte reste en boîte 1 |
| P1-UT-56 | Bonne réponse, la date de la boîte cible est recalculée avec jours exclus | Nouvelle date = date session + intervalle boîte (jours exclus respectés) |

### UT — GetDailySessionPlanUseCase

| ID | Description | Résultat attendu |
|---|---|---|
| P1-UT-60 | Aucune boîte planifiée aujourd'hui | `SessionPlan` avec liste vide |
| P1-UT-61 | Une boîte planifiée aujourd'hui | `SessionPlan` avec 1 `BoxSession` |
| P1-UT-62 | Deux boîtes du même deck planifiées aujourd'hui | `SessionPlan` avec 2 `BoxSession` |
| P1-UT-63 | Boîte planifiée demain | Non incluse dans le plan |
| P1-UT-64 | Boîte vide planifiée aujourd'hui | Non incluse (boîte sans cartes) |

### UT — PostponeBoxSessionUseCase

| ID | Description | Résultat attendu |
|---|---|---|
| P1-UT-70 | Reporter une boîte planifiée un lundi, aucun jour exclu | Nouvelle date = mardi |
| P1-UT-71 | Reporter une boîte planifiée un vendredi, samedi+dimanche exclus | Nouvelle date = lundi suivant |
| P1-UT-72 | Reporter une boîte non vide | La date n'est jamais nulle après le report |

---

## Phase 2 — Couche Données

**Objectif :** vérifier que la persistance, les requêtes et les mappings sont corrects.

### IT — DAOs Room (base in-memory)

| ID | Description | Résultat attendu |
|---|---|---|
| P2-IT-01 | Insérer un `DeckEntity`, le relire par ID | Données identiques |
| P2-IT-02 | Insérer 3 decks, appeler `getAllDecks()` | Flow émet une liste de 3 decks triés par nom |
| P2-IT-03 | Supprimer un deck | Les boîtes et cartes associées sont supprimées en cascade (FK `CASCADE`) |
| P2-IT-04 | Insérer une `CardEntity` avec un `box_id` valide | Insertion réussie |
| P2-IT-05 | Supprimer une boîte référencée par des cartes | Les cartes passent à `box_id = null` (FK `SET_NULL`) |
| P2-IT-06 | Appeler `getBoxesDueForSession(date)` avec une date passée | Retourne uniquement les boîtes non vides dont `next_session_date <= date` |
| P2-IT-07 | Appeler `getMasteredCards()` | Retourne uniquement les cartes avec `is_mastered = true` |
| P2-IT-08 | Modifier une carte et relire | Les champs modifiés (dont `answer_normalized`) sont mis à jour |
| P2-IT-09 | Insérer une session et lire l'historique | Flow émet la session dans l'ordre décroissant |

### IT — Mappers

| ID | Description | Résultat attendu |
|---|---|---|
| P2-IT-20 | `DeckEntity.toDomain()` puis `.toEntity()` | Objet initial et objet final identiques (round-trip) |
| P2-IT-21 | `CardEntity.toDomain()` round-trip | Identique |
| P2-IT-22 | `BoxEntity` avec `next_session_date = null` → domaine | `Box.nextSessionDate = null` |
| P2-IT-23 | `Box` avec `nextSessionDate` non nulle → entité | Date sérialisée en ISO-8601 correct |

### IT — SettingsRepository (DataStore)

| ID | Description | Résultat attendu |
|---|---|---|
| P2-IT-30 | Lire les settings sans fichier existant | Valeurs par défaut retournées |
| P2-IT-31 | Mettre à jour les jours exclus et relire | Nouveaux jours persistés |
| P2-IT-32 | Mettre à jour le thème et relire | Valeur persistée |
| P2-IT-33 | Mettre à jour l'horaire de notification et relire | Heure et minute persistées |

---

## Phase 3 — Decks et cartes

**Objectif :** vérifier les écrans de gestion des decks et des cartes, y compris la navigation et les états vides.

### UT — ViewModels

| ID | Description | Résultat attendu |
|---|---|---|
| P3-UT-01 | `DeckListViewModel` — repository vide | `uiState.decks` vide, `isLoading = false` |
| P3-UT-02 | `DeckEditViewModel.saveDeck()` — champs valides | `CreateDeckUseCase` appelé, événement navigation émis |
| P3-UT-03 | `DeckEditViewModel.saveDeck()` — nom vide | Erreur de validation, `CreateDeckUseCase` non appelé |
| P3-UT-04 | `CardEditViewModel.saveCard()` — `saisieRequise = true`, réponse non vide | `answerNormalized` calculé et transmis |
| P3-UT-05 | `CardEditViewModel.saveCard()` — question vide | Erreur de validation |

### UI — Liste des decks

| ID | Description | Résultat attendu |
|---|---|---|
| P3-UI-01 | Ouvrir l'onglet Decks sans deck créé | Empty state affiché avec message et bouton "Créer un deck" |
| P3-UI-02 | Taper "Créer un deck" depuis l'empty state | Écran de création affiché |
| P3-UI-03 | Créer un deck, revenir à la liste | Le deck apparaît dans la liste |
| P3-UI-04 | Créer 3 decks | Ils apparaissent triés par nom |
| P3-UI-05 | Appui long ou swipe sur un deck | Option de suppression disponible |
| P3-UI-06 | Supprimer un deck | Disparaît de la liste ; si c'était le dernier, empty state réaffiché |

### UI — Création / édition d'un deck

| ID | Description | Résultat attendu |
|---|---|---|
| P3-UI-10 | Ouvrir l'écran de création | 5 boîtes pré-remplies avec intervalles 1, 3, 5, 7, 14 jours |
| P3-UI-11 | Laisser le nom vide et valider | Message d'erreur affiché, pas de navigation |
| P3-UI-12 | Ajouter une 6e boîte | Elle apparaît avec un intervalle à saisir |
| P3-UI-13 | Modifier l'intervalle d'une boîte | Valeur mise à jour |
| P3-UI-14 | Sélectionner la règle "Retour à la boîte précédente" | Option cochée |
| P3-UI-15 | Valider avec un nom et des intervalles valides | Navigation vers le détail du deck |

### UI — Liste des cartes

| ID | Description | Résultat attendu |
|---|---|---|
| P3-UI-20 | Ouvrir un deck sans cartes | Empty state affiché avec message et bouton "Ajouter une carte" |
| P3-UI-21 | Créer une carte `saisieRequise = false` | Carte visible dans la liste, badge boîte 1 |
| P3-UI-22 | Créer une carte `saisieRequise = true` | Indicateur visible sur la carte |
| P3-UI-23 | Filtrer par boîte 1 | Seules les cartes de la boîte 1 affichées |
| P3-UI-24 | Filtrer par état "maîtrisée" — aucune carte maîtrisée | Liste vide avec message approprié |
| P3-UI-25 | Modifier une carte existante | Modifications sauvegardées et visibles |
| P3-UI-26 | Supprimer une carte | Elle disparaît de la liste |

---

## Phase 4 — Session de révision

**Objectif :** valider le cœur fonctionnel de l'app — la boucle d'apprentissage complète.

### UT — SessionViewModel

| ID | Description | Résultat attendu |
|---|---|---|
| P4-UT-01 | Initialiser une session avec 5 cartes | `uiState.cards.size = 5`, `currentIndex = 0` |
| P4-UT-02 | `onFlip()` | `uiState.isFlipped` passe à `true` |
| P4-UT-03 | `onFlip()` deux fois | Retour à `isFlipped = false` |
| P4-UT-04 | `onEvaluate(true)` — bonne réponse | `currentIndex` incrémenté, `EvaluateCardUseCase` appelé avec `isCorrect = true` |
| P4-UT-05 | `onEvaluate(false)` — mauvaise réponse | `EvaluateCardUseCase` appelé avec `isCorrect = false` |
| P4-UT-06 | `onEvaluate()` sur la dernière carte | Événement `SessionComplete` émis |
| P4-UT-07 | `onUndo()` après une évaluation | `currentIndex` décrémenté, la carte revient à son état précédent |
| P4-UT-08 | `onUndo()` sur la première carte (pas d'historique) | Aucun effet |
| P4-UT-09 | Bonne réponse sur la dernière boîte | Événement `CardMastered` émis |

### UI — Sélection de session

| ID | Description | Résultat attendu |
|---|---|---|
| P4-UI-01 | Ouvrir la sélection sans session planifiée aujourd'hui | Message "Rien à réviser aujourd'hui" |
| P4-UI-02 | Une boîte planifiée aujourd'hui | Deck correspondant affiché, sélectionnable |
| P4-UI-03 | Deux boîtes du même deck aujourd'hui | Option "Fusionner" proposée |
| P4-UI-04 | Deux boîtes de decks différents aujourd'hui | Option "Fusionner" proposée |
| P4-UI-05 | Refuser une boîte | Boîte retirée de la liste, streak visuellement remis à zéro |
| P4-UI-06 | Valider la sélection | Navigation vers l'écran de session |

### UI — Session de révision (`saisieRequise = false`)

| ID | Description | Résultat attendu |
|---|---|---|
| P4-UI-10 | Affichage initial d'une carte | Recto (question) visible, verso masqué |
| P4-UI-11 | Taper sur la carte | Animation de retournement déclenchée (350 ms), verso affiché |
| P4-UI-12 | Taper sur le bouton "Retourner" | Même résultat que P4-UI-11 (accessibilité) |
| P4-UI-13 | Swipe vers la droite après retournement | Évaluation "bonne réponse", carte suivante affichée |
| P4-UI-14 | Swipe vers la gauche après retournement | Évaluation "mauvaise réponse", carte suivante affichée |
| P4-UI-15 | Swipe incomplet (< 40% de la largeur) | Carte revient à sa position (spring animation) |
| P4-UI-16 | Swipe vers la droite — overlay | Couleur verte visible et proportionnelle au déplacement |
| P4-UI-17 | Swipe vers la gauche — overlay | Couleur rouge visible et proportionnelle |
| P4-UI-18 | Taper "Bonne réponse" | Même résultat que P4-UI-13 |
| P4-UI-19 | Taper "Mauvaise réponse" | Même résultat que P4-UI-14 |
| P4-UI-20 | Indicateur de progression | Affiche "Carte 1 / N" puis "Carte 2 / N"… à chaque évaluation |
| P4-UI-21 | Taper "Annuler" après une évaluation | Carte précédente réaffichée, indicateur décrémenté |

### UI — Session de révision (`saisieRequise = true`)

| ID | Description | Résultat attendu |
|---|---|---|
| P4-UI-30 | Affichage d'une carte `saisieRequise = true` | Champ de saisie affiché à la place des boutons bonne/mauvaise réponse |
| P4-UI-31 | Saisir la bonne réponse (exacte) et valider | Résultat "correct" affiché, carte avance |
| P4-UI-32 | Saisir une réponse dans le seuil Levenshtein | Résultat "correct" |
| P4-UI-33 | Saisir une réponse hors seuil | Résultat "incorrect", réponse attendue affichée |
| P4-UI-34 | Valider avec champ vide | Résultat "incorrect" |

### UI — Maîtrise et résultat

| ID | Description | Résultat attendu |
|---|---|---|
| P4-UI-40 | Bonne réponse sur une carte en dernière boîte | Animation confetti légère déclenchée |
| P4-UI-41 | Fin de session avec taux > 60 % | Écran résultat + animation confetti complète |
| P4-UI-42 | Fin de session avec taux ≤ 60 % | Écran résultat sans animation |
| P4-UI-43 | Vérifier l'écran de résultat | Affiche : nb cartes, taux de réussite, nb nouvellement maîtrisées |

---

## Phase 5 — Dashboard et paramètres

**Objectif :** vérifier le dashboard, les paramètres globaux et l'envoi des notifications.

### UT — DashboardViewModel

| ID | Description | Résultat attendu |
|---|---|---|
| P5-UT-01 | Aucune session planifiée aujourd'hui | `uiState.sessionsToday = 0` |
| P5-UT-02 | Streak à 0 | `uiState.streak = 0` |
| P5-UT-03 | Streak incrémenté après une session sans report | `uiState.streak` augmente de 1 |
| P5-UT-04 | Streak remis à zéro après un report | `uiState.streak = 0` |

### UI — Dashboard

| ID | Description | Résultat attendu |
|---|---|---|
| P5-UI-01 | Ouvrir le dashboard sans activité | Streak = 0, message "Rien à réviser aujourd'hui" |
| P5-UI-02 | Session planifiée aujourd'hui | Raccourci vers la session visible |
| P5-UI-03 | Taper le raccourci session | Navigation vers l'écran de sélection de session |

### UI — Paramètres

| ID | Description | Résultat attendu |
|---|---|---|
| P5-UI-10 | Ouvrir les paramètres | Jours exclus, horaire notification et thème affichés avec leurs valeurs courantes |
| P5-UI-11 | Cocher samedi et dimanche comme jours exclus | Valeur persistée, vérifiable en rouvrant les paramètres |
| P5-UI-12 | Modifier l'horaire de notification | Nouvel horaire persisté |
| P5-UI-13 | Changer le thème en "Sombre" | Thème appliqué immédiatement à toute l'app |
| P5-UI-14 | Relancer l'app après modification des paramètres | Paramètres conservés |

### IT — SessionReminderWorker

| ID | Description | Résultat attendu |
|---|---|---|
| P5-IT-01 | Exécuter le worker avec des cartes à réviser aujourd'hui | Notification émise |
| P5-IT-02 | Exécuter le worker sans cartes à réviser | Aucune notification |
| P5-IT-03 | Exécuter le worker un jour exclu | Aucune notification (pas de session planifiée ce jour) |
| P5-IT-04 | Worker retourne `Result.success()` dans tous les cas | Pas de retry intempestif |

### UI — Notifications

| ID | Description | Résultat attendu |
|---|---|---|
| P5-UI-20 | Premier lancement sur Android 13+ | Dialogue de demande de permission `POST_NOTIFICATIONS` affiché |
| P5-UI-21 | Permission accordée | Notification planifiée |
| P5-UI-22 | Permission refusée | Pas de crash, notification silencieusement désactivée |
| P5-UI-23 | Taper sur la notification | App ouverte sur l'écran de sélection de session |

---

## Phase 6 — Statistiques

**Objectif :** vérifier l'exactitude des calculs et l'affichage des données de progression.

### UT — GetStatsUseCase

| ID | Description | Résultat attendu |
|---|---|---|
| P6-UT-01 | Aucune session en historique | Taux de réussite = 0, cartes maîtrisées = 0 |
| P6-UT-02 | 10 cartes révisées, 7 bonnes réponses | Taux de réussite = 70 % |
| P6-UT-03 | Répartition par boîte : 5 cartes boîte 1, 3 en boîte 2, 2 maîtrisées | Comptages corrects par boîte |
| P6-UT-04 | Taux de maîtrise : 2 cartes maîtrisées sur 10 totales | 20 % |
| P6-UT-05 | Historique de 3 sessions | Liste de 3 entrées triées par date décroissante |

### UI — Écran statistiques

| ID | Description | Résultat attendu |
|---|---|---|
| P6-UI-01 | Aucune session réalisée | Empty state historique affiché |
| P6-UI-02 | Répartition cartes par boîte | Graphique ou tableau correct pour chaque deck |
| P6-UI-03 | Taux de réussite par deck | Valeur correcte par rapport aux données en base |
| P6-UI-04 | Taux de maîtrise par deck | Valeur correcte |
| P6-UI-05 | Historique des sessions | Chaque ligne affiche date, decks, cartes, maîtrisées, taux |

---

## Phase 7 — Import / Export CSV

**Objectif :** vérifier la robustesse du parsing, la détection des doublons et l'export.

### UT — CsvParser

| ID | Description | Résultat attendu |
|---|---|---|
| P7-UT-01 | Fichier valide, 3 lignes | 3 `CsvCard` retournées |
| P7-UT-02 | Valeur `saisieRequise` = `"true"` | `saisieRequise = true` |
| P7-UT-03 | Valeur `saisieRequise` = `"false"` | `saisieRequise = false` |
| P7-UT-04 | Réponse avec virgule entre guillemets RFC 4180 | Parsing correct |
| P7-UT-05 | Colonne manquante | `IllegalArgumentException` avec numéro de ligne |
| P7-UT-06 | Fichier vide | Liste vide retournée |
| P7-UT-07 | Encodage UTF-8 valide avec accents | Parsing correct |
| P7-UT-08 | Encodage non reconnu | Exception avec message clair |

### UT — ImportCsvUseCase

| ID | Description | Résultat attendu |
|---|---|---|
| P7-UT-20 | Fichier sans doublon, deck existant vide | `ImportResult.Success(imported=N, skipped=0, replaced=0)` |
| P7-UT-21 | Doublon interne au fichier (même question, deux lignes) | `ImportResult.DuplicatesFound` avec numéros de lignes |
| P7-UT-22 | Doublon avec le deck cible (même question qu'une carte existante) | `ImportResult.DuplicatesFound` |
| P7-UT-23 | Deck cible nouveau — doublon interne détecté | `ImportResult.DuplicatesFound` |
| P7-UT-24 | Deck cible nouveau — aucun doublon | Import réussi, cartes créées |
| P7-UT-25 | Doublon : comparaison insensible à la casse ("Chat" vs "chat") | Doublon détecté |
| P7-UT-26 | Doublon : comparaison insensible aux accents ("Éléphant" vs "elephant") | Doublon détecté |
| P7-UT-27 | Fichier mal formé | `ImportResult.Error` |

### UT — CsvExporter

| ID | Description | Résultat attendu |
|---|---|---|
| P7-UT-30 | Exporter une carte sans virgule | Ligne CSV correcte |
| P7-UT-31 | Exporter une carte dont la réponse contient une virgule | Valeur entre guillemets RFC 4180 |
| P7-UT-32 | Exporter une carte maîtrisée | `état maîtrisée = true`, `boîte courante` vide |
| P7-UT-33 | Exporter une carte non révisée | `date dernière révision` vide |
| P7-UT-34 | Export puis re-import du fichier généré | Les données correspondent aux originales |

### UI — Écran Import / Export

| ID | Description | Résultat attendu |
|---|---|---|
| P7-UI-01 | Sélectionner un fichier CSV valide sans doublon | Import déclenché, confirmation affichée |
| P7-UI-02 | Sélectionner un fichier avec doublon | Liste des doublons affichée avec numéros de lignes, import bloqué |
| P7-UI-03 | Sélectionner un fichier mal formé | Message d'erreur affiché, import annulé |
| P7-UI-04 | Exporter un deck | Intent de partage Android déclenché |
| P7-UI-05 | Importer vers un nouveau deck | Deck créé, cartes importées |

---

## Phase 8 — Challenge

**Objectif :** vérifier le comportement spécifique du challenge sur les cartes maîtrisées.

### UT — ChallengeViewModel

| ID | Description | Résultat attendu |
|---|---|---|
| P8-UT-01 | Aucune carte maîtrisée | `uiState.masteredCount = 0`, bouton démarrer désactivé |
| P8-UT-02 | 10 cartes maîtrisées | `uiState.masteredCount = 10` |
| P8-UT-03 | Bonne réponse sur une carte maîtrisée | Carte reste maîtrisée, pas de déplacement en boîte |
| P8-UT-04 | Mauvaise réponse sur une carte maîtrisée (V1) | Carte reste maîtrisée, `isMastered` inchangé |
| P8-UT-05 | Indicateur de progression après 3 bonnes réponses sur 10 | `progressMastered = 3`, `total = 10` |

### UI — Lancement et déroulement du challenge

| ID | Description | Résultat attendu |
|---|---|---|
| P8-UI-01 | Ouvrir le challenge sans carte maîtrisée | Empty state "Pas encore de cartes maîtrisées" |
| P8-UI-02 | Ouvrir le challenge avec cartes maîtrisées | Nombre de cartes affiché, bouton démarrer actif |
| P8-UI-03 | Démarrer le challenge | Écran de session affiché avec les cartes maîtrisées |
| P8-UI-04 | Indicateur de progression pendant le challenge | Format "Maîtrisées X / Y" (et non "Carte X / Y") |
| P8-UI-05 | Répondre correctement à une carte | Indicateur X incrémenté |
| P8-UI-06 | Répondre incorrectement | Indicateur X non incrémenté, carte reste maîtrisée |
| P8-UI-07 | Fin du challenge | Écran de résultat affiché |

---

## Phase 9 — Polish et qualité

**Objectif :** vérifier la robustesse globale, les animations, l'accessibilité et les performances avant distribution.

### UI — Animations de célébration

| ID | Description | Résultat attendu |
|---|---|---|
| P9-UI-01 | Carte passant à l'état maîtrisée | Animation confetti légère déclenchée et non bloquante |
| P9-UI-02 | Session terminée avec taux > 60 % | Animation confetti complète sur l'écran de résultat |
| P9-UI-03 | Session terminée avec taux = 60 % | Aucune animation (seuil strictement supérieur) |
| P9-UI-04 | Animation en cours — l'utilisateur tape sur "Continuer" | Navigation immédiate, animation interrompue sans crash |

### UI — Accessibilité (TalkBack)

| ID | Description | Résultat attendu |
|---|---|---|
| P9-UI-10 | Activer TalkBack et naviguer dans la liste des decks | Chaque deck est annoncé avec son nom et son état |
| P9-UI-11 | Activer TalkBack sur l'écran de session | La question est lue, le bouton "Retourner" est accessible |
| P9-UI-12 | Contraste couleurs — thème clair | Ratio ≥ 4.5:1 pour tout texte de corps (WCAG AA) |
| P9-UI-13 | Contraste couleurs — thème sombre | Ratio ≥ 4.5:1 pour tout texte de corps |

### UI — Tailles de police dynamiques

| ID | Description | Résultat attendu |
|---|---|---|
| P9-UI-20 | Police système "très grande" — liste des decks | Textes lisibles, pas de débordement hors écran |
| P9-UI-21 | Police système "très grande" — session de révision | Question et réponse lisibles, boutons accessibles |
| P9-UI-22 | Police système "très grande" — écran résultat | Statistiques lisibles sans troncature |

### Performance — Compose

| ID | Description | Résultat attendu |
|---|---|---|
| P9-PERF-01 | Profiler les recompositions sur la liste des decks (Layout Inspector) | Aucune recomposition inutile au scroll |
| P9-PERF-02 | Profiler l'écran de session pendant 20 évaluations consécutives | Pas de dégradation de fluidité |
| P9-PERF-03 | Mesure mémoire sur une session de 50 cartes | Pas de croissance mémoire anormale |
| P9-PERF-04 | Temps de démarrage cold start | < 1 seconde sur un appareil milieu de gamme |

### Internationalisation

| ID | Description | Résultat attendu |
|---|---|---|
| P9-I18N-01 | Vérifier `strings.xml` — aucune chaîne en dur dans le code | Grep sur le code source : 0 occurrence de texte visible hardcodé |
| P9-I18N-02 | Passer la langue système en anglais | Tous les textes basculent en anglais (si traduction EN disponible) ou restent en français sans crash |
| P9-I18N-03 | Vérifier `values-fr/strings.xml` | Toutes les clés de `values/strings.xml` présentes |

### Qualité du code

| ID | Description | Résultat attendu |
|---|---|---|
| P9-QA-01 | Exécuter Detekt sur l'ensemble du projet | 0 violation bloquante |
| P9-QA-02 | Exécuter ktlint | 0 erreur de formatage |
| P9-QA-03 | Exécuter tous les tests unitaires | 100 % de tests verts |
| P9-QA-04 | Exécuter tous les tests d'intégration | 100 % de tests verts |
| P9-QA-05 | Exécuter tous les tests UI | 100 % de tests verts |
| P9-QA-06 | Vérifier la couverture de la couche domaine | ≥ 90 % de couverture ligne |
