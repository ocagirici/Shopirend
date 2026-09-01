package com.shopirend.android.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("shopirend_session")

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val tokenKey = stringPreferencesKey("access_token")
    @Volatile var currentToken: String? = null
        private set

    suspend fun restore(): String? = context.dataStore.data.first()[tokenKey].also { currentToken = it }

    suspend fun save(token: String) {
        currentToken = token
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear() {
        currentToken = null
        context.dataStore.edit { it.remove(tokenKey) }
    }
}

