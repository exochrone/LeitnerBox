# LeitnerBox — User Stories Phase 4 : Session de révision

**Version** : 1.1  
**Lien** : Spécifications fonctionnelles v1.1, Roadmap v1.0

---

## US-01 — Voir les boîtes planifiées aujourd'hui et choisir sur quoi réviser

**Formulation**

> En tant qu'utilisateur, je veux voir quelles boîtes sont planifiées pour aujourd'hui, afin de savoir ce que j'ai à réviser et de démarrer ma session.

**Description de l'écran**

L'écran de sélection de session est accessible depuis le dashboard (raccourci) ou depuis l'onglet Decks. Il affiche la liste des decks ayant au moins une boîte planifiée à la date du jour. Pour chaque deck, les boîtes du jour sont listées avec leur numéro, le nombre de cartes qu'elles contiennent et leur intervalle de révision.

Si aucune boîte n'est planifiée aujourd'hui, un empty state est affiché ("Rien à réviser aujourd'hui").

Un bouton "Démarrer" en bas de l'écran est actif dès qu'au moins une boîte est sélectionnée.

**Exemple**

> Deck "Anglais" — Boîte 1 (12 cartes), Boîte 3 (4 cartes)  
> Deck "Kotlin" — Boîte 2 (7 cartes)

**Critères d'acceptation**

- [ ] Seuls les decks ayant au moins une boîte planifiée aujourd'hui sont affichés.
- [ ] Chaque boîte affiche son numéro et le nombre de cartes qu'elle contient.
- [ ] Toutes les boîtes sont sélectionnées par défaut.
- [ ] L'utilisateur peut décocher individuellement des boîtes.
- [ ] Le bouton "Démarrer" est désactivé si aucune boîte n'est sélectionnée.
- [ ] Si aucune boîte n'est planifiée aujourd'hui, l'empty state est affiché et le bouton "Démarrer" est absent.
- [ ] Le nombre total de cartes à réviser est affiché (somme des boîtes sélectionnées).

**Implémentation technique**

*Bonne pratique appliquée : le ViewModel n'injecte que des Use Cases, jamais des repositories directement.*

**Use Case existant à utiliser**

`GetDailySessionPlanUseCase` — retourne un `Flow<SessionPlan>` contenant la liste des `SessionPlanItem` (deck + numéro de boîte + nombre de cartes) dont la date de planning est aujourd'hui.

**UiState à créer**

```kotlin
// feature/session/src/main/.../session/selection/SessionSelectionUiState.kt

data class SessionSelectionUiState(
    val items: List<SelectableBoxItem> = emptyList(),
    val isLoading: Boolean = true
)

data class SelectableBoxItem(
    val planItem: SessionPlanItem,
    val isSelected: Boolean = true
)

val SessionSelectionUiState.totalSelectedCards: Int
    get() = items.filter { it.isSelected }.sumOf { it.planItem.cardCount }

val SessionSelectionUiState.canStart: Boolean
    get() = items.any { it.isSelected }
```

**ViewModel à créer**

```kotlin
// feature/session/src/main/.../session/selection/SessionSelectionViewModel.kt

@HiltViewModel
class SessionSelectionViewModel @Inject constructor(
    private val getDailySessionPlan: GetDailySessionPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionSelectionUiState())
    val uiState: StateFlow<SessionSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getDailySessionPlan().collect { plan ->
                _uiState.update {
                    it.copy(
                        items = plan.items.map { item -> SelectableBoxItem(item) },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onBoxToggled(item: SelectableBoxItem) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it == item) it.copy(isSelected = !it.isSelected) else it
            })
        }
    }
}
```

**Écran à créer**

```
feature/session/src/main/.../session/selection/SessionSelectionScreen.kt
feature/session/src/main/.../session/selection/SessionSelectionContent.kt
```

`SessionSelectionScreen` observe le `uiState` et délègue à `SessionSelectionContent` (prévisualisable, sans ViewModel). L'empty state utilise le composable `EmptyState` de `:core:ui`.

**Navigation**

Ajouter dans `Screen.kt` :
```kotlin
object SessionSelection : Screen("session/selection")
```

Ajouter la route dans `LeitnerNavHost.kt` avec `hiltViewModel()`.

---

## US-02 — [TÂCHE TECHNIQUE] Annulation de l'option fusion / sessions séparées

**Formulation**

> En tant que développeur, je veux annuler l'implémentation de l'option "Fusionner / Sessions séparées" prévue dans F5, afin d'aligner le code avec la règle simplifiée : toute sélection de plusieurs boîtes produit automatiquement une session fusionnée.

**Contexte**

La spécification fonctionnelle F5 décrit un choix explicite proposé à l'utilisateur : "Fusionner en une session unique" / "Conserver des sessions distinctes". Cette option est annulée. La fusion est désormais toujours automatique dès que l'utilisateur coche plusieurs boîtes.

**Ce qui doit être annulé ou modifié**

- Supprimer tout composant UI proposant le choix "Fusionner / Sessions séparées" s'il a été créé.
- Supprimer la logique conditionnelle dans le `SessionSelectionViewModel` qui gérait ce choix.
- Vérifier que `GetDailySessionPlanUseCase` et `EvaluateCardUseCase` ne contiennent pas de branche liée à ce choix.
- Vérifier les tests unitaires et UI associés à ce choix et les supprimer ou les adapter.

**Critères d'acceptation**

- [ ] Aucun composant UI ne propose le choix "Fusionner / Sessions séparées".
- [ ] Aucune logique conditionnelle liée à ce choix ne subsiste dans les ViewModels ou Use Cases.
- [ ] Les tests liés à ce choix sont supprimés ou mis à jour.
- [ ] Le comportement de fusion automatique est couvert par un test unitaire dédié.

**Implémentation technique**

*Bonne pratique appliquée : cette tâche est à réaliser en premier dans la phase 4, avant toute création de fichier de session, pour partir sur une base propre.*

**Checklist de vérification**

Faire un grep sur le projet entier pour les termes suivants et supprimer ou corriger chaque occurrence :

```
"fusionner"
"merge"
"sessions séparées"
"separate"
"MergeMode"
```

**Test unitaire à créer**

