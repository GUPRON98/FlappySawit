#!/data/data/com.termux/files/usr/bin/bash
# =============================================
#  BUILD SCRIPT - Flappy Sawit Pure Native APK
#  Modern Fullscreen Edition (API 33, Max Aspect 3.0)
# =============================================

set -e

PROJECT_DIR="$HOME/FlappySawit"
cd "$PROJECT_DIR"

echo "🔥 [1/5] Menyiapkan direktori build..."
rm -rf build
mkdir -p build/gen build/obj build/apk

echo "🔨 [2/5] Generate R.java (Target SDK 33)..."
aapt package -f -m \
  -J build/gen \
  -M app/src/main/AndroidManifest.xml \
  -S app/src/main/res \
  -I android.jar \
  --min-sdk-version 21 \
  --target-sdk-version 33

echo "☕ [3/5] Mengompilasi Java murni (SurfaceView Engine)..."
javac -cp "android.jar:build/gen" \
  -d build/obj \
  $(find build/gen -name "*.java") \
  $(find app/src/main/java -name "*.java")

d8 --lib android.jar \
   --output build/apk \
   $(find build/obj -name "*.class")

echo "📦 [4/5] Mengemas APK (Full Aspect 3.0) & Zipalign..."
aapt package -f \
  -M app/src/main/AndroidManifest.xml \
  -S app/src/main/res \
  -I android.jar \
  --min-sdk-version 21 \
  --target-sdk-version 33 \
  -F build/unaligned.apk

cd build/apk
aapt add ../unaligned.apk classes.dex
cd "$PROJECT_DIR"

zipalign -v -p 4 build/unaligned.apk build/aligned.apk

echo "🔐 [5/5] Menandatangani APK..."
if [ ! -f flappysawit.keystore ]; then
  keytool -genkeypair -v \
    -keystore flappysawit.keystore \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -alias flappysawit \
    -storepass sawit123 \
    -keypass sawit123 \
    -dname "CN=FlappySawit, OU=Game, O=Sawit, L=ID, ST=ID, C=ID"
fi

mkdir -p /sdcard/ai
apksigner sign \
  --ks flappysawit.keystore \
  --ks-key-alias flappysawit \
  --ks-pass pass:sawit123 \
  --key-pass pass:sawit123 \
  --out /sdcard/ai/FlappySawit.apk \
  build/aligned.apk

cp -f /sdcard/ai/FlappySawit.apk /sdcard/FlappySawit.apk 2>/dev/null || true

echo "🎉 BERHASIL! APK Fullscreen tersimpan di /sdcard/ai/FlappySawit.apk"
