#!/usr/bin/env bash
# =====================================================================
# FILE: /root/CPvpn-App/deploy-apk.sh
# PURPOSE: Build the CPvpn Android APK and stage it for distribution.
# =====================================================================
set -euo pipefail

# DebugOTool Palette
RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; GOLD=$'\033[0;33m'; TEAL=$'\033[0;36m'; NC=$'\033[0m'

log() { echo "${TEAL}[BUILD]${NC} $*"; }
ok()  { echo "${GREEN}  ✔${NC} $*"; }
err() { echo "${RED}  ✘${NC} $*" >&2; exit 1; }

cd /root/CPvpn-App || err "Failed to find /root/CPvpn-App"

log "Cleaning previous builds..."
./gradlew clean >/dev/null 2>&1 || true

log "Assembling Release APK (this may take a minute)..."
# We use assembleRelease. If you do not have a signing config in build.gradle,
# use assembleDebug instead.
./gradlew assembleRelease || err "Gradle build failed"

# Find the generated APK (handles both release and debug outputs)
APK_PATH=$(find app/build/outputs/apk -name "*.apk" | head -n 1)

if [ -z "$APK_PATH" ]; then
    err "No APK found. Check Gradle output."
fi

# Copy to a predictable location
cp "$APK_PATH" /root/CPvpn-App/CPvpn-latest.apk
ok "APK built successfully!"
ok "Location: ${GOLD}/root/CPvpn-App/CPvpn-latest.apk${NC}"

log "Generating SHA-256 fingerprint for App Links verification:"
keytool -printcert -jarfile /root/CPvpn-App/CPvpn-latest.apk | grep SHA256 || echo "Could not extract SHA256 (ensure keytool is installed)"

echo ""
echo "${GOLD}NEXT STEPS:${NC}"
echo "1. Download the APK to your phone."
echo "2. Install it (you may need to uninstall the old version first if the signature matches)."
echo "3. Test the LOGOUT -> LOGIN -> Enter Voucher flow."
