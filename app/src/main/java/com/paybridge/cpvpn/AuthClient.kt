package com.paybridge.cpvpn

import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthResult {
    data class Success(val token: String, val tier: String) : AuthResult()
    data class Failure(val error: String) : AuthResult()
    object NetworkError : AuthResult()
}

data class LicenseResult(val isActive: Boolean, val tier: String)

object AuthClient {

    fun guestLogin(deviceId: String): AuthResult {
        return try {
            val url = URL(Config.ENDPOINT_AUTH_GUEST)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = Config.HTTP_CONNECT_TIMEOUT_MS
                readTimeout = Config.HTTP_READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            // THE FIX: Change "deviceId" to "device_id" to match the backend
            val body = JSONObject().put("device_id", deviceId).toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            conn.disconnect()

            if (code in 200..299) {
                val json = JSONObject(response)
                if (json.optBoolean("ok", false)) {
                    AuthResult.Success(
                        token = json.getString("token"),
                        tier = json.optString("tier", "free")
                    )
                } else {
                    AuthResult.Failure(json.optString("error", "unknown"))
                }
            } else {
                AuthResult.Failure("HTTP $code: $response")
            }
        } catch (e: Exception) {
            AuthResult.NetworkError
        }
    }

    fun updateEmail(token: String, newEmail: String): AuthResult {
        return try {
            val url = URL(Config.BASE_URL + "/api/auth/update-email")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = Config.HTTP_CONNECT_TIMEOUT_MS
                readTimeout = Config.HTTP_READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
            }
            val body = JSONObject().put("newEmail", newEmail).toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            conn.disconnect()

            if (code in 200..299) {
                val json = JSONObject(response)
                if (json.optBoolean("ok", false)) {
                    AuthResult.Success(
                        token = json.getString("token"),
                        tier = json.optString("tier", "free")
                    )
                } else {
                    AuthResult.Failure(json.optString("error", "unknown"))
                }
            } else {
                AuthResult.Failure("HTTP $code: $response")
            }
        } catch (e: Exception) {
            AuthResult.NetworkError
        }
    }

    fun checkLicense(token: String): LicenseResult {
        return try {
            val url = URL(Config.BASE_URL + "/api/license/me")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = Config.HTTP_CONNECT_TIMEOUT_MS
                readTimeout = Config.HTTP_READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            conn.disconnect()

            if (code in 200..299) {
                val json = JSONObject(response)
                LicenseResult(
                    isActive = json.optBoolean("is_active", false),
                    tier = json.optString("tier", "free")
                )
            } else {
                LicenseResult(false, "free")
            }
        } catch (e: Exception) {
            LicenseResult(false, "free")
        }
    }
}
