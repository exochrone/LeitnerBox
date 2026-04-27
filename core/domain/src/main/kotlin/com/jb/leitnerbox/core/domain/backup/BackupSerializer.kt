package com.jb.leitnerbox.core.domain.backup

import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class BackupSerializer {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun serialize(data: BackupData): ByteArray {
        val jsonString = json.encodeToString(BackupData.serializer(), data)
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { gzip ->
            gzip.write(jsonString.toByteArray(Charsets.UTF_8))
        }
        return outputStream.toByteArray()
    }

    fun deserialize(bytes: ByteArray): BackupData {
        return try {
            val jsonString = GZIPInputStream(bytes.inputStream()).use { gzip ->
                gzip.bufferedReader(Charsets.UTF_8).readText()
            }
            json.decodeFromString(BackupData.serializer(), jsonString)
        } catch (e: Exception) {
            throw BackupException.InvalidFile(e.message ?: "Fichier invalide")
        }
    }
}

sealed class BackupException(message: String) : Exception(message) {
    class InvalidFile(detail: String) : BackupException("Fichier de sauvegarde invalide : $detail")
    class IncompatibleVersion(found: Int, supported: Int) :
        BackupException("Version incompatible : fichier v$found, app supporte v$supported")
}
