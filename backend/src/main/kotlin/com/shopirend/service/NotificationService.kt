package com.shopirend.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import com.shopirend.repository.DeviceTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

interface NotificationService {
    fun send(userIds: Collection<UUID>, title: String, body: String, data: Map<String, String> = emptyMap())
}

@Service
@ConditionalOnProperty(name = ["app.fcm.enabled"], havingValue = "false", matchIfMissing = true)
class LoggingNotificationService : NotificationService {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun send(userIds: Collection<UUID>, title: String, body: String, data: Map<String, String>) {
        logger.info("Notification to {}: {} — {} ({})", userIds, title, body, data)
    }
}

@Service
@ConditionalOnProperty(name = ["app.fcm.enabled"], havingValue = "true")
class FirebaseNotificationService(private val tokens: DeviceTokenRepository) : NotificationService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val messaging: FirebaseMessaging by lazy {
        val app = FirebaseApp.getApps().firstOrNull() ?: FirebaseApp.initializeApp(
            FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).build()
        )
        FirebaseMessaging.getInstance(app)
    }

    override fun send(userIds: Collection<UUID>, title: String, body: String, data: Map<String, String>) {
        val deviceTokens = tokens.findByUserIdIn(userIds).map { it.token }.distinct()
        deviceTokens.chunked(500).forEach { chunk ->
            runCatching {
                messaging.sendEachForMulticast(
                    MulticastMessage.builder()
                        .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                        .putAllData(data)
                        .addAllTokens(chunk)
                        .build()
                )
            }.onFailure { logger.error("Unable to send FCM notification", it) }
        }
    }
}

