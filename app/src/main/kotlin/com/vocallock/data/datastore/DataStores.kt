package com.vocallock.data.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.vocallock.data.datastore.proto.GlobalSettingsPrefs
import com.vocallock.data.datastore.proto.HardenedStatePrefs
import com.vocallock.data.datastore.proto.NudgeStatePrefs
import com.vocallock.data.datastore.proto.SplashPrefs
import java.io.InputStream
import java.io.OutputStream

// ── Serializers ───────────────────────────────────────────────

object HardenedStateSerializer : Serializer<HardenedStatePrefs> {
    override val defaultValue: HardenedStatePrefs = HardenedStatePrefs.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): HardenedStatePrefs =
        HardenedStatePrefs.parseFrom(input)

    override suspend fun writeTo(t: HardenedStatePrefs, output: OutputStream) = t.writeTo(output)
}

object SplashPrefsSerializer : Serializer<SplashPrefs> {
    override val defaultValue: SplashPrefs = SplashPrefs.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): SplashPrefs = SplashPrefs.parseFrom(input)
    override suspend fun writeTo(t: SplashPrefs, output: OutputStream) = t.writeTo(output)
}

object NudgeStateSerializer : Serializer<NudgeStatePrefs> {
    override val defaultValue: NudgeStatePrefs = NudgeStatePrefs.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): NudgeStatePrefs =
        NudgeStatePrefs.parseFrom(input)

    override suspend fun writeTo(t: NudgeStatePrefs, output: OutputStream) = t.writeTo(output)
}

// ── Global Settings Serializer ──────────

object GlobalSettingsSerializer : Serializer<GlobalSettingsPrefs> {
    override val defaultValue: GlobalSettingsPrefs = GlobalSettingsPrefs.newBuilder()
        .setMaxGridTiles(4)
        .setNudgeThreshold(3)
        .build()

    override suspend fun readFrom(input: InputStream): GlobalSettingsPrefs {
        try {
            val prefs = GlobalSettingsPrefs.parseFrom(input)

            // Proto3 defaults to 0. If it's 0, we apply your custom defaults.
            val tiles = if (prefs.maxGridTiles == 0) 4 else prefs.maxGridTiles
            val threshold = if (prefs.nudgeThreshold == 0) 3 else prefs.nudgeThreshold

            return prefs.toBuilder()
                .setMaxGridTiles(tiles)
                .setNudgeThreshold(threshold)
                .build()
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read global settings proto.", exception)
        }
    }

    override suspend fun writeTo(t: GlobalSettingsPrefs, output: OutputStream) = t.writeTo(output)
}

// ── DataStore Extension Properties ────────────────────────────

val Context.hardenedStateDataStore: DataStore<HardenedStatePrefs> by dataStore(
    fileName = "hardened_state.pb", serializer = HardenedStateSerializer
)
val Context.splashPrefsDataStore: DataStore<SplashPrefs> by dataStore(
    fileName = "splash_prefs.pb", serializer = SplashPrefsSerializer
)
val Context.nudgeStateDataStore: DataStore<NudgeStatePrefs> by dataStore(
    fileName = "nudge_state.pb", serializer = NudgeStateSerializer
)
val Context.globalSettingsDataStore: DataStore<GlobalSettingsPrefs> by dataStore(
    fileName = "global_settings.pb", serializer = GlobalSettingsSerializer
)