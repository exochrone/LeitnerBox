package com.jb.leitnerbox.core.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.jb.leitnerbox.core.data.AppSettingsProto
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettingsProto> {
    override val defaultValue: AppSettingsProto = AppSettingsProto.newBuilder()
        .setNotificationHour(20)
        .setNotificationMinute(0)
        .setTheme(0) // SYSTEM
        .build()

    override suspend fun readFrom(input: InputStream): AppSettingsProto {
        try {
            return AppSettingsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AppSettingsProto, output: OutputStream) = t.writeTo(output)
}
