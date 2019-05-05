package com.piotrwilczek.newliner.github

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.receive
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpHeaders
import java.net.URL
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

/**
 * Github provides .pem files. To get key from .pem file run:
 * openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in file.pem -out out.key
 * Remove first and last line, remove newline characters and use as string.
 * Ref: https://stackoverflow.com/a/39327439
 */
class ApiClient(val appId: String, val key: String) {

    private val config: HttpClientConfig<*>.() -> Unit = {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    private val httpClient = HttpClient(config)
    private var installationId: Long? = null
    private var token: String? = null
    private var tokenExpirationDate: Calendar? = null

    private val algorithm: Algorithm by lazy {
        val keyBytes = Base64.getDecoder().decode(key)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyBytes)) as RSAPrivateKey
        Algorithm.RSA256(null, privateKey)
    }

    suspend fun authenticate(installationId: Long) {
        if (installationId == this.installationId && token != null && now().before(tokenExpirationDate)) {
            return
        }

        val tokenExpirationDate = now()
        tokenExpirationDate.add(Calendar.MINUTE, 9)
        val jwtToken = JWT.create()
            .withIssuer(appId)
            .withIssuedAt(Date())
            .withExpiresAt(tokenExpirationDate.time)
            .sign(algorithm)

        val response = httpClient.post<TokenResponse> {
            header(HttpHeaders.Authorization, "Bearer $jwtToken")
            header(HttpHeaders.Accept, "application/vnd.github.machine-man-preview+json")
            url(URL("https://api.github.com/app/installations/$installationId/access_tokens"))
        }

        token = response.token
        this.tokenExpirationDate = tokenExpirationDate
        this.installationId = installationId
    }

    suspend fun getRepositories(): List<Repository> {
        val repositories = mutableListOf<Repository>()
        var url = "https://api.github.com/installation/repositories"
        while (true) {
            val response = httpClient.get<HttpResponse> {
                header(HttpHeaders.Authorization, "token $token")
                header(HttpHeaders.Accept, "application/vnd.github.machine-man-preview+json")
                url(URL(url))
            }
            repositories.addAll(response.receive<Repositories>().repositories)
            url = response.headers[HttpHeaders.Link]?.parseNextLinkHeader() ?: break
        }
        return repositories.toList()
    }

    fun getAuthToken() = token ?: throw IllegalStateException("Not authenticated.")

    private fun String.parseNextLinkHeader(): String? {
        val links = this.split(",")
        for (link in links) {
            val segments = link.split(";")
            if (segments.size < 2) continue
            if (segments[1].trim() == "rel=\"next\"") {
                val linkPart = segments[0].trim()
                if (!linkPart.startsWith("<") || !linkPart.endsWith(">")) continue
                return linkPart.substring(1, linkPart.length - 1)
            }
        }
        return null
    }

    private fun now() = Calendar.getInstance()
}
