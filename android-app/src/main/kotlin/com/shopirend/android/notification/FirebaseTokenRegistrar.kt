package com.shopirend.android.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.shopirend.android.BuildConfig
import com.shopirend.android.data.ShopirendRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTokenRegistrar @Inject constructor(private val repository: ShopirendRepository) {
    suspend fun registerIfConfigured() {
        if (!BuildConfig.FCM_CONFIGURED) return
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            repository.registerDeviceToken(token)
        }
    }
}

