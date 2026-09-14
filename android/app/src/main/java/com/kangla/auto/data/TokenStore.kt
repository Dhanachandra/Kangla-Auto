package com.kangla.auto.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kangla.auto.data.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "kangla_session")

data class Session(
    val token: String,
    val user: UserDto,
)

class TokenStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val tokenKey = stringPreferencesKey("token")
    private val userKey = stringPreferencesKey("user")

    val session: Flow<Session?> = context.dataStore.data.map { prefs ->
        val token = prefs[tokenKey] ?: return@map null
        val rawUser = prefs[userKey] ?: return@map null
        runCatching { Session(token, json.decodeFromString<UserDto>(rawUser)) }.getOrNull()
    }

    suspend fun save(token: String, user: UserDto) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[userKey] = json.encodeToString(UserDto.serializer(), user)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}