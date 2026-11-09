# 🛡️ CPvpn - Android Client (Cerberus Protocol)

**Package ID:** `com.paybridge.cpvpn`  
**Product:** CPvpn (A SASE Platform by PayBridge)  
**Repository:** `Navscan/CPvpn-App`

## 📱 Project Overview

CPvpn is a lightweight SASE (Secure Access Service Edge) Android application powered by WireGuard. It operates on a 3-tier architecture:

1.  **Open (Free):** Silent guest registration. Local (SA) routing only. 14-day free trial limit.
2.  **Pro (B2C):** In-app Paystack billing. Unlocks US/Global endpoints and bypasses geo-restrictions.
3.  **Enterprise (B2B):** Requires a corporate login and a 16-character voucher. Unlocks the **Cerberus Threat Intel** dashboard (live Fail2ban monitoring, GeoIP tracking).

## 🏗️ Architecture & Tech Stack

*   **Language:** Kotlin
*   **VPN Engine:** WireGuard (via `GoBackend`)
*   **Backend API:** Node.js (`/var/www/ttvpn-server/` on VPS)
*   **Database:** MongoDB Atlas (`ttvpn` database)
*   **Auth:** JWT (JSON Web Tokens)

## 📂 Critical File Structure

All Kotlin files are located in `app/src/main/java/com/paybridge/cpvpn/`:

*   `MainActivity.kt`: The core UI, state manager, and tier flipper.
*   `AuthClient.kt`: Handles JWT, Guest registration, and Login.
*   `ApiClient.kt`: Handles WireGuard provisioning and Paystack initiation.
*   `ConfigStore.kt`: Manages local `SharedPreferences` (JWT, tier, voucher, WG config).
*   `VpnService.kt`: The actual WireGuard tunnel service.

## 🚀 Build & Deployment

**Do NOT build locally on the VPS** (limited RAM). 
The app is automatically compiled via **GitHub Actions** on every push to `main`.

To trigger a build:
```bash
cd /root/CPvpn-App
git add .
git commit -m "feat: your update"
git push origin main