```kotlin
// feature/session/src/test/.../SessionSelectionViewModelTest.kt

@Test
fun `selecting multiple boxes automatically merges cards into one list`() {
    // Given : deux boîtes sélectionnées de deux decks différents
    // When : l'utilisateur lance la session
    // Then : la liste de cartes contient les cartes des deux boîtes mélangées
    //        sans aucun choix préalable de fusion
}
```

---

## US-03 — Sélectionner les boîtes à réviser et lancer la session

**Formulation**

> En tant qu'utilisateur, je veux cocher les boîtes que je souhaite réviser parmi celles proposées aujourd'hui et lancer la session, afin de choisir la quantité de révision adaptée à mon temps disponible.

**Description de l'écran**

L'écran de sélection affiche toutes les boîtes dont le planning est à la date du jour, regroupées par deck. Chaque boîte affiche son numéro et le nombre de cartes qu'elle contient. Toutes les boîtes sont cochées par défaut.

L'utilisateur peut décocher les boîtes qu'il ne souhaite pas réviser maintenant. **Les boîtes décochées ne sont pas reportées : leur date de planning reste aujourd'hui**. Elles seront à nouveau proposées si l'utilisateur revient sur cet écran dans la journée.

En tapant "Démarrer", les cartes de toutes les boîtes cochées sont automatiquement mélangées dans une seule file de révision, quel que soit le nombre de boîtes ou de decks sélectionnés.

**Exemple**

> 3 boîtes proposées : Boîte 1 "Anglais" (8 cartes), Boîte 2 "Histoire" (5 cartes), Boîte 1 "Kotlin" (6 cartes).

L'utilisateur décoche "Boîte 1 Kotlin" et démarre. La session contient 13 cartes mélangées issues des deux boîtes cochées. Boîte 1 "Kotlin" reste disponible pour plus tard dans la journée.

**Critères d'acceptation**

- [ ] Toutes les boîtes planifiées aujourd'hui sont affichées et cochées par défaut.
- [ ] L'utilisateur peut décocher une ou plusieurs boîtes.
- [ ] Une boîte décochée conserve sa date de planning à aujourd'hui (elle n'est pas reportée).
- [ ] Le bouton "Démarrer" est désactivé si aucune boîte n'est cochée.
- [ ] Le nombre total de cartes affiché correspond à la somme des cartes des boîtes cochées.
- [ ] Les cartes des boîtes cochées sont mélangées automatiquement dans une seule file de révision.
- [ ] Chaque carte conserve les règles de son deck d'origine pendant la session (règle de mauvaise réponse).
- [ ] Après la session, les boîtes non cochées sont toujours proposées si l'utilisateur revient sur l'écran.

**Implémentation technique**

*Bonne pratiques appliquées : logique de mélange dans un Use Case, pas dans le ViewModel. L'état UI est immuable. La navigation transmet les données via un argument sérialisé ou via un état partagé.*

**Use Case à créer**

```kotlin
// core/domain/src/main/.../usecase/session/BuildSessionUseCase.kt

class BuildSessionUseCase(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(
        selectedItems: List<SessionPlanItem>
    ): List<Card> {
        return selectedItems
            .flatMap { item ->
                cardRepository.getCardsByDeckId(item.deck.id)
                    .first()
                    .filter { card -> card.box == item.boxNumber && !card.isLearned }
            }
            .shuffled()
    }
}
```

**Transmission des données à l'écran de session**

Les cartes à réviser sont trop volumineuses pour passer en argument de navigation. Utiliser un `SessionStateHolder` singleton (scope `@Singleton` Hilt) pour transporter la liste entre les deux écrans :

```kotlin
// core/domain/src/main/.../session/SessionStateHolder.kt

@Singleton
class SessionStateHolder @Inject constructor() {
    var pendingCards: List<Card> = emptyList()
    var selectedItems: List<SessionPlanItem> = emptyList()
}
```

**Dans `SessionSelectionViewModel`**

```kotlin
fun onStartSession() {
    viewModelScope.launch {
        val selected = _uiState.value.items
            .filter { it.isSelected }
            .map { it.planItem }
        val cards = buildSession(selected)
        sessionStateHolder.pendingCards = cards
        sessionStateHolder.selectedItems = selected
        _events.emit(SessionSelectionEvent.NavigateToSession)
    }
}
```

**Enregistrement dans `DomainModule`**

```kotlin
@Provides @Singleton
fun provideBuildSessionUseCase(repo: CardRepository): BuildSessionUseCase =
    BuildSessionUseCase(repo)
```

---

## US-04 — Refuser une boîte pour la reporter au lendemain

**Formulation**

> En tant qu'utilisateur, je veux pouvoir refuser de réviser une boîte aujourd'hui, afin de la reporter au lendemain quand je n'ai pas le temps ou l'énergie.

**Description de l'écran**

Sur l'écran de sélection de session, chaque boîte dispose d'une action "Reporter" (icône ou bouton secondaire). Taper dessus décale la date de planning de cette boîte au jour ouvrable suivant (jours exclus respectés) et retire la boîte de la liste du jour. Le streak est remis à zéro.

Si toutes les boîtes du jour sont reportées, l'empty state "Rien à réviser aujourd'hui" s'affiche.

**Exemple**

> L'utilisateur reporte la boîte 1 un vendredi (samedi et dimanche exclus). La boîte 1 sera planifiée le lundi suivant.

**Critères d'acceptation**

- [ ] Chaque boîte dispose d'une action individuelle de report.
- [ ] Le report décale la date de planning d'exactement un jour ouvrable (jours exclus respectés).
- [ ] La boîte reportée disparaît de la liste des boîtes du jour immédiatement.
- [ ] Le streak est remis à zéro dès qu'un report est effectué.
- [ ] Si toutes les boîtes sont reportées, l'empty state est affiché.
- [ ] La date de planning d'une boîte ne peut jamais être nulle ni dans le passé après un report.
- [ ] Le report d'une boîte n'affecte pas la date de planning des autres boîtes du même deck.

**Implémentation technique**

*Bonne pratique appliquée : `PostponeBoxSessionUseCase` existe déjà dans le domaine et implémente exactement cette logique. Le ViewModel l'appelle sans connaître le repository.*

**Use Case existant**

