package com.nandkishor.fileserver.server

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ServerPrefs {
    private val Context.dataStore by preferencesDataStore(name = "server_prefs")
    private val KEY_IS_RUNNING = booleanPreferencesKey("is_running")

    suspend fun setServerRunning(context: Context, isRunning: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_RUNNING] = isRunning
        }
    }

    fun isServerRunningFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data
            .map { prefs -> prefs[KEY_IS_RUNNING] == true }
    }
}
