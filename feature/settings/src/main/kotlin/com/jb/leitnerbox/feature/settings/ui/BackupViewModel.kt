package com.jb.leitnerbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.backup.BackupException
import com.jb.leitnerbox.core.domain.backup.BackupSummary
import com.jb.leitnerbox.core.domain.usecase.backup.ExportBackupUseCase
import com.jb.leitnerbox.core.domain.usecase.backup.RestoreBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isLoading: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val backupSummary: BackupSummary? = null,
    val pendingRestore: ByteArray? = null,
    val error: String? = null
)

sealed class BackupEvent {
    data class ShareFile(val bytes: ByteArray) : BackupEvent()
    data object RestoreSuccess : BackupEvent()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportBackup: ExportBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _events = Channel<BackupEvent>(Channel.BUFFERED)
    val events: Flow<BackupEvent> = _events.receiveAsFlow()

    fun onExportClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bytes = exportBackup()
                _uiState.update { it.copy(isLoading = false) }
                _events.send(BackupEvent.ShareFile(bytes))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onFileSelected(bytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val summary = restoreBackup.validate(bytes)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingRestore = bytes,
                        backupSummary = summary,
                        showConfirmDialog = true
                    )
                }
            } catch (e: Exception) {
                val message = when (e) {
                    is BackupException.IncompatibleVersion -> e.message
                    is BackupException.InvalidFile -> e.message
                    else -> "Une erreur est survenue lors de la lecture du fichier"
                }
                _uiState.update { it.copy(isLoading = false, error = message) }
            }
        }
    }

    fun onRestoreConfirmed() {
        val bytes = _uiState.value.pendingRestore ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmDialog = false) }
            try {
                restoreBackup.restore(bytes)
                _uiState.update { it.copy(isLoading = false) }
                _events.send(BackupEvent.RestoreSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onRestoreCancelled() {
        _uiState.update { it.copy(
            showConfirmDialog = false,
            pendingRestore = null,
            backupSummary = null
        )}
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }
}