`PostponeBoxSessionUseCase(deckId: Long, boxNumber: Int)` — décale les cartes de la boîte au jour ouvrable suivant en tenant compte des jours exclus via `SettingsRepository`.

**Gestion du streak**

La remise à zéro du streak passe par la persistance d'une session avec `isReported = true`. Ajouter dans `SessionSelectionViewModel` :

```kotlin
fun onPostponeBox(item: SelectableBoxItem) {
    viewModelScope.launch {
        // 1. Reporter la boîte via le use case
        postponeBoxSession(
            deckId = item.planItem.deck.id,
            boxNumber = item.planItem.boxNumber
        )
        // 2. Enregistrer une session reportée pour casser le streak
        saveSession(
            Session(
                date = Instant.now(),
                deckIds = listOf(item.planItem.deck.id),
                cardCount = 0,
                successCount = 0,
                masteredCount = 0,
                isReported = true
            )
        )
        // 3. La boîte disparaît automatiquement du Flow car sa date
        //    n'est plus aujourd'hui — GetDailySessionPlanUseCase se met à jour
    }
}
```

**Use Case `SaveSessionUseCase` à créer**

```kotlin
// core/domain/src/main/.../usecase/session/SaveSessionUseCase.kt

class SaveSessionUseCase(private val repository: SessionRepository) {
    suspend operator fun invoke(session: Session): Long =
        repository.insertSession(session)
}
```

**Entité et DAO à créer dans `:core:data`**

```kotlin
// core/data/src/main/.../local/entity/SessionEntity.kt
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,           // Instant.toEpochMilli()
    val deckIds: String,      // JSON sérialisé via RoomConverters
    val cardCount: Int,
    val successCount: Int,
    val masteredCount: Int,
    val advancedCount: Int,
    val retreatedCount: Int,
    val isReported: Boolean
)
```

```kotlin
// core/data/src/main/.../local/dao/SessionDao.kt
@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: SessionEntity): Long
}
```

Ajouter `SessionEntity` dans `@Database(entities = [...])` et bumper la version Room à 3 avec migration.

---

## US-05 — Retourner une carte pour découvrir la réponse

**Formulation**

> En tant qu'utilisateur, je veux retourner une carte pour voir la réponse, afin de comparer ma réponse mentale avec la réponse attendue.

**Description de l'écran**

L'écran de session affiche la carte en mode recto (question visible, verso masqué). Un tap sur la carte ou sur le bouton "Retourner" déclenche une animation de retournement sur l'axe Y (350 ms, effet physique). Une fois retournée, le verso (réponse) est affiché et les boutons d'évaluation apparaissent.

Le texte est masqué à 90° de rotation pour éviter l'effet miroir : le recto disparaît entre 89° et 91°, le verso apparaît à partir de 91°.

**Exemple**

> Question : "Quelle est la capitale de l'Allemagne ?"  
> L'utilisateur tape la carte → animation 350 ms → "Berlin" affiché.

**Critères d'acceptation**

