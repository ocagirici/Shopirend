package com.shopirend.android.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shopirend.android.data.ShopirendRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShopirendMessagingService : FirebaseMessagingService() {
    @Inject lateinit var repository: ShopirendRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { runCatching { repository.registerDeviceToken(token) } }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // FCM displays notification payloads automatically in the background.
        // Foreground in-app banners can be added here when the product needs them.
    }
}
