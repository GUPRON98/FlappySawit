# 🔥 Flappy Sawit - Game Flappy Bird Tema Kebakaran Kelapa Sawit

## Tentang Game
Game Flappy Bird dengan background kebakaran hutan kelapa sawit. Burung pakai masker terbang melewati batang sawit yang terbakar!

## Fitur
- 🌴 Background kebakaran hutan sawit
- 🔥 Efek api & asap realistis
- 🐦 Burung pakai masker (karena asap!)
- 📊 Skor & High Score tersimpan
- 📱 Responsive untuk semua ukuran layar
- 🎮 Kontrol: Tap layar / Spasi

## Cara Build APK

### Metode 1: Build Manual di Termux (Direkomendasikan)

```bash
# 1. Install tools yang diperlukan
pkg update && pkg install -y aapt dx ecj apksigner

# 2. Jalankan build script
cd ~/FlappySawit
chmod +x build_apk.sh
bash build_apk.sh

# 3. Install APK
termux-open ~/FlappySawit/FlappySawit.apk
```

### Metode 2: Gradle Build

```bash
pkg install -y gradle openjdk-17
cd ~/FlappySawit
chmod +x build_gradle.sh
bash build_gradle.sh
```

### Metode 3: Test di Browser dulu

Buka file `app/src/main/assets/game.html` langsung di browser untuk test game sebelum build APK.

```bash
termux-open app/src/main/assets/game.html
```

## Struktur Project

```
FlappySawit/
├── build_apk.sh              # Script build APK (Termux)
├── build_gradle.sh            # Alternatif Gradle build
├── build.gradle               # Gradle config
├── settings.gradle
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   └── game.html      # 🎮 GAME FILE (HTML5 Canvas)
│       ├── java/com/flappysawit/game/
│       │   └── MainActivity.java
│       └── res/
│           ├── values/colors.xml
│           └── mipmap-anydpi-v26/ic_launcher.xml
```

## Pesan Lingkungan
🌳 **Stop Pembakaran Hutan!** Game ini dibuat untuk mengingatkan bahaya pembakaran lahan kelapa sawit bagi lingkungan dan kesehatan.