- [ ] Au démarrage d'un tour, seul le recto (question) est visible.
- [ ] Un tap sur la carte déclenche l'animation de retournement.
- [ ] Le bouton textuel "Retourner" produit le même effet (accessibilité).
- [ ] L'animation dure 350 ms avec une easing `FastOutSlowIn`.
- [ ] Le texte est invisible entre 89° et 91° (pas d'effet miroir).
- [ ] Après retournement, le verso (réponse) est affiché.
- [ ] Après retournement, les boutons d'évaluation sont visibles.
- [ ] La carte ne peut pas être évaluée avant d'avoir été retournée.

**Implémentation technique**

*Bonne pratique appliquée : aucune logique métier dans le composable. L'état `isFlipped` est géré par le ViewModel et descendu via state hoisting.*

**Composable existant à améliorer**

`LeitnerCard.kt` dans `:core:ui` implémente déjà l'animation de base. Vérifier et compléter :

```kotlin
// core/ui/src/main/.../components/FlipCard.kt

@Composable
fun FlipCard(
    isFlipped: Boolean,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "cardFlip"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() }
    ) {
        // Recto visible de 0° à 89°
        if (rotation <= 90f) {
            front()
        } else {
            // Verso visible de 91° à 180° — contre-rotation pour éviter l'effet miroir
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                back()
            }
        }
    }
}
```

**État dans `SessionViewModel`**

```kotlin
data class SessionUiState(
    val cards: List<Card> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    ...
)

fun onFlip() {
    _uiState.update { it.copy(isFlipped = true) }
}
```

La carte est réinitialisée côté recto (`isFlipped = false`) à chaque passage à la carte suivante.

**Bouton d'accessibilité**

Dans `SessionContent`, ajouter sous la carte :

```kotlin
if (!uiState.isFlipped) {
    TextButton(onClick = onFlip) {
        Text(stringResource(R.string.session_flip_card))
    }
}
```

---

## US-06 — S'auto-évaluer après retournement

**Formulation**

> En tant qu'utilisateur, je veux indiquer moi-même si ma réponse était bonne ou mauvaise après avoir retourné la carte, afin de contrôler mon évaluation et progresser au bon rythme.

**Description de l'écran**

Après retournement, deux boutons apparaissent en bas de l'écran : **"Bonne réponse"** (vert) et **"Mauvaise réponse"** (rouge). Ces boutons ne sont disponibles que pour les cartes avec `saisieRequise = false`.

Un tap sur l'un des deux boutons déclenche le déplacement de la carte dans les boîtes et passe à la carte suivante.

**Exemple**

> La réponse attendue est "Berlin". L'utilisateur se souvient correctement → il tape "Bonne réponse" → la carte avance en boîte 2.

**Critères d'acceptation**

- [ ] Les boutons "Bonne réponse" et "Mauvaise réponse" n'apparaissent qu'après retournement.
- [ ] Ces boutons ne sont présents que pour les cartes avec `saisieRequise = false`.
- [ ] "Bonne réponse" déclenche le déplacement de la carte vers la boîte suivante.
- [ ] "Mauvaise réponse" déclenche le déplacement selon la règle du deck (boîte 1 ou boîte précédente).
- [ ] Après évaluation, la carte suivante est affichée.
- [ ] L'indicateur de progression est mis à jour après chaque évaluation.

**Implémentation technique**

*Bonne pratique appliquée : `EvaluateCardUseCase` existe déjà dans le domaine et gère le déplacement en boîte. Le ViewModel l'appelle et met à jour l'UiState. Aucune logique de déplacement dans le composable.*

**`SessionViewModel` — méthode d'évaluation**

```kotlin
fun onEvaluate(isCorrect: Boolean) {
    val currentCard = _uiState.value.cards[_uiState.value.currentIndex]
    val deck = sessionStateHolder.selectedItems
        .first { it.boxNumber == currentCard.box && it.deck.id == currentCard.deckId }
        .deck

    viewModelScope.launch {
        evaluateCard(currentCard, deck, isCorrect)
        moveToNextCard(isCorrect)
    }
}

private fun moveToNextCard(isCorrect: Boolean) {
    val state = _uiState.value
    val nextIndex = state.currentIndex + 1
    if (nextIndex >= state.cards.size) {
        _events.emit(SessionUiEvent.SessionComplete)
    } else {
        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                isFlipped = false,
                evaluatedCount = it.evaluatedCount + 1,
                successCount = if (isCorrect) it.successCount + 1 else it.successCount
            )
        }
    }
}
```

**Composable des boutons d'évaluation**

```kotlin
// Affiché uniquement si isFlipped && !card.needsInput
if (uiState.isFlipped && !currentCard.needsInput) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = { onEvaluate(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) { Text(stringResource(R.string.session_wrong_answer)) }

        Button(
            onClick = { onEvaluate(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) { Text(stringResource(R.string.session_correct_answer)) }
    }
}
```

---

## US-07 — Évaluer via swipe gestuel

**Formulation**

> En tant qu'utilisateur, je veux pouvoir évaluer une carte d'un geste de swipe, afin de progresser dans ma session plus rapidement et de façon plus intuitive.

**Description de l'écran**

Après retournement, la carte est "swipeable". Un swipe vers la droite équivaut à "Bonne réponse", un swipe vers la gauche à "Mauvaise réponse". Pendant le swipe, un overlay coloré apparaît progressivement (vert à droite, rouge à gauche), proportionnel à l'amplitude du déplacement.

Le seuil de déclenchement est fixé à 40 % de la largeur de l'écran. En deçà, la carte revient à sa position initiale par une animation spring.

**Exemple**

> L'utilisateur glisse la carte vers la droite de plus de 40 % de la largeur → overlay vert atteint son intensité maximale → évaluation "bonne réponse" déclenchée → carte suivante affichée.

**Critères d'acceptation**

- [ ] Le swipe n'est disponible qu'après retournement de la carte.
- [ ] Swipe droite > 40 % de la largeur → "Bonne réponse" déclenchée.
- [ ] Swipe gauche > 40 % de la largeur → "Mauvaise réponse" déclenchée.
- [ ] L'overlay vert / rouge est proportionnel à l'amplitude du swipe.
- [ ] Swipe inférieur au seuil → la carte revient à sa position initiale (spring animation) sans déclencher d'évaluation.
- [ ] Les boutons "Bonne réponse" / "Mauvaise réponse" restent disponibles en alternative.
- [ ] Le swipe produit exactement le même résultat métier que les boutons.

**Implémentation technique**

*Bonne pratique appliquée : le swipe est une préoccupation UI pure. Il appelle la même fonction `onEvaluate` que les boutons — aucune logique dupliquée.*

**Composable à créer**

```kotlin
// core/ui/src/main/.../components/SwipeableCard.kt

@Composable
fun SwipeableCard(
    isFlipped: Boolean,
    onEvaluate: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val threshold = screenWidth * 0.4f

    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipeOffset"
    )

    // Couleur de l'overlay : vert si offset > 0, rouge si offset < 0
    val overlayColor = when {
        animatedOffsetX > 0 -> Color(0xFF4CAF50).copy(
            alpha = (animatedOffsetX / threshold.value).coerceIn(0f, 0.4f)
        )
        animatedOffsetX < 0 -> Color.Red.copy(
            alpha = (-animatedOffsetX / threshold.value).coerceIn(0f, 0.4f)
        )
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .background(overlayColor)
            .pointerInput(isFlipped) {
                if (!isFlipped) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > threshold.value -> {
                                onEvaluate(true)
                                offsetX = 0f
                            }
                            offsetX < -threshold.value -> {
                                onEvaluate(false)
                                offsetX = 0f
                            }
                            else -> offsetX = 0f  // spring retour
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
    ) {
        content()
    }
}
```

**Intégration dans `SessionContent`**

`FlipCard` est enveloppé dans `SwipeableCard`. Le swipe n'est actif que si `isFlipped = true` et `!card.needsInput`.

---

## US-08 — Saisir une réponse quand `saisieRequise = true`

**Formulation**

> En tant qu'utilisateur, je veux saisir ma réponse au clavier pour les cartes qui le requièrent, afin que l'app vérifie automatiquement si ma réponse est correcte.

**Description de l'écran**

Pour les cartes avec `saisieRequise = true`, l'écran de session affiche un champ de saisie texte à la place des boutons d'évaluation. Le clavier s'ouvre automatiquement. Le champ de saisie et le bouton "OK" sont sur la même ligne pour gagner de la place. Le clavier ne doit pas cacher le champ de saisie. Il n'y a pas de bouton "Retourner" : l'affichage du verso est déclenché par la validation.

Après validation, la réponse attendue est toujours affichée pour que l'utilisateur puisse comparer, que la réponse soit correcte ou non.

**Exemple**

> Question : "Symbole chimique du fer ?"  
> L'utilisateur tape "Fe" → tape "OK" → l'app affiche "Correct" et montre la réponse attendue "Fe".

**Critères d'acceptation**

- [ ] Le champ de saisie apparaît uniquement pour les cartes `saisieRequise = true`.
- [ ] Le clavier s'ouvre automatiquement à l'affichage de la carte.
- [ ] Le bouton "Valider" soumet la réponse saisie.
- [ ] La validation est également possible via la touche "Entrée" du clavier.
- [ ] Après validation, la réponse attendue (réponse brute, non normalisée) est affichée.
- [ ] Le champ de saisie est désactivé après validation.
- [ ] Une saisie vide soumise est traitée comme une mauvaise réponse.

**Implémentation technique**

*Bonne pratique appliquée : la vérification Levenshtein est déléguée à `CheckAnswerUseCase` dans le domaine. Le ViewModel reçoit le résultat et met à jour l'UiState.*

**État supplémentaire dans `SessionUiState`**

```kotlin
data class SessionUiState(
    ...
    val userInput: String = "",
    val inputValidated: Boolean = false,   // true après validation
    val checkResult: AnswerCheckResult? = null
)
```

**Dans `SessionViewModel`**

```kotlin
fun onInputChanged(text: String) {
    _uiState.update { it.copy(userInput = text) }
}

fun onInputValidated() {
    val state = _uiState.value
    val card = state.cards[state.currentIndex]
    val result = checkAnswer(card, state.userInput)
    val isCorrect = result is AnswerCheckResult.Correct
    _uiState.update {
        it.copy(
            inputValidated = true,
            checkResult = result,
            isFlipped = true   // afficher le verso avec la réponse attendue
        )
    }
    // EvaluateCardUseCase est appelé automatiquement — voir US-09
}
```

**Composable de saisie**

```kotlin
// Affiché uniquement si card.needsInput && !inputValidated
if (currentCard.needsInput && !uiState.inputValidated) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),       // évite que le clavier cache le champ
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = uiState.userInput,
            onValueChange = onInputChanged,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onInputValidated() }),
            singleLine = true
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = onInputValidated) {
            Text("OK")
        }
    }
}
```

---

## US-09 — Voir le résultat de la vérification automatique

**Formulation**

> En tant qu'utilisateur, je veux voir immédiatement si ma réponse saisie est correcte ou non, afin de savoir si je connais la carte.

**Description de l'écran**

Après validation de la saisie (US-08), l'app affiche un feedback visuel immédiat :

- **Réponse correcte** : indicateur vert, message "Correct", réponse attendue affichée.
- **Réponse incorrecte** : indicateur rouge, message "Incorrect", réponse attendue affichée en regard de la saisie de l'utilisateur.

Un bouton "Continuer" permet de passer à la carte suivante. L'évaluation (bonne ou mauvaise) est déclenchée automatiquement en fonction du résultat, sans action supplémentaire de l'utilisateur.

**Exemple**

> Réponse attendue : "photosynthèse" (normalisée : "photosynthese", longueur 13, seuil 2).  
> Saisie : "photosinthese" (distance de Levenshtein : 1 ≤ 2) → "Correct" affiché en vert.

> Saisie : "photosinttese" (distance 3 > 2) → "Incorrect" affiché en rouge, réponse correcte affichée.

**Critères d'acceptation**

- [ ] Le feedback est affiché immédiatement après validation.
- [ ] Le feedback vert "Correct" s'affiche si la distance normalisée ≤ seuil.
- [ ] Le feedback rouge "Incorrect" s'affiche si la distance normalisée > seuil.
- [ ] La réponse attendue (brute, non normalisée) est toujours affichée après validation.
- [ ] L'évaluation métier (déplacement en boîte) est déclenchée automatiquement, sans bouton supplémentaire.
- [ ] Un bouton "Continuer" permet de passer à la carte suivante.

**Implémentation technique**

*Bonne pratique appliquée : `CheckAnswerUseCase` fait la comparaison Levenshtein. `EvaluateCardUseCase` fait le déplacement en boîte. Le ViewModel les enchaîne. Le composable affiche uniquement l'état.*

**Enchaînement dans `SessionViewModel`**

```kotlin
fun onInputValidated() {
    val state = _uiState.value
    val card = state.cards[state.currentIndex]
    val result = checkAnswer(card, state.userInput)
    val isCorrect = result is AnswerCheckResult.Correct

    viewModelScope.launch {
        // 1. Afficher le feedback immédiatement
        _uiState.update {
            it.copy(inputValidated = true, checkResult = result, isFlipped = true)
        }
        // 2. Évaluer la carte en base (déplacement en boîte)
        val deck = getDeckForCard(card)
        evaluateCard(card, deck, isCorrect)
    }
}
```

**Composable de feedback**

```kotlin
// Affiché si card.needsInput && inputValidated
if (currentCard.needsInput && uiState.inputValidated) {
    val isCorrect = uiState.checkResult is AnswerCheckResult.Correct
    Column {
        Text(
            text = if (isCorrect) "Correct ✓" else "Incorrect ✗",
            color = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Réponse : ${currentCard.verso}",
            style = MaterialTheme.typography.bodyMedium
        )
        if (!isCorrect) {
            Text(
                text = "Votre saisie : ${uiState.userInput}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onContinue) {
            Text(stringResource(R.string.session_continue))
        }
    }
}
```

---

## US-10 — Suivre sa progression pendant la session

**Formulation**

> En tant qu'utilisateur, je veux voir où j'en suis dans la session en cours, afin de savoir combien de cartes il me reste à réviser.

**Description de l'écran**

Un indicateur de progression est affiché en haut de l'écran de session, visible en permanence. Il comprend :

- Un **texte** : "Carte X / Y" (session ordinaire) ou "Maîtrisées X / Y" (challenge).
- Une **barre de progression linéaire** dont la largeur est proportionnelle à X / Y.

L'indicateur est mis à jour immédiatement après chaque évaluation.

**Exemple**

> Session de 12 cartes. Après la 4e évaluation → "Carte 4 / 12", barre à 33 %.

**Critères d'acceptation**

- [ ] L'indicateur est visible en permanence pendant la session.
- [ ] Le format est "Carte X / Y" pour une session de révision ordinaire.
- [ ] Le format est "Maîtrisées X / Y" pour un challenge (X = bonnes réponses sur cartes maîtrisées).
- [ ] Y correspond au nombre total de cartes de la session (toutes boîtes fusionnées incluses).
- [ ] X est incrémenté après chaque évaluation.
- [ ] La barre de progression est proportionnelle à X / Y.

**Implémentation technique**

*Bonne pratique appliquée : les valeurs X et Y sont des propriétés dérivées du `SessionUiState`, calculées via `derivedStateOf` pour éviter les recompositions inutiles.*

**Dans `SessionUiState`**

```kotlin
data class SessionUiState(
    val cards: List<Card> = emptyList(),
    val currentIndex: Int = 0,
    val evaluatedCount: Int = 0,
    val isChallenge: Boolean = false,
    val masteredThisSession: Int = 0,
    ...
)

// Propriétés dérivées (calculées hors du composable)
val SessionUiState.progressCurrent: Int
    get() = if (isChallenge) masteredThisSession else evaluatedCount

val SessionUiState.progressTotal: Int
    get() = cards.size

val SessionUiState.progressFraction: Float
    get() = if (progressTotal == 0) 0f
            else progressCurrent.toFloat() / progressTotal
```

**Composable à créer**

```kotlin
// core/ui/src/main/.../components/SessionProgressIndicator.kt

@Composable
fun SessionProgressIndicator(
    current: Int,
    total: Int,
    isChallenge: Boolean,
    modifier: Modifier = Modifier
) {
    val fraction by remember(current, total) {
        derivedStateOf { if (total == 0) 0f else current.toFloat() / total }
    }
    Column(modifier = modifier) {
        Text(
            text = if (isChallenge) "Maîtrisées $current / $total"
                   else "Carte $current / $total",
            style = MaterialTheme.typography.labelMedium
        )
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

---

## US-11 — Voir une carte avancer ou reculer dans les boîtes après évaluation

**Formulation**

> En tant qu'utilisateur, je veux que mes cartes soient déplacées dans la bonne boîte après chaque évaluation, afin que le système de révision espacée reflète fidèlement mon niveau de maîtrise.

**Description de l'écran**

Ce comportement est invisible pendant la session (pas de feedback visuel spécifique sur le déplacement en boîte). Son résultat est consultable dans l'écran de détail du deck après la session (répartition des cartes par boîte).

La logique appliquée est :

- Bonne réponse → carte déplacée vers la boîte suivante.
- Mauvaise réponse → carte déplacée selon la règle du deck (boîte 1 ou boîte précédente).
- La carte déjà en boîte 1 avec une mauvaise réponse reste en boîte 1.

**Exemple**

> Deck avec règle "Retour à la boîte précédente". Mauvaise réponse sur une carte en boîte 3 → carte déplacée en boîte 2.

> Deck avec règle "Retour en boîte 1". Mauvaise réponse sur une carte en boîte 4 → carte déplacée en boîte 1.

**Critères d'acceptation**

- [ ] Une bonne réponse déplace la carte vers la boîte N+1 du même deck.
- [ ] Une mauvaise réponse avec règle "Retour en boîte 1" déplace la carte en boîte 1.
- [ ] Une mauvaise réponse avec règle "Retour à la boîte précédente" déplace la carte en boîte N-1.
- [ ] Une mauvaise réponse sur une carte déjà en boîte 1 la laisse en boîte 1.
- [ ] Après la session, la répartition dans l'écran de détail du deck reflète les déplacements.
- [ ] La date de prochaine session de la boîte cible est recalculée (intervalle + jours exclus) après déplacement.

**Implémentation technique**

*Bonne pratique appliquée : `EvaluateCardUseCase` existe déjà et implémente toute cette logique. Aucun code métier à écrire ici — uniquement l'appel depuis le ViewModel.*

**`EvaluateCardUseCase` — rappel de la signature**

```kotlin
suspend operator fun invoke(
    card: Card,
    deck: Deck,
    isCorrect: Boolean,
    referenceInstant: Instant = Instant.now()
)
```

Le use case gère :
- Le calcul de la boîte cible selon `deck.wrongAnswerRule`.
- Le recalcul de `nextReviewDate` via `NextSessionDateCalculator` avec les jours exclus.
- La mise à jour de la carte en base via `CardRepository`.

**Récupération du deck depuis le ViewModel**

Le `SessionStateHolder` contient `selectedItems: List<SessionPlanItem>` qui embarque le `Deck` de chaque item. Pour retrouver le deck d'une carte :

```kotlin
private fun getDeckForCard(card: Card): Deck {
    return sessionStateHolder.selectedItems
        .first { it.deck.id == card.deckId }
        .deck
}
```

**Comptage pour l'écran de résultat**

Mettre à jour `SessionUiState` lors de chaque évaluation :

```kotlin
_uiState.update {
    it.copy(
        advancedCount = if (isCorrect && !isMastered) it.advancedCount + 1 else it.advancedCount,
        retreatedCount = if (!isCorrect) it.retreatedCount + 1 else it.retreatedCount
    )
}
```

---

## US-12 — Voir une carte passer à l'état maîtrisée

**Formulation**

> En tant qu'utilisateur, je veux être informé qu'une carte est désormais maîtrisée lorsque je réponds correctement depuis la dernière boîte, afin de ressentir la satisfaction de ma progression.

**Description de l'écran**

Quand une bonne réponse est donnée sur une carte se trouvant dans la dernière boîte du deck, la carte passe à l'état "maîtrisée" et quitte le circuit de révision. Un feedback visuel immédiat est déclenché : une animation de confetti légère et non bloquante s'affiche par-dessus l'écran de session. La session continue normalement vers la carte suivante.

**Exemple**

> Deck à 5 boîtes. Bonne réponse sur une carte en boîte 5 → confetti léger → l'indicateur de progression avance → carte suivante affichée. Dans l'écran de détail du deck, cette carte n'apparaît plus dans les boîtes mais dans le compteur "Maîtrisées".

**Critères d'acceptation**

- [ ] La transition vers l'état maîtrisée se déclenche uniquement sur une bonne réponse depuis la dernière boîte.
- [ ] Une bonne réponse depuis une boîte intermédiaire ne déclenche pas de maîtrise.
- [ ] L'animation confetti légère est déclenchée immédiatement après l'évaluation.
- [ ] L'animation est non bloquante : la session continue sans attendre sa fin.
- [ ] La carte est retirée du circuit de révision ordinaire en base de données.
- [ ] Le compteur de cartes maîtrisées dans l'écran de résultat (US-13) est incrémenté.

**Implémentation technique**

*Bonne pratique appliquée : la détection de la maîtrise est dans `EvaluateCardUseCase` (qui pose `isLearned = true`). L'événement one-shot vers l'UI transite par un `Channel<SessionUiEvent>`, pas par l'UiState, pour éviter de rejouer l'animation à la recomposition.*

**Event one-shot à ajouter**

```kotlin
sealed class SessionUiEvent {
    object CardMastered : SessionUiEvent()
    object SessionComplete : SessionUiEvent()
}

// Dans SessionViewModel
private val _events = Channel<SessionUiEvent>(Channel.BUFFERED)
val events: Flow<SessionUiEvent> = _events.receiveAsFlow()
```

**Détection dans `SessionViewModel`**

```kotlin
private suspend fun moveToNextCard(isCorrect: Boolean, isMastered: Boolean) {
    if (isMastered) {
        _uiState.update { it.copy(masteredThisSession = it.masteredThisSession + 1) }
        _events.send(SessionUiEvent.CardMastered)
    }
    // ... passage à la carte suivante
}
```

`isMastered` est déterminé en comparant `card.box == deck.intervals.size && isCorrect` avant l'appel à `EvaluateCardUseCase`, ou en observant que la carte a `isLearned = true` après l'appel.

**Composable `CelebrationOverlay` à créer**

```kotlin
// core/ui/src/main/.../components/CelebrationOverlay.kt

enum class CelebrationType { CARD_MASTERED, SESSION_SUCCESS }

@Composable
fun CelebrationOverlay(
    type: CelebrationType,
    onFinished: () -> Unit
) {
    // Utiliser la bibliothèque Lottie pour les animations JSON
    // confetti_light.json → CARD_MASTERED (légère, 1.5s)
    // confetti_full.json  → SESSION_SUCCESS (ample, 2s)
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            if (type == CelebrationType.CARD_MASTERED) R.raw.confetti_light
            else R.raw.confetti_full
        )
    )
    LottieAnimation(
        composition = composition,
        iterations = 1,
        modifier = Modifier.fillMaxSize(),
        // Non bloquant : pointer input transparent
        contentScale = ContentScale.FillBounds
    )
    LaunchedEffect(Unit) {
        delay(if (type == CelebrationType.CARD_MASTERED) 1500L else 2000L)
        onFinished()
    }
}
```

**Dans `SessionScreen`**

```kotlin
var showCelebration by remember { mutableStateOf<CelebrationType?>(null) }

LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            SessionUiEvent.CardMastered ->
                showCelebration = CelebrationType.CARD_MASTERED
            SessionUiEvent.SessionComplete ->
                navController.navigate(Screen.SessionResult.route)
        }
    }
}

showCelebration?.let { type ->
    CelebrationOverlay(type = type, onFinished = { showCelebration = null })
}
```

**Dépendance Lottie à ajouter dans `core/ui/build.gradle.kts`**

```kotlin
implementation(libs.lottie.compose)
```

---

## US-13 — Voir le récapitulatif à la fin d'une session

**Formulation**

> En tant qu'utilisateur, je veux voir un récapitulatif de ma session une fois toutes les cartes révisées, afin de connaître mes performances et ma progression.

**Description de l'écran**

À la fin de la dernière carte, l'écran de résultat est affiché automatiquement. Il présente :

- Le **nombre de cartes révisées**.
- Le **taux de réussite** (pourcentage de bonnes réponses).
- Le **nombre de cartes nouvellement maîtrisées** lors de cette session.
- Le **nombre de cartes qui ont avancé d'une boîte**.
- Le **nombre de cartes qui ont reculé**.

Un bouton "Terminer" (ou "Retour au dashboard") permet de quitter l'écran.

**Exemple**

> Session de 10 cartes : 7 bonnes réponses, 2 nouvellement maîtrisées.  
> Récapitulatif : "10 cartes révisées — 70 % de réussite — 2 cartes maîtrisées — 7 cartes ont avancé — 1 carte a reculé"

**Critères d'acceptation**

- [ ] L'écran de résultat s'affiche automatiquement après évaluation de la dernière carte.
- [ ] Le nombre de cartes révisées est exact.
- [ ] Le taux de réussite est calculé sur le nombre total de cartes de la session.
- [ ] Le nombre de cartes nouvellement maîtrisées correspond aux cartes ayant quitté la dernière boîte pendant cette session.
- [ ] Le nombre de cartes ayant avancé d'une boîte est affiché.
- [ ] Le nombre de cartes ayant reculé est affiché.
- [ ] La session est sauvegardée en base (historique) avec toutes ces données.
- [ ] Le bouton "Terminer" ramène au dashboard ou à l'écran de sélection.

**Implémentation technique**

*Bonne pratique appliquée : les données de résultat transitent via `SessionStateHolder` depuis `SessionViewModel` vers `SessionResultViewModel`. La sauvegarde en base est faite dans `SessionViewModel` à la fin de la session, avant la navigation.*

**Modèle `Session` — vérification des champs**

Le modèle `Session` doit couvrir toutes les métriques affichées. Vérifier que `Session.kt` contient :

```kotlin
data class Session(
    val id: Long = 0,
    val date: Instant,
    val deckIds: List<Long>,
    val cardCount: Int,
    val successCount: Int,
    val masteredCount: Int,
    val advancedCount: Int,    // ← à ajouter si absent
    val retreatedCount: Int,   // ← à ajouter si absent
    val isReported: Boolean = false
)
```

Si ces champs sont ajoutés, bumper `SessionEntity` et la migration Room en conséquence.

**Sauvegarde dans `SessionViewModel`**

```kotlin
private suspend fun onSessionComplete() {
    val state = _uiState.value
    val session = Session(
        date = Instant.now(),
        deckIds = sessionStateHolder.selectedItems.map { it.deck.id }.distinct(),
        cardCount = state.cards.size,
        successCount = state.successCount,
        masteredCount = state.masteredThisSession,
        advancedCount = state.advancedCount,
        retreatedCount = state.retreatedCount,
        isReported = false
    )
    saveSession(session)

    // Stocker le résultat pour l'écran suivant
    sessionStateHolder.lastSessionResult = session
    _events.send(SessionUiEvent.SessionComplete)
}
```

**`SessionResultViewModel` à créer**

```kotlin
// feature/session/src/main/.../session/result/SessionResultViewModel.kt

