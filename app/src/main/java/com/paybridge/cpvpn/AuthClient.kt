package com.paybridge.cpvpn

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthResult {
    data class Success(val token: String, val tier: String) : AuthResult()
    data class Failure(val error: String) : AuthResult()
    object NetworkError : AuthResult()
}

object AuthClient {

    private fun postAuth(endpoint: String, jsonBody: String): AuthResult {
        return try {
            // FIXED: Removed the extra /api from the URL construction
            val url = URL(Config.BASE_URL + endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = Config.HTTP_CONNECT_TIMEOUT_MS
                readTimeout = Config.HTTP_READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(jsonBody)
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            val inputStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = reader.readText()
            reader.close()

            val json = JSONObject(response)
            
            if (responseCode in 200..299 && json.optBoolean("ok", false)) {
                val token = json.getString("token")
                val tier = json.optString("tier", "free")
                AuthResult.Success(token, tier)
            } else {
                val error = json.optString("error", "unknown_error")
                AuthResult.Failure(error)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.NetworkError
        }
    }

    fun login(email: String, password: String): AuthResult {
        val body = JSONObject().put("email", email).put("password", password).toString()
        return postAuth(Config.ENDPOINT_AUTH_LOGIN, body)
    }

    fun register(email: String, password: String): AuthResult {
        val body = JSONObject().put("email", email).put("password", password).toString()
        return postAuth(Config.ENDPOINT_AUTH_REGISTER, body)
    }

    fun guestLogin(deviceId: String): AuthResult {
        val body = JSONObject().put("device_id", deviceId).toString()
        return postAuth(Config.ENDPOINT_AUTH_GUEST, body)
    }

    fun updateEmail(token: String, newEmail: String): AuthResult {
        return try {
            // FIXED: Removed the extra /api from the URL construction
            val url = URL(Config.BASE_URL + "/auth/update-email")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = Config.HTTP_CONNECT_TIMEOUT_MS
                readTimeout = Config.HTTP_READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
            }

            val body = JSONObject().put("new_email", newEmail).toString()
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(body)
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            val inputStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = reader.readText()
            reader.close()

            val json = JSONObject(response)
            
            if (responseCode in 200..299 && json.optBoolean("ok", false)) {
                val newToken = json.getString("token")
                AuthResult.Success(newToken, "free")
            } else {
                val error = json.optString("error", "unknown_error")
                AuthResult.Failure(error)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.NetworkError
        }
    }

    // FIXED: Renamed back to checkLicense so MainActivity can find it
    fun checkLicense(token: String): AuthResult {
        return try {
            val url = URL(Config.BASE_URL + Config.ENDPOINT_AUTH_ME)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = Config.HTTP_CONNECT_TIMEOUT_MS
                readTimeout = Config.HTTP_READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
            }

            val responseCode = conn.responseCode
            val inputStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = reader.readText()
            reader.close()

            val json = JSONObject(response)
            
            if (responseCode in 200..299 && json.optBoolean("ok", false)) {
                val tier = json.optString("tier", "free")
                AuthResult.Success(token, tier)
            } else {
                AuthResult.Failure("session_expired")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.NetworkError
        }
    }
}
