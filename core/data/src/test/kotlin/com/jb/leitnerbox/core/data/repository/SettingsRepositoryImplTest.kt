package com.jb.leitnerbox.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.jb.leitnerbox.core.data.AppSettingsProto
import com.jb.leitnerbox.core.data.datastore.AppSettingsSerializer
import com.jb.leitnerbox.core.domain.model.AppTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var dataStore: DataStore<AppSettingsProto>
    private lateinit var repository: SettingsRepositoryImpl
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @BeforeEach
    fun setup() {
        dataStore = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            produceFile = { File(tempDir, "test_settings.pb") },
            scope = testScope
        )
        repository = SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `P5-IT-01 Ecrir puis lire excludedDays`() = runTest {
        val days = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        repository.setExcludedDays(days)
        
        val result = repository.getExcludedDays().first()
        assertEquals(days, result)
    }

    @Test
    fun `P5-IT-02 Ecrir puis lire theme = DARK`() = runTest {
        repository.setTheme(AppTheme.DARK)
        
        val result = repository.getTheme().first()
        assertEquals(AppTheme.DARK, result)
    }

    @Test
    fun `P5-IT-03 Ecrir puis lire notificationTime = 08-30`() = runTest {
        val time = LocalTime.of(8, 30)
        repository.setNotificationTime(time)
        
        val result = repository.getNotificationTime().first()
        assertEquals(time, result)
    }

    @Test
    fun `P5-IT-04 Lire sans ecriture prealable retourne les valeurs par defaut`() = runTest {
        val excluded = repository.getExcludedDays().first()
        val theme = repository.getTheme().first()
        val time = repository.getNotificationTime().first()

        assertEquals(emptySet<DayOfWeek>(), excluded)
        assertEquals(AppTheme.SYSTEM, theme)
        assertEquals(LocalTime.of(20, 0), time)
    }
}