@HiltViewModel
class SessionResultViewModel @Inject constructor(
    private val sessionStateHolder: SessionStateHolder
) : ViewModel() {

    val session: Session
        get() = sessionStateHolder.lastSessionResult
            ?: error("No session result available")

    val successRate: Int
        get() = if (session.cardCount == 0) 0
                else (session.successCount * 100) / session.cardCount
}
```

**Navigation**

```kotlin
// Screen.kt
object SessionResult : Screen("session/result")
```

---

## US-14 — Voir la célébration de fin de session

**Formulation**

> En tant qu'utilisateur, je veux voir une animation de célébration quand j'ai bien réussi ma session, afin d'être encouragé à continuer mes révisions.

**Description de l'écran**

Sur l'écran de résultat (US-13), si le taux de réussite est **strictement supérieur à 60 %**, une animation de confetti complète (plus ample que celle de la maîtrise d'une carte) est déclenchée automatiquement à l'affichage de l'écran. L'animation est non bloquante et se termine d'elle-même après environ 2 secondes. L'utilisateur peut taper "Terminer" sans attendre la fin de l'animation.

**Exemple**

> 8 bonnes réponses sur 10 → taux 80 % > 60 % → animation confetti complète au chargement de l'écran de résultat.

> 6 bonnes réponses sur 10 → taux 60 %, non strictement supérieur → aucune animation.

**Critères d'acceptation**

- [ ] L'animation se déclenche uniquement si le taux de réussite est **strictement supérieur à 60 %** (61 % et plus).
- [ ] À exactement 60 %, aucune animation n'est déclenchée.
- [ ] L'animation est différente (plus ample) de celle de la maîtrise d'une carte (US-12).
- [ ] L'animation se déclenche automatiquement à l'affichage de l'écran de résultat, sans action de l'utilisateur.
- [ ] L'animation est non bloquante : le bouton "Terminer" est accessible immédiatement.
- [ ] L'animation se termine d'elle-même après environ 2 secondes.
- [ ] En l'absence de célébration (taux ≤ 60 %), l'écran de résultat est affiché normalement sans animation.

**Implémentation technique**

*Bonne pratique appliquée : la règle "taux > 60 %" est une règle métier — elle appartient au domaine, pas à l'UI. Elle est exposée comme propriété dérivée du ViewModel, pas calculée dans le composable.*

**Dans `SessionResultViewModel`**

```kotlin
val shouldCelebrate: Boolean
    get() = successRate > 60
