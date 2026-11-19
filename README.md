# 🔥 Flappy Sawit - Game Flappy Bird Tema Kebakaran Kelapa Sawit

## 📥 Download Game (Siap Install di Android)
Langsung unduh file APK resmi yang sudah ditandatangani:
👉 [**Download FlappySawit.apk**](https://github.com/GUPRON98/FlappySawit/raw/main/FlappySawit.apk)

## Tentang Game
Game Flappy Bird bernuansa kebakaran rimba gambut perkebunan kelapa sawit. Kendalikan burung tangguh bermasker menembus rintangan pohon sawit dan kobaran api hutan!

## Fitur Unggulan
- 🌴 **Background Kebakaran Rimba Sawit**: Multi-layer paralaks kanopi sawit, gunung berapi, awan asap, dan percikan bara api (*embers*).
- ⛰️ **Lapisan Tanah Gambut & Perkebunan (*Peatland Earth*)**: Penampang tanah subur berlapis, batuan alami, jalinan akar serabut sawit, dan rerumputan tropis berujung keemasan.
- 🔊 **Sound Design Modern (*Zero-Latency Audio*)**: Suara kepakan aerodinamis (*whoosh-pop*), denting bel kristal saat meraih skor, benturan sinematik *sub-bass impact*, dan *bubble pop* pada tombol.
- 🏠 **Tombol Menu Utama di Layar Game Over**: Opsi langsung kembali ke menu utama atau mencoba kembali dengan sekali sentuh.
- 🐦 **Wardrobe & Custom Skins**: Pilihan skin karakter burung dan tema rintangan sawit.
- 📊 **Skor & Rekor Tertinggi**: Tersimpan otomatis secara lokal.
- 📱 **Fullscreen Immersive Native**: Berjalan 100% mulus (60/120 FPS SurfaceView Engine murni).

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
