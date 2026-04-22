# LeitnerBox — Roadmap de développement

\---

## Phase 0 — Mise en place du projet

> \*\*Socle\*\*

* Structure des modules Gradle (`:app`, `:core:domain`, `:core:data`, `:core:ui`, `:feature:\*`)
* Hilt — configuration globale (`@HiltAndroidApp`, modules de base)
* Navigation Compose — skeleton (NavHost, routes vides)
* Thème Material 3 (clair / sombre / système)
* Bottom Navigation Bar (3 entrées)
* CI basique (lint + tests)

**Livrable :** app qui se lance, navigue entre 3 onglets vides, thème fonctionnel.

\---

## Phase 1 — Couche Domaine

> \*\*`:core:domain`\*\*

* Entités métier (`Deck`, `Box`, `Card`, `Session`, `SessionPlan`…)
* Enums et sealed classes (`WrongAnswerRule`, `CardOrder`, `ImportResult`, `AnswerCheckResult`…)
* Interfaces Repository (`DeckRepository`, `BoxRepository`, `CardRepository`, `SessionRepository`, `SettingsRepository`)
* `AnswerNormalizer`
* `LevenshteinCalculator`
* `NextSessionDateCalculator`
* Tous les Use Cases (`CreateDeckUseCase`, `EvaluateCardUseCase`, `CheckAnswerUseCase`, `GetDailySessionPlanUseCase`…)
* Tests unitaires exhaustifs sur l'ensemble de la couche

**Livrable :** couche domaine 100 % testée, zéro dépendance Android — base immuable pour la suite.

\---

## Phase 2 — Couche Données

> \*\*`:core:data`\*\*

* Entités Room + DAOs (`DeckEntity`, `BoxEntity`, `CardEntity`, `SessionEntity`)
* `LeitnerDatabase` (Room, migrations, TypeConverters)
* Proto DataStore (paramètres globaux)
* Mappers domaine ↔ entité
* Implémentations Repository (`DeckRepositoryImpl`, `BoxRepositoryImpl`…)
* Modules Hilt data (`DatabaseModule`, `RepositoryModule`, `DataStoreModule`)
* Tests Room (base in-memory)

**Livrable :** persistance opérationnelle, repositories branchés sur le domaine, vérifiée par tests.

\---

## Phase 3 — Decks et cartes

> \*\*`:feature:decks` / `:feature:cards`\*\*

* Liste des decks + empty state
* Création / édition d'un deck (nom, règle mauvaise réponse, ordre de présentation)
* Détail d'un deck (répartition par boîte, planning)
* Configuration des boîtes (intervalles, ajout de boîte — pas de suppression en V1)
* Liste des cartes + filtres (par boîte, par état)
* Création / édition d'une carte (`saisieRequise`, calcul `answerNormalized` à la sauvegarde)
* Navigation complète entre les écrans

**Livrable :** première version utilisable — on peut créer des decks et y ajouter des cartes.

\---

## Phase 4 — Session de révision

> \*\*`:feature:session` — cœur de l'app\*\*

* Écran de sélection de session (decks du jour, option de fusion)
* `FlipCard` — animation de retournement (Compose, axe Y, 350 ms)
* Swipe gestuel (droite = bonne réponse, gauche = mauvaise, overlay coloré progressif)
* Évaluation et déplacement des cartes (règle par deck)
* `saisieRequise = true` — vérification automatique via Levenshtein
* Undo (annulation de la dernière évaluation)
* Indicateur de progression ("Carte X / Y")
* Passage à l'état maîtrisée (dernière boîte + bonne réponse)
* Report de session (au niveau de la boîte, streak remis à zéro)
* Écran de résultat (taux de réussite, cartes maîtrisées)

**Livrable :** boucle d'apprentissage complète et fonctionnelle — l'app tient sa promesse principale.

\---

## Phase 5 — Dashboard et paramètres

> \*\*`:feature:dashboard` / `:feature:settings`\*\*

* Dashboard (résumé du jour, streak, raccourci vers la session)
* Écran paramètres (jours exclus, horaire de notification, thème)
* `SessionReminderWorker` (WorkManager, notification quotidienne conditionnelle)
* `NotificationHelper` + canal Android (8+)
* Demande de permission `POST\_NOTIFICATIONS` (Android 13+)
* Tailles de police dynamiques (toutes les tailles en `sp`)

**Livrable :** app autonome — elle notifie, respecte les jours exclus, applique les préférences.

\---

## Phase 6 — Statistiques

> \*\*`:feature:stats`\*\*

* Répartition des cartes par deck et par boîte
* Taux de réussite par deck et par boîte
* Taux de maîtrise par deck
* Historique des sessions (date, decks, cartes, taux de réussite)
* Empty state historique

**Livrable :** l'utilisateur peut mesurer sa progression dans le temps.

\---

## Phase 7 — Import / Export CSV

> \*\*`:feature:importexport`\*\*

* `CsvParser` (RFC 4180, gestion encodage, colonnes manquantes)
* `CsvExporter`
* Détection des doublons (même question, après normalisation)
* Gestion fichier mal formé (message d'erreur, import bloqué)
* Partage via intent Android standard (SAF — Storage Access Framework)
* Écran Import / Export (sélection fichier, affichage des doublons, partage)

**Livrable :** échange de données avec l'extérieur (partage entre utilisateurs, sauvegarde).

\---

## Phase 8 — Challenge

> \*\*`:feature:challenge`\*\*

* Écran de lancement du challenge (nombre de cartes maîtrisées disponibles)
* Réutilisation de l'écran de session (`:feature:session`)
* Indicateur de progression spécifique : "Maîtrisées X / Y"
* Règle V1 : la carte reste maîtrisée en cas d'échec

**Livrable :** fonctionnalité bonus sur les cartes maîtrisées, réutilisant le socle session.

\---

## Phase 9 — Polish et qualité

> \*\*V1 finale\*\*

* Animations Lottie : confetti légère (maîtrise d'une carte) + confetti complète (fin de session > 60 %)
* Illustrations vectorielles pour les empty states
* Tests UI Compose (écran de session, flip, swipe)
* Profiling (CPU, mémoire, recompositions Compose inutiles)
* Accessibilité (TalkBack, contrastes, tailles dynamiques)
* Revue statique Detekt + ktlint
* Vérification internationalisation (aucun texte en dur, strings français complets)

**Livrable :** app V1 distribuable — stable, accessible, performante.

\---

### Remarques :

#### Pourquoi le domaine avant tout (phase 1) :

c'est la décision la plus importante de l'architecture. En codant et testant tous les Use Cases avant d'avoir une seule ligne d'UI, tu valides la logique métier (calcul des dates, Levenshtein, déplacement des cartes) de façon isolée. Si quelque chose est faux là, c'est trivial à corriger. Si tu le découvres en phase 4, tu dois déboguer à travers trois couches.

#### Pourquoi les données avant l'UI (phase 2) :

les ViewModels dépendent des Use Cases, qui dépendent des repositories. Avoir les repositories réels (même sans UI) permet de tester les flux end-to-end dès la phase 3.

#### La phase 4 est le cœur :

c'est elle qui donne envie d'utiliser l'app. Les phases 6, 7 et 8 sont des enrichissements importants mais non bloquants — quelqu'un pourrait utiliser l'app après la phase 5.

#### La phase 9 n'est pas optionnelle :

le polish, les tests UI et le profiling sont souvent relégués en "si le temps le permet". Ici ils ont leur propre phase pour que ça reste une vraie priorité avant toute distribution.

