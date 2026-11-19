#!/bin/bash
# =============================================
#  ALTERNATIVE: Gradle Build (jika Gradle terinstall)
# =============================================
cd "$HOME/FlappySawit"

echo "🔥 Building Flappy Sawit with Gradle..."

# Install gradle jika belum
if ! command -v gradle &> /dev/null; then
    echo "Installing gradle..."
    pkg install -y gradle
fi

gradle assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "$HOME/FlappySawit/FlappySawit.apk"
    echo "✅ APK ready: $HOME/FlappySawit/FlappySawit.apk"
else
    echo "❌ Build failed. Gunakan build_apk.sh sebagai alternatif."
fi