```

**Dans `SessionResultScreen`**

```kotlin
@Composable
fun SessionResultScreen(
    viewModel: SessionResultViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    SessionResultContent(
        session = viewModel.session,
        successRate = viewModel.successRate,
        shouldCelebrate = viewModel.shouldCelebrate,
        onFinish = onFinish
    )
}

@Composable
fun SessionResultContent(
    session: Session,
    successRate: Int,
    shouldCelebrate: Boolean,
    onFinish: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        // Contenu principal du résultat
        Column(...) {
            Text("${session.cardCount} cartes révisées")
            Text("$successRate % de réussite")
            Text("${session.masteredCount} cartes maîtrisées")
            Text("${session.advancedCount} cartes ont avancé")
            Text("${session.retreatedCount} cartes ont reculé")
            Button(onClick = onFinish) {
                Text(stringResource(R.string.session_finish))
            }
        }

        // Overlay de célébration — par-dessus le contenu, non bloquant
        if (shouldCelebrate) {
            CelebrationOverlay(
                type = CelebrationType.SESSION_SUCCESS,
                onFinished = {}  // pas de navigation ici, l'utilisateur choisit
            )
        }
    }
}
```

**Test unitaire à ajouter dans `SessionResultViewModelTest`**

```kotlin
@Test
fun `successRate strictly above 60 triggers celebration`() {
    // 7 bonnes sur 10 → 70% → shouldCelebrate = true
}

