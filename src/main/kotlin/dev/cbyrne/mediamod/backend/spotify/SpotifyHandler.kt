package dev.cbyrne.mediamod.backend.spotify

import com.google.gson.JsonObject
import com.uchuhimo.konf.Config
import dev.cbyrne.mediamod.backend.MediaModBackend
import dev.cbyrne.mediamod.backend.config.ConfigurationSpec
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.http.Parameters
import java.io.File

class SpotifyHandler {
    private var configExists = true
    private lateinit var config: Config

    init {
        if(!File("config.json").exists()) {
            configExists = false
            MediaModBackend.logger.warn("Config.json doesn't exist!")
        } else {
            config = Config { addSpec(ConfigurationSpec) }.from.json.file(File("config.json"))
        }
    }


    private val http = HttpClient(Apache) {
        if(configExists) {
            install(Auth) {
                basic {
                    username = config[ConfigurationSpec.spotifyClientID]
                    password = config[ConfigurationSpec.spotifyClientSecret]
                    sendWithoutRequest = true
                }
            }
        }
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
                setPrettyPrinting()
            }
        }
    }

    suspend fun getTokensFromCode(code: String): JsonObject {
        if(!configExists) {
            val response = JsonObject()
            response.addProperty("status", "Internal Server Error")
            return response
        }

        return try {
            http.post("https://accounts.spotify.com/api/token") {
                body = FormDataContent(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("redirect_uri", config[ConfigurationSpec.spotifyRedirectURI])
                })
            }
        } catch (e: Exception) {
            val error = JsonObject()
            error.addProperty("error", e.localizedMessage)
            error
        }
    }

    suspend fun getRefreshToken(refreshToken: String): JsonObject {
        if(!configExists) {
            val response = JsonObject()
            response.addProperty("status", "Internal Server Error")
            return response
        }

        return try {
            http.post("https://accounts.spotify.com/api/token") {
                body = FormDataContent(Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                })
            }
        } catch (e: Exception) {
            val error = JsonObject()
            error.addProperty("error", e.localizedMessage)
            error
        }
    }
}