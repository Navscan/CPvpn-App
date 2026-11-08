package com.paybridge.cpvpn

object Config {
    // --- API Base URL ---
    const val BASE_URL = "https://cpvpn.flipstack.store/api"

    // --- HTTP Timeouts ---
    const val HTTP_CONNECT_TIMEOUT_MS = 15000
    const val HTTP_READ_TIMEOUT_MS = 15000

    // --- API Endpoints ---
    const val ENDPOINT_AUTH_LOGIN = "/auth/login"
    const val ENDPOINT_AUTH_REGISTER = "/auth/register"
    const val ENDPOINT_AUTH_GUEST = "/auth/guest"
    const val ENDPOINT_AUTH_ME = "/auth/me"
    const val ENDPOINT_PROVISION = "/config/provision"
    const val ENDPOINT_PAY_INITIATE = "/pay/initiate"
    const val ENDPOINT_CERBERUS = "/cerberus"

    // --- Shared Preferences Keys ---
    const val PREFS_NAME = "cpvpn_prefs"
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_TIER = "user_tier"
    const val PREF_VOUCHER_CODE = "voucher_code"
    const val PREF_WG_CONFIG = "wg_config"
    const val PREF_WG_CONFIG_ORIGINAL = "wg_config_original"
    const val PREF_ASSIGNED_IP = "assigned_ip"
    const val PREF_SUBSCRIPTION_EXPIRES = "subscription_expires"
    const val PREF_WG_ENDPOINT = "wg_endpoint"
    const val PREF_WG_SERVER_FLAG = "wg_server_flag"
    const val PREF_WG_SERVER_COUNTRY = "wg_server_country"
    const val PREF_CONFIGURED_AT = "configured_at"
    const val PREF_CUSTOM_DNS = "custom_dns"
    const val PREF_VPN_RUNNING = "vpn_running"
    const val PREF_TUNNEL_STARTED_AT = "tunnel_started_at"
    const val PREF_LOCK_ENABLED = "lock_enabled"
    const val PREF_LOG_ENABLED = "log_enabled"
    const val PREF_EVENT_LOG = "event_log"

    // --- Updater Preferences ---
    const val PREF_UPDATE_CHECKED_AT = "update_checked_at"
    const val PREF_STAGED_UPDATE_URL = "staged_update_url"
    const val PREF_STAGED_UPDATE_PATH = "staged_update_path"
    const val PREF_STAGED_UPDATE_VERSION = "staged_update_version"

    // --- App Constants ---
    const val LOG_MAX_ENTRIES = 500
    const val STATE_POLL_INTERVAL_MS = 1000L
    const val DNS_RESTART_DELAY_MS = 2000L
    const val UPDATE_CHECK_INTERVAL_MS = 86400000L // 24 hours
    const val STATS_POLL_INTERVAL_MS = 1000L
    const val RECONNECT_DELAY_MS = 3000L

    // --- VPN Service Constants ---
    const val TUNNEL_NAME = "cpvpn_tunnel"
    const val NOTIFICATION_CHANNEL_ID = "cpvpn_vpn_channel"
    const val NOTIFICATION_ID = 1337

    // --- Server Display Details ---
    const val WG_ENDPOINT_DISPLAY = "cpvpn.flipstack.store"
    const val WG_SERVER_FLAG = "🇿🇦"
    const val WG_SERVER_COUNTRY = "South Africa"

    // --- Updater Config ---
    const val GITHUB_API_LATEST_RELEASE = "https://api.github.com/repos/Navscan/CPvpn-App/releases/latest"
    const val UPDATER_SUBDIR = "updates"
    const val UPDATER_APK_NAME = "CPvpn.apk"
    const val UPDATER_USER_AGENT = "CPvpn-Updater"
    const val UPDATER_FILEPROVIDER_AUTHORITY = "com.paybridge.cpvpn.fileprovider"
}
