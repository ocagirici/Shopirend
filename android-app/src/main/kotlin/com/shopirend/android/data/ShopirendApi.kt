package com.shopirend.android.data

import com.shopirend.android.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

class ApiFailure(message: String) : RuntimeException(message)

@Singleton
class ShopirendApi @Inject constructor(private val session: SessionManager) {
    @PublishedApi internal val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    @PublishedApi internal val client = HttpClient(OkHttp) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        defaultRequest {
            url(BuildConfig.API_BASE_URL)
            contentType(ContentType.Application.Json)
            session.currentToken?.let { bearerAuth(it) }
        }
    }

    suspend inline fun <reified T> get(path: String): T = execute(client.get(path))
    suspend inline fun <reified T, reified B> post(path: String, body: B): T = execute(client.post(path) { setBody(body) })
    suspend inline fun <reified T, reified B> put(path: String, body: B): T = execute(client.put(path) { setBody(body) })
    suspend inline fun <reified T, reified B> patch(path: String, body: B): T = execute(client.patch(path) { setBody(body) })

    suspend fun delete(path: String) {
        val response = client.delete(path)
        if (!response.status.isSuccess()) throw ApiFailure(errorMessage(response.bodyAsText()))
    }

    suspend fun postNoContent(path: String, body: DeviceTokenRequest) {
        val response = client.post(path) { setBody(body) }
        if (!response.status.isSuccess()) throw ApiFailure(errorMessage(response.bodyAsText()))
    }

    suspend inline fun <reified T> execute(response: io.ktor.client.statement.HttpResponse): T {
        if (!response.status.isSuccess()) throw ApiFailure(errorMessage(response.bodyAsText()))
        return response.body()
    }

    @PublishedApi internal fun errorMessage(raw: String): String = runCatching { json.decodeFromString<ErrorResponse>(raw).message }.getOrDefault("The server could not complete the request")
}

