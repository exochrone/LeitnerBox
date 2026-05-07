package com.jb.leitnerbox.feature.decks.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.model.PresentationOrder
import com.jb.leitnerbox.core.domain.model.WrongAnswerRule
import com.jb.leitnerbox.core.ui.components.ColorPickerDialog
import com.jb.leitnerbox.core.ui.theme.DEFAULT_DECK_COLOR
import com.jb.leitnerbox.core.ui.utils.ColorUtils
import com.jb.leitnerbox.feature.decks.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckEditScreen(
    viewModel: DeckEditViewModel,
    onBackClick: () -> Unit,
    onDeckSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showColorPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                DeckEditEvent.DeckSaved -> onDeckSaved()
                DeckEditEvent.ExitWizard -> onBackClick()
            }
        }
    }

    val onBack = {
        if (uiState.step > 1) {
            viewModel.previousStep()
        } else {
            viewModel.onBackRequest()
        }
    }

    BackHandler(onBack = onBack)

    if (uiState.showExitConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissExitConfirm,
            title = { Text(stringResource(R.string.deck_edit_exit_title)) },
            text = { Text(stringResource(R.string.deck_edit_exit_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmExit) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissExitConfirm) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColorHex = uiState.color,
            onColorSelected = {
                viewModel.onColorSelected(it)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val prefix = if (uiState.isEditing) 
                        stringResource(R.string.deck_edit_title_edit) 
                    else 
                        stringResource(R.string.deck_edit_title_new)
                    Text("$prefix ${stringResource(R.string.deck_edit_step, uiState.step).trim()}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = viewModel::nextStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !uiState.isLoading && when (uiState.step) {
                        1    -> uiState.name.isNotBlank()
                        2    -> uiState.intervals.all { it.isNotBlank() }
                        else -> false
                    }
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        val label = when {
                            uiState.step < 2 -> stringResource(R.string.next)
                            uiState.isEditing -> stringResource(R.string.save)
                            else -> stringResource(R.string.create_deck)
                        }
                        Text(label)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (uiState.step) {
                1 -> {
                    StepOne(uiState, viewModel, onShowColorPicker = { showColorPicker = true })
                    StepTwo(uiState, viewModel)
                }
                2 -> StepThree(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun StepOne(
    uiState: DeckEditUiState,
    viewModel: DeckEditViewModel,
    onShowColorPicker: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text(stringResource(R.string.deck_name_label)) },
            isError = uiState.nameError,
            supportingText = {
                if (uiState.nameError) {
                    Text(stringResource(R.string.deck_name_error))
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.wrong_answer_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Column(Modifier.selectableGroup()) {
            WizardRadioButton(
                text = stringResource(R.string.wrong_answer_previous),
                selected = uiState.wrongAnswerRule == WrongAnswerRule.PREVIOUS_BOX,
                onClick = { viewModel.onWrongAnswerRuleChange(WrongAnswerRule.PREVIOUS_BOX) }
            )
            WizardRadioButton(
                text = stringResource(R.string.wrong_answer_first),
                selected = uiState.wrongAnswerRule == WrongAnswerRule.BACK_TO_BOX_ONE,
                onClick = { viewModel.onWrongAnswerRuleChange(WrongAnswerRule.BACK_TO_BOX_ONE) }
            )
        }

        Text(
            text = stringResource(R.string.deck_color_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowColorPicker() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val color = if (uiState.color == DEFAULT_DECK_COLOR) 
                MaterialTheme.colorScheme.onSurface 
            else 
                ColorUtils.fromHex(uiState.color)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = if (uiState.color == DEFAULT_DECK_COLOR) 
                    stringResource(R.string.deck_color_default) 
                else 
                    stringResource(R.string.deck_color_custom),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun StepTwo(
    uiState: DeckEditUiState,
    viewModel: DeckEditViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.box_count_question),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Column(Modifier.selectableGroup()) {
            listOf(3, 5, 7).forEach { count ->
                WizardRadioButton(
                    text = stringResource(R.string.box_count_value, count),
                    selected = uiState.boxCount == count,
                    onClick = { viewModel.onBoxCountChange(count) }
                )
            }
        }
    }
}

@Composable
private fun StepThree(
    uiState: DeckEditUiState,
    viewModel: DeckEditViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.box_intervals_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        uiState.intervals.forEachIndexed { index, interval ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.box_label, index + 1),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(80.dp)
                )
                OutlinedTextField(
                    value = interval,
                    onValueChange = { viewModel.onIntervalChange(index, it) },
                    label = { Text(stringResource(R.string.box_interval_label)) },
                    placeholder = { Text(stringResource(R.string.box_interval_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    suffix = { Text(stringResource(R.string.days)) }
                )
            }
        }
    }
}

@Composable
private fun WizardRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
