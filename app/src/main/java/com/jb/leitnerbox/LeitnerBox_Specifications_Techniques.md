# LeitnerBox — Spécifications techniques

**Version** : 1.0 — Draft   
**Prérequis** : Lecture des spécifications fonctionnelles (v1.0)

\---

## Table des matières

1. [Stack technique](#1-stack-technique)
2. [Architecture et modularisation](#2-architecture-et-modularisation)
3. [Couche Domaine](#3-couche-domaine)
4. [Couche Données](#4-couche-données)
5. [Couche Présentation](#5-couche-présentation)
6. [Navigation](#6-navigation)
7. [Notifications](#7-notifications)
8. [Import / Export CSV](#8-import--export-csv)
9. [Thème et UI système](#9-thème-et-ui-système)
10. [Injection de dépendances](#10-injection-de-dépendances)
11. [Inventaire complet des fichiers](#11-inventaire-complet-des-fichiers)

\---

## 1\. Stack technique

|Composant|Bibliothèque|Version cible|
|-|-|-|
|Language|Kotlin|2.x|
|UI|Jetpack Compose + Material 3|BOM stable|
|Architecture|MVVM + Clean Architecture|—|
|DI|Hilt|2.x|
|Persistance locale|Room|2.x|
|Préférences|DataStore (Proto)|1.x|
|Async|Coroutines + Flow|1.8+|
|Navigation|Navigation Compose|2.x|
|Tâches de fond|WorkManager|2.x|
|Animations|Compose Animation + Lottie|—|
|Tests unitaires|JUnit 5 + MockK|—|
|Tests UI|Compose Testing|—|
|Lint / qualité|Detekt + ktlint|—|

\---

## 2\. Architecture et modularisation

### 2.1 Règle fondamentale des dépendances

```
:feature:\* → :core:domain  ← (aucune dépendance externe)
:feature:\* → :core:ui
:core:data → :core:domain
:app       → :feature:\*, :core:data, :core:domain
```

Le module `:core:domain` est un module Kotlin pur (pas de dépendance Android).  
Aucun module `:feature:\*` ne dépend de `:core:data` directement.

### 2.2 Structure des modules

```
app/
core/
  domain/          ← entités, interfaces repository, use cases, utilitaires métier
  data/            ← Room, DataStore, implémentations repository, mappers
  ui/              ← thème, composables partagés, animations
feature/
  dashboard/
  decks/
  cards/
  session/
  stats/
  settings/
  challenge/
  importexport/
```

### 2.3 Convention de nommage des packages

```
com.leitnerbox.{module}.{layer}
ex : com.leitnerbox.feature.session.ui
     com.leitnerbox.feature.session.viewmodel
     com.leitnerbox.core.domain.usecase
     com.leitnerbox.core.data.repository
```

\---

## 3\. Couche Domaine

### 3.1 Entités métier

Les entités du domaine sont des `data class` Kotlin pures, sans annotation Room.

```kotlin
// core/domain/model/Deck.kt
data class Deck(
    val id: Long = 0,
    val name: String,
    val wrongAnswerRule: WrongAnswerRule,
    val cardOrder: CardOrder,
    val createdAt: LocalDate
)

// core/domain/model/Box.kt
data class Box(
    val id: Long = 0,
    val deckId: Long,
    val position: Int,          // 1-based
    val intervalDays: Int,
    val nextSessionDate: LocalDate?   // null si la boîte est vide
)

// core/domain/model/Card.kt
data class Card(
    val id: Long = 0,
    val deckId: Long,
    val boxId: Long,
    val question: String,
    val answer: String,
    val answerNormalized: String,   // calculé et stocké à la création/modification
    val saisieRequise: Boolean,
    val isMastered: Boolean,
    val lastReviewedAt: LocalDate?,
    val createdAt: LocalDate
)

// core/domain/model/Session.kt
data class Session(
    val id: Long = 0,
    val date: LocalDate,
    val deckIds: List<Long>,
    val cardCount: Int,
    val successCount: Int,
    val masteredCount: Int
)

// core/domain/model/SessionPlan.kt
// Représente les boîtes planifiées pour aujourd'hui, avant lancement
data class SessionPlan(
    val boxSessions: List<BoxSession>
)

data class BoxSession(
    val box: Box,
    val deck: Deck,
    val cards: List<Card>
)

// core/domain/model/EvaluationResult.kt
data class EvaluationResult(
    val card: Card,
    val isCorrect: Boolean,
    val newBoxPosition: Int?,   // null si carte maîtrisée
    val isMastered: Boolean
)
```

### 3.2 Enums et sealed classes

```kotlin
// core/domain/model/WrongAnswerRule.kt
enum class WrongAnswerRule { BACK\_TO\_BOX\_ONE, PREVIOUS\_BOX }

// core/domain/model/CardOrder.kt
enum class CardOrder { RANDOM, CREATION\_DATE, BY\_BOX }

// core/domain/model/Theme.kt
enum class AppTheme { LIGHT, DARK, SYSTEM }

// core/domain/model/DuplicateResolution.kt
enum class DuplicateResolution { IGNORE, REPLACE }

// core/domain/model/ImportResult.kt
sealed class ImportResult {
    data class Success(val imported: Int, val skipped: Int, val replaced: Int) : ImportResult()
    data class DuplicatesFound(val duplicates: List<CardDuplicate>) : ImportResult()
    data class Error(val cause: Throwable) : ImportResult()
}

// core/domain/model/AnswerCheckResult.kt
sealed class AnswerCheckResult {
    object Correct : AnswerCheckResult()
    object Incorrect : AnswerCheckResult()
    object AutoCheckDisabled : AnswerCheckResult()
}
```

### 3.3 Interfaces Repository

```kotlin
// core/domain/repository/DeckRepository.kt
interface DeckRepository {
    fun getDecks(): Flow<List<Deck>>
    suspend fun getDeckById(id: Long): Deck?
    suspend fun createDeck(deck: Deck): Long
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(id: Long)
}

// core/domain/repository/BoxRepository.kt
interface BoxRepository {
    fun getBoxesForDeck(deckId: Long): Flow<List<Box>>
    suspend fun getBoxById(id: Long): Box?
    suspend fun createBox(box: Box): Long
    suspend fun updateBox(box: Box)
    suspend fun deleteBoxesForDeck(deckId: Long)
    suspend fun postponeBox(boxId: Long, excludedDays: Set<DayOfWeek>)
}

// core/domain/repository/CardRepository.kt
interface CardRepository {
    fun getCardsForDeck(deckId: Long): Flow<List<Card>>
    fun getCardsForBox(boxId: Long): Flow<List<Card>>
    fun getMasteredCards(): Flow<List<Card>>
    suspend fun getCardById(id: Long): Card?
    suspend fun createCard(card: Card): Long
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(id: Long)
    suspend fun moveCardToBox(cardId: Long, boxId: Long)
    suspend fun markCardAsMastered(cardId: Long, date: LocalDate)
}

// core/domain/repository/SessionRepository.kt
interface SessionRepository {
    fun getSessionHistory(): Flow<List<Session>>
    suspend fun saveSession(session: Session): Long
}

// core/domain/repository/SettingsRepository.kt
interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateExcludedDays(days: Set<DayOfWeek>)
    suspend fun updateNotificationTime(hour: Int, minute: Int)
    suspend fun updateTheme(theme: AppTheme)
}

// core/domain/model/AppSettings.kt
data class AppSettings(
    val excludedDays: Set<DayOfWeek>,
    val notificationHour: Int,
    val notificationMinute: Int,
    val theme: AppTheme
)
```

### 3.4 Use Cases

Chaque use case est une classe avec un seul point d'entrée (`operator fun invoke`), injectée via Hilt, sans dépendance Android.

#### Decks

```kotlin
// core/domain/usecase/deck/GetDecksUseCase.kt
class GetDecksUseCase @Inject constructor(private val repo: DeckRepository) {
    operator fun invoke(): Flow<List<Deck>> = repo.getDecks()
}

// core/domain/usecase/deck/CreateDeckUseCase.kt
class CreateDeckUseCase @Inject constructor(
    private val deckRepo: DeckRepository,
    private val boxRepo: BoxRepository
) {
    suspend operator fun invoke(deck: Deck, boxes: List<Box>): Result<Long>
    // Crée le deck puis crée toutes ses boîtes en transaction
}

// core/domain/usecase/deck/UpdateDeckUseCase.kt
// core/domain/usecase/deck/DeleteDeckUseCase.kt
// Suppression en cascade : deck → boîtes → cartes
```

#### Cartes

```kotlin
// core/domain/usecase/card/CreateCardUseCase.kt
class CreateCardUseCase @Inject constructor(
    private val cardRepo: CardRepository,
    private val normalizer: AnswerNormalizer
) {
    suspend operator fun invoke(card: Card): Result<Long>
    // Calcule answerNormalized avant persistance
}

// core/domain/usecase/card/UpdateCardUseCase.kt
// Recalcule answerNormalized si la réponse a changé

// core/domain/usecase/card/DeleteCardUseCase.kt
// core/domain/usecase/card/GetCardsForDeckUseCase.kt
```

#### Sessions

```kotlin
// core/domain/usecase/session/GetDailySessionPlanUseCase.kt
class GetDailySessionPlanUseCase @Inject constructor(
    private val boxRepo: BoxRepository,
    private val cardRepo: CardRepository,
    private val deckRepo: DeckRepository,
    private val settingsRepo: SettingsRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): SessionPlan
    // Retourne toutes les boîtes dont nextSessionDate <= date et non vides
}

// core/domain/usecase/session/EvaluateCardUseCase.kt
class EvaluateCardUseCase @Inject constructor(
    private val cardRepo: CardRepository,
    private val boxRepo: BoxRepository,
    private val settingsRepo: SettingsRepository,
    private val dateCalculator: NextSessionDateCalculator
) {
    suspend operator fun invoke(
        card: Card,
        isCorrect: Boolean,
        deck: Deck,
        allBoxes: List<Box>
    ): EvaluationResult
    // Applique la règle de mauvaise réponse du deck
    // Détecte la maîtrise (dernière boîte + bonne réponse)
    // Met à jour nextSessionDate de la boîte cible
}

// core/domain/usecase/session/PostponeBoxSessionUseCase.kt
class PostponeBoxSessionUseCase @Inject constructor(
    private val boxRepo: BoxRepository,
    private val settingsRepo: SettingsRepository,
    private val dateCalculator: NextSessionDateCalculator
) {
    suspend operator fun invoke(boxId: Long): Result<Unit>
    // Décale nextSessionDate de +1 jour ouvrable (jours exclus respectés)
}

// core/domain/usecase/session/SaveSessionUseCase.kt
// core/domain/usecase/session/GetSessionHistoryUseCase.kt
```

#### Vérification de réponse

```kotlin
// core/domain/usecase/card/CheckAnswerUseCase.kt
class CheckAnswerUseCase @Inject constructor(
    private val normalizer: AnswerNormalizer
) {
    operator fun invoke(card: Card, userInput: String): AnswerCheckResult
    // Normalise userInput, compare à card.answerNormalized via Levenshtein
}
```

#### Import / Export

```kotlin
// core/domain/usecase/importexport/ImportCsvUseCase.kt
class ImportCsvUseCase @Inject constructor(
    private val parser: CsvParser,
    private val cardRepo: CardRepository,
    private val normalizer: AnswerNormalizer
) {
    suspend operator fun invoke(
        uri: Uri,
        targetDeckId: Long,
        resolution: DuplicateResolution
    ): ImportResult
}

// core/domain/usecase/importexport/ExportCsvUseCase.kt
```

### 3.5 Utilitaires métier (domain)

```kotlin
// core/domain/util/AnswerNormalizer.kt
class AnswerNormalizer {
    fun normalize(input: String): String
    // 1. lowercase
    // 2. suppression diacritiques (Normalizer.normalize + regex \[^\\\\p{ASCII}])
    // 3. suppression caractères spéciaux non alphanumériques
    // 4. trim
    // 5. compression espaces multiples
}

// core/domain/util/LevenshteinCalculator.kt
object LevenshteinCalculator {
    fun distance(a: String, b: String): Int       // algorithme DP standard
    fun threshold(normalizedLength: Int): Int      // floor(length / 5)
    fun isMatch(expected: String, input: String): Boolean
}

// core/domain/util/NextSessionDateCalculator.kt
class NextSessionDateCalculator {
    fun compute(
        fromDate: LocalDate,
        intervalDays: Int,
        excludedDays: Set<DayOfWeek>
    ): LocalDate
    // Itère jour par jour en sautant les jours exclus
    // jusqu'à avoir compté intervalDays jours valides
}
```

\---

## 4\. Couche Données

### 4.1 Entités Room

```kotlin
// core/data/local/entity/DeckEntity.kt
@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val wrongAnswerRule: String,    // enum sérialisée en String
    val cardOrder: String,
    val createdAt: String           // ISO-8601 (LocalDate.toString())
)

// core/data/local/entity/BoxEntity.kt
@Entity(
    tableName = "boxes",
    foreignKeys = \[ForeignKey(
        entity = DeckEntity::class,
        parentColumns = \["id"],
        childColumns = \["deck\_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = \[Index("deck\_id")]
)
data class BoxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "deck\_id") val deckId: Long,
    val position: Int,
    @ColumnInfo(name = "interval\_days") val intervalDays: Int,
    @ColumnInfo(name = "next\_session\_date") val nextSessionDate: String?
)

// core/data/local/entity/CardEntity.kt
@Entity(
    tableName = "cards",
    foreignKeys = \[
        ForeignKey(entity = DeckEntity::class, parentColumns = \["id"],
            childColumns = \["deck\_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BoxEntity::class, parentColumns = \["id"],
            childColumns = \["box\_id"], onDelete = ForeignKey.SET\_NULL)
    ],
    indices = \[Index("deck\_id"), Index("box\_id")]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "deck\_id") val deckId: Long,
    @ColumnInfo(name = "box\_id") val boxId: Long?,
    val question: String,
    val answer: String,
    @ColumnInfo(name = "answer\_normalized") val answerNormalized: String,
    @ColumnInfo(name = "saisie\_requise") val saisieRequise: Boolean,
    @ColumnInfo(name = "is\_mastered") val isMastered: Boolean,
    @ColumnInfo(name = "last\_reviewed\_at") val lastReviewedAt: String?,
    @ColumnInfo(name = "created\_at") val createdAt: String
)

// core/data/local/entity/SessionEntity.kt
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    @ColumnInfo(name = "deck\_ids") val deckIds: String,  // JSON array sérialisé
    @ColumnInfo(name = "card\_count") val cardCount: Int,
    @ColumnInfo(name = "success\_count") val successCount: Int,
    @ColumnInfo(name = "mastered\_count") val masteredCount: Int
)
```

### 4.2 DAOs

```kotlin
// core/data/local/dao/DeckDao.kt
@Dao
interface DeckDao {
    @Query("SELECT \* FROM decks ORDER BY name ASC")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT \* FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Long): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(deck: DeckEntity): Long

    @Update
    suspend fun update(deck: DeckEntity)

    @Query("DELETE FROM decks WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// core/data/local/dao/BoxDao.kt
@Dao
interface BoxDao {
    @Query("SELECT \* FROM boxes WHERE deck\_id = :deckId ORDER BY position ASC")
    fun getBoxesForDeck(deckId: Long): Flow<List<BoxEntity>>

    @Query("SELECT \* FROM boxes WHERE id = :id")
    suspend fun getBoxById(id: Long): BoxEntity?

    @Query("SELECT \* FROM boxes WHERE next\_session\_date <= :date AND id IN (SELECT DISTINCT box\_id FROM cards WHERE is\_mastered = 0 AND box\_id IS NOT NULL)")
    suspend fun getBoxesDueForSession(date: String): List<BoxEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(box: BoxEntity): Long

    @Update
    suspend fun update(box: BoxEntity)

    @Query("DELETE FROM boxes WHERE deck\_id = :deckId")
    suspend fun deleteBoxesForDeck(deckId: Long)
}

// core/data/local/dao/CardDao.kt
@Dao
interface CardDao {
    @Query("SELECT \* FROM cards WHERE deck\_id = :deckId ORDER BY created\_at ASC")
    fun getCardsForDeck(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT \* FROM cards WHERE box\_id = :boxId")
    fun getCardsForBox(boxId: Long): Flow<List<CardEntity>>

    @Query("SELECT \* FROM cards WHERE is\_mastered = 1")
    fun getMasteredCards(): Flow<List<CardEntity>>

    @Query("SELECT \* FROM cards WHERE deck\_id = :deckId AND question = :question LIMIT 1")
    suspend fun findByQuestion(deckId: Long, question: String): CardEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// core/data/local/dao/SessionDao.kt
@Dao
interface SessionDao {
    @Query("SELECT \* FROM sessions ORDER BY date DESC")
    fun getSessionHistory(): Flow<List<SessionEntity>>

    @Insert
    suspend fun insert(session: SessionEntity): Long
}
```

### 4.3 Base de données Room

```kotlin
// core/data/local/LeitnerDatabase.kt
@Database(
    entities = \[DeckEntity::class, BoxEntity::class, CardEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LeitnerDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun boxDao(): BoxDao
    abstract fun cardDao(): CardDao
    abstract fun sessionDao(): SessionDao
}

// core/data/local/Converters.kt
// TypeConverters Room pour LocalDate (String ISO-8601) et List<Long> (JSON)
```

### 4.4 DataStore (paramètres)

Utilisation de **Proto DataStore** pour typage fort et évolution de schéma.

```
// core/data/local/datastore/app\_settings.proto
syntax = "proto3";
message AppSettingsProto {
  repeated int32 excluded\_days = 1;   // DayOfWeek.value
  int32 notification\_hour = 2;
  int32 notification\_minute = 3;
  string theme = 4;
}
```

```kotlin
// core/data/local/datastore/AppSettingsSerializer.kt
object AppSettingsSerializer : Serializer<AppSettingsProto>

// core/data/local/datastore/AppSettingsDataStore.kt
// Wrapper exposant Flow<AppSettings> et les fonctions de mise à jour
```

### 4.5 Mappers

Un mapper par entité, sous forme d'extension functions ou de classes dédiées.

```kotlin
// core/data/mapper/DeckMapper.kt
fun DeckEntity.toDomain(): Deck
fun Deck.toEntity(): DeckEntity

// core/data/mapper/BoxMapper.kt
fun BoxEntity.toDomain(): Box
fun Box.toEntity(): BoxEntity

// core/data/mapper/CardMapper.kt
fun CardEntity.toDomain(): Card
fun Card.toEntity(): CardEntity

// core/data/mapper/SessionMapper.kt
fun SessionEntity.toDomain(): Session
fun Session.toEntity(): SessionEntity
```

### 4.6 Implémentations Repository

```kotlin
// core/data/repository/DeckRepositoryImpl.kt
class DeckRepositoryImpl @Inject constructor(
    private val dao: DeckDao,
    private val db: LeitnerDatabase     // pour les transactions multi-DAO
) : DeckRepository

// core/data/repository/BoxRepositoryImpl.kt
class BoxRepositoryImpl @Inject constructor(
    private val dao: BoxDao,
    private val settingsRepo: SettingsRepository,
    private val calculator: NextSessionDateCalculator
) : BoxRepository

// core/data/repository/CardRepositoryImpl.kt
class CardRepositoryImpl @Inject constructor(private val dao: CardDao) : CardRepository

// core/data/repository/SessionRepositoryImpl.kt
class SessionRepositoryImpl @Inject constructor(private val dao: SessionDao) : SessionRepository

// core/data/repository/SettingsRepositoryImpl.kt
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<AppSettingsProto>
) : SettingsRepository
```

\---

## 5\. Couche Présentation

### 5.1 Pattern UI State

Chaque écran expose un `UiState` immuable via `StateFlow`. Les événements one-shot (navigation, snackbar) transitent par un `Channel<UiEvent>` consommé côté composable.

```kotlin
// Exemple : feature/session/viewmodel/SessionViewModel.kt
data class SessionUiState(
    val cards: List<Card> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = false,
    val sessionResult: SessionResult? = null
)

sealed class SessionUiEvent {
    data class CardMastered(val card: Card) : SessionUiEvent()
    object SessionComplete : SessionUiEvent()
    data class ShowError(val message: String) : SessionUiEvent()
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val evaluateCard: EvaluateCardUseCase,
    private val saveSession: SaveSessionUseCase,
    private val checkAnswer: CheckAnswerUseCase
) : ViewModel() {
    private val \_uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = \_uiState.asStateFlow()

    private val \_events = Channel<SessionUiEvent>(Channel.BUFFERED)
    val events: Flow<SessionUiEvent> = \_events.receiveAsFlow()

    fun onFlip() { ... }
    fun onEvaluate(isCorrect: Boolean) { ... }
    fun onUndo() { ... }
}
```

### 5.2 ViewModels par écran

|ViewModel|Module feature|Rôle|
|-|-|-|
|`DashboardViewModel`|dashboard|Cartes du jour, streak, résumé|
|`DeckListViewModel`|decks|Liste des decks, indicateurs|
|`DeckDetailViewModel`|decks|Détail d'un deck, répartition par boîte|
|`DeckEditViewModel`|decks|Création et édition d'un deck|
|`CardListViewModel`|cards|Liste filtrée des cartes|
|`CardEditViewModel`|cards|Création et édition d'une carte|
|`SessionSelectionViewModel`|session|Plan du jour, fusion, sélection|
|`SessionViewModel`|session|Déroulement de la session, undo|
|`SessionResultViewModel`|session|Résultat, déclenchement célébration|
|`StatsViewModel`|stats|Statistiques globales et par deck|
|`SettingsViewModel`|settings|Paramètres globaux|
|`ChallengeViewModel`|challenge|Lancement challenge maîtrisées|
|`ImportExportViewModel`|importexport|Import CSV, gestion doublons, export|

### 5.3 Composables principaux

Chaque écran est un composable `\*Screen` qui observe le `UiState` et délègue les actions. Les composables internes sont sans état (state hoisting).

```kotlin
// Exemple de découpage pour l'écran de session
// feature/session/ui/SessionScreen.kt
@Composable
fun SessionScreen(viewModel: SessionViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Collecte des events one-shot
    SessionContent(
        uiState = uiState,
        onFlip = viewModel::onFlip,
        onEvaluate = viewModel::onEvaluate,
        onUndo = viewModel::onUndo
    )
}

// feature/session/ui/SessionContent.kt (préviewable, sans ViewModel)
@Composable
fun SessionContent(
    uiState: SessionUiState,
    onFlip: () -> Unit,
    onEvaluate: (Boolean) -> Unit,
    onUndo: () -> Unit
) { ... }

// feature/session/ui/FlipCard.kt
// Composable de la carte avec animation de retournement

// feature/session/ui/SwipeableCardWrapper.kt
// Wrapper gérant le swipe gestuel autour de FlipCard

// feature/session/ui/ProgressIndicator.kt
// Barre de progression + "Carte X / Y"
```

### 5.4 Animation de retournement

```kotlin
// core/ui/component/FlipCard.kt
@Composable
fun FlipCard(
    isFlipped: Boolean,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )
    Box(modifier.graphicsLayer { rotationY = rotation }) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) { back() }
        }
    }
}
```

### 5.5 Swipe gestuel

```kotlin
// core/ui/component/SwipeableCard.kt
// Utilise AnchoredDraggableState (Compose Foundation)
// Seuil de déclenchement : 40% de la largeur de l'écran
// Overlay coloré progressif : vert (droite) / rouge (gauche) via alpha proportionnel à l'offset
// Rappel à l'état initial si le seuil n'est pas atteint (spring animation)
```

### 5.6 Animations de célébration

```kotlin
// core/ui/component/CelebrationOverlay.kt
// Animation Lottie non bloquante, auto-dismiss après 2s
// Deux variantes : confetti léger (maîtrise carte) et confetti complet (fin session >60%)
// Paramètre : CelebrationType { CARD\_MASTERED, SESSION\_SUCCESS }
```

### 5.7 Empty States

```kotlin
// core/ui/component/EmptyState.kt
@Composable
fun EmptyState(
    illustration: Int,          // @DrawableRes
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
)
// Illustrations vectorielles (VectorDrawable) incluses dans :core:ui/res/drawable
```

\---

## 6\. Navigation

### 6.1 Structure

Application **Single Activity** (`MainActivity`). Un seul `NavHost` racine géré dans `:app`.

```kotlin
// app/navigation/LeitnerNavHost.kt
// app/navigation/Screen.kt

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object DeckList : Screen("decks")
    object DeckDetail : Screen("decks/{deckId}") {
        fun createRoute(deckId: Long) = "decks/$deckId"
    }
    object DeckEdit : Screen("decks/{deckId}/edit") {
        fun createRoute(deckId: Long?) = "decks/${deckId ?: "new"}/edit"
    }
    object CardList : Screen("decks/{deckId}/cards") {
        fun createRoute(deckId: Long) = "decks/$deckId/cards"
    }
    object CardEdit : Screen("decks/{deckId}/cards/{cardId}/edit") {
        fun createRoute(deckId: Long, cardId: Long?) = "decks/$deckId/cards/${cardId ?: "new"}/edit"
    }
    object ImportExport : Screen("decks/{deckId}/importexport") {
        fun createRoute(deckId: Long) = "decks/$deckId/importexport"
    }
    object SessionSelection : Screen("session/select")
    object Session : Screen("session/run")
    object SessionResult : Screen("session/result")
    object Stats : Screen("stats")
    object Challenge : Screen("challenge")
    object Settings : Screen("settings")
}
```

### 6.2 Bottom Navigation

```kotlin
// app/navigation/BottomNavItem.kt
enum class BottomNavItem(val screen: Screen, val labelRes: Int, val iconRes: Int) {
    DASHBOARD(Screen.Dashboard, R.string.nav\_home, R.drawable.ic\_home),
    DECKS(Screen.DeckList, R.string.nav\_decks, R.drawable.ic\_decks),
    SETTINGS(Screen.Settings, R.string.nav\_settings, R.drawable.ic\_settings)
}
```

\---

## 7\. Notifications

### 7.1 Architecture

Les notifications de rappel sont gérées via **WorkManager** avec un `PeriodicWorkRequest` quotidien.

```kotlin
// core/data/worker/SessionReminderWorker.kt
@HiltWorker
class SessionReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getDailyPlan: GetDailySessionPlanUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val plan = getDailyPlan()
        if (plan.boxSessions.isNotEmpty()) {
            notificationHelper.showReminderNotification(plan.boxSessions.size)
        }
        return Result.success()
    }
}

// core/data/worker/NotificationScheduler.kt
// Planifie / replanifie le PeriodicWorkRequest quand l'horaire change
// Contrainte : heure fixe simulée via initialDelay recalculé

// core/data/notification/NotificationHelper.kt
// Crée le NotificationChannel (Android 8+) et envoie la notification
```

### 7.2 Permissions

* `POST\_NOTIFICATIONS` requis sur Android 13+ (demande au premier lancement ou à l'activation des notifications dans les paramètres).

\---

## 8\. Import / Export CSV

```kotlin
// core/data/csv/CsvParser.kt
class CsvParser {
    // Parsing ligne par ligne avec gestion des guillemets RFC 4180
    // Retourne List<CsvCard> (question, réponse, saisieRequise)
    // Erreur si colonnes manquantes → IllegalArgumentException avec numéro de ligne
    fun parse(inputStream: InputStream): List<CsvCard>
}

// core/data/csv/CsvExporter.kt
class CsvExporter {
    // Génère le contenu CSV en mémoire (StringBuilder)
    // Échappe les virgules et guillemets (RFC 4180)
    fun export(cards: List<Card>): String
}

// core/data/csv/CsvCard.kt
data class CsvCard(
    val question: String,
    val answer: String,
    val saisieRequise: Boolean
)
```

L'accès au fichier se fait via `ContentResolver` + `Uri` (SAF — Storage Access Framework), sans permission `READ\_EXTERNAL\_STORAGE` sur Android 10+.

\---

## 9\. Thème et UI système

```kotlin
// core/ui/theme/Color.kt
// Palettes Material 3 : LightColorScheme, DarkColorScheme

// core/ui/theme/Type.kt
// Définition de la typographie Material 3
// Toutes les tailles en sp (respect des préférences système)

// core/ui/theme/LeitnerBoxTheme.kt
@Composable
fun LeitnerBoxTheme(
    theme: AppTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.SYSTEM -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = LeitnerTypography, content = content)
}
```

\---

## 10\. Injection de dépendances

```kotlin
// core/data/di/DatabaseModule.kt
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LeitnerDatabase

    @Provides fun provideDeckDao(db: LeitnerDatabase): DeckDao
    @Provides fun provideBoxDao(db: LeitnerDatabase): BoxDao
    @Provides fun provideCardDao(db: LeitnerDatabase): CardDao
    @Provides fun provideSessionDao(db: LeitnerDatabase): SessionDao
}

// core/data/di/DataStoreModule.kt
@Module @InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<AppSettingsProto>
}

// core/data/di/RepositoryModule.kt
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindDeckRepository(impl: DeckRepositoryImpl): DeckRepository
    @Binds abstract fun bindBoxRepository(impl: BoxRepositoryImpl): BoxRepository
    @Binds abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository
    @Binds abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
    @Binds abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

// core/data/di/WorkerModule.kt
@Module @InstallIn(SingletonComponent::class)
object WorkerModule {
    // HiltWorkerFactory binding
}

// app/di/AppModule.kt
// Dépendances applicatives transverses (context, etc.)
```

Les Use Cases utilisent `@Inject constructor` directement — pas besoin de module dédié.

\---

## 11\. Inventaire complet des fichiers

### Module `:app`

```
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── kotlin/com/leitnerbox/
│   │   ├── MainActivity.kt                  ← Single Activity, setup NavHost + thème
│   │   ├── LeitnerBoxApplication.kt         ← @HiltAndroidApp
│   │   ├── di/
│   │   │   └── AppModule.kt
│   │   └── navigation/
│   │       ├── LeitnerNavHost.kt            ← NavHost racine, BottomNavBar
│   │       ├── Screen.kt                    ← Sealed class de toutes les routes
│   │       └── BottomNavItem.kt
├── build.gradle.kts
└── proguard-rules.pro
```

### Module `:core:domain`

```
core/domain/
├── src/main/kotlin/com/leitnerbox/core/domain/
│   ├── model/
│   │   ├── Deck.kt
│   │   ├── Box.kt
│   │   ├── Card.kt
│   │   ├── Session.kt
│   │   ├── SessionPlan.kt
│   │   ├── BoxSession.kt
│   │   ├── EvaluationResult.kt
│   │   ├── AppSettings.kt
│   │   ├── WrongAnswerRule.kt
│   │   ├── CardOrder.kt
│   │   ├── AppTheme.kt
│   │   ├── DuplicateResolution.kt
│   │   ├── ImportResult.kt
│   │   └── AnswerCheckResult.kt
│   ├── repository/
│   │   ├── DeckRepository.kt
│   │   ├── BoxRepository.kt
│   │   ├── CardRepository.kt
│   │   ├── SessionRepository.kt
│   │   └── SettingsRepository.kt
│   ├── usecase/
│   │   ├── deck/
│   │   │   ├── GetDecksUseCase.kt
│   │   │   ├── CreateDeckUseCase.kt
│   │   │   ├── UpdateDeckUseCase.kt
│   │   │   └── DeleteDeckUseCase.kt
│   │   ├── card/
│   │   │   ├── GetCardsForDeckUseCase.kt
│   │   │   ├── CreateCardUseCase.kt
│   │   │   ├── UpdateCardUseCase.kt
│   │   │   ├── DeleteCardUseCase.kt
│   │   │   └── CheckAnswerUseCase.kt
│   │   ├── session/
│   │   │   ├── GetDailySessionPlanUseCase.kt
│   │   │   ├── EvaluateCardUseCase.kt
│   │   │   ├── PostponeBoxSessionUseCase.kt
│   │   │   ├── SaveSessionUseCase.kt
│   │   │   └── GetSessionHistoryUseCase.kt
│   │   ├── stats/
│   │   │   └── GetStatsUseCase.kt
│   │   └── importexport/
│   │       ├── ImportCsvUseCase.kt
│   │       └── ExportCsvUseCase.kt
│   └── util/
│       ├── AnswerNormalizer.kt
│       ├── LevenshteinCalculator.kt
│       └── NextSessionDateCalculator.kt
└── build.gradle.kts                         ← module Kotlin pur (pas de plugin Android)
```

### Module `:core:data`

```
core/data/
├── src/main/
│   ├── kotlin/com/leitnerbox/core/data/
│   │   ├── local/
│   │   │   ├── LeitnerDatabase.kt
│   │   │   ├── Converters.kt
│   │   │   ├── entity/
│   │   │   │   ├── DeckEntity.kt
│   │   │   │   ├── BoxEntity.kt
│   │   │   │   ├── CardEntity.kt
│   │   │   │   └── SessionEntity.kt
│   │   │   └── dao/
│   │   │       ├── DeckDao.kt
│   │   │       ├── BoxDao.kt
│   │   │       ├── CardDao.kt
│   │   │       └── SessionDao.kt
│   │   ├── datastore/
│   │   │   ├── AppSettingsSerializer.kt
│   │   │   └── AppSettingsDataStore.kt
│   │   ├── repository/
│   │   │   ├── DeckRepositoryImpl.kt
│   │   │   ├── BoxRepositoryImpl.kt
│   │   │   ├── CardRepositoryImpl.kt
│   │   │   ├── SessionRepositoryImpl.kt
│   │   │   └── SettingsRepositoryImpl.kt
│   │   ├── mapper/
│   │   │   ├── DeckMapper.kt
│   │   │   ├── BoxMapper.kt
│   │   │   ├── CardMapper.kt
│   │   │   └── SessionMapper.kt
│   │   ├── csv/
│   │   │   ├── CsvParser.kt
│   │   │   ├── CsvExporter.kt
│   │   │   └── CsvCard.kt
│   │   ├── worker/
│   │   │   ├── SessionReminderWorker.kt
│   │   │   └── NotificationScheduler.kt
│   │   ├── notification/
│   │   │   └── NotificationHelper.kt
│   │   └── di/
│   │       ├── DatabaseModule.kt
│   │       ├── DataStoreModule.kt
│   │       ├── RepositoryModule.kt
│   │       └── WorkerModule.kt
│   └── proto/
│       └── app\_settings.proto
└── build.gradle.kts
```

### Module `:core:ui`

```
core/ui/
├── src/main/
│   ├── kotlin/com/leitnerbox/core/ui/
│   │   ├── theme/
│   │   │   ├── Color.kt                     ← LightColorScheme, DarkColorScheme
│   │   │   ├── Type.kt                      ← LeitnerTypography (tailles en sp)
│   │   │   └── LeitnerBoxTheme.kt
│   │   └── component/
│   │       ├── FlipCard.kt                  ← Animation de retournement
│   │       ├── SwipeableCard.kt             ← Swipe gestuel + overlay coloré
│   │       ├── ProgressIndicator.kt         ← Barre + "Carte X / Y"
│   │       ├── CelebrationOverlay.kt        ← Animation Lottie confetti
│   │       ├── EmptyState.kt                ← Illustration + message + action
│   │       └── BottomNavBar.kt
│   └── res/
│       ├── drawable/
│       │   ├── ic\_home.xml
│       │   ├── ic\_decks.xml
│       │   ├── ic\_settings.xml
│       │   ├── empty\_state\_no\_decks.xml
│       │   ├── empty\_state\_no\_cards.xml
│       │   ├── empty\_state\_no\_session.xml
│       │   ├── empty\_state\_no\_mastered.xml
│       │   └── empty\_state\_no\_history.xml
│       └── raw/
│           ├── confetti\_light.json          ← Animation Lottie (maîtrise carte)
│           └── confetti\_full.json           ← Animation Lottie (fin session)
└── build.gradle.kts
```

### Modules `:feature:\*`

```
feature/dashboard/
├── src/main/kotlin/com/leitnerbox/feature/dashboard/
│   ├── ui/
│   │   ├── DashboardScreen.kt               ← Résumé du jour, streak, raccourci session
│   │   └── DashboardContent.kt
│   └── viewmodel/
│       ├── DashboardViewModel.kt
│       └── DashboardUiState.kt

feature/decks/
├── src/main/kotlin/com/leitnerbox/feature/decks/
│   ├── ui/
│   │   ├── DeckListScreen.kt
│   │   ├── DeckListContent.kt
│   │   ├── DeckDetailScreen.kt
│   │   ├── DeckDetailContent.kt
│   │   ├── DeckEditScreen.kt
│   │   └── DeckEditContent.kt
│   └── viewmodel/
│       ├── DeckListViewModel.kt
│       ├── DeckListUiState.kt
│       ├── DeckDetailViewModel.kt
│       ├── DeckDetailUiState.kt
│       ├── DeckEditViewModel.kt
│       └── DeckEditUiState.kt

feature/cards/
├── src/main/kotlin/com/leitnerbox/feature/cards/
│   ├── ui/
│   │   ├── CardListScreen.kt
│   │   ├── CardListContent.kt
│   │   ├── CardEditScreen.kt
│   │   └── CardEditContent.kt
│   └── viewmodel/
│       ├── CardListViewModel.kt
│       ├── CardListUiState.kt
│       ├── CardEditViewModel.kt
│       └── CardEditUiState.kt

feature/session/
├── src/main/kotlin/com/leitnerbox/feature/session/
│   ├── ui/
│   │   ├── SessionSelectionScreen.kt
│   │   ├── SessionSelectionContent.kt
│   │   ├── SessionScreen.kt                 ← Réutilisé pour Challenge
│   │   ├── SessionContent.kt
│   │   ├── SessionResultScreen.kt
│   │   └── SessionResultContent.kt
│   └── viewmodel/
│       ├── SessionSelectionViewModel.kt
│       ├── SessionSelectionUiState.kt
│       ├── SessionViewModel.kt
│       ├── SessionUiState.kt
│       ├── SessionResultViewModel.kt
│       └── SessionResultUiState.kt

feature/stats/
├── src/main/kotlin/com/leitnerbox/feature/stats/
│   ├── ui/
│   │   ├── StatsScreen.kt
│   │   └── StatsContent.kt
│   └── viewmodel/
│       ├── StatsViewModel.kt
│       └── StatsUiState.kt

feature/settings/
├── src/main/kotlin/com/leitnerbox/feature/settings/
│   ├── ui/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsContent.kt
│   └── viewmodel/
│       ├── SettingsViewModel.kt
│       └── SettingsUiState.kt

feature/challenge/
├── src/main/kotlin/com/leitnerbox/feature/challenge/
│   ├── ui/
│   │   ├── ChallengeScreen.kt
│   │   └── ChallengeContent.kt
│   └── viewmodel/
│       ├── ChallengeViewModel.kt
│       └── ChallengeUiState.kt

feature/importexport/
├── src/main/kotlin/com/leitnerbox/feature/importexport/
│   ├── ui/
│   │   ├── ImportExportScreen.kt
│   │   └── ImportExportContent.kt
│   └── viewmodel/
│       ├── ImportExportViewModel.kt
│       └── ImportExportUiState.kt
```

### Ressources globales

```
app/src/main/res/
├── values/
│   └── strings.xml                          ← Toutes les chaînes (langue par défaut)
├── values-fr/
│   └── strings.xml                          ← Chaînes françaises (langue de dev)
└── xml/
    ├── network\_security\_config.xml
    └── backup\_rules.xml
```

### Tests

```
core/domain/src/test/
├── usecase/
│   ├── EvaluateCardUseCaseTest.kt
│   ├── GetDailySessionPlanUseCaseTest.kt
│   ├── PostponeBoxSessionUseCaseTest.kt
│   └── CheckAnswerUseCaseTest.kt
└── util/
    ├── AnswerNormalizerTest.kt
    ├── LevenshteinCalculatorTest.kt
    └── NextSessionDateCalculatorTest.kt

core/data/src/test/
├── repository/
│   └── (tests Room avec in-memory database)
└── csv/
    ├── CsvParserTest.kt
    └── CsvExporterTest.kt

feature/session/src/androidTest/
└── ui/
    └── SessionScreenTest.kt                 ← Tests Compose UI
```

\---

*Fin des spécifications techniques v1.0*

