package com.paybridge.cpvpn

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object Updater {

    @Volatile
    var checkInProgress = false

    fun checkForUpdate(context: Context): Int? {
        if (checkInProgress) return null
        checkInProgress = true
        try {
            val prefs = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
            val lastChecked = prefs.getLong(Config.PREF_UPDATE_CHECKED_AT, 0L)
            if (System.currentTimeMillis() - lastChecked < Config.UPDATE_CHECK_INTERVAL_MS) {
                return null
            }

            val releaseRaw = httpGet(Config.GITHUB_API_LATEST_RELEASE) ?: return null
            val release = try {
                JSONObject(releaseRaw)
            } catch (_: Exception) {
                return null
            }

            val assets = release.optJSONArray("assets") ?: return null
            var versionJsonUrl: String? = null
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                when (asset.optString("name", "")) {
                    "version.json" -> versionJsonUrl = asset.optString("browser_download_url", "")
                    "CPvpn.apk" -> apkUrl = asset.optString("browser_download_url", "") // FIXED: Matches CI/CD
                }
            }
            if (versionJsonUrl.isNullOrEmpty() || apkUrl.isNullOrEmpty()) return null

            val versionRaw = httpGet(versionJsonUrl) ?: return null
            val versionJson = try {
                JSONObject(versionRaw)
            } catch (_: Exception) {
                return null
            }
            val remoteCode = versionJson.optInt("versionCode", 0)

            prefs.edit()
                .putLong(Config.PREF_UPDATE_CHECKED_AT, System.currentTimeMillis())
                .apply()

            if (remoteCode > BuildConfig.VERSION_CODE) {
                prefs.edit()
                    .putString(Config.PREF_STAGED_UPDATE_URL, apkUrl)
                    .putInt(Config.PREF_STAGED_UPDATE_VERSION, remoteCode)
                    .apply()
                return remoteCode
            }
            return null
        } finally {
            checkInProgress = false
        }
    }

    fun downloadApk(context: Context): Boolean {
        if (!isUnmetered(context)) return false

        val prefs = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
        val url = prefs.getString(Config.PREF_STAGED_UPDATE_URL, null) ?: return false

        val dir = File(context.cacheDir, Config.UPDATER_SUBDIR)
        if (!dir.exists() && !dir.mkdirs()) return false
        val finalFile = File(dir, Config.UPDATER_APK_NAME)
        val tmpFile = File(dir, "${Config.UPDATER_APK_NAME}.part")
        if (tmpFile.exists()) tmpFile.delete()

        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = Config.HTTP_CONNECT_TIMEOUT_MS
                readTimeout = 60_000
                setRequestProperty("User-Agent", Config.UPDATER_USER_AGENT)
                setRequestProperty("Accept", "application/octet-stream")
                instanceFollowRedirects = true
            }
            val code = conn.responseCode
            if (code !in 200..299) {
                conn.disconnect()
                return false
            }
            conn.inputStream.use { input ->
                tmpFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            conn.disconnect()

            if (!tmpFile.exists() || tmpFile.length() == 0L) return false
            if (finalFile.exists()) finalFile.delete()
            if (!tmpFile.renameTo(finalFile)) return false

            prefs.edit()
                .putString(Config.PREF_STAGED_UPDATE_PATH, finalFile.absolutePath)
                .apply()
            return true
        } catch (_: Exception) {
            if (tmpFile.exists()) tmpFile.delete()
            return false
        }
    }

    fun isUpdateStaged(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
        val path = prefs.getString(Config.PREF_STAGED_UPDATE_PATH, null) ?: return false
        val stagedVersion = prefs.getInt(Config.PREF_STAGED_UPDATE_VERSION, 0)
        if (stagedVersion <= BuildConfig.VERSION_CODE) return false
        val file = File(path)
        return file.exists() && file.length() > 0L
    }

    fun installStagedUpdate(context: Context) {
        val prefs = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
        val path = prefs.getString(Config.PREF_STAGED_UPDATE_PATH, null) ?: return
        val file = File(path)
        if (!file.exists()) return

        try {
            val uri = FileProvider.getUriForFile(
                context,
                Config.UPDATER_FILEPROVIDER_AUTHORITY,
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun clearStagedUpdate(context: Context) {
        val prefs = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
        val dir = File(context.cacheDir, Config.UPDATER_SUBDIR)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                try { file.delete() } catch (_: Exception) {}
            }
        }
        prefs.edit()
            .remove(Config.PREF_STAGED_UPDATE_URL)
            .remove(Config.PREF_STAGED_UPDATE_PATH)
            .remove(Config.PREF_STAGED_UPDATE_VERSION)
            .apply()
    }

    private fun isUnmetered(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    private fun httpGet(url: String): String? {
        return try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = Config.HTTP_CONNECT_TIMEOUT_MS
                readTimeout = Config.HTTP_READ_TIMEOUT_MS
                setRequestProperty("User-Agent", Config.UPDATER_USER_AGENT)
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()
            if (code in 200..299) body else null
        } catch (_: Exception) {
            null
        }
    }
}