@Test
fun `successRate exactly 60 does not trigger celebration`() {
    // 6 bonnes sur 10 → 60% → shouldCelebrate = false
}

@Test
fun `successRate below 60 does not trigger celebration`() {
    // 5 bonnes sur 10 → 50% → shouldCelebrate = false
}
```

---

## Récapitulatif des fichiers à créer / modifier

### Nouveaux fichiers

| Fichier | Module |
|---|---|
| `SessionSelectionScreen.kt` + `SessionSelectionContent.kt` | `:feature:session` |
| `SessionSelectionViewModel.kt` + `SessionSelectionUiState.kt` | `:feature:session` |
| `SessionScreen.kt` + `SessionContent.kt` | `:feature:session` |
| `SessionViewModel.kt` + `SessionUiState.kt` | `:feature:session` |
| `SessionResultScreen.kt` + `SessionResultContent.kt` | `:feature:session` |
| `SessionResultViewModel.kt` | `:feature:session` |
| `SessionStateHolder.kt` | `:core:domain` |
| `BuildSessionUseCase.kt` | `:core:domain` |
| `SaveSessionUseCase.kt` | `:core:domain` |
| `SessionEntity.kt` | `:core:data` |
| `SessionDao.kt` | `:core:data` |
| `SessionRepositoryImpl.kt` | `:core:data` |
| `FlipCard.kt` (remplace `LeitnerCard.kt`) | `:core:ui` |
| `SwipeableCard.kt` | `:core:ui` |
| `SessionProgressIndicator.kt` | `:core:ui` |
| `CelebrationOverlay.kt` | `:core:ui` |
| `confetti_light.json` + `confetti_full.json` | `:core:ui/res/raw` |

### Fichiers à modifier

| Fichier | Modification |
|---|---|
| `Screen.kt` | Ajout de `SessionSelection`, `Session`, `SessionResult` |
| `LeitnerNavHost.kt` | Ajout des 3 nouvelles routes |
| `LeitnerDatabase.kt` | Version 3 + `SessionEntity` + migration |
| `DataModule.kt` | Ajout `SessionDao`, `SessionRepositoryImpl` |
| `DomainModule.kt` | Ajout `BuildSessionUseCase`, `SaveSessionUseCase`, `SessionStateHolder` |
| `Session.kt` | Ajout de `advancedCount` et `retreatedCount` |
