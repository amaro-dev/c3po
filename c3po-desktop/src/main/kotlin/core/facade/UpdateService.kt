package core.facade

import core.model.UpdateInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class UpdateService(private val httpClient: HttpClient = HttpClient.newHttpClient()) {

    suspend fun checkForUpdates(
        currentVersion: String,
        updateUrl: String = "https://api.github.com/repos/amaro-dev/c3po/releases/latest"
    ): UpdateInfo? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(updateUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                parseUpdateResponse(response.body(), currentVersion)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseUpdateResponse(responseBody: String, currentVersion: String): UpdateInfo? {
        val json = Json.parseToJsonElement(responseBody).jsonObject
        val tagName = json["tag_name"]?.jsonPrimitive?.content ?: return null
        val version = tagName.removePrefix("v")

        if (!isNewerVersion(version, currentVersion)) {
            return null
        }

        val assets = json["assets"]?.jsonArray ?: return null
        val dmgAsset = assets.find { asset ->
            val name = asset.jsonObject["name"]?.jsonPrimitive?.content ?: ""
            name.endsWith(".dmg")
        }?.jsonObject ?: return null

        val downloadUrl = dmgAsset["browser_download_url"]?.jsonPrimitive?.content ?: return null

        return UpdateInfo(
            version = version,
            downloadUrl = downloadUrl,
            checksum = null,
            releaseNotesUrl = null
        )
    }

    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        val newParts = newVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(newParts.size, currentParts.size)

        for (i in 0 until maxLength) {
            val newPart = newParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }

            when {
                newPart > currentPart -> return true
                newPart < currentPart -> return false
            }
        }
        return false
    }
}