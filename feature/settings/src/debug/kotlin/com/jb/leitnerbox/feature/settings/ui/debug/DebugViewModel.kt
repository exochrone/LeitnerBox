package com.jb.leitnerbox.feature.settings.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val seeder: DebugDataSeeder
) : ViewModel() {

    private val _message = Channel<String>(Channel.BUFFERED)
    val message: Flow<String> = _message.receiveAsFlow()

    fun seedLongNames() = launch("Jeu 'Noms longs' créé") { seeder.seedLongNames() }
    fun seedBoxCirculation() = launch("Jeu 'Circulation boîtes' créé") { seeder.seedBoxCirculation() }
    fun seedMasteryTest() = launch("Jeu 'Test maîtrise' créé") { seeder.seedMasteryTest() }
    fun seedIntervalTest() = launch("Jeu 'Test intervalles' créé") { seeder.seedIntervalTest() }

    fun advanceOneDay() = launch("⏩ +1 jour simulé") { seeder.advanceTime(1) }
    fun advanceSevenDays() = launch("⏩ +7 jours simulés") { seeder.advanceTime(7) }

    fun cleanTestData() = launch("🗑 Données [TEST] supprimées") { seeder.cleanAllTestData() }

    private fun launch(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _message.send(successMessage)
            } catch (e: Exception) {
                _message.send("Erreur : ${e.message}")
            }
        }
    }
}
