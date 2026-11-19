package com.flappysawit.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    // ========== GAME STATES ==========
    private static final int STATE_MENU = 0;
    private static final int STATE_PLAYING = 1;
    private static final int STATE_GAMEOVER = 2;
    private static final int STATE_SETTINGS = 3;
    private static final int STATE_SKINS = 4;
    private static final int STATE_DYING = 5;

    private int gameState = STATE_MENU;
    private int prevState = STATE_MENU;
    private boolean isPaused = false;
    private long lastTimeNs = 0;

    // Death & Impact Animation variables
    private float birdVx = 0;
    private float deathTimer = 0;
    private float screenShakeTimer = 0;
    private float screenShakeMagnitude = 0;
    private float impactFlashTimer = 0;
    private float gameOverAnimProgress = 0;
    private boolean birdHitGround = false;

    // ========== SATIRICAL HEADLINES & QUOTES ==========
    private String currentSatireQuote = "Mana dana reboisasi 10 triliun?!";
    private float satireQuoteTimer = 0;
    private static final String[] SATIRE_QUOTES = {
        "Mana dana reboisasi 10 triliun?!",
        "Pipa siapa ini di hutan lindung?!",
        "3,3 juta hektar sawit kok ilegal?",
        "Stop bakar hutan demi cuan pribadi!",
        "KPK tolong periksa berkas HGU ini!",
        "Sawit subur, rakyat yang sesak asap!",
        "Audit izin sawit mafia sekarang juga!",
        "Bansos sawit jangan dikorupsi oi!",
        "Hutan adat dirampas, burung mau ke mana?",
        "Pejabat kongkalikong, alam yang rugi!"
    };

    private static final String[] SATIRE_HEADLINES = {
        "3,3 Juta Hektar Kebun Sawit Masuk Kawasan Hutan Lindung",
        "Dana Reboisasi Triliunan Lenyap, Hutan Tetap Terbakar",
        "Berkas Perizinan HGU Hilang Misterius Saat Mau Diaudit",
        "Pejabat Dalih Asap Dari Tetangga, Rakyat Malah Sesak Napas",
        "Misi Rakyat: Lolos dari Cengkeraman Pipa Kongkalikong Pejabat",
        "Kawal Kas Sawit Bersih untuk Memulihkan Rimba Nusantara"
    };

    // ========== SETTINGS ==========
    private boolean soundEnabled = true;
    private int graphicsLevel = 2;  // 0=Rendah, 1=Sedang, 2=Tinggi
    private int fpsTarget = 60;     // 30, 60, 120
    private int fpsActual = 0;
    private int frameCounter = 0;
    private long fpsTimer = 0;

    // ========== CHARACTER SKINS SYSTEM ==========
    private int currentSkin = 0;   // 0=Garuda, 1=Damkar, 2=Phoenix, 3=Mecha, 4=Intelijen, 5=Cendrawasih, 6=Naga, 7=Gakkum, 8=Drone, 9=Dayak, 10=Hantu, 11=Sultan
    private int previewSkin = 0;
    private static final int TOTAL_SKINS = 12;

    private static final String[] SKIN_NAMES = {
        "Garuda Relawan",
        "Satgas Damkar",
        "Phoenix Rimba",
        "Cyber Mecha",
        "Elang Intelijen",
        "Raja Cendrawasih",
        "Naga Rimba Purba",
        "Jaksa Gakkum",
        "Aktivis Drone",
        "Panglima Dayak",
        "Hantu Karhutla",
        "Garuda Emas 24K"
    };

    private static final String[] SKIN_TITLES = {
        "RELAWAN N95",
        "SATGAS MANGGALA",
        "SANG PHOENIX",
        "DRONE PENYELAMAT",
        "PENYIDIK OTT KPK",
        "MAHKOTA RIMBA",
        "LEGENDA PURBA",
        "PENEGAK VONIS HGU",
        "MATA-MATA SATELIT",
        "PENJAGA HUTAN ADAT",
        "ARWAH GAMBUT HITAM",
        "SULTAN ANTI-KORUPSI"
    };

    private static final String[] SKIN_DESC = {
        "Elang pejuang memakai masker N95 penahan asap kabut pekat.",
        "Helm damkar pelindung api & rompi fosfor Manggala Agni.",
        "Burung mistis abadi, kebal panas & melibas bara api rimba!",
        "Drone futuristik anti-asap dengan jet plasma booster canggih.",
        "Agen penyidik OTT berdasi & kacamata hitam, menyita aset sawit.",
        "Burung surga bermahkota zamrud & ekor pita emas nusantara.",
        "Naga purba bersisik rubi api pembakar habis mafia tanah!",
        "Jaksa rimba berjas hijau tua & toga wibawa, siap mengeksekusi vonis sita kebun ilegal!",
        "Burung berhelm night-vision & antena radar, merekam titik api dan pembalakan liar secara live!",
        "Burung Enggang Gading keramat bermahkota bulu adat Dayak, penjaga benteng terakhir tanah adat!",
        "Entitas mistis asap gambut yang tembus pandang berhawa dingin, menuntut balas pada pembakar hutan!",
        "Garuda emas murni 24 karat berlapis mahkota berlian intan dari triliunan dana sitaan negara!"
    };

    // ========== OBSTACLE SKINS SYSTEM ==========
    private int currentPipeSkin = 0; // 0=Sawit, 1=PKS, 2=Tower, 3=Magma, 4=Koruptor, 5=Cyber, 6=Drum, 7=Excavator, 8=Brankas, 9=Penjara, 10=PohonHayat
    private int previewPipeSkin = 0;
    private static final int TOTAL_PIPE_SKINS = 11;
    private int skinTab = 0; // 0 = Karakter, 1 = Rintangan

    private static final String[] PIPE_SKIN_NAMES = {
        "Sawit Membara",
        "Cerobong PKS",
        "Tower Listrik",
        "Magma Obsidian",
        "Pilar Koruptor",
        "Reaktor Cyber",
        "Tumpukan Drum CPO",
        "Ekskavator HGU",
        "Brankas Suap",
        "Sel Terali Tipikor",
        "Pohon Hayat Rimba"
    };

    private static final String[] PIPE_SKIN_TITLES = {
        "KLASIK KEBUN SAWIT",
        "PABRIK INDUSTRI SAWIT",
        "TRANSMISI GAMBUT",
        "INFERNO PURBA",
        "ASET DISITA KPK",
        "MATRIX ZONA HIJAU",
        "KONTRABAN MINYAK CPO",
        "ALAT BERAT PEMBALAK",
        "BUNKER SUAP PEJABAT",
        "SEL KHUSUS TIPIKOR",
        "MONOLIT RIMBA ABADI"
    };

    private static final String[] PIPE_SKIN_DESC = {
        "Batang kelapa sawit terbakar dengan serat wajik dan bara api.",
        "Pipa baja pabrik sawit bergaris hazard kuning-hitam & suar merah.",
        "Menara baja transmisi dengan sengatan petir busur plasma biru.",
        "Pilar batu obsidian hitam kristal dengan urat lahar magma membara.",
        "Pilar marmer emas mewah dililit pita segel garis polisi DISITA KPK.",
        "Pilar conduit siber bergelombang sirkuit neon pelindung rimba.",
        "Tumpukan drum minyak sawit mentah selundupan berkarat dengan rembesan minyak emas pekat.",
        "Lengan hidrolik ekskavator perambah hutan dengan pilar baja bercakar tajam dan hidrolik bertekanan.",
        "Pilar tumpukan koper brankas titanium berkunci kombinasi, penuh sesak gepokan uang gratifikasi HGU.",
        "Jeruji besi sel tahanan korupsi bertingkat dengan gembok baja raksasa dan nomor registrasi napi KPK.",
        "Pilar kayu ulin raksasa berusia 1000 tahun bertabur ukiran etnik Dayak dan lumut fosfor hijau abadi."
    };

    // ========== ECONOMY & PROGRESSION SYSTEM ==========
    private int sawitCoins = 0;
    private int runCoins = 0;
    private static final int[] SKIN_COSTS = { 0, 20, 50, 100, 180, 280, 450, 600, 800, 1100, 1500, 2000 };
    private static final int[] PIPE_SKIN_COSTS = { 0, 25, 60, 120, 175, 260, 380, 550, 750, 1100, 1600 };

    // Cached full-screen shaders to prevent GC spikes and maintain 60 FPS
    private LinearGradient skyShader;
    private LinearGradient groundShader;

    // ========== DIMENSIONS & SCALING ==========
    private float W = 720, H = 1280;
    private float SCALE = 2.0f;
    private float safeTop = 80, groundH = 140, playableH = 1000;

    // ========== BIRD ==========
    private float birdX, birdY, birdVy = 0, birdRotation = 0, wingAngle = 0;
    private float eyeBlinkTimer = 0;
    private boolean isBlinking = false;
    private static final float BIRD_W = 44, BIRD_H = 32;

    // ========== PIPES ==========
    private static class Pipe {
        float x, topH, gap;
        boolean passed;
        boolean coinCollected;
        Pipe(float x, float t, float g) {
            this.x = x;
            topH = t;
            gap = g;
            this.passed = false;
            this.coinCollected = false;
        }
    }
    private ArrayList<Pipe> pipes = new ArrayList<>();
    private float pipeSpawnTimer = 0, worldSpeed = 160;

    // ========== ENVIRONMENT ==========
    private static class PalmTree {
        float x, height, scale, firePhase, trunkSway;
        boolean isBurning;
    }
    private ArrayList<PalmTree> farTrees = new ArrayList<>();
    private ArrayList<PalmTree> midTrees = new ArrayList<>();
    private ArrayList<PalmTree> nearTrees = new ArrayList<>();

    private static class SmokeCloud {
        float x, y, radius, speed, opacity;
    }
    private ArrayList<SmokeCloud> smokeClouds = new ArrayList<>();

    private static class Particle {
        float x, y, vx, vy, size, life, maxLife;
        int color;
    }
    private ArrayList<Particle> particles = new ArrayList<>();

    private static class Ember {
        float x, y, speedX, speedY, size, opacity, flicker;
    }
    private ArrayList<Ember> embers = new ArrayList<>();

    private static class GroundFire {
        float x, size, phase;
    }
    private ArrayList<GroundFire> groundFires = new ArrayList<>();

    private static class FleeingBird {
        float x, y, speed, flapPhase, size;
    }
    private ArrayList<FleeingBird> fleeingBirds = new ArrayList<>();

    private float groundOffset = 0;
    private int score = 0, highScore = 0;
    private SharedPreferences prefs;
    private Vibrator vibrator;
    private Random rng = new Random();

    // ========== REUSABLE GRAPHICS OBJECTS ==========
    private Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint sp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rf = new RectF();
    private Path pa = new Path();

    // Typefaces for arcade typography
    private Typeface tfBlack;
    private Typeface tfCondensed;
    private Typeface tfMedium;

    // Menu layout coordinate cache
    private float playBtnX, playBtnY, playBtnW, playBtnH;
    private float skinBtnX, skinBtnY, skinBtnW, skinBtnH;
    private float setBtnX, setBtnY, setBtnW, setBtnH;
    private float settingsBtnX, settingsBtnY, settingsBtnSize;

    // Game Over button coordinate cache
    private float goRetryBtnX, goRetryBtnY, goRetryBtnW, goRetryBtnH;
    private float goMenuBtnX, goMenuBtnY, goMenuBtnW, goMenuBtnH;

    public GameView(Context context) {
        super(context);
        setFocusable(true);
        setClickable(true);

        prefs = context.getSharedPreferences("FlappySawitNative", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);
        soundEnabled = prefs.getBoolean("sound", true);
        graphicsLevel = prefs.getInt("graphics", 2);
        fpsTarget = prefs.getInt("fps", 60);
        currentSkin = prefs.getInt("selected_skin", 0);
        previewSkin = currentSkin;
        currentPipeSkin = prefs.getInt("selected_pipe_skin", 0);
        previewPipeSkin = currentPipeSkin;
        sawitCoins = prefs.getInt("sawit_coins", 0);

        try {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        try {
            tfBlack = Typeface.create("sans-serif-black", Typeface.BOLD);
        } catch (Exception e) {
            tfBlack = Typeface.DEFAULT_BOLD;
        }
        try {
            tfCondensed = Typeface.create("sans-serif-condensed", Typeface.BOLD);
        } catch (Exception e) {
            tfCondensed = Typeface.DEFAULT_BOLD;
        }
        try {
            tfMedium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        } catch (Exception e) {
            tfMedium = Typeface.DEFAULT;
        }

        initModernAudio();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseModernAudio();
    }

    private void saveSettings() {
        prefs.edit()
            .putBoolean("sound", soundEnabled)
            .putInt("graphics", graphicsLevel)
            .putInt("fps", fpsTarget)
            .putInt("selected_skin", currentSkin)
            .putInt("selected_pipe_skin", currentPipeSkin)
            .apply();
    }

    private boolean isSkinUnlocked(int id) {
        if (id == 0) return true; // Default free skin
        return prefs.getBoolean("skin_unlocked_" + id, false);
    }

    private boolean isPipeSkinUnlocked(int id) {
        if (id == 0) return true; // Default free skin
        return prefs.getBoolean("pipe_skin_unlocked_" + id, false);
    }

    private void unlockSkin(int id) {
        prefs.edit().putBoolean("skin_unlocked_" + id, true).apply();
    }

    private void unlockPipeSkin(int id) {
        prefs.edit().putBoolean("pipe_skin_unlocked_" + id, true).apply();
    }

    private void saveCoins() {
        prefs.edit().putInt("sawit_coins", sawitCoins).apply();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        W = w;
        H = h;
        SCALE = Math.min(W / 380.0f, H / 680.0f);
        safeTop = Math.max(48 * SCALE, H * 0.06f);
        groundH = Math.max(82 * SCALE, H * 0.12f);
        playableH = H - groundH - safeTop;

        settingsBtnSize = 36 * SCALE;
        settingsBtnX = W - settingsBtnSize - 16 * SCALE;
        settingsBtnY = safeTop + 6 * SCALE;

        // Calculate Menu Button coordinates for modern arcade layout
        float menuBirdCenterY = safeTop + playableH * 0.36f;
        float dashY = menuBirdCenterY + 44 * SCALE;
        float dashH = 50 * SCALE;
        playBtnW = Math.min(270 * SCALE, W * 0.82f);
        playBtnH = 52 * SCALE;
        playBtnX = (W - playBtnW) / 2;
        playBtnY = dashY + dashH + 14 * SCALE;

        float subRowY = playBtnY + playBtnH + 12 * SCALE;
        float subRowH = 44 * SCALE;
        float gap = 10 * SCALE;

        // Left button: 🎭 PILIH SKIN & TEMA
        skinBtnW = (playBtnW - gap) / 2;
        skinBtnH = subRowH;
        skinBtnX = playBtnX;
        skinBtnY = subRowY;

        // Right button: ⚙ PENGATURAN
        setBtnW = skinBtnW;
        setBtnH = subRowH;
        setBtnX = skinBtnX + skinBtnW + gap;
        setBtnY = subRowY;

        // Calculate Game Over Button coordinates (Centered Horizontally & Vertically)
        float goCardW = Math.min(320 * SCALE, W * 0.88f);
        float goCardH = 292 * SCALE;
        float goCardX = (W - goCardW) / 2;
        float goCardY = safeTop + (playableH - goCardH) * 0.46f;
        float goScoreBoxY = goCardY + 72 * SCALE;
        float goScoreBoxH = 84 * SCALE;

        goRetryBtnW = goCardW - 40 * SCALE;
        goRetryBtnH = 46 * SCALE;
        goRetryBtnX = (W - goRetryBtnW) / 2;
        goRetryBtnY = goScoreBoxY + goScoreBoxH + 12 * SCALE;

        goMenuBtnW = goRetryBtnW;
        goMenuBtnH = 46 * SCALE;
        goMenuBtnX = goRetryBtnX;
        goMenuBtnY = goRetryBtnY + goRetryBtnH + 10 * SCALE;

        // Cache static full-screen shaders
        skyShader = new LinearGradient(0, 0, 0, H,
                new int[]{0xFF1C0502, 0xFF4A0E02, 0xFF8A1E00, 0xFFD84315, 0xFFFF7043, 0xFF5A1604},
                new float[]{0, 0.18f, 0.38f, 0.62f, 0.82f, 1.0f}, Shader.TileMode.CLAMP);
        groundShader = new LinearGradient(0, H - groundH, 0, H,
                new int[]{0xFF5A2A14, 0xFF421C0B, 0xFF321406, 0xFF240D04},
                new float[]{0.0f, 0.25f, 0.65f, 1.0f}, Shader.TileMode.CLAMP);

        initEnvironment();
        resetBird();
    }

    public void resume() {
        isPaused = false;
        lastTimeNs = System.nanoTime();
        postInvalidateOnAnimation();
    }

    public void pause() {
        isPaused = true;
    }

    private void initEnvironment() {
        farTrees.clear();
        midTrees.clear();
        nearTrees.clear();
        smokeClouds.clear();
        embers.clear();
        groundFires.clear();
        fleeingBirds.clear();

        // 1. Far Trees (Distant palm silhouettes reaching into mid-sky)
        int cf = (int)(W / (85 * SCALE)) + 3;
        for (int i = 0; i < cf; i++) {
            PalmTree t = new PalmTree();
            t.x = i * (85 * SCALE) + rng.nextFloat() * 30 * SCALE;
            t.height = (180 + rng.nextFloat() * 80) * SCALE;
            t.scale = 0.70f + rng.nextFloat() * 0.20f;
            t.firePhase = rng.nextFloat() * 10;
            t.trunkSway = rng.nextFloat() * 5;
            t.isBurning = (i % 3 == 1);
            farTrees.add(t);
        }

        // 2. Mid Trees (Tall, majestic oil palms with lush fountain fronds reaching upper-mid sky!)
        int cm = (int)(W / (130 * SCALE)) + 3;
        for (int i = 0; i < cm; i++) {
            PalmTree t = new PalmTree();
            t.x = i * (130 * SCALE) + rng.nextFloat() * 40 * SCALE;
            t.height = (250 + rng.nextFloat() * 90) * SCALE;
            t.scale = 0.95f + rng.nextFloat() * 0.25f;
            t.firePhase = rng.nextFloat() * 10;
            t.trunkSway = rng.nextFloat() * 8;
            t.isBurning = (i % 2 == 1);
            midTrees.add(t);
        }

        // 3. Near Trees (Framing palms at edges)
        int cn = (int)(W / (220 * SCALE)) + 2;
        for (int i = 0; i < cn; i++) {
            PalmTree t = new PalmTree();
            t.x = i * (220 * SCALE) + rng.nextFloat() * 50 * SCALE;
            t.height = (320 + rng.nextFloat() * 70) * SCALE;
            t.scale = 1.15f + rng.nextFloat() * 0.25f;
            t.firePhase = rng.nextFloat() * 10;
            t.trunkSway = rng.nextFloat() * 10;
            t.isBurning = (i == 0);
            nearTrees.add(t);
        }

        // 4. Volumetric Smoke Clouds (Visible warm billowing smoke covering upper sky)
        for (int i = 0; i < 14; i++) {
            SmokeCloud s = new SmokeCloud();
            s.x = rng.nextFloat() * W * 1.5f;
            s.y = safeTop + rng.nextFloat() * (playableH * 0.58f);
            s.radius = (90 + rng.nextFloat() * 120) * SCALE;
            s.speed = (16 + rng.nextFloat() * 26) * SCALE;
            s.opacity = 0.16f + rng.nextFloat() * 0.20f;
            smokeClouds.add(s);
        }

        // 5. Embers drifting across screen
        int emberCount = graphicsLevel >= 2 ? 50 : (graphicsLevel == 1 ? 30 : 15);
        for (int i = 0; i < emberCount; i++) {
            Ember e = new Ember();
            e.x = rng.nextFloat() * W;
            e.y = rng.nextFloat() * H;
            e.size = (1.4f + rng.nextFloat() * 2.8f) * SCALE;
            e.speedX = -(22 + rng.nextFloat() * 45) * SCALE;
            e.speedY = -(14 + rng.nextFloat() * 32) * SCALE;
            e.opacity = 0.35f + rng.nextFloat() * 0.65f;
            e.flicker = 2 + rng.nextFloat() * 5;
            embers.add(e);
        }

        // 6. Ground Fires in peat fissures
        for (float gx = 0; gx < W + 200 * SCALE; gx += (35 + rng.nextFloat() * 45) * SCALE) {
            GroundFire gf = new GroundFire();
            gf.x = gx;
            gf.size = (9 + rng.nextFloat() * 15) * SCALE;
            gf.phase = rng.nextFloat() * 10;
            groundFires.add(gf);
        }

        // 7. Fleeing jungle wildlife in upper sky
        for (int i = 0; i < 5; i++) {
            FleeingBird fb = new FleeingBird();
            fb.x = rng.nextFloat() * W;
            fb.y = safeTop + 20 * SCALE + rng.nextFloat() * (playableH * 0.25f);
            fb.speed = (42 + rng.nextFloat() * 38) * SCALE;
            fb.flapPhase = 0.8f + rng.nextFloat() * 0.6f;
            fb.size = (7 + rng.nextFloat() * 4) * SCALE;
            fleeingBirds.add(fb);
        }
    }

    private void resetBird() {
        birdX = W * 0.22f;
        birdY = safeTop + playableH * 0.38f;
        birdVx = 0;
        birdVy = 0;
        birdRotation = 0;
        wingAngle = 0;
        deathTimer = 0;
        screenShakeTimer = 0;
        impactFlashTimer = 0;
        gameOverAnimProgress = 0;
        birdHitGround = false;
    }

    private void flap() {
        if (gameState != STATE_PLAYING) return;
        birdVy = -410 * SCALE;
        wingAngle = -(float)Math.PI / 3;
        playFlapSound();
        spawnFlapSparks(birdX, birdY + (BIRD_H * 0.4f) * SCALE, currentSkin);
    }

    private void startDying(boolean hitPipe) {
        if (gameState == STATE_DYING || gameState == STATE_GAMEOVER) return;
        gameState = STATE_DYING;
        deathTimer = 0;
        birdHitGround = !hitPipe;
        gameOverAnimProgress = 0;

        playHitSound();
        playDieSound();
        try {
            if (vibrator != null) vibrator.vibrate(hitPipe ? 80 : 50);
        } catch (Exception ignored) {}

        screenShakeTimer = 0.35f;
        screenShakeMagnitude = 10 * SCALE;
        impactFlashTimer = 0.22f;

        if (score > highScore) {
            highScore = score;
            prefs.edit().putInt("high_score", highScore).apply();
        }

        float bx = birdX + (BIRD_W * SCALE) / 2;
        float by = birdY + (BIRD_H * SCALE) / 2;
        spawnFeathersAndSparks(bx, by, currentSkin);

        if (hitPipe) {
            // Upward pop & horizontal knockback away from obstacle
            birdVy = -250 * SCALE;
            birdVx = -90 * SCALE;
            birdRotation = -35;
        } else {
            // Direct ground crash
            birdVy = -120 * SCALE;
            birdVx = 0;
            birdRotation = 85;
            spawnGroundDust(bx, H - groundH);
        }
    }

    // ========================================================
    //  RENDER LOOP (VSYNC SYNCHRONIZED)
    // ========================================================
    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (isPaused || W <= 0 || H <= 0) return;

        long now = System.nanoTime();
        if (lastTimeNs == 0) lastTimeNs = now;
        float dt = (now - lastTimeNs) / 1_000_000_000.0f;
        if (dt > 0.08f) dt = 0.08f;
        lastTimeNs = now;

        frameCounter++;
        long elapsed = now - fpsTimer;
        if (elapsed >= 1_000_000_000L) {
            fpsActual = frameCounter;
            frameCounter = 0;
            fpsTimer = now;
        }

        float maxDt = 1.0f / fpsTarget;
        if (dt < maxDt * 0.5f) dt = maxDt;

        updateAll(dt);

        c.drawColor(0xFF080100);

        c.save();
        if (screenShakeTimer > 0) {
            screenShakeTimer -= dt;
            float factor = screenShakeTimer / 0.35f;
            float shakeX = (rng.nextFloat() - 0.5f) * 2 * screenShakeMagnitude * factor;
            float shakeY = (rng.nextFloat() - 0.5f) * 2 * screenShakeMagnitude * factor;
            c.translate(shakeX, shakeY);
        }

        // Rich Multi-Layer Atmosphere (Zero Empty Space)
        drawSkyAndSun(c);
        if (graphicsLevel >= 1) drawSmokeClouds(c);
        drawVolcanicRidge(c);
        drawContinuousPalmCanopy(c, dt);
        drawParallaxForest(c, farTrees, 0.2f, dt);
        drawParallaxForest(c, midTrees, 0.45f, dt);
        if (graphicsLevel >= 1) drawEmbers(c);
        if (graphicsLevel >= 2) drawParallaxForest(c, nearTrees, 0.7f, dt);
        drawPipes(c);
        drawGround(c);
        drawGroundDropShadow(c);
        drawParticles(c);

        if (gameState == STATE_PLAYING) {
            drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, isBlinking, currentSkin);
            drawScoreHUD(c);
            drawSettingsGearBtn(c);
        } else if (gameState == STATE_DYING) {
            drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, false, currentSkin);
            drawScoreHUD(c);
        } else if (gameState == STATE_MENU) {
            drawIdleBirdWithStage(c);
            drawModernArcadeMenu(c);
        } else if (gameState == STATE_GAMEOVER) {
            drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, false, currentSkin);
            drawDizzyStars(c, birdX + (BIRD_W * SCALE) / 2, birdY);
            drawGameOverScreen(c);
        } else if (gameState == STATE_SETTINGS) {
            if (prevState == STATE_PLAYING) {
                drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, isBlinking, currentSkin);
            }
            drawSettingsScreen(c);
        } else if (gameState == STATE_SKINS) {
            drawSkinSelectionScreen(c);
        }

        c.restore();

        if (impactFlashTimer > 0) {
            impactFlashTimer -= dt;
            float alpha = impactFlashTimer / 0.22f;
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb((int)(80 * alpha), 255, 30, 0));
            c.drawRect(0, 0, W, H, p);
        }

        if (graphicsLevel >= 1 && (gameState == STATE_PLAYING || gameState == STATE_DYING)) {
            tp.setTypeface(tfCondensed);
            tp.setColor(0x88FFFFFF);
            tp.setTextSize(9 * SCALE);
            tp.setTextAlign(Paint.Align.LEFT);
            tp.setFakeBoldText(false);
            c.drawText(fpsActual + " FPS", 10 * SCALE, H - groundH + 20 * SCALE, tp);
        }

        postInvalidateOnAnimation();
    }

    private void updateAll(float dt) {
        if (satireQuoteTimer > 0) {
            satireQuoteTimer -= dt;
        }
        groundOffset = (groundOffset + worldSpeed * SCALE * dt) % (140 * SCALE);

        for (SmokeCloud s : smokeClouds) {
            s.x -= s.speed * dt;
            if (s.x + s.radius < -50) s.x = W + s.radius + 50;
        }

        for (Ember e : embers) {
            e.x += e.speedX * dt;
            e.y += e.speedY * dt;
            if (e.x < -10) e.x = W + 10;
            if (e.y < -10) { e.y = H; e.x = rng.nextFloat() * W; }
        }

        for (FleeingBird fb : fleeingBirds) {
            fb.x += fb.speed * dt;
            if (fb.x > W + 40 * SCALE) {
                fb.x = -40 * SCALE;
                fb.y = safeTop + 20 * SCALE + rng.nextFloat() * (playableH * 0.25f);
            }
        }

        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle pr = particles.get(i);
            pr.life -= dt;
            if (pr.life <= 0) {
                particles.remove(i);
                continue;
            }
            pr.x += pr.vx * dt;
            pr.y += pr.vy * dt;
        }

        if (gameState == STATE_DYING) {
            // Realistic falling, nosedive, and impact tumble physics
            birdVy += 1750 * SCALE * dt;
            birdY += birdVy * dt;
            birdX += birdVx * dt;
            birdVx *= (float)Math.pow(0.10, dt);

            if (birdRotation < 90) {
                birdRotation += 420 * dt;
                if (birdRotation > 90) birdRotation = 90;
            }
            wingAngle += 24 * dt;

            float groundY = H - groundH;
            float bh = BIRD_H * SCALE;
            if (birdY + bh >= groundY) {
                birdY = groundY - bh;
                if (!birdHitGround) {
                    birdHitGround = true;
                    playHitSound();
                    screenShakeTimer = 0.20f;
                    screenShakeMagnitude = 6 * SCALE;
                    spawnGroundDust(birdX + (BIRD_W * SCALE) / 2, groundY);
                    try {
                        if (vibrator != null) vibrator.vibrate(50);
                    } catch (Exception ignored) {}
                    birdVy = -110 * SCALE; // ground thud bounce
                } else {
                    birdVy = 0;
                    birdVx = 0;
                    birdRotation = 85;
                }
            }

            if (birdHitGround) {
                deathTimer += dt;
                if (deathTimer >= 0.45f) {
                    gameState = STATE_GAMEOVER;
                    gameOverAnimProgress = 0;
                }
            }
            return;
        }

        if (gameState != STATE_PLAYING) return;

        // Bird Physics
        birdVy += 1250 * SCALE * dt;
        birdY += birdVy * dt;
        wingAngle += 15 * dt;
        float targetRot = Math.max(-25, Math.min(75, birdVy / (7.0f * SCALE)));
        birdRotation += (targetRot - birdRotation) * 12 * dt;
        eyeBlinkTimer += dt;
        if (eyeBlinkTimer > 3.2f) {
            isBlinking = true;
            if (eyeBlinkTimer > 3.36f) {
                isBlinking = false;
                eyeBlinkTimer = 0;
            }
        }

        float groundY = H - groundH;
        float bh = BIRD_H * SCALE;
        if (birdY + bh >= groundY) {
            birdY = groundY - bh;
            startDying(false);
            return;
        }
        if (birdY < 0) {
            birdY = 0;
            birdVy = 0;
        }

        pipeSpawnTimer += dt;
        if (pipeSpawnTimer >= 2.1f) {
            pipeSpawnTimer = 0;
            float gap = 180 * SCALE;
            float minTop = 60 * SCALE, maxTop = groundY - gap - 60 * SCALE;
            pipes.add(new Pipe(W + 20 * SCALE, minTop + rng.nextFloat() * (maxTop - minTop), gap));
        }

        float pw = 74 * SCALE;
        float birdL = birdX + 8 * SCALE, birdR = birdX + BIRD_W * SCALE - 6 * SCALE;
        float birdT = birdY + 6 * SCALE, birdB = birdY + bh - 6 * SCALE;

        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe pp = pipes.get(i);
            pp.x -= worldSpeed * SCALE * dt;

            // Direct coin collection in gap
            if (!pp.coinCollected) {
                float coinCenterX = pp.x + pw / 2;
                float coinCenterY = pp.topH + pp.gap / 2;
                float coinRadius = 18 * SCALE;
                if (birdR > coinCenterX - coinRadius && birdL < coinCenterX + coinRadius
                    && birdB > coinCenterY - coinRadius && birdT < coinCenterY + coinRadius) {
                    pp.coinCollected = true;
                    sawitCoins++;
                    runCoins++;
                    saveCoins();
                    spawnCoinSparkles(coinCenterX, coinCenterY);
                    playScoreSound();
                }
            }

            if (!pp.passed && pp.x + pw < birdX) {
                pp.passed = true;
                score++;
                if (!pp.coinCollected) {
                    pp.coinCollected = true;
                    sawitCoins++;
                    runCoins++;
                    saveCoins();
                    spawnCoinSparkles(pp.x + pw / 2, pp.topH + pp.gap / 2);
                }
                playScoreSound();
                if (score % 6 == 0 && worldSpeed < 260) worldSpeed += 12;
            }
            if (birdR > pp.x && birdL < pp.x + pw) {
                if (birdT < pp.topH || birdB > pp.topH + pp.gap) {
                    startDying(true);
                    return;
                }
            }
            if (pp.x + pw < -40 * SCALE) pipes.remove(i);
        }
    }

    // ========================================================
    //  3D STYLIZED TEXT DRAWING ENGINE
    // ========================================================
    private void draw3DText(Canvas c, String text, float x, float y, float size, Typeface tf, int topColor, int bottomColor, int depthColor) {
        tp.setTypeface(tf);
        tp.setTextSize(size);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(true);

        tp.setColor(0x88000000);
        c.drawText(text, x, y + 6 * SCALE, tp);

        tp.setColor(depthColor);
        for (float d = 4 * SCALE; d >= 1 * SCALE; d -= 1 * SCALE) {
            c.drawText(text, x, y + d, tp);
        }

        tp.setShader(new LinearGradient(x, y - size * 0.85f, x, y,
                new int[]{topColor, bottomColor}, null, Shader.TileMode.CLAMP));
        c.drawText(text, x, y, tp);
        tp.setShader(null);
    }

    // Text Wrapping Helper
    private void drawWrappedText(Canvas c, String text, float cx, float startY, float maxW, Paint paint, float lineSpacing) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float curY = startY;

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float w = paint.measureText(testLine);
            if (w > maxW && line.length() > 0) {
                c.drawText(line.toString(), cx, curY, paint);
                line = new StringBuilder(word);
                curY += lineSpacing;
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            c.drawText(line.toString(), cx, curY, paint);
        }
    }

    // ========================================================
    //  MODERN ARCADE MENU SCREEN (Satirical Anti-Corruption Edition)
    // ========================================================
    private void drawModernArcadeMenu(Canvas c) {
        long timeMs = System.currentTimeMillis();

        // 1. TOP SATIRICAL NEWS TICKER (KPK & Forestry Corruption Watch)
        float tickerW = Math.min(320 * SCALE, W * 0.94f);
        float tickerH = 25 * SCALE;
        float tickerY = safeTop + 5 * SCALE;
        float tickerX = (W - tickerW) / 2;

        p.setStyle(Paint.Style.FILL);
        p.setColor(0x77050201);
        rf.set(tickerX, tickerY, tickerX + tickerW, tickerY + tickerH);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x88FF5722);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        // Left Tag Badge: 🔴 KORUPSI WATCH
        float tagW = 86 * SCALE;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFD50000);
        rf.set(tickerX + 2 * SCALE, tickerY + 2 * SCALE, tickerX + tagW, tickerY + tickerH - 2 * SCALE);
        c.drawRoundRect(rf, 10 * SCALE, 10 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(Color.WHITE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTextSize(9.5f * SCALE);
        tp.setFakeBoldText(true);
        c.drawText("🔴 KORUPSI WATCH", tickerX + tagW / 2, tickerY + 16 * SCALE, tp);

        // Smooth Marquee Scrolling Satirical News Ticker (No text clipping!)
        float clipLeft = tickerX + tagW + 6 * SCALE;
        float clipRight = tickerX + tickerW - 6 * SCALE;
        float clipW = clipRight - clipLeft;

        int headlineIdx = (int)((timeMs / 6500) % SATIRE_HEADLINES.length);
        String currentHeadline = SATIRE_HEADLINES[headlineIdx];

        tp.setTextSize(10.5f * SCALE);
        tp.setTypeface(tfCondensed);
        tp.setFakeBoldText(false);
        float textW = tp.measureText(currentHeadline);

        float marqueeCycle = (timeMs % 6500) / 6500.0f;
        float scrollX = clipRight - marqueeCycle * (clipW + textW + 16 * SCALE);

        tp.setColor(0xFFFFE082);
        tp.setTextAlign(Paint.Align.LEFT);

        c.save();
        c.clipRect(clipLeft, tickerY, clipRight, tickerY + tickerH);
        c.drawText(currentHeadline, scrollX, tickerY + 16.5f * SCALE, tp);
        c.restore();

        // 2. 3D GRAND ARCADE TITLE & SATIRICAL SUBTITLE (Fixed Vertical Spacing)
        float titleY = safeTop + 76 * SCALE;
        draw3DText(c, "FLAPPY SAWIT", W / 2, titleY, 38 * SCALE, tfBlack, 0xFFFFF176, 0xFFFF3D00, 0xFF7F1D1D);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFCC80);
        tp.setTextSize(12 * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(true);
        tp.setShadowLayer(4 * SCALE, 0, 2 * SCALE, 0xFF000000);
        c.drawText("Lawan Kongkalikong Mafia HGU & Pipa Pejabat!", W / 2, titleY + 22 * SCALE, tp);
        tp.clearShadowLayer();

        // 3. MODERN GLASSMORPHISM DASHBOARD (DUAL-COLUMN)
        float menuBirdCenterY = safeTop + playableH * 0.36f;
        float dashY = menuBirdCenterY + 44 * SCALE;
        float cardH = 50 * SCALE;
        float gap = 10 * SCALE;
        float cardW = (playBtnW - gap) / 2;
        float leftX = playBtnX;
        float rightX = leftX + cardW + gap;

        // [A] LEFT CARD: REKOR OPERASI & SATIRICAL RANK
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(leftX, dashY, leftX, dashY + cardH,
                new int[]{0x662A1005, 0x66150602}, null, Shader.TileMode.CLAMP));
        rf.set(leftX, dashY, leftX + cardW, dashY + cardH);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x99FFA726);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFD54F);
        tp.setTextSize(9.5f * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(true);
        c.drawText("🏆 REKOR OPERASI", leftX + cardW / 2, dashY + 15 * SCALE, tp);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(18 * SCALE);
        c.drawText(String.valueOf(highScore), leftX + cardW / 2, dashY + 33 * SCALE, tp);

        String rankTitle;
        if (highScore == 0) rankTitle = "Warga Terdampak";
        else if (highScore < 6) rankTitle = "Relawan Asap";
        else if (highScore < 16) rankTitle = "Aktivis Hutan";
        else if (highScore < 31) rankTitle = "Penyidik KPK";
        else rankTitle = "Pahlawan Rimba";

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFCC80);
        tp.setTextSize(8.5f * SCALE);
        tp.setFakeBoldText(false);
        c.drawText(rankTitle, leftX + cardW / 2, dashY + 44 * SCALE, tp);

        // [B] RIGHT CARD: KAS ANTI-KORUPSI (With Vector Mini Coin Icon)
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(rightX, dashY, rightX, dashY + cardH,
                new int[]{0x662A1005, 0x66150602}, null, Shader.TileMode.CLAMP));
        rf.set(rightX, dashY, rightX + cardW, dashY + cardH);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x99FFD54F);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFD54F);
        tp.setTextSize(9.5f * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(true);
        c.drawText("KAS ANTI-KORUPSI", rightX + cardW / 2, dashY + 15 * SCALE, tp);

        // Value + Real Vector Coin (No broken glyph!)
        tp.setTypeface(tfBlack);
        tp.setColor(0xFFFFE082);
        tp.setTextSize(18 * SCALE);

        String coinValStr = String.valueOf(sawitCoins);
        float numW = tp.measureText(coinValStr);
        float vCoinR = 8.0f * SCALE;
        float totalRowW = numW + vCoinR * 2 + 6 * SCALE;
        float rowStartX = rightX + (cardW - totalRowW) / 2;

        tp.setTextAlign(Paint.Align.LEFT);
        c.drawText(coinValStr, rowStartX, dashY + 33 * SCALE, tp);
        drawMiniVectorCoin(c, rowStartX + numW + 4 * SCALE + vCoinR, dashY + 27 * SCALE, vCoinR);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFF81C784); // Clean green tag
        tp.setTextSize(8.5f * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(false);
        c.drawText("Dana Sitaan Bersih", rightX + cardW / 2, dashY + 44 * SCALE, tp);

        // 4. MAIN ACTION BUTTON ("▶ SERGAP KEBUN" - TACTICAL ANTI-CORRUPTION RAID)
        float pulse = 0.90f + (float)Math.sin(timeMs * 0.005f) * 0.10f;
        float btnR = 20 * SCALE;

        // [A] Physically-Based Multi-Layer Ambient Occlusion & Ground Drop Shadow
        // 1. Far diffused ambient ground shadow
        sp.setStyle(Paint.Style.FILL);
        sp.setColor(0x28000000);
        rf.set(playBtnX - 3 * SCALE, playBtnY + 6 * SCALE, playBtnX + playBtnW + 3 * SCALE, playBtnY + playBtnH + 11 * SCALE);
        c.drawRoundRect(rf, btnR + 4 * SCALE, btnR + 4 * SCALE, sp);

        // 2. Mid elevation shadow
        sp.setColor(0x4A000000);
        rf.set(playBtnX - 1 * SCALE, playBtnY + 4 * SCALE, playBtnX + playBtnW + 1 * SCALE, playBtnY + playBtnH + 7.5f * SCALE);
        c.drawRoundRect(rf, btnR + 2 * SCALE, btnR + 2 * SCALE, sp);

        // 3. Sharp contact occlusion shadow
        sp.setColor(0x80000000);
        rf.set(playBtnX, playBtnY + 2.5f * SCALE, playBtnX + playBtnW, playBtnY + playBtnH + 5 * SCALE);
        c.drawRoundRect(rf, btnR, btnR, sp);

        // 4. Atmospheric Peatland Fire Underglow (Subtle breathing warm aura under button)
        int glowAlpha = (int)(32 * pulse);
        sp.setColor(Color.argb(glowAlpha, 255, 60, 0));
        rf.set(playBtnX + 8 * SCALE, playBtnY + 5 * SCALE, playBtnX + playBtnW - 8 * SCALE, playBtnY + playBtnH + 10 * SCALE);
        c.drawRoundRect(rf, btnR + 4 * SCALE, btnR + 4 * SCALE, sp);

        // [B] Tactical Heavy Base Chassis (Solid Peat Iron Foundation)
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF140803); // Charred peat iron
        rf.set(playBtnX, playBtnY + 4 * SCALE, playBtnX + playBtnW, playBtnY + playBtnH + 4 * SCALE);
        c.drawRoundRect(rf, btnR, btnR, p);

        // Base chassis bronze rim bevel
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF4A1A0C);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, btnR, btnR, p);

        // [C] Button Face Plate (Emergency Raid Tactical Crimson & Peat Flame Gradient)
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(playBtnX, playBtnY, playBtnX, playBtnY + playBtnH,
                new int[]{0xFFFF7A00, 0xFFE53935, 0xFF9E0B0B, 0xFF520303},
                new float[]{0.0f, 0.32f, 0.76f, 1.0f}, Shader.TileMode.CLAMP));
        rf.set(playBtnX, playBtnY, playBtnX + playBtnW, playBtnY + playBtnH);
        c.drawRoundRect(rf, btnR, btnR, p);
        p.setShader(null);

        // [D] Inner Chamfer Specular Highlight (Subtle metallic top rim sheen)
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.4f * SCALE);
        p.setShader(new LinearGradient(playBtnX, playBtnY, playBtnX, playBtnY + playBtnH * 0.40f,
                new int[]{0xAAFFFFFF, 0x10FFFFFF}, null, Shader.TileMode.CLAMP));
        rf.set(playBtnX + 1.5f * SCALE, playBtnY + 1.2f * SCALE, playBtnX + playBtnW - 1.5f * SCALE, playBtnY + playBtnH - 1.2f * SCALE);
        c.drawRoundRect(rf, btnR - 1.5f * SCALE, btnR - 1.5f * SCALE, p);
        p.setShader(null);

        // [E] Outer Tactical Border Frame with Pulsing Amber Neon Edge
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.6f * SCALE);
        int borderAlpha = Math.min(255, (int)(180 + 75 * pulse));
        p.setColor(Color.argb(borderAlpha, 255, 179, 0)); // Vibrant tactical amber border
        rf.set(playBtnX, playBtnY, playBtnX + playBtnW, playBtnY + playBtnH);
        c.drawRoundRect(rf, btnR, btnR, p);

        // [F] Flank Tactical Chevron Grips (Field Equipment Aesthetic)
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.0f * SCALE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(0x88FFD54F);

        float leftGripX = playBtnX + 16 * SCALE;
        float gripCenterY = playBtnY + playBtnH / 2;
        // Left chevron grips »
        c.drawLine(leftGripX - 3 * SCALE, gripCenterY - 6 * SCALE, leftGripX + 2 * SCALE, gripCenterY, p);
        c.drawLine(leftGripX + 2 * SCALE, gripCenterY, leftGripX - 3 * SCALE, gripCenterY + 6 * SCALE, p);
        c.drawLine(leftGripX + 3 * SCALE, gripCenterY - 6 * SCALE, leftGripX + 8 * SCALE, gripCenterY, p);
        c.drawLine(leftGripX + 8 * SCALE, gripCenterY, leftGripX + 3 * SCALE, gripCenterY + 6 * SCALE, p);

        // Right chevron grips «
        float rightGripX = playBtnX + playBtnW - 16 * SCALE;
        c.drawLine(rightGripX + 3 * SCALE, gripCenterY - 6 * SCALE, rightGripX - 2 * SCALE, gripCenterY, p);
        c.drawLine(rightGripX - 2 * SCALE, gripCenterY, rightGripX + 3 * SCALE, gripCenterY + 6 * SCALE, p);
        c.drawLine(rightGripX - 3 * SCALE, gripCenterY - 6 * SCALE, rightGripX - 8 * SCALE, gripCenterY, p);
        c.drawLine(rightGripX - 8 * SCALE, gripCenterY, rightGripX - 3 * SCALE, gripCenterY + 6 * SCALE, p);
        p.setStrokeCap(Paint.Cap.BUTT);

        // [G] Tactical Typography
        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(19 * SCALE);
        tp.setFakeBoldText(true);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setShadowLayer(5 * SCALE, 0, 2.5f * SCALE, 0xDD000000);
        c.drawText("▶ SERGAP KEBUN", W / 2, playBtnY + 28 * SCALE, tp);
        tp.clearShadowLayer();

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFE082); // Anti-corruption gold tag
        tp.setTextSize(9.5f * SCALE);
        tp.setFakeBoldText(true);
        tp.setShadowLayer(3 * SCALE, 0, 1.5f * SCALE, 0xCC000000);
        c.drawText("⚡ OPERASI SITA ASET MAFIA SAWIT ⚡", W / 2, playBtnY + 42.5f * SCALE, tp);
        tp.clearShadowLayer();

        // 5. SECONDARY CONTROL ROW ([ 🎭 WARDROBE & SKIN ] and [ ⚙ PENGATURAN ])
        // [A] Skin Selection Button
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1E0E08);
        rf.set(skinBtnX, skinBtnY + 3 * SCALE, skinBtnX + skinBtnW, skinBtnY + skinBtnH + 3 * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        p.setShader(new LinearGradient(skinBtnX, skinBtnY, skinBtnX, skinBtnY + skinBtnH,
                new int[]{0xFF4A1F3D, 0xFF280B20}, null, Shader.TileMode.CLAMP));
        rf.set(skinBtnX, skinBtnY, skinBtnX + skinBtnW, skinBtnY + skinBtnH);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFE040FB);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFF8BBD0);
        tp.setTextSize(13.5f * SCALE);
        tp.setFakeBoldText(true);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setShadowLayer(3 * SCALE, 0, 2 * SCALE, 0xFF000000);
        float skinTextY = skinBtnY + skinBtnH / 2 - (tp.descent() + tp.ascent()) / 2;
        c.drawText("🎭 WARDROBE", skinBtnX + skinBtnW / 2, skinTextY, tp);
        tp.clearShadowLayer();

        // [B] Settings Button
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1E0E08);
        rf.set(setBtnX, setBtnY + 3 * SCALE, setBtnX + setBtnW, setBtnY + setBtnH + 3 * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        p.setShader(new LinearGradient(setBtnX, setBtnY, setBtnX, setBtnY + setBtnH,
                new int[]{0xFF422114, 0xFF281109}, null, Shader.TileMode.CLAMP));
        rf.set(setBtnX, setBtnY, setBtnX + setBtnW, setBtnY + setBtnH);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x99FFA726);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFE0B2);
        tp.setTextSize(13.5f * SCALE);
        tp.setFakeBoldText(true);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setShadowLayer(3 * SCALE, 0, 2 * SCALE, 0xFF000000);
        float setTextY = setBtnY + setBtnH / 2 - (tp.descent() + tp.ascent()) / 2;
        c.drawText("⚙ PENGATURAN", setBtnX + setBtnW / 2, setTextY, tp);
        tp.clearShadowLayer();
    }

    // ========== HERO CHARACTER STAGE IN MENU (Satirical Speech Bubble) ==========
    private void drawIdleBirdWithStage(Canvas c) {
        long timeMs = System.currentTimeMillis();
        float birdCenterX = W / 2;
        float birdCenterY = safeTop + playableH * 0.36f;

        // Glowing Holographic Pedestal
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(birdCenterX, birdCenterY, 65 * SCALE,
                new int[]{0x44FF6D00, 0x1AEE3900, 0x00000000}, null, Shader.TileMode.CLAMP));
        c.drawCircle(birdCenterX, birdCenterY, 65 * SCALE, p);
        p.setShader(null);

        for (int i = 0; i < 6; i++) {
            float orbitAngle = (timeMs * 0.002f) + (i * (float)Math.PI / 3.0f);
            float ox = birdCenterX + (float)Math.cos(orbitAngle) * (50 * SCALE);
            float oy = birdCenterY + (float)Math.sin(orbitAngle) * (24 * SCALE);
            p.setColor(0xCCFFB74D);
            c.drawCircle(ox, oy, 2.5f * SCALE, p);
        }

        birdX = birdCenterX - (BIRD_W * SCALE) / 2;
        birdY = birdCenterY - (BIRD_H * SCALE) / 2 + (float)Math.sin(timeMs * 0.0035f) * (10 * SCALE);
        birdRotation = (float)Math.sin(timeMs * 0.0035f) * 6;
        drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, isBlinking, currentSkin);

        // Satirical Speech Bubble or Tapping Hint
        if (satireQuoteTimer > 0) {
            drawSpeechBubble(c, birdCenterX, birdCenterY - 14 * SCALE, currentSatireQuote);
        } else {
            float hintPulse = 0.7f + 0.3f * (float)Math.sin(timeMs * 0.005f);
            float pillW = 164 * SCALE;
            float pillH = 22 * SCALE;
            float pillX = birdCenterX - pillW / 2;
            float pillY = birdCenterY - 44 * SCALE;

            p.setStyle(Paint.Style.FILL);
            p.setColor(0x55000000);
            rf.set(pillX, pillY, pillX + pillW, pillY + pillH);
            c.drawRoundRect(rf, 11 * SCALE, 11 * SCALE, p);

            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.argb((int)(180 * hintPulse), 255, 179, 0));
            p.setStrokeWidth(1.0f * SCALE);
            c.drawRoundRect(rf, 11 * SCALE, 11 * SCALE, p);

            tp.setTypeface(tfCondensed);
            tp.setColor(Color.argb((int)(255 * hintPulse), 255, 236, 179));
            tp.setTextSize(10 * SCALE);
            tp.setTextAlign(Paint.Align.CENTER);
            tp.setFakeBoldText(true);
            c.drawText("💬 Ketuk Burung untuk Bersuara!", birdCenterX, pillY + 14.5f * SCALE, tp);
        }
    }

    private void drawSpeechBubble(Canvas c, float bx, float by, String text) {
        float bubbleW = Math.min(270 * SCALE, W * 0.88f);
        float bubbleH = 32 * SCALE;
        float bubbleX = bx - bubbleW / 2;
        float bubbleY = by - 48 * SCALE;

        // Shadow
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x40000000);
        rf.set(bubbleX + 2 * SCALE, bubbleY + 2 * SCALE, bubbleX + bubbleW + 2 * SCALE, bubbleY + bubbleH + 2 * SCALE);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        // Bubble Body
        p.setColor(0xFFFFFDE7);
        rf.set(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        // Border
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFF6D00);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        // Triangle pointer pointing to bird head
        Path pointer = new Path();
        pointer.moveTo(bx - 6 * SCALE, bubbleY + bubbleH - 0.5f);
        pointer.lineTo(bx + 6 * SCALE, bubbleY + bubbleH - 0.5f);
        pointer.lineTo(bx, bubbleY + bubbleH + 7 * SCALE);
        pointer.close();

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFFDE7);
        c.drawPath(pointer, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFF6D00);
        c.drawPath(pointer, p);

        // Dynamic auto-fit satirical quote text
        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFBF360C);
        float qSize = 11.5f * SCALE;
        tp.setTextSize(qSize);
        String quoted = "\"" + text + "\"";
        while (tp.measureText(quoted) > (bubbleW - 20 * SCALE) && qSize > 8.0f * SCALE) {
            qSize -= 0.5f * SCALE;
            tp.setTextSize(qSize);
        }
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(true);
        c.drawText(quoted, bx, bubbleY + bubbleH / 2 - (tp.descent() + tp.ascent()) / 2, tp);
    }

    // ========================================================
    //  PERFECTED WARDROBE & THEME SELECTION MODAL
    // ========================================================
    private void drawSkinSelectionScreen(Canvas c) {
        long timeMs = System.currentTimeMillis();

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xEE0A0301);
        c.drawRect(0, 0, W, H, p);

        float cardW = Math.min(320 * SCALE, W * 0.88f);
        float cardH = Math.min(430 * SCALE, playableH * 0.90f);
        float cardX = (W - cardW) / 2;
        float cardY = safeTop + (playableH - cardH) / 2;

        p.setShader(new LinearGradient(cardX, cardY, cardX, cardY + cardH,
                new int[]{0xFF250D1C, 0xFF12040E}, null, Shader.TileMode.CLAMP));
        rf.set(cardX, cardY, cardX + cardW, cardY + cardH);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(skinTab == 0 ? 0xFFE040FB : 0xFFFF9100);
        p.setStrokeWidth(2 * SCALE);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);

        // 1. Header Title & Coin Badge
        draw3DText(c, "WARDROBE & TEMA", cardX + cardW * 0.40f, cardY + 28 * SCALE, 18 * SCALE, tfBlack, 0xFFFFF176, 0xFFFF5722, 0xFF880E4F);

        float coinBadgeW = 86 * SCALE;
        float coinBadgeH = 24 * SCALE;
        float coinBadgeX = cardX + cardW - coinBadgeW - 14 * SCALE;
        float coinBadgeY = cardY + 16 * SCALE;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x75000000);
        rf.set(coinBadgeX, coinBadgeY, coinBadgeX + coinBadgeW, coinBadgeY + coinBadgeH);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xAAFFD54F);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(0xFFFFD54F);
        tp.setTextSize(12 * SCALE);
        String wCoinStr = String.valueOf(sawitCoins);
        float wCoinNumW = tp.measureText(wCoinStr);
        float wCoinR = 6.5f * SCALE;
        float wRowW = wCoinNumW + wCoinR * 2 + 5 * SCALE;
        float wStartX = coinBadgeX + (coinBadgeW - wRowW) / 2;
        drawMiniVectorCoin(c, wStartX + wCoinR, coinBadgeY + coinBadgeH / 2, wCoinR);
        tp.setTextAlign(Paint.Align.LEFT);
        c.drawText(wCoinStr, wStartX + wCoinR * 2 + 5 * SCALE, coinBadgeY + 17.5f * SCALE, tp);

        // 2. Tabs Row
        float tabRowY = cardY + 44 * SCALE;
        float tabH = 32 * SCALE;
        float tabGap = 8 * SCALE;
        float tabW = (cardW - 36 * SCALE - tabGap) / 2;
        float tab1X = cardX + 18 * SCALE;
        float tab2X = tab1X + tabW + tabGap;

        // Tab 1: KARAKTER
        p.setStyle(Paint.Style.FILL);
        if (skinTab == 0) {
            p.setShader(new LinearGradient(tab1X, tabRowY, tab1X + tabW, tabRowY + tabH,
                    new int[]{0xFFE040FB, 0xFF880E4F}, null, Shader.TileMode.CLAMP));
        } else {
            p.setColor(0x35000000);
        }
        rf.set(tab1X, tabRowY, tab1X + tabW, tabRowY + tabH);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(skinTab == 0 ? 0xFFFF80AB : 0x33FFFFFF);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(skinTab == 0 ? Color.WHITE : 0xAAFFFFFF);
        tp.setTextSize(12 * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText("🦅 KARAKTER", tab1X + tabW / 2, tabRowY + 20 * SCALE, tp);

        // Tab 2: RINTANGAN
        p.setStyle(Paint.Style.FILL);
        if (skinTab == 1) {
            p.setShader(new LinearGradient(tab2X, tabRowY, tab2X + tabW, tabRowY + tabH,
                    new int[]{0xFFFF9100, 0xFFD84315}, null, Shader.TileMode.CLAMP));
        } else {
            p.setColor(0x35000000);
        }
        rf.set(tab2X, tabRowY, tab2X + tabW, tabRowY + tabH);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(skinTab == 1 ? 0xFFFFD54F : 0x33FFFFFF);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(skinTab == 1 ? Color.WHITE : 0xAAFFFFFF);
        tp.setTextSize(12 * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText("🌴 RINTANGAN", tab2X + tabW / 2, tabRowY + 20 * SCALE, tp);

        // 3. Showcase Stage
        float stageCenterX = W / 2;
        float stageCenterY = cardY + 132 * SCALE;

        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(stageCenterX, stageCenterY, 52 * SCALE,
                new int[]{skinTab == 0 ? 0x55E040FB : 0x55FF6D00, 0x1AFF3D00, 0x00000000}, null, Shader.TileMode.CLAMP));
        c.drawCircle(stageCenterX, stageCenterY, 52 * SCALE, p);
        p.setShader(null);

        if (skinTab == 0) {
            float prevBirdX = stageCenterX - (BIRD_W * SCALE) / 2;
            float prevBirdY = stageCenterY - (BIRD_H * SCALE) / 2 + (float)Math.sin(timeMs * 0.004f) * (6 * SCALE);
            float prevRot = (float)Math.sin(timeMs * 0.004f) * 4;
            drawBirdWithSkin(c, prevBirdX, prevBirdY, prevRot, (float)Math.sin(timeMs * 0.008f) * 15, false, previewSkin);
        } else {
            drawPipePreview(c, stageCenterX, stageCenterY, previewPipeSkin);
        }

        boolean isUnlocked = (skinTab == 0) ? isSkinUnlocked(previewSkin) : isPipeSkinUnlocked(previewPipeSkin);
        int cost = (skinTab == 0) ? SKIN_COSTS[previewSkin] : PIPE_SKIN_COSTS[previewPipeSkin];

        // Lock badge overlay on preview stage if not unlocked
        if (!isUnlocked) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x88000000);
            c.drawCircle(stageCenterX, stageCenterY, 36 * SCALE, p);

            p.setStyle(Paint.Style.STROKE);
            p.setColor(0xFFFFD54F);
            p.setStrokeWidth(1.6f * SCALE);
            c.drawCircle(stageCenterX, stageCenterY, 36 * SCALE, p);

            tp.setTypeface(tfBlack);
            tp.setColor(0xFFFFD54F);
            tp.setTextSize(22 * SCALE);
            tp.setTextAlign(Paint.Align.CENTER);
            c.drawText("🔒", stageCenterX, stageCenterY + 8 * SCALE, tp);
        }

        // Left Navigation Arrow [◀]
        float arrowSize = 36 * SCALE;
        float arrowY = stageCenterY - arrowSize / 2;
        float leftArrowX = cardX + 14 * SCALE;

        p.setStyle(Paint.Style.FILL);
        p.setColor(0x40000000);
        rf.set(leftArrowX, arrowY, leftArrowX + arrowSize, arrowY + arrowSize);
        c.drawRoundRect(rf, 10 * SCALE, 10 * SCALE, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xAAFFFFFF);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 10 * SCALE, 10 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(19 * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText("◀", leftArrowX + arrowSize / 2, arrowY + arrowSize * 0.72f, tp);

        // Right Navigation Arrow [▶]
        float rightArrowX = cardX + cardW - arrowSize - 14 * SCALE;
        rf.set(rightArrowX, arrowY, rightArrowX + arrowSize, arrowY + arrowSize);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x40000000);
        c.drawRoundRect(rf, 10 * SCALE, 10 * SCALE, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xAAFFFFFF);
        c.drawRoundRect(rf, 10 * SCALE, 10 * SCALE, p);

        c.drawText("▶", rightArrowX + arrowSize / 2, arrowY + arrowSize * 0.72f, tp);

        // 4. Dot Indicators (Adaptive Spacing for 12 Skins)
        float dotsY = cardY + 188 * SCALE;
        int activeIdx = (skinTab == 0) ? previewSkin : previewPipeSkin;
        int totalDots = (skinTab == 0) ? TOTAL_SKINS : TOTAL_PIPE_SKINS;
        float dotSpacing = Math.min(15 * SCALE, (cardW - 54 * SCALE) / Math.max(1, totalDots));
        for (int i = 0; i < totalDots; i++) {
            float dotX = W / 2 + (i - (totalDots - 1) / 2.0f) * dotSpacing;
            p.setStyle(Paint.Style.FILL);
            p.setColor(i == activeIdx ? (skinTab == 0 ? 0xFFE040FB : 0xFFFF9100) : 0x44FFFFFF);
            c.drawCircle(dotX, dotsY, i == activeIdx ? 3.5f * SCALE : 2.0f * SCALE, p);
        }

        // 5. Title badge & names
        float infoBoxY = cardY + 208 * SCALE;
        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFD54F);
        tp.setTextSize(11.5f * SCALE);
        tp.setFakeBoldText(true);
        String subTitle = (skinTab == 0) ? SKIN_TITLES[previewSkin] : PIPE_SKIN_TITLES[previewPipeSkin];
        c.drawText(subTitle, W / 2, infoBoxY, tp);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(20 * SCALE);
        tp.setShadowLayer(4 * SCALE, 0, 2 * SCALE, 0xFF000000);
        String mainName = (skinTab == 0) ? SKIN_NAMES[previewSkin] : PIPE_SKIN_NAMES[previewPipeSkin];
        c.drawText(mainName, W / 2, infoBoxY + 22 * SCALE, tp);
        tp.clearShadowLayer();

        // 6. Unlock price badge & Description Text
        float descOffsetY = 40 * SCALE;
        if (!isUnlocked) {
            tp.setTypeface(tfCondensed);
            tp.setColor(sawitCoins >= cost ? 0xFF81C784 : 0xFFFF8A80);
            tp.setTextSize(11.5f * SCALE);
            tp.setFakeBoldText(true);
            c.drawText("HARGA BUKA: " + cost + " KOIN SAWIT", W / 2, infoBoxY + 36 * SCALE, tp);
            descOffsetY = 50 * SCALE;
        }

        tp.setTypeface(tfMedium);
        tp.setColor(0xFFF8BBD0);
        tp.setTextSize(10.5f * SCALE);
        tp.setFakeBoldText(false);
        String desc = (skinTab == 0) ? SKIN_DESC[previewSkin] : PIPE_SKIN_DESC[previewPipeSkin];
        drawWrappedText(c, desc, W / 2, infoBoxY + descOffsetY, cardW - 36 * SCALE, tp, 14 * SCALE);

        // 7. Equip / Unlock Button
        float btnW = cardW - 48 * SCALE;
        float btnH = 44 * SCALE;
        float btnX = cardX + 24 * SCALE;
        float equipBtnY = cardY + cardH - btnH * 2 - 18 * SCALE;

        boolean isEquipped = (skinTab == 0) ? (previewSkin == currentSkin) : (previewPipeSkin == currentPipeSkin);
        p.setStyle(Paint.Style.FILL);
        if (isEquipped) {
            p.setColor(0xFF2E7D32);
        } else if (isUnlocked) {
            p.setShader(new LinearGradient(btnX, equipBtnY, btnX + btnW, equipBtnY + btnH,
                    new int[]{skinTab == 0 ? 0xFFAB47BC : 0xFFFF9100, skinTab == 0 ? 0xFF7B1FA2 : 0xFFFF3D00}, null, Shader.TileMode.CLAMP));
        } else if (sawitCoins >= cost) {
            p.setShader(new LinearGradient(btnX, equipBtnY, btnX + btnW, equipBtnY + btnH,
                    new int[]{0xFFFFD54F, 0xFFFF8F00}, null, Shader.TileMode.CLAMP));
        } else {
            p.setColor(0xFF37474F);
        }
        rf.set(btnX, equipBtnY, btnX + btnW, equipBtnY + btnH);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(isEquipped ? 0xFF81C784 : (isUnlocked ? 0xFFFFD54F : (sawitCoins >= cost ? 0xFFFFF59D : 0x66FFFFFF)));
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(14 * SCALE);
        tp.setFakeBoldText(true);
        tp.setShadowLayer(4 * SCALE, 0, 2 * SCALE, 0xFF000000);
        String btnText;
        if (isEquipped) {
            btnText = "✓ SEDANG DIGUNAKAN";
        } else if (isUnlocked) {
            btnText = (skinTab == 0 ? "▶ GUNAKAN KARAKTER INI" : "▶ GUNAKAN TEMA RINTANGAN");
        } else if (sawitCoins >= cost) {
            btnText = "🔓 BUKA: " + cost + " KOIN SAWIT";
        } else {
            btnText = "🔒 BUTUH " + cost + " KOIN (KURANG " + (cost - sawitCoins) + ")";
        }
        c.drawText(btnText, W / 2, equipBtnY + 28 * SCALE, tp);
        tp.clearShadowLayer();

        // 8. Back Button
        float backBtnY = equipBtnY + btnH + 10 * SCALE;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x35000000);
        rf.set(btnX, backBtnY, btnX + btnW, backBtnY + btnH);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x66FFFFFF);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFE0E0E0);
        tp.setTextSize(14.5f * SCALE);
        tp.setFakeBoldText(true);
        c.drawText("✕ KEMBALI KE MENU", W / 2, backBtnY + 28 * SCALE, tp);
    }

    private void drawPipePreview(Canvas c, float cx, float cy, int skinId) {
        float pw = 48 * SCALE;
        float h = 30 * SCALE;
        float gap = 38 * SCALE;

        c.save();
        rf.set(cx - pw / 2 - 10 * SCALE, cy - gap / 2 - h - 4 * SCALE, cx + pw / 2 + 10 * SCALE, cy + gap / 2 + h + 4 * SCALE);
        c.clipRect(rf);
        drawThemedObstacle(c, cx - pw / 2, cy - gap / 2 - h, pw, h, true, skinId);
        drawThemedObstacle(c, cx - pw / 2, cy + gap / 2, pw, h, false, skinId);
        c.restore();
    }

    // ========================================================
    //  DRAW BIRD WITH SPECIFIC SKIN
    // ========================================================
    private void drawBirdWithSkin(Canvas c, float x, float y, float rot, float wAngle, boolean blink, int skinId) {
        float bw = BIRD_W * SCALE, bh = BIRD_H * SCALE, hw = bw / 2, hh = bh / 2;
        boolean isDead = (gameState == STATE_DYING || gameState == STATE_GAMEOVER);
        c.save();
        c.translate(x + hw, y + hh);
        c.rotate(rot);

        switch (skinId) {
            case 1:
                drawBirdDamkar(c, hw, hh, wAngle, blink, isDead);
                break;
            case 2:
                drawBirdPhoenix(c, hw, hh, wAngle, blink, isDead);
                break;
            case 3:
                drawBirdMecha(c, hw, hh, wAngle, isDead);
                break;
            case 4:
                drawBirdIntelijen(c, hw, hh, wAngle, blink, isDead);
                break;
            case 5:
                drawBirdCendrawasih(c, hw, hh, wAngle, blink, isDead);
                break;
            case 6:
                drawBirdNaga(c, hw, hh, wAngle, isDead);
                break;
            case 7:
                drawBirdGakkum(c, hw, hh, wAngle, blink, isDead);
                break;
            case 8:
                drawBirdDrone(c, hw, hh, wAngle, isDead);
                break;
            case 9:
                drawBirdDayak(c, hw, hh, wAngle, blink, isDead);
                break;
            case 10:
                drawBirdHantu(c, hw, hh, wAngle, isDead);
                break;
            case 11:
                drawBirdSultan(c, hw, hh, wAngle, isDead);
                break;
            case 0:
            default:
                drawBirdGaruda(c, hw, hh, wAngle, blink, isDead);
                break;
        }
        c.restore();
    }

    // [SKIN 0] Garuda Relawan
    private void drawBirdGaruda(Canvas c, float hw, float hh, float wAngle, boolean blink, boolean isDead) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFB300);
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);

        p.setColor(0xFFFFF9C4);
        rf.set(-hw * 0.4f, hh * 0.1f, hw * 0.6f, hh * 0.85f);
        c.drawOval(rf, p);

        p.setColor(0xFFFF8F00);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.8f);
        rf.set(-hw * 0.5f, -hh * 0.4f, hw * 0.5f, hh * 0.4f);
        c.drawOval(rf, p);
        c.restore();

        p.setColor(0xFFECEFF1);
        rf.set(hw * 0.1f, -hh * 0.2f, hw * 0.95f, hh * 0.7f);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF90A4AE);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        drawBirdEye(c, hw, hh, blink, isDead);
        drawBirdBeak(c, hw);
    }

    // [SKIN 1] Satgas Damkar
    private void drawBirdDamkar(Canvas c, float hw, float hh, float wAngle, boolean blink, boolean isDead) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFE65100);
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);

        p.setColor(0xFFFFD600);
        c.drawRect(-hw * 0.7f, -hh * 0.1f, hw * 0.5f, hh * 0.25f, p);
        p.setColor(0xFFEEEEEE);
        c.drawRect(-hw * 0.7f, 0, hw * 0.5f, hh * 0.12f, p);

        p.setColor(0xFFBF360C);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.8f);
        rf.set(-hw * 0.5f, -hh * 0.4f, hw * 0.5f, hh * 0.4f);
        c.drawOval(rf, p);
        c.restore();

        p.setColor(0xFFFFD600);
        rf.set(-hw * 0.7f, -hh * 1.05f, hw * 0.75f, -hh * 0.25f);
        c.drawRoundRect(rf, 8 * SCALE, 8 * SCALE, p);
        p.setColor(0xFFFFAB00);
        c.drawRect(-hw * 0.85f, -hh * 0.35f, hw * 0.9f, -hh * 0.15f, p);

        drawBirdEye(c, hw, hh, blink, isDead);
        drawBirdBeak(c, hw);
    }

    // [SKIN 2] Phoenix Rimba
    private void drawBirdPhoenix(Canvas c, float hw, float hh, float wAngle, boolean blink, boolean isDead) {
        long tm = System.currentTimeMillis();
        float pulse = 0.85f + 0.15f * (float)Math.sin(tm * 0.008f);

        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(0, 0, hw * 1.5f * pulse,
                new int[]{0x66FF3D00, 0x00D50000}, null, Shader.TileMode.CLAMP));
        c.drawCircle(0, 0, hw * 1.5f * pulse, p);
        p.setShader(null);

        p.setColor(0xFFD50000);
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);

        p.setColor(0xFFFF6D00);
        rf.set(-hw * 0.4f, hh * 0.1f, hw * 0.6f, hh * 0.85f);
        c.drawOval(rf, p);

        p.setColor(0xFFFFAB00);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.9f);
        pa.reset();
        pa.moveTo(-hw * 0.5f, 0);
        pa.lineTo(hw * 0.4f, -hh * 0.7f);
        pa.lineTo(hw * 0.2f, hh * 0.6f);
        pa.close();
        c.drawPath(pa, p);
        c.restore();

        drawBirdEye(c, hw, hh, blink, isDead);
        drawBirdBeak(c, hw);
    }

    // [SKIN 3] Cyber Mecha
    private void drawBirdMecha(Canvas c, float hw, float hh, float wAngle, boolean isDead) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF37474F);
        rf.set(-hw, -hh, hw, hh);
        c.drawRoundRect(rf, 8 * SCALE, 8 * SCALE, p);

        p.setColor(0xFF78909C);
        c.drawRect(-hw * 0.5f, -hh * 0.3f, hw * 0.3f, hh * 0.5f, p);

        p.setColor(0xFF263238);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.8f);
        pa.reset();
        pa.moveTo(-hw * 0.5f, -hh * 0.3f);
        pa.lineTo(hw * 0.4f, -hh * 0.5f);
        pa.lineTo(hw * 0.2f, hh * 0.4f);
        pa.close();
        c.drawPath(pa, p);
        c.restore();

        if (isDead) {
            p.setColor(0xFFFF1744); // Critical red visor
            rf.set(hw * 0.15f, -hh * 0.35f, hw * 0.85f, -hh * 0.05f);
            c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);

            // Red warning cross on visor
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0xFFFFFFFF);
            p.setStrokeWidth(1.4f * SCALE);
            float vx = hw * 0.5f, vy = -hh * 0.20f, vs = 3.5f * SCALE;
            c.drawLine(vx - vs, vy - vs, vx + vs, vy + vs, p);
            c.drawLine(vx + vs, vy - vs, vx - vs, vy + vs, p);
            p.setStyle(Paint.Style.FILL);
        } else {
            p.setColor(0xFF00E5FF);
            rf.set(hw * 0.15f, -hh * 0.35f, hw * 0.85f, -hh * 0.05f);
            c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);
        }

        p.setColor(0xFFFFAB00);
        pa.reset();
        pa.moveTo(hw * 0.7f, -hh * 0.1f);
        pa.lineTo(hw + 12 * SCALE, 0);
        pa.lineTo(hw * 0.7f, hh * 0.3f);
        pa.close();
        c.drawPath(pa, p);

        if (!isDead) {
            p.setColor(0xFF00E5FF);
            c.drawCircle(-hw * 0.85f, 0, (4 + (float)Math.sin(System.currentTimeMillis() * 0.02f) * 2) * SCALE, p);
        } else {
            p.setColor(0x88424242);
            c.drawCircle(-hw * 0.85f, 0, 3 * SCALE, p);
        }
    }

    // [SKIN 4] Elang Intelijen (KPK Agent Hawk)
    private void drawBirdIntelijen(Canvas c, float hw, float hh, float wAngle, boolean blink, boolean isDead) {
        // Dark charcoal tactical body
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF263238);
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);

        // White formal shirt collar & chest
        p.setColor(0xFFECEFF1);
        rf.set(-hw * 0.2f, -hh * 0.1f, hw * 0.5f, hh * 0.85f);
        c.drawOval(rf, p);

        // Red tie / tactical badge
        p.setColor(0xFFD50000);
        pa.reset();
        pa.moveTo(hw * 0.1f, -hh * 0.05f);
        pa.lineTo(hw * 0.25f, -hh * 0.05f);
        pa.lineTo(hw * 0.22f, hh * 0.65f);
        pa.lineTo(hw * 0.17f, hh * 0.85f);
        pa.lineTo(hw * 0.13f, hh * 0.65f);
        pa.close();
        c.drawPath(pa, p);

        // Tactical dark wing with flap rotation
        p.setColor(0xFF37474F);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.8f);
        rf.set(-hw * 0.5f, -hh * 0.4f, hw * 0.5f, hh * 0.4f);
        c.drawOval(rf, p);
        p.setColor(0xFF102027);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawLine(-hw * 0.2f, 0, hw * 0.4f, 0, p);
        c.restore();

        p.setStyle(Paint.Style.FILL);
        drawBirdBeak(c, hw);

        // Cool black Aviator sunglasses / agent shades
        if (isDead) {
            drawBirdEye(c, hw, hh, false, true);
        } else {
            p.setColor(0xFF111111);
            rf.set(hw * 0.05f, -hh * 0.45f, hw * 0.65f, hh * 0.15f);
            c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);

            // Reflection glint
            p.setColor(0xFF00E5FF);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.0f * SCALE);
            c.drawLine(hw * 0.12f, -hh * 0.15f, hw * 0.35f, -hh * 0.38f, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    // [SKIN 5] Raja Cendrawasih (Royal Bird of Paradise)
    private void drawBirdCendrawasih(Canvas c, float hw, float hh, float wAngle, boolean blink, boolean isDead) {
        long timeMs = System.currentTimeMillis();

        // Elegant flowing twin tail ribbons
        float wave1 = (float)Math.sin(timeMs * 0.007f) * (5 * SCALE);
        float wave2 = (float)Math.cos(timeMs * 0.007f) * (5 * SCALE);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFD54F);
        p.setStrokeWidth(1.8f * SCALE);
        pa.reset();
        pa.moveTo(-hw * 0.8f, hh * 0.2f);
        pa.quadTo(-hw * 1.5f, hh * 0.4f + wave1, -hw * 2.2f, hh * 0.9f + wave1);
        c.drawPath(pa, p);

        pa.reset();
        pa.moveTo(-hw * 0.8f, hh * 0.4f);
        pa.quadTo(-hw * 1.4f, hh * 0.7f + wave2, -hw * 2.1f, hh * 1.3f + wave2);
        c.drawPath(pa, p);

        // Emerald green body
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF00796B);
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);

        // Golden amber chest & belly
        p.setColor(0xFFFFB300);
        rf.set(-hw * 0.3f, -hh * 0.2f, hw * 0.6f, hh * 0.85f);
        c.drawOval(rf, p);

        // Royal sapphire wing
        p.setColor(0xFF1565C0);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.8f);
        rf.set(-hw * 0.5f, -hh * 0.4f, hw * 0.5f, hh * 0.4f);
        c.drawOval(rf, p);
        c.restore();

        p.setStyle(Paint.Style.FILL);

        // Golden feather crown on head
        p.setColor(0xFFFFD54F);
        pa.reset();
        pa.moveTo(-hw * 0.1f, -hh * 0.8f);
        pa.lineTo(hw * 0.15f, -hh * 1.3f);
        pa.lineTo(hw * 0.3f, -hh * 0.7f);
        pa.close();
        c.drawPath(pa, p);

        drawBirdEye(c, hw, hh, blink, isDead);
        drawBirdBeak(c, hw);
    }

    // [SKIN 6] Naga Rimba Purba (Mythic Flame Dragon)
    private void drawBirdNaga(Canvas c, float hw, float hh, float wAngle, boolean isDead) {
        // Ruby crimson dragon body
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFB71C1C);
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);

        // Magma underbelly plates
        p.setColor(0xFFFF6D00);
        rf.set(-hw * 0.3f, hh * 0.1f, hw * 0.5f, hh * 0.85f);
        c.drawOval(rf, p);

        // Jagged fiery dragon wings
        c.save();
        c.translate(-hw * 0.25f, -hh * 0.1f);
        c.rotate((float)Math.toDegrees(wAngle) * 0.9f);
        pa.reset();
        pa.moveTo(0, 0);
        pa.lineTo(-hw * 0.9f, -hh * 1.1f);
        pa.lineTo(-hw * 0.4f, -hh * 0.4f);
        pa.lineTo(-hw * 0.8f, 0);
        pa.lineTo(-hw * 0.2f, hh * 0.2f);
        pa.close();
        p.setColor(0xFFFF3D00);
        c.drawPath(pa, p);
        p.setColor(0xFFFFD600);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawPath(pa, p);
        c.restore();

        p.setStyle(Paint.Style.FILL);

        // Golden dragon horns
        p.setColor(0xFFFFD54F);
        pa.reset();
        pa.moveTo(-hw * 0.2f, -hh * 0.6f);
        pa.quadTo(-hw * 0.6f, -hh * 1.4f, -hw * 0.8f, -hh * 1.3f);
        pa.quadTo(-hw * 0.4f, -hh * 0.9f, 0, -hh * 0.7f);
        pa.close();
        c.drawPath(pa, p);

        // Glowing dragon eye
        if (isDead) {
            tp.setTypeface(tfBlack);
            tp.setColor(0xFFFF1744);
            tp.setTextSize(9 * SCALE);
            tp.setTextAlign(Paint.Align.CENTER);
            c.drawText("X", hw * 0.35f, -hh * 0.05f, tp);
        } else {
            p.setColor(0xFFFFEA00); // Glowing yellow sclera
            c.drawCircle(hw * 0.35f, -hh * 0.15f, 4.2f * SCALE, p);
            p.setColor(0xFF212121); // Slit pupil
            rf.set(hw * 0.33f, -hh * 0.25f, hw * 0.37f, -hh * 0.05f);
            c.drawOval(rf, p);
        }

        // Dragon jaw / snout
        p.setColor(0xFF7F0000);
        pa.reset();
        pa.moveTo(hw * 0.45f, -hh * 0.2f);
        pa.lineTo(hw + 14 * SCALE, hh * 0.1f);
        pa.lineTo(hw * 0.5f, hh * 0.45f);
        pa.close();
        c.drawPath(pa, p);
    }

    // [SKIN 7] Jaksa Gakkum (PENEGAK VONIS HGU)
    private void drawBirdGakkum(Canvas c, float hw, float hh, float wAngle, boolean blink, boolean isDead) {
        // Deep forest green formal body
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1B5E20);
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);

        // White judicial shirt front
        p.setColor(0xFFFFFFFF);
        rf.set(-hw * 0.1f, -hh * 0.15f, hw * 0.5f, hh * 0.85f);
        c.drawOval(rf, p);

        // Dark green suit lapel V-cut
        p.setColor(0xFF0D3310);
        pa.reset();
        pa.moveTo(-hw * 0.2f, -hh * 0.2f);
        pa.lineTo(hw * 0.2f, hh * 0.75f);
        pa.lineTo(hw * 0.5f, -hh * 0.2f);
        pa.close();
        c.drawPath(pa, p);

        // Scale of Justice golden pin
        p.setColor(0xFFFFD54F);
        c.drawCircle(hw * 0.18f, hh * 0.35f, 2.5f * SCALE, p);

        // Wing with deep green gradient
        p.setColor(0xFF2E7D32);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.8f);
        rf.set(-hw * 0.5f, -hh * 0.4f, hw * 0.5f, hh * 0.4f);
        c.drawOval(rf, p);
        p.setColor(0xFF0D3310);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawLine(-hw * 0.2f, 0, hw * 0.4f, 0, p);
        c.restore();

        p.setStyle(Paint.Style.FILL);
        drawBirdBeak(c, hw);

        // Black Judicial Cap (Peci / Toga) on head
        p.setColor(0xFF111111);
        rf.set(-hw * 0.3f, -hh * 1.05f, hw * 0.45f, -hh * 0.55f);
        c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);
        // Golden tassel
        p.setColor(0xFFFFD54F);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawLine(hw * 0.1f, -hh * 1.0f, hw * 0.45f, -hh * 0.7f, p);
        p.setStyle(Paint.Style.FILL);

        drawBirdEye(c, hw, hh, blink, isDead);

        // Gold-rimmed reading spectacles
        if (!isDead) {
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0xFFFFD54F);
            p.setStrokeWidth(1.2f * SCALE);
            c.drawCircle(hw * 0.38f, -hh * 0.25f, 6.0f * SCALE, p);
            c.drawLine(hw * 0.15f, -hh * 0.25f, hw * 0.32f, -hh * 0.25f, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    // [SKIN 8] Aktivis Drone (MATA-MATA SATELIT)
    private void drawBirdDrone(Canvas c, float hw, float hh, float wAngle, boolean isDead) {
        long timeMs = System.currentTimeMillis();

        // Matte charcoal drone chassis
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF263238);
        rf.set(-hw, -hh, hw, hh);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        // Battery compartment / neon orange telemetry pack
        p.setColor(0xFFFF6D00);
        rf.set(-hw * 0.4f, hh * 0.1f, hw * 0.4f, hh * 0.75f);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        // Blinking status telemetry LED bar
        p.setColor((timeMs % 600 < 300) ? 0xFF00E676 : 0xFF00B0FF);
        c.drawCircle(-hw * 0.15f, hh * 0.42f, 1.8f * SCALE, p);
        c.drawCircle(hw * 0.15f, hh * 0.42f, 1.8f * SCALE, p);

        // Aerodynamic rotor / wing
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.85f);
        p.setColor(0xFF455A64);
        rf.set(-hw * 0.5f, -hh * 0.35f, hw * 0.5f, hh * 0.35f);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);
        // Wing edge warning tip
        p.setColor(0xFFFFD600);
        rf.set(-hw * 0.5f, -hh * 0.35f, -hw * 0.35f, hh * 0.35f);
        c.drawRoundRect(rf, 2 * SCALE, 2 * SCALE, p);
        c.restore();

        // High-tech titanium beak
        p.setColor(0xFFFFB300);
        drawBirdBeak(c, hw);

        // Dual Night-Vision Goggles (NVG) with glowing emerald lens
        p.setColor(0xFF102027);
        rf.set(hw * 0.1f, -hh * 0.50f, hw * 0.65f, -hh * 0.05f);
        c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);

        if (isDead) {
            tp.setTypeface(tfBlack);
            tp.setColor(0xFFFF1744);
            tp.setTextSize(9 * SCALE);
            tp.setTextAlign(Paint.Align.CENTER);
            c.drawText("X", hw * 0.38f, -hh * 0.20f, tp);
        } else {
            p.setColor(0xFF00E676); // Glowing night-vision green
            c.drawCircle(hw * 0.38f, -hh * 0.28f, 5.0f * SCALE, p);
            p.setColor(0xFFFFFFFF);
            c.drawCircle(hw * 0.42f, -hh * 0.32f, 1.8f * SCALE, p);
        }

        // Antenna with blinking red REC beacon
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF90A4AE);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawLine(-hw * 0.5f, -hh * 0.4f, -hw * 0.7f, -hh * 1.1f, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor((timeMs % 500 < 250) ? 0xFFFF1744 : 0xFF7F0000); // Blinking REC
        c.drawCircle(-hw * 0.7f, -hh * 1.1f, 2.2f * SCALE, p);
    }

    // [SKIN 9] Panglima Dayak (PENJAGA HUTAN ADAT - ENGGANG GADING)
    private void drawBirdDayak(Canvas c, float hw, float hh, float wAngle, boolean blink, boolean isDead) {
        // Deep obsidian hornbill body
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1F1F1F);
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);

        // Pure white underbelly / chest
        p.setColor(0xFFF5F5F5);
        rf.set(-hw * 0.2f, hh * 0.05f, hw * 0.5f, hh * 0.85f);
        c.drawOval(rf, p);

        // Black wing with white tip feathers
        p.setColor(0xFF262626);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.8f);
        rf.set(-hw * 0.5f, -hh * 0.4f, hw * 0.5f, hh * 0.4f);
        c.drawOval(rf, p);
        // White primary tips
        p.setColor(0xFFFFFFFF);
        rf.set(-hw * 0.5f, -hh * 0.2f, -hw * 0.25f, hh * 0.4f);
        c.drawOval(rf, p);
        c.restore();

        // Majestic Oversized Ivory & Red Casque Hornbill Beak (Paruh Enggang Cula)
        p.setStyle(Paint.Style.FILL);
        // Lower beak: Ivory white
        p.setColor(0xFFFFF9C4);
        pa.reset();
        pa.moveTo(hw * 0.45f, -hh * 0.1f);
        pa.lineTo(hw + 16 * SCALE, hh * 0.15f);
        pa.lineTo(hw * 0.45f, hh * 0.35f);
        pa.close();
        c.drawPath(pa, p);

        // Upper Casque (Cula Gading): Fiery scarlet to gold
        p.setColor(0xFFD50000);
        pa.reset();
        pa.moveTo(hw * 0.1f, -hh * 0.65f);
        pa.quadTo(hw * 0.7f, -hh * 0.85f, hw + 14 * SCALE, -hh * 0.1f);
        pa.lineTo(hw * 0.4f, -hh * 0.1f);
        pa.close();
        c.drawPath(pa, p);
        p.setColor(0xFFFFB300);
        c.drawCircle(hw * 0.45f, -hh * 0.45f, 3.5f * SCALE, p);

        // Dayak Sacred Feather Headband (Ikat Kepala Adat)
        p.setColor(0xFFD50000); // Red woven band
        rf.set(-hw * 0.2f, -hh * 0.7f, hw * 0.4f, -hh * 0.45f);
        c.drawRoundRect(rf, 2 * SCALE, 2 * SCALE, p);
        p.setColor(0xFFFFFFFF); // White feather crest standing tall
        pa.reset();
        pa.moveTo(-hw * 0.1f, -hh * 0.7f);
        pa.lineTo(-hw * 0.25f, -hh * 1.4f);
        pa.lineTo(0, -hh * 0.7f);
        pa.lineTo(hw * 0.15f, -hh * 1.5f);
        pa.lineTo(hw * 0.3f, -hh * 0.7f);
        pa.close();
        c.drawPath(pa, p);
        p.setColor(0xFF212121); // Black feather tips
        c.drawCircle(-hw * 0.25f, -hh * 1.35f, 2.0f * SCALE, p);
        c.drawCircle(hw * 0.15f, -hh * 1.45f, 2.0f * SCALE, p);

        drawBirdEye(c, hw, hh, blink, isDead);

        // Red war paint across cheek
        if (!isDead) {
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0xFFD50000);
            p.setStrokeWidth(1.8f * SCALE);
            c.drawLine(hw * 0.18f, -hh * 0.1f, hw * 0.45f, -hh * 0.05f, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    // [SKIN 10] Hantu Karhutla (ARWAH GAMBUT HITAM)
    private void drawBirdHantu(Canvas c, float hw, float hh, float wAngle, boolean isDead) {
        long timeMs = System.currentTimeMillis();
        float pulse = 0.85f + 0.15f * (float)Math.sin(timeMs * 0.007f);

        // Ethereal smoky purple-charcoal body
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(0, 0, hw * 1.2f,
                new int[]{0xEE4A148C, 0xDD311B92, 0x88120024}, null, Shader.TileMode.CLAMP));
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);
        p.setShader(null);

        // Ghostly mist tendrils / smoke tail
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.argb((int)(140 * pulse), 178, 102, 255));
        p.setStrokeWidth(2.2f * SCALE);
        pa.reset();
        pa.moveTo(-hw * 0.8f, 0);
        pa.quadTo(-hw * 1.5f, hh * 0.3f * pulse, -hw * 2.0f, -hh * 0.4f * pulse);
        c.drawPath(pa, p);

        // Translucent ghostly flame wing
        p.setStyle(Paint.Style.FILL);
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.9f);
        p.setColor(0xBB7B1FA2);
        rf.set(-hw * 0.6f, -hh * 0.45f, hw * 0.4f, hh * 0.45f);
        c.drawOval(rf, p);
        p.setColor(0xEEBA68C8);
        rf.set(-hw * 0.4f, -hh * 0.25f, hw * 0.2f, hh * 0.25f);
        c.drawOval(rf, p);
        c.restore();

        // Dark amethyst beak
        p.setColor(0xFF311B92);
        drawBirdBeak(c, hw);

        // Ethereal glowing spectral cyan eyes (Souls of burnt peatland)
        if (isDead) {
            tp.setTypeface(tfBlack);
            tp.setColor(0xFFE040FB);
            tp.setTextSize(9 * SCALE);
            tp.setTextAlign(Paint.Align.CENTER);
            c.drawText("X", hw * 0.38f, -hh * 0.20f, tp);
        } else {
            // Glowing cyan orb
            p.setColor(0xFF00E5FF);
            c.drawCircle(hw * 0.38f, -hh * 0.25f, 5.0f * SCALE * pulse, p);
            p.setColor(0xFFFFFFFF);
            c.drawCircle(hw * 0.40f, -hh * 0.27f, 2.0f * SCALE, p);
            // Purple outer eye flare
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0x88E040FB);
            p.setStrokeWidth(1.5f * SCALE);
            c.drawCircle(hw * 0.38f, -hh * 0.25f, 7.0f * SCALE, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    // [SKIN 11] Garuda Emas 24K (SULTAN ANTI-KORUPSI)
    private void drawBirdSultan(Canvas c, float hw, float hh, float wAngle, boolean isDead) {
        long timeMs = System.currentTimeMillis();

        // Shimmering 24K Gold Body
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(-hw, -hh, hw, hh,
                new int[]{0xFFFFF9C4, 0xFFFFD700, 0xFFFFB300, 0xFFC67C00}, null, Shader.TileMode.CLAMP));
        rf.set(-hw, -hh, hw, hh);
        c.drawOval(rf, p);
        p.setShader(null);

        // White-gold breastplate with diamond facet
        p.setColor(0xFFFFFDE7);
        rf.set(-hw * 0.2f, -hh * 0.1f, hw * 0.5f, hh * 0.85f);
        c.drawOval(rf, p);

        // Royal diamond emblem on chest
        p.setColor(0xFF00E5FF); // Cut cyan diamond
        pa.reset();
        pa.moveTo(hw * 0.15f, hh * 0.2f);
        pa.lineTo(hw * 0.25f, hh * 0.35f);
        pa.lineTo(hw * 0.15f, hh * 0.55f);
        pa.lineTo(hw * 0.05f, hh * 0.35f);
        pa.close();
        c.drawPath(pa, p);

        // Golden feathered wing with sparkle
        p.setShader(new LinearGradient(-hw * 0.5f, 0, hw * 0.5f, 0,
                new int[]{0xFFFFE082, 0xFFFFB300, 0xFFC67C00}, null, Shader.TileMode.CLAMP));
        c.save();
        c.translate(-hw * 0.3f, 0);
        c.rotate((float)Math.toDegrees(wAngle) * 0.8f);
        rf.set(-hw * 0.5f, -hh * 0.4f, hw * 0.5f, hh * 0.4f);
        c.drawOval(rf, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFFDE7);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawLine(-hw * 0.2f, 0, hw * 0.4f, 0, p);
        c.restore();
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);

        // Imperial Golden Crown on head
        p.setColor(0xFFFFD700);
        pa.reset();
        pa.moveTo(-hw * 0.2f, -hh * 0.7f);
        pa.lineTo(-hw * 0.25f, -hh * 1.35f);
        pa.lineTo(-hw * 0.05f, -hh * 0.95f);
        pa.lineTo(hw * 0.1f, -hh * 1.45f); // Center peak
        pa.lineTo(hw * 0.25f, -hh * 0.95f);
        pa.lineTo(hw * 0.4f, -hh * 1.35f);
        pa.lineTo(hw * 0.35f, -hh * 0.7f);
        pa.close();
        c.drawPath(pa, p);
        // Crown jewels
        p.setColor(0xFFFF1744); // Ruby
        c.drawCircle(hw * 0.1f, -hh * 1.45f, 1.8f * SCALE, p);
        p.setColor(0xFF00E5FF); // Diamonds
        c.drawCircle(-hw * 0.25f, -hh * 1.35f, 1.5f * SCALE, p);
        c.drawCircle(hw * 0.4f, -hh * 1.35f, 1.5f * SCALE, p);

        // Polished platinum-gold beak
        p.setColor(0xFFFF9100);
        drawBirdBeak(c, hw);

        drawBirdEye(c, hw, hh, false, isDead);

        // Diamond glint sparkle
        float glintT = (timeMs % 1200) / 1200f;
        if (glintT < 0.4f) {
            float gScale = (float)Math.sin(glintT / 0.4f * Math.PI) * 4 * SCALE;
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFFFFFFF);
            c.drawCircle(hw * 0.15f, hh * 0.35f, gScale * 0.5f, p);
        }
    }

    private void drawBirdEye(Canvas c, float hw, float hh, boolean blink, boolean isDead) {
        if (isDead) {
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0xFF212121);
            p.setStrokeWidth(2.4f * SCALE);
            float ex = hw * 0.40f, ey = -hh * 0.25f, es = 4.2f * SCALE;
            c.drawLine(ex - es, ey - es, ex + es, ey + es, p);
            c.drawLine(ex + es, ey - es, ex - es, ey + es, p);
            return;
        }
        if (blink) {
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0xFF263238);
            p.setStrokeWidth(2.5f * SCALE);
            c.drawLine(hw * 0.2f, -hh * 0.25f, hw * 0.6f, -hh * 0.25f, p);
            return;
        }
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        c.drawCircle(hw * 0.38f, -hh * 0.25f, 6.5f * SCALE, p);
        p.setColor(0xFF1A1A1A);
        c.drawCircle(hw * 0.45f, -hh * 0.25f, 3.5f * SCALE, p);
        p.setColor(Color.WHITE);
        c.drawCircle(hw * 0.48f, -hh * 0.35f, 1.4f * SCALE, p);
    }

    private void drawBirdBeak(Canvas c, float hw) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFF6D00);
        pa.reset();
        pa.moveTo(hw * 0.6f, -6 * SCALE);
        pa.lineTo(hw + 14 * SCALE, SCALE);
        pa.lineTo(hw * 0.6f, 7 * SCALE);
        pa.close();
        c.drawPath(pa, p);
    }

    // ========================================================
    //  ATMOSPHERIC INFERNO SKY & RADIANT GOD RAYS
    // ========================================================
    private void drawSkyAndSun(Canvas c) {
        // Vibrant twilight dusk sky
        p.setStyle(Paint.Style.FILL);
        if (skyShader != null) {
            p.setShader(skyShader);
        } else {
            p.setShader(new LinearGradient(0, 0, 0, H,
                    new int[]{0xFF1C0502, 0xFF4A0E02, 0xFF8A1E00, 0xFFD84315, 0xFFFF7043, 0xFF5A1604},
                    new float[]{0, 0.18f, 0.38f, 0.62f, 0.82f, 1.0f}, Shader.TileMode.CLAMP));
        }
        c.drawRect(0, 0, W, H, p);
        p.setShader(null);

        // Blood Sun / Inferno Sun
        float sunX = W * 0.72f, sunY = safeTop + playableH * 0.15f;
        long timeMs = System.currentTimeMillis();
        float sunPulse = 1 + (float)Math.sin(timeMs * 0.0008f) * 0.05f;

        // Radiant God Rays / Sun shafts streaming through smoke haze!
        if (graphicsLevel >= 1) {
            for (int r = 0; r < 5; r++) {
                float rayAngle = 0.52f + r * 0.38f + (float)Math.sin(timeMs * 0.0005f + r * 1.5f) * 0.04f;
                float rayLength = W * 1.7f;
                float spread = 0.11f;

                pa.reset();
                pa.moveTo(sunX, sunY);
                pa.lineTo(sunX + (float)Math.cos(rayAngle - spread) * rayLength, sunY + (float)Math.sin(rayAngle - spread) * rayLength);
                pa.lineTo(sunX + (float)Math.cos(rayAngle + spread) * rayLength, sunY + (float)Math.sin(rayAngle + spread) * rayLength);
                pa.close();

                p.setShader(new LinearGradient(sunX, sunY,
                        sunX + (float)Math.cos(rayAngle) * (W * 0.9f), sunY + (float)Math.sin(rayAngle) * (W * 0.9f),
                        new int[]{0x22FFD54F, 0x12FF7043, 0x00000000}, null, Shader.TileMode.CLAMP));
                c.drawPath(pa, p);
                p.setShader(null);
            }
        }

        // Sun corona
        float sunR = 92 * SCALE * sunPulse;
        p.setShader(new RadialGradient(sunX, sunY, sunR,
                new int[]{0xDDFF5500, 0x77CC2200, 0x22781400, 0x00000000}, null, Shader.TileMode.CLAMP));
        c.drawCircle(sunX, sunY, sunR, p);
        p.setShader(null);

        p.setColor(0xFFFF3D00);
        c.drawCircle(sunX, sunY, 28 * SCALE, p);
        p.setColor(0xFFFF9100);
        c.drawCircle(sunX, sunY, 17 * SCALE, p);
        p.setColor(0xFFFFF59D);
        c.drawCircle(sunX, sunY, 9 * SCALE, p);

        // Fleeing wildlife
        drawFleeingBirds(c);
    }

    private void drawFleeingBirds(Canvas c) {
        long timeMs = System.currentTimeMillis();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xAA1E0502);
        p.setStrokeWidth(2.2f * SCALE);
        p.setStrokeCap(Paint.Cap.ROUND);

        for (FleeingBird fb : fleeingBirds) {
            float flap = (float)Math.sin(timeMs * 0.007f * fb.flapPhase) * (6 * SCALE);
            float bx = fb.x;
            float by = fb.y;
            float sz = fb.size;

            pa.reset();
            pa.moveTo(bx - sz, by - flap);
            pa.quadTo(bx - sz * 0.5f, by - sz * 0.3f, bx, by);
            pa.quadTo(bx + sz * 0.5f, by - sz * 0.3f, bx + sz, by - flap);
            c.drawPath(pa, p);
        }
    }

    private void drawVolcanicRidge(Canvas c) {
        float horizonY = H - groundH - 35 * SCALE;

        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, horizonY - 150 * SCALE, 0, horizonY + 10 * SCALE,
                new int[]{0xFF240602, 0xFF4A1004, 0xFF240602}, null, Shader.TileMode.CLAMP));

        pa.reset();
        pa.moveTo(0, horizonY);
        pa.lineTo(0, horizonY - 50 * SCALE);
        pa.lineTo(W * 0.18f, horizonY - 110 * SCALE);
        pa.lineTo(W * 0.28f, horizonY - 75 * SCALE);
        pa.lineTo(W * 0.48f, horizonY - 150 * SCALE); // Majestic volcanic peak
        pa.lineTo(W * 0.62f, horizonY - 95 * SCALE);
        pa.lineTo(W * 0.82f, horizonY - 125 * SCALE);
        pa.lineTo(W, horizonY - 65 * SCALE);
        pa.lineTo(W, horizonY);
        pa.close();
        c.drawPath(pa, p);
        p.setShader(null);

        // Lava ravine on volcanic slope
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xCCFF3D00);
        p.setStrokeWidth(2.5f * SCALE);
        pa.reset();
        pa.moveTo(W * 0.48f, horizonY - 150 * SCALE);
        pa.lineTo(W * 0.46f, horizonY - 100 * SCALE);
        pa.lineTo(W * 0.50f, horizonY - 55 * SCALE);
        c.drawPath(pa, p);

        // Volcanic smoke plume
        long timeMs = System.currentTimeMillis();
        for (int v = 0; v < 3; v++) {
            float vt = timeMs * 0.003f + v * 1.5f;
            float vx = W * 0.48f + (float)Math.sin(vt) * 14 * SCALE + v * 9 * SCALE;
            float vy = horizonY - 150 * SCALE - (22 + v * 32) * SCALE;
            float vr = (18 + v * 15) * SCALE;
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb((int)(85 - v * 22), 55, 22, 10));
            c.drawCircle(vx, vy, vr, p);
        }
    }

    private void drawContinuousPalmCanopy(Canvas c, float dt) {
        float horizonY = H - groundH;
        float canopyH = 75 * SCALE;

        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, horizonY - canopyH, 0, horizonY,
                new int[]{0xFF240A03, 0xFF4D1705, 0xFF2A0C03}, null, Shader.TileMode.CLAMP));

        pa.reset();
        pa.moveTo(0, horizonY);
        float step = 32 * SCALE;
        for (float cx = 0; cx <= W + step; cx += step) {
            float frondPeakY = horizonY - canopyH * (0.65f + 0.35f * (float)Math.abs(Math.sin(cx * 0.028f)));
            pa.quadTo(cx - step * 0.5f, frondPeakY, cx, horizonY - canopyH * 0.38f);
        }
        pa.lineTo(W, horizonY);
        pa.close();
        c.drawPath(pa, p);
        p.setShader(null);
    }

    private void drawSmokeClouds(Canvas c) {
        for (SmokeCloud s : smokeClouds) {
            int alpha = (int)(s.opacity * 255);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new RadialGradient(s.x, s.y, s.radius,
                    new int[]{Color.argb(alpha, 85, 38, 16), Color.argb(alpha / 2, 55, 22, 10), 0x00000000},
                    null, Shader.TileMode.CLAMP));
            c.drawCircle(s.x, s.y, s.radius, p);
            p.setShader(null);
        }
    }

    private void drawParallaxForest(Canvas c, ArrayList<PalmTree> trees, float speedFactor, float dt) {
        float groundY = H - groundH;
        long timeMs = System.currentTimeMillis();

        for (PalmTree t : trees) {
            t.x -= worldSpeed * speedFactor * SCALE * dt;
            float resetDist = (speedFactor > 0.6f) ? 150 : 100;
            if (t.x < -resetDist * SCALE) t.x = W + resetDist * SCALE;
            drawEnhancedPalmTree(c, t, groundY, timeMs);
        }
    }

    // ========================================================
    //  REALISTIC OIL PALM TREE (Solid Lush Fronds, No Weird Teeth/Balls!)
    // ========================================================
    private void drawEnhancedPalmTree(Canvas c, PalmTree t, float baseY, long timeMs) {
        c.save();
        c.translate(t.x, baseY);
        c.scale(t.scale, t.scale);

        float sway = (float)Math.sin(timeMs * 0.001f + t.trunkSway) * 4 * SCALE;
        float crownX = 4 * SCALE + sway * 0.6f;
        float crownY = -t.height;
        float trunkBaseW = 20 * SCALE;
        float trunkTopW = 13 * SCALE;

        // 1. Tapered Trunk with Flared Base
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(-trunkBaseW, 0, trunkBaseW, 0,
                new int[]{t.isBurning ? 0xFF140500 : 0xFF1E0802,
                         t.isBurning ? 0xFF4A1A06 : 0xFF421806,
                         t.isBurning ? 0xFF2E0E02 : 0xFF280C03,
                         0xFF100301},
                new float[]{0, 0.35f, 0.75f, 1.0f}, Shader.TileMode.CLAMP));

        pa.reset();
        pa.moveTo(-trunkBaseW * 0.8f, 0);
        pa.quadTo(-trunkBaseW * 0.35f + sway * 0.3f, -t.height * 0.55f, crownX - trunkTopW * 0.5f, crownY);
        pa.lineTo(crownX + trunkTopW * 0.5f, crownY);
        pa.quadTo(trunkBaseW * 0.35f + sway * 0.3f, -t.height * 0.55f, trunkBaseW * 0.8f, 0);
        pa.close();
        c.drawPath(pa, p);
        p.setShader(null);

        // 2. Diamond Pattern Leaf Scars (Bekas Pelepah Sawit)
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.4f * SCALE);
        p.setColor(t.isBurning ? 0xAA0A0200 : 0xAA120401);
        float stepY = 20 * SCALE;
        for (float y = -stepY; y > crownY + 10 * SCALE; y -= stepY) {
            float frac = -y / t.height;
            float curSway = sway * frac;
            float halfW = (trunkBaseW * (1 - frac) + trunkTopW * frac) * 0.65f;
            pa.reset();
            pa.moveTo(-halfW + curSway, y);
            pa.quadTo(curSway, y - 4 * SCALE, halfW + curSway, y);
            c.drawPath(pa, p);
        }

        // 3. Glowing Ember Crevice inside Burning Trunk
        if (t.isBurning) {
            float pulse = 0.6f + 0.4f * (float)Math.sin(timeMs * 0.006f + t.firePhase);
            p.setColor(Color.argb((int)(160 * pulse), 255, 90, 0));
            p.setStrokeWidth(1.6f * SCALE);
            pa.reset();
            pa.moveTo(sway * 0.3f, -t.height * 0.2f);
            pa.lineTo(sway * 0.4f + 3 * SCALE, -t.height * 0.55f);
            pa.lineTo(crownX - 2 * SCALE, crownY + 10 * SCALE);
            c.drawPath(pa, p);
        }

        // 4. Fire Glow Behind Crown (Soft ambient lighting, NOT a giant sphere!)
        if (t.isBurning) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(new RadialGradient(crownX, crownY, 45 * SCALE,
                    new int[]{0x77FF6D00, 0x33FF3D00, 0x00000000}, null, Shader.TileMode.CLAMP));
            c.drawCircle(crownX, crownY, 45 * SCALE, p);
            p.setShader(null);
        }

        // 5. Clusters of Oil Palm Fruit Bunches (Tandan Buah Segar - TBS)
        p.setStyle(Paint.Style.FILL);
        for (int b = -1; b <= 1; b += 2) {
            float bx = crownX + b * (7 * SCALE);
            float by = crownY + 10 * SCALE;
            p.setColor(t.isBurning ? 0xFF2A0C02 : 0xFFD84315);
            c.drawCircle(bx, by, 5.5f * SCALE, p);
            p.setColor(0xFFFF8F00);
            c.drawCircle(bx - b * 1.5f * SCALE, by - 1.5f * SCALE, 1.8f * SCALE, p);
        }

        // 6. SOLID, LUSH FOUNTAIN-LIKE PALM FRONDS (Real palm leaf plumes, not chicken wire!)
        int frondCount = graphicsLevel >= 2 ? 14 : (graphicsLevel == 1 ? 10 : 7);
        for (int i = 0; i < frondCount; i++) {
            float angle = (i / (float)frondCount) * (float)Math.PI * 2;
            float len = (70 + (i % 3) * 18) * SCALE;
            if (t.isBurning) len *= (0.8f + (float)Math.sin(i * 1.5f) * 0.2f);

            float cosA = (float)Math.cos(angle);
            float sinA = (float)Math.sin(angle);
            float endX = crownX + cosA * len;
            float endY = crownY + sinA * len * 0.55f + 22 * SCALE;

            // Broad solid leaf plume (quadratic curve blade!)
            p.setStyle(Paint.Style.FILL);
            int frondColor = t.isBurning ? 0xFF2A0F03 : (i % 2 == 0 ? 0xFF1B3808 : 0xFF2A4E0E);
            p.setColor(frondColor);

            float ctrlX1 = crownX + cosA * len * 0.45f - sinA * (11 * SCALE);
            float ctrlY1 = crownY + sinA * len * 0.35f - 8 * SCALE;
            float ctrlX2 = crownX + cosA * len * 0.55f + sinA * (7 * SCALE);
            float ctrlY2 = crownY + sinA * len * 0.45f;

            pa.reset();
            pa.moveTo(crownX, crownY);
            pa.quadTo(ctrlX1, ctrlY1, endX, endY);
            pa.quadTo(ctrlX2, ctrlY2, crownX, crownY);
            pa.close();
            c.drawPath(pa, p);

            // Frond central spine highlight
            p.setStyle(Paint.Style.STROKE);
            p.setColor(t.isBurning ? 0xFF4A1F07 : 0xFF3D6D14);
            p.setStrokeWidth(1.4f * SCALE);
            pa.reset();
            pa.moveTo(crownX, crownY);
            pa.quadTo(crownX + cosA * len * 0.5f, crownY + sinA * len * 0.28f - 6 * SCALE, endX, endY);
            c.drawPath(pa, p);
        }

        // 7. Natural Flames licking along leaf stems (Organic fire tongues!)
        if (t.isBurning) {
            p.setStyle(Paint.Style.FILL);
            float ti = timeMs * 0.009f + t.firePhase;
            int flameCount = graphicsLevel >= 2 ? 3 : 2;

            for (int f = 0; f < flameCount; f++) {
                float fx = crownX + (f - flameCount / 2.0f) * (10 * SCALE);
                float fy = crownY + 2 * SCALE;
                float fw = (7 + (float)Math.sin(ti * 1.2f + f) * 2.5f) * SCALE;
                float fh = (20 + (float)Math.sin(ti * 1.5f + f * 2) * 7) * SCALE;
                float tipOffset = (float)Math.sin(ti * 1.8f + f) * 4 * SCALE;

                p.setShader(new LinearGradient(0, fy - fh, 0, fy,
                        new int[]{0xFFFFF59D, 0xFFFF7043, 0xFFD84315}, null, Shader.TileMode.CLAMP));
                pa.reset();
                pa.moveTo(fx - fw, fy);
                pa.quadTo(fx - fw * 0.25f, fy - fh * 0.6f, fx + tipOffset, fy - fh);
                pa.quadTo(fx + fw * 0.25f, fy - fh * 0.6f, fx + fw, fy);
                pa.close();
                c.drawPath(pa, p);
                p.setShader(null);
            }

            // Rising crown smoke
            if (graphicsLevel >= 1) {
                for (int s = 0; s < 3; s++) {
                    float sx = crownX + (float)Math.sin(ti * 0.4f + s * 1.8f) * 18 * SCALE;
                    float sy = crownY - (16 + s * 24) * SCALE;
                    float sr = (12 + s * 9) * SCALE;
                    int smokeAlpha = (int)(60 - s * 15);
                    p.setColor(Color.argb(Math.max(10, smokeAlpha), 50, 22, 10));
                    c.drawCircle(sx, sy, sr, p);
                }
            }
        }

        c.restore();
    }

    private void drawEmbers(Canvas c) {
        long timeMs = System.currentTimeMillis();
        for (Ember e : embers) {
            float alpha = e.opacity * (0.6f + 0.4f * (float)Math.sin(timeMs * 0.005f * e.flicker));
            p.setColor(Color.argb((int)(alpha * 255), 255, (int)(120 + 80 * Math.sin(timeMs * 0.003f)), 20));
            c.drawCircle(e.x, e.y, e.size, p);
        }
    }

    // ========================================================
    //  PIPES (OBSTACLES) WITH 4 SELECTABLE RICH THEMES
    // ========================================================
    private void drawPipes(Canvas c) {
        float pw = 74 * SCALE, groundY = H - groundH;
        for (Pipe pp : pipes) {
            sp.setStyle(Paint.Style.FILL);
            sp.setColor(0x55000000);
            rf.set(pp.x - 4 * SCALE, groundY - 3 * SCALE, pp.x + pw + 8 * SCALE, groundY + 6 * SCALE);
            c.drawOval(rf, sp);

            drawThemedObstacle(c, pp.x, 0, pw, pp.topH, true, currentPipeSkin);
            drawThemedObstacle(c, pp.x, pp.topH + pp.gap, pw, groundY - pp.topH - pp.gap, false, currentPipeSkin);

            if (!pp.coinCollected) {
                drawFloatingSawitCoin(c, pp.x + pw / 2, pp.topH + pp.gap / 2);
            }
        }
    }

    private void drawFloatingSawitCoin(Canvas c, float cx, float cy) {
        long timeMs = System.currentTimeMillis();
        float bobY = (float)Math.sin(timeMs * 0.005f + cx * 0.025f) * (4.0f * SCALE);
        float cyBob = cy + bobY;

        float spinPhase = timeMs * 0.0042f + cx * 0.015f;
        float cosA = (float)Math.cos(spinPhase);
        float sinA = (float)Math.sin(spinPhase);
        float absCos = Math.abs(cosA);
        boolean isFront = (cosA >= 0);

        float coinR = 16.0f * SCALE;

        // 1. Soft Pulsing Golden Peat Aura
        float auraPulse = 0.86f + 0.14f * (float)Math.sin(timeMs * 0.006f + cx);
        sp.setStyle(Paint.Style.FILL);
        sp.setColor(Color.argb((int)(30 * auraPulse), 255, 179, 0));
        c.drawCircle(cx, cyBob, coinR * 1.55f, sp);
        sp.setColor(Color.argb((int)(55 * auraPulse), 255, 215, 0));
        c.drawCircle(cx, cyBob, coinR * 1.25f, sp);

        // 2. Pure Tangent Multi-Layer Cylindrical 3D Coin Edge (100% Curvature, ZERO Rect Artifacts!)
        if (absCos < 0.96f) {
            float edgeThick = 4.2f * SCALE * (1.0f - absCos);
            float edgeDir = (sinA >= 0 ? 1.0f : -1.0f);
            float backX = cx - edgeDir * edgeThick;

            int slices = 7;
            float stepX = (cx - backX) / slices;
            for (int s = 0; s < slices; s++) {
                float curX = backX + s * stepX;
                c.save();
                c.translate(curX, cyBob);
                c.scale(absCos, 1.0f);
                p.setStyle(Paint.Style.FILL);
                float tS = (float)s / slices;
                int cr = (int)(175 + 50 * tS);
                int cg = (int)(95 + 65 * tS);
                int cb = (int)(10 + 20 * tS);
                p.setColor(Color.rgb(cr, cg, cb));
                c.drawCircle(0, 0, coinR, p);
                c.restore();
            }
        }

        // 3. Minted Coin Face (GPU Transformed Ellipse)
        c.save();
        c.translate(cx, cyBob);
        c.scale(absCos, 1.0f);

        // [A] Outer Polished Gold Rim
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(-coinR, -coinR, coinR, coinR,
                new int[]{0xFFFFF9C4, 0xFFFFB300, 0xFFB26A00}, null, Shader.TileMode.CLAMP));
        c.drawCircle(0, 0, coinR, p);
        p.setShader(null);

        // Specular Rim Bevel
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFFFFF);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawCircle(0, 0, coinR - 0.7f * SCALE, p);

        // [B] Inner Recessed Gold Field
        float innerR = coinR * 0.78f;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, -innerR, 0, innerR,
                new int[]{0xFFFFD54F, 0xFFFF9800, 0xFFE65100}, null, Shader.TileMode.CLAMP));
        c.drawCircle(0, 0, innerR, p);
        p.setShader(null);

        // Elegant Concentric Coin Groove (Clean & Sharp, No Moire Noise)
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x88FFE082);
        p.setStrokeWidth(0.9f * SCALE);
        c.drawCircle(0, 0, innerR * 0.90f, p);

        // [C] Embossed Theme Motif (Front: Royal Palm Tree / Back: Golden CPO Droplet)
        if (absCos > 0.12f) {
            if (isFront) {
                // --- FRONT SIDE: POHON KELAPA SAWIT EMAS ---
                float trunkTop = -innerR * 0.05f;
                float trunkBottom = innerR * 0.62f;

                // 1. Palm Trunk Relief & Face
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFF6D3300); // Dark relief shadow
                pa.reset();
                pa.moveTo(-2.4f * SCALE, trunkBottom + 0.8f * SCALE);
                pa.lineTo(-1.6f * SCALE, trunkTop + 0.8f * SCALE);
                pa.lineTo(1.6f * SCALE, trunkTop + 0.8f * SCALE);
                pa.lineTo(2.4f * SCALE, trunkBottom + 0.8f * SCALE);
                pa.close();
                c.drawPath(pa, p);

                p.setColor(0xFFFFFDE7); // Polished gold trunk face
                pa.reset();
                pa.moveTo(-2.4f * SCALE, trunkBottom);
                pa.lineTo(-1.6f * SCALE, trunkTop);
                pa.lineTo(1.6f * SCALE, trunkTop);
                pa.lineTo(2.4f * SCALE, trunkBottom);
                pa.close();
                c.drawPath(pa, p);

                // Trunk ring segments
                p.setStyle(Paint.Style.STROKE);
                p.setColor(0xFF8D5200);
                p.setStrokeWidth(0.8f * SCALE);
                c.drawLine(-2.0f * SCALE, trunkBottom * 0.4f, 2.0f * SCALE, trunkBottom * 0.4f, p);
                c.drawLine(-2.2f * SCALE, trunkBottom * 0.7f, 2.2f * SCALE, trunkBottom * 0.7f, p);

                // 2. Arching Royal Palm Fronds (5 High-Contrast Elegant Fronds)
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);

                float[][] fronds = {
                    {0, -innerR * 0.65f, 0, -innerR * 0.35f},                      // Center spear
                    {-innerR * 0.45f, -innerR * 0.45f, -innerR * 0.22f, -innerR * 0.42f}, // Top Left
                    {innerR * 0.45f, -innerR * 0.45f, innerR * 0.22f, -innerR * 0.42f},   // Top Right
                    {-innerR * 0.50f, -innerR * 0.08f, -innerR * 0.28f, -innerR * 0.20f}, // Low Left
                    {innerR * 0.50f, -innerR * 0.08f, innerR * 0.28f, -innerR * 0.20f}    // Low Right
                };

                // Shadow
                p.setStrokeWidth(2.2f * SCALE);
                p.setColor(0xFF5D2800);
                for (float[] f : fronds) {
                    pa.reset();
                    pa.moveTo(0, trunkTop + 0.8f * SCALE);
                    pa.quadTo(f[2], f[3] + 0.8f * SCALE, f[0], f[1] + 0.8f * SCALE);
                    c.drawPath(pa, p);
                }

                // Face Highlight
                p.setStrokeWidth(1.6f * SCALE);
                p.setColor(0xFFFFFDE7);
                for (float[] f : fronds) {
                    pa.reset();
                    pa.moveTo(0, trunkTop);
                    pa.quadTo(f[2], f[3], f[0], f[1]);
                    c.drawPath(pa, p);
                }
                p.setStrokeCap(Paint.Cap.BUTT);

                // 3. Tandan Buah Sawit (TBS Ripe Clusters)
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFFBF360C); // Deep rust shadow
                c.drawCircle(-2.2f * SCALE, trunkTop + 1.2f * SCALE, 1.8f * SCALE, p);
                c.drawCircle(2.2f * SCALE, trunkTop + 1.2f * SCALE, 1.8f * SCALE, p);

                p.setColor(0xFFFF9100); // Ripe golden oil fruit
                c.drawCircle(-2.2f * SCALE, trunkTop + 0.6f * SCALE, 1.5f * SCALE, p);
                c.drawCircle(2.2f * SCALE, trunkTop + 0.6f * SCALE, 1.5f * SCALE, p);

                p.setColor(0xFFFFF9C4); // Fruit glint
                c.drawCircle(-2.5f * SCALE, trunkTop + 0.2f * SCALE, 0.6f * SCALE, p);
                c.drawCircle(1.9f * SCALE, trunkTop + 0.2f * SCALE, 0.6f * SCALE, p);
            } else {
                // --- BACK SIDE: TETESAN EMAS CPO (Pure Golden Oil Tear) ---
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFF6D3300); // Tear shadow
                pa.reset();
                pa.moveTo(0, -innerR * 0.50f + 0.8f * SCALE);
                pa.quadTo(-innerR * 0.35f, innerR * 0.15f + 0.8f * SCALE, 0, innerR * 0.50f + 0.8f * SCALE);
                pa.quadTo(innerR * 0.35f, innerR * 0.15f + 0.8f * SCALE, 0, -innerR * 0.50f + 0.8f * SCALE);
                pa.close();
                c.drawPath(pa, p);

                p.setColor(0xFFFFFDE7); // Outer tear highlight
                pa.reset();
                pa.moveTo(0, -innerR * 0.50f);
                pa.quadTo(-innerR * 0.35f, innerR * 0.15f, 0, innerR * 0.50f);
                pa.quadTo(innerR * 0.35f, innerR * 0.15f, 0, -innerR * 0.50f);
                pa.close();
                c.drawPath(pa, p);

                // Inner Amber Glowing Core
                p.setColor(0xFFFF9800);
                pa.reset();
                pa.moveTo(0, -innerR * 0.35f);
                pa.quadTo(-innerR * 0.22f, innerR * 0.12f, 0, innerR * 0.38f);
                pa.quadTo(innerR * 0.22f, innerR * 0.12f, 0, -innerR * 0.35f);
                pa.close();
                c.drawPath(pa, p);

                // Central Radiant Star (✦) instead of broken tiny text!
                p.setColor(0xFFFFFDE7);
                float starR = innerR * 0.18f;
                pa.reset();
                pa.moveTo(0, -starR);
                pa.quadTo(0, 0, starR, 0);
                pa.quadTo(0, 0, 0, starR);
                pa.quadTo(0, 0, -starR, 0);
                pa.quadTo(0, 0, 0, -starR);
                pa.close();
                c.drawPath(pa, p);

                // Laurel Palm Arcs
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(1.4f * SCALE);
                p.setColor(0xFFFFF9C4);
                p.setStrokeCap(Paint.Cap.ROUND);
                pa.reset();
                pa.moveTo(-innerR * 0.48f, innerR * 0.35f);
                pa.quadTo(-innerR * 0.58f, 0, -innerR * 0.35f, -innerR * 0.38f);
                c.drawPath(pa, p);
                pa.reset();
                pa.moveTo(innerR * 0.48f, innerR * 0.35f);
                pa.quadTo(innerR * 0.58f, 0, innerR * 0.35f, -innerR * 0.38f);
                c.drawPath(pa, p);
                p.setStrokeCap(Paint.Cap.BUTT);
            }
        }

        // [D] Metallic Crescent Specular Gleam Arc
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.8f * SCALE);
        p.setColor(0x60FFFFFF);
        rf.set(-innerR * 0.88f, -innerR * 0.88f, innerR * 0.88f, innerR * 0.88f);
        c.drawArc(rf, 200, 75, false, p);

        c.restore();

        // 4. Diamond Twinkle (✦) on Specular Corner
        if (absCos > 0.80f) {
            float glintProgress = (absCos - 0.80f) / 0.20f;
            float glintX = cx + coinR * 0.52f * absCos;
            float glintY = cyBob - coinR * 0.55f;
            float gLen = (6.5f * SCALE) * glintProgress;
            float gThick = (1.8f * SCALE) * glintProgress;

            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFFFD54F);
            pa.reset();
            pa.moveTo(glintX - gLen, glintY);
            pa.lineTo(glintX, glintY - gThick);
            pa.lineTo(glintX + gLen, glintY);
            pa.lineTo(glintX, glintY + gThick);
            pa.close();
            c.drawPath(pa, p);

            pa.reset();
            pa.moveTo(glintX, glintY - gLen);
            pa.lineTo(glintX + gThick, glintY);
            pa.lineTo(glintX, glintY + gLen);
            pa.lineTo(glintX - gThick, glintY);
            pa.close();
            c.drawPath(pa, p);

            p.setColor(0xFFFFFFFF);
            c.drawCircle(glintX, glintY, 1.3f * SCALE * glintProgress, p);
        }
    }

    private void drawMiniVectorCoin(Canvas c, float cx, float cy, float r) {
        // Outer Polished Gold Rim
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(cx - r, cy - r, cx + r, cy + r,
                new int[]{0xFFFFF9C4, 0xFFFFB300, 0xFFB26A00}, null, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, p);
        p.setShader(null);

        // Specular Rim Highlight
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFFFFF);
        p.setStrokeWidth(0.8f * SCALE);
        c.drawCircle(cx, cy, r - 0.5f * SCALE, p);

        // Recessed Inner Gold Field
        float inR = r * 0.74f;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFC107);
        c.drawCircle(cx, cy, inR, p);

        // Vector Palm Tree Silhouette (Clean & Sharp at mini scale)
        float trunkTop = cy - inR * 0.05f;
        float trunkBottom = cy + inR * 0.58f;

        // Trunk
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.2f * SCALE);
        p.setColor(0xFF7A3900);
        c.drawLine(cx, trunkTop, cx, trunkBottom, p);

        // Palm Fronds (5 sharp arched fronds)
        p.setStrokeWidth(1.0f * SCALE);
        p.setColor(0xFFFFFDE7);
        p.setStrokeCap(Paint.Cap.ROUND);
        // Center spear
        c.drawLine(cx, trunkTop, cx, cy - inR * 0.65f, p);
        // Left & right fronds
        c.drawLine(cx, trunkTop, cx - inR * 0.50f, cy - inR * 0.35f, p);
        c.drawLine(cx, trunkTop, cx + inR * 0.50f, cy - inR * 0.35f, p);
        c.drawLine(cx, trunkTop, cx - inR * 0.55f, cy + inR * 0.05f, p);
        c.drawLine(cx, trunkTop, cx + inR * 0.55f, cy + inR * 0.05f, p);
        p.setStrokeCap(Paint.Cap.BUTT);

        // Tandan Buah Segar (TBS mini orange dot)
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFF3D00);
        c.drawCircle(cx, trunkTop + 0.8f * SCALE, 1.0f * SCALE, p);

        // Top Specular Crescent Arc
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.8f * SCALE);
        p.setColor(0x60FFFFFF);
        rf.set(cx - inR * 0.85f, cy - inR * 0.85f, cx + inR * 0.85f, cy + inR * 0.85f);
        c.drawArc(rf, 210, 60, false, p);
    }

    private void drawThemedObstacle(Canvas c, float x, float y, float w, float h, boolean isTop, int skinId) {
        if (h <= 0) return;
        long timeMs = System.currentTimeMillis();

        switch (skinId) {
            case 1:
                drawObstaclePks(c, x, y, w, h, isTop, timeMs);
                break;
            case 2:
                drawObstaclePylon(c, x, y, w, h, isTop, timeMs);
                break;
            case 3:
                drawObstacleMagma(c, x, y, w, h, isTop, timeMs);
                break;
            case 4:
                drawObstacleKoruptor(c, x, y, w, h, isTop, timeMs);
                break;
            case 5:
                drawObstacleCyber(c, x, y, w, h, isTop, timeMs);
                break;
            case 6:
                drawObstacleDrumCpo(c, x, y, w, h, isTop, timeMs);
                break;
            case 7:
                drawObstacleExcavator(c, x, y, w, h, isTop, timeMs);
                break;
            case 8:
                drawObstacleBrankas(c, x, y, w, h, isTop, timeMs);
                break;
            case 9:
                drawObstaclePenjara(c, x, y, w, h, isTop, timeMs);
                break;
            case 10:
                drawObstaclePohonHayat(c, x, y, w, h, isTop, timeMs);
                break;
            case 0:
            default:
                drawObstacleSawit(c, x, y, w, h, isTop, timeMs);
                break;
        }
    }

    // [THEME 0] Sawit Membara
    private void drawObstacleSawit(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFF240E05, 0xFF4A1F0B, 0xFF2B1006, 0xFF140602},
                new float[]{0, 0.35f, 0.75f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xC00E0502);
        p.setStrokeWidth(2.2f * SCALE);
        float scarStep = 22 * SCALE;
        for (float py = (float)Math.floor(y / scarStep) * scarStep; py < y + h; py += scarStep) {
            if (py < y) continue;
            pa.reset();
            pa.moveTo(x + 2, py);
            pa.quadTo(x + w * 0.5f, py + (isTop ? -7 : 7) * SCALE, x + w - 2, py);
            c.drawPath(pa, p);
        }

        float pulse = 0.7f + 0.3f * (float)Math.sin(timeMs * 0.005f);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f * SCALE);
        p.setColor(Color.argb((int)(180 * pulse), 255, 90, 0));
        pa.reset();
        pa.moveTo(x + w * 0.35f, y);
        pa.lineTo(x + w * 0.45f, y + h * 0.4f);
        pa.lineTo(x + w * 0.3f, y + h * 0.7f);
        pa.lineTo(x + w * 0.4f, y + h);
        c.drawPath(pa, p);

        float capH = 24 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x - 6 * SCALE, 0, x + w + 6 * SCALE, 0,
                new int[]{0xFF240D04, 0xFF521E08, 0xFF1B0702}, null, Shader.TileMode.CLAMP));
        rf.set(x - 6 * SCALE, capY, x + w + 6 * SCALE, capY + capH);
        c.drawRoundRect(rf, 6 * SCALE, 6 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFF6D00);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 6 * SCALE, 6 * SCALE, p);

        // Flame tongues at tip
        float flameBaseY = isTop ? (y + h) : y;
        float flameDir = isTop ? -1 : 1;
        float ft = timeMs * 0.009f;
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 5; i++) {
            float fx = x + 8 * SCALE + i * (w / 4.8f);
            float wave = (float)Math.sin(ft + i * 1.7f) * 3 * SCALE;
            float fH = (16 + (float)Math.sin(ft * 1.4f + i * 2) * 6) * SCALE * flameDir;
            float fr = 9 * SCALE;
            p.setShader(new RadialGradient(fx + wave, flameBaseY + fH * 0.5f, fr,
                    new int[]{0xEEFFFFDC, 0xCCFFAA00, 0x88E62800, 0x00960000}, null, Shader.TileMode.CLAMP));
            c.drawCircle(fx + wave, flameBaseY + fH * 0.5f, fr, p);
            p.setShader(null);
        }
    }

    // [THEME 1] Cerobong PKS
    private void drawObstaclePks(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFF1C1F22, 0xFF474C54, 0xFF2D3036, 0xFF121416},
                new float[]{0, 0.35f, 0.75f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        float bandStep = 45 * SCALE;
        p.setStyle(Paint.Style.FILL);
        for (float py = (float)Math.floor(y / bandStep) * bandStep; py < y + h; py += bandStep) {
            if (py < y) continue;
            p.setColor(0xFF262A30);
            c.drawRect(x, py, x + w, py + 7 * SCALE, p);
            p.setColor(0xFF6B7280);
            c.drawLine(x, py, x + w, py, p);
            p.setColor(0xFFB0BEC5);
            for (int r = 0; r < 4; r++) {
                float rx = x + 10 * SCALE + r * (w / 4.2f);
                c.drawCircle(rx, py + 3.5f * SCALE, 1.8f * SCALE, p);
            }
        }

        float capH = 26 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        rf.set(x - 7 * SCALE, capY, x + w + 7 * SCALE, capY + capH);
        p.setColor(0xFF181A1D);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        c.save();
        c.clipRect(rf);
        p.setColor(0xFFFFD600);
        p.setStrokeWidth(9 * SCALE);
        for (float sx = x - 25 * SCALE; sx < x + w + 35 * SCALE; sx += 18 * SCALE) {
            c.drawLine(sx, capY - 4 * SCALE, sx + 22 * SCALE, capY + capH + 4 * SCALE, p);
        }
        c.restore();

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFAB00);
        p.setStrokeWidth(1.6f * SCALE);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        float beaconPulse = 0.5f + 0.5f * (float)Math.sin(timeMs * 0.012f);
        float beaconX = isTop ? (x - 3 * SCALE) : (x + w + 3 * SCALE);
        float beaconY = isTop ? (capY + capH) : capY;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF263238);
        c.drawCircle(beaconX, beaconY, 4 * SCALE, p);
        int beaconAlpha = (int)(255 * beaconPulse);
        p.setShader(new RadialGradient(beaconX, beaconY, 12 * SCALE,
                new int[]{Color.argb(beaconAlpha, 255, 23, 68), Color.argb(beaconAlpha / 2, 255, 0, 0), 0x00000000},
                null, Shader.TileMode.CLAMP));
        c.drawCircle(beaconX, beaconY, 12 * SCALE, p);
        p.setShader(null);
        p.setColor(Color.argb(beaconAlpha, 255, 200, 200));
        c.drawCircle(beaconX, beaconY, 2.2f * SCALE, p);

        float smokeBaseY = isTop ? (y + h) : y;
        float smokeDir = isTop ? -1 : 1;
        p.setStyle(Paint.Style.FILL);
        for (int s = 0; s < 3; s++) {
            float st = timeMs * 0.005f + s * 1.6f;
            float sx = x + 12 * SCALE + s * (w / 3.0f) + (float)Math.sin(st) * 3 * SCALE;
            float sy = smokeBaseY + (float)Math.cos(st) * 5 * SCALE - 5 * SCALE * smokeDir;
            float sr = (10 + (float)Math.sin(st * 1.3f) * 3) * SCALE;
            p.setShader(new RadialGradient(sx, sy, sr,
                    new int[]{0xDD2A1C16, 0x883E2723, 0x00000000}, null, Shader.TileMode.CLAMP));
            c.drawCircle(sx, sy, sr, p);
            p.setShader(null);
        }
    }

    // [THEME 2] Tower Listrik Gambut
    private void drawObstaclePylon(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFF212529, 0xFF3E444A, 0xFF1C2024}, null, Shader.TileMode.CLAMP));
        c.drawRect(x + 4 * SCALE, y, x + 14 * SCALE, y + h, p);
        c.drawRect(x + w - 14 * SCALE, y, x + w - 4 * SCALE, y + h, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF5A626A);
        p.setStrokeWidth(2.8f * SCALE);
        float trussStep = 30 * SCALE;
        for (float py = (float)Math.floor(y / trussStep) * trussStep; py < y + h; py += trussStep) {
            if (py < y) continue;
            c.drawLine(x + 9 * SCALE, py, x + w - 9 * SCALE, py + trussStep, p);
            c.drawLine(x + w - 9 * SCALE, py, x + 9 * SCALE, py + trussStep, p);
            c.drawLine(x + 9 * SCALE, py, x + w - 9 * SCALE, py, p);
        }

        p.setColor(0x35FF5722);
        p.setStyle(Paint.Style.FILL);
        c.drawRect(x + 14 * SCALE, y, x + w - 14 * SCALE, y + h, p);

        float capH = 24 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF2C343D);
        rf.set(x - 7 * SCALE, capY, x + w + 7 * SCALE, capY + capH);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF78909C);
        p.setStrokeWidth(1.6f * SCALE);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        p.setStyle(Paint.Style.FILL);
        for (int ins = 0; ins < 3; ins++) {
            float ix = x + 12 * SCALE + ins * (w / 2.8f);
            for (int d = 0; d < 3; d++) {
                float iy = capY + 4 * SCALE + d * 5.5f * SCALE;
                p.setColor(d % 2 == 0 ? 0xFFCFD8DC : 0xFF90A4AE);
                rf.set(ix - 4.5f * SCALE, iy, ix + 4.5f * SCALE, iy + 3.5f * SCALE);
                c.drawRoundRect(rf, 2 * SCALE, 2 * SCALE, p);
            }
        }

        float tipY = isTop ? (y + h) : y;
        float dir = isTop ? 1 : -1;
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF263238);
        p.setStrokeWidth(2.2f * SCALE);
        pa.reset();
        pa.moveTo(x + 12 * SCALE, tipY);
        pa.quadTo(x + 18 * SCALE, tipY + 14 * SCALE * dir, x + 24 * SCALE, tipY + 20 * SCALE * dir);
        c.drawPath(pa, p);

        pa.reset();
        pa.moveTo(x + w - 12 * SCALE, tipY);
        pa.quadTo(x + w - 18 * SCALE, tipY + 14 * SCALE * dir, x + w - 24 * SCALE, tipY + 20 * SCALE * dir);
        c.drawPath(pa, p);

        boolean flashArc = (timeMs % 400) < 180;
        if (flashArc) {
            float arcX1 = x + 24 * SCALE, arcY1 = tipY + 20 * SCALE * dir;
            float arcX2 = x + w - 24 * SCALE, arcY2 = tipY + 18 * SCALE * dir;
            float midX = (arcX1 + arcX2) / 2 + (float)Math.sin(timeMs * 0.05f) * 6 * SCALE;
            float midY = (arcY1 + arcY2) / 2 + (float)Math.cos(timeMs * 0.05f) * 5 * SCALE;

            p.setColor(0x8800E5FF);
            p.setStrokeWidth(4.5f * SCALE);
            pa.reset();
            pa.moveTo(arcX1, arcY1);
            pa.lineTo(midX, midY);
            pa.lineTo(arcX2, arcY2);
            c.drawPath(pa, p);

            p.setColor(0xFFFFFFFF);
            p.setStrokeWidth(1.8f * SCALE);
            c.drawPath(pa, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF00E5FF);
            c.drawCircle(arcX1, arcY1, 3.5f * SCALE, p);
            c.drawCircle(arcX2, arcY2, 3.5f * SCALE, p);
        }
    }

    // [THEME 3] Pilar Magma Obsidian
    private void drawObstacleMagma(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFF0E0713, 0xFF221128, 0xFF140A18, 0xFF08030B},
                new float[]{0, 0.4f, 0.75f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF381F40);
        p.setStrokeWidth(1.8f * SCALE);
        c.drawLine(x + w * 0.3f, y, x + w * 0.3f, y + h, p);
        c.drawLine(x + w * 0.7f, y, x + w * 0.7f, y + h, p);

        float lavaPulse = 0.65f + 0.35f * (float)Math.sin(timeMs * 0.005f);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);

        p.setColor(Color.argb((int)(120 * lavaPulse), 255, 61, 0));
        p.setStrokeWidth(7 * SCALE);
        pa.reset();
        pa.moveTo(x + w * 0.5f, y);
        pa.lineTo(x + w * 0.42f, y + h * 0.25f);
        pa.lineTo(x + w * 0.58f, y + h * 0.55f);
        pa.lineTo(x + w * 0.46f, y + h * 0.8f);
        pa.lineTo(x + w * 0.52f, y + h);
        c.drawPath(pa, p);

        p.setColor(Color.argb((int)(240 * lavaPulse), 255, 234, 0));
        p.setStrokeWidth(2.5f * SCALE);
        c.drawPath(pa, p);

        float capH = 26 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        float dir = isTop ? 1 : -1;
        float tipY = isTop ? (y + h) : y;

        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x - 7 * SCALE, 0, x + w + 7 * SCALE, 0,
                new int[]{0xFF1A0A21, 0xFF351740, 0xFF120516}, null, Shader.TileMode.CLAMP));
        rf.set(x - 7 * SCALE, capY, x + w + 7 * SCALE, capY + capH);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFF5252);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1F0D29);
        for (int cr = 0; cr < 5; cr++) {
            float cx = x + 4 * SCALE + cr * (w / 4.2f);
            pa.reset();
            pa.moveTo(cx - 4.5f * SCALE, tipY);
            pa.lineTo(cx, tipY + 10 * SCALE * dir);
            pa.lineTo(cx + 4.5f * SCALE, tipY);
            pa.close();
            c.drawPath(pa, p);
        }

        float dripT = (timeMs * 0.003f) % 1.0f;
        float dripY = tipY + (dripT * 22 * SCALE) * dir;
        p.setColor(0xFFFF6D00);
        c.drawCircle(x + w * 0.5f, dripY, 3 * (1 - dripT * 0.5f) * SCALE, p);
        p.setColor(0xFFFFFF8D);
        c.drawCircle(x + w * 0.5f, dripY, 1.5f * (1 - dripT * 0.5f) * SCALE, p);
    }

    // [THEME 4] Pilar Koruptor (Aset Disita KPK)
    private void drawObstacleKoruptor(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        // Luxury gold & white fluted neoclassical marble column
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFFD4AF37, 0xFFFFF8E1, 0xFFFFE082, 0xFFB8860B},
                new float[]{0, 0.35f, 0.70f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        // Fluted vertical ridges on marble
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x35000000);
        p.setStrokeWidth(1.2f * SCALE);
        for (int i = 1; i <= 5; i++) {
            float fx = x + i * (w / 6f);
            c.drawLine(fx, y, fx, y + h, p);
        }

        // Red & White Police Tape wrapped diagonally ("DISITA KPK")
        float tapeStep = 55 * SCALE;
        p.setStyle(Paint.Style.FILL);
        for (float py = (float)Math.floor(y / tapeStep) * tapeStep; py < y + h; py += tapeStep) {
            if (py + 16 * SCALE < y) continue;
            // Red tape band
            p.setColor(0xFFD50000);
            pa.reset();
            pa.moveTo(x, py);
            pa.lineTo(x + w, py + 18 * SCALE);
            pa.lineTo(x + w, py + 28 * SCALE);
            pa.lineTo(x, py + 10 * SCALE);
            pa.close();
            c.drawPath(pa, p);

            // White text label "DISITA KPK"
            tp.setTypeface(tfBlack);
            tp.setColor(Color.WHITE);
            tp.setTextSize(7.5f * SCALE);
            tp.setTextAlign(Paint.Align.CENTER);
            c.save();
            c.rotate(10, x + w / 2, py + 18 * SCALE);
            c.drawText("DISITA KPK", x + w / 2, py + 21 * SCALE, tp);
            c.restore();
        }

        // Luxurious Golden Corinthian Capital Cap
        float capH = 26 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x - 6 * SCALE, 0, x + w + 6 * SCALE, 0,
                new int[]{0xFFFFD54F, 0xFFFFFDE7, 0xFFFFB300, 0xFF8D5B00}, null, Shader.TileMode.CLAMP));
        rf.set(x - 6 * SCALE, capY, x + w + 6 * SCALE, capY + capH);
        c.drawRoundRect(rf, 6 * SCALE, 6 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFF6F00);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 6 * SCALE, 6 * SCALE, p);
    }

    // [THEME 5] Reaktor Cyber (Matrix Zona Hijau)
    private void drawObstacleCyber(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        // Dark metallic cyber obsidian conduit
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFF05131E, 0xFF0B253A, 0xFF071927, 0xFF02090F},
                new float[]{0, 0.35f, 0.70f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        // Glowing neon circuits
        float neonPulse = 0.7f + 0.3f * (float)Math.sin(timeMs * 0.006f);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.argb((int)(220 * neonPulse), 0, 229, 255));
        p.setStrokeWidth(1.5f * SCALE);

        float cx1 = x + w * 0.3f;
        float cx2 = x + w * 0.7f;
        c.drawLine(cx1, y, cx1, y + h, p);
        c.drawLine(cx2, y, cx2, y + h, p);

        // Moving data packets along circuits
        float packetY1 = y + ((timeMs * 0.06f) % Math.max(1, h));
        float packetY2 = y + h - ((timeMs * 0.07f) % Math.max(1, h));
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFE040FB);
        c.drawCircle(cx1, packetY1, 2.5f * SCALE, p);
        p.setColor(0xFF00E5FF);
        c.drawCircle(cx2, packetY2, 2.5f * SCALE, p);

        // Neon Plasma Ring Cap
        float capH = 24 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x - 5 * SCALE, 0, x + w + 5 * SCALE, 0,
                new int[]{0xFF0091EA, 0xFF00E5FF, 0xFF0064B7}, null, Shader.TileMode.CLAMP));
        rf.set(x - 5 * SCALE, capY, x + w + 5 * SCALE, capY + capH);
        c.drawRoundRect(rf, 5 * SCALE, 5 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF80D8FF);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 5 * SCALE, 5 * SCALE, p);
    }

    // [THEME 6] Tumpukan Drum CPO Ilegal
    private void drawObstacleDrumCpo(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        // Red-orange industrial oil drum steel
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFF7F0000, 0xFFD84315, 0xFF870000, 0xFF3E0000},
                new float[]{0, 0.35f, 0.70f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        // Black metal reinforcement barrel hoops
        float hoopStep = 38 * SCALE;
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF212121);
        p.setStrokeWidth(2.5f * SCALE);
        for (float hy = (float)Math.floor(y / hoopStep) * hoopStep; hy < y + h; hy += hoopStep) {
            if (hy < y) continue;
            c.drawLine(x, hy, x + w, hy, p);
            // Golden oil seepage drip
            p.setColor(0xFFFFB300);
            p.setStrokeWidth(1.2f * SCALE);
            c.drawLine(x + w * 0.4f, hy, x + w * 0.4f, hy + 8 * SCALE, p);
            p.setColor(0xFF212121);
            p.setStrokeWidth(2.5f * SCALE);
        }

        // Barrel Lip Cap
        float capH = 24 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x - 5 * SCALE, 0, x + w + 5 * SCALE, 0,
                new int[]{0xFF4A1000, 0xFFBF360C, 0xFF330800}, null, Shader.TileMode.CLAMP));
        rf.set(x - 5 * SCALE, capY, x + w + 5 * SCALE, capY + capH);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFB300);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        // Stencil "CPO" text on cap with auto-fit
        tp.setTypeface(tfBlack);
        tp.setColor(0xFFFFD54F);
        float cpoSize = 7.5f * SCALE;
        tp.setTextSize(cpoSize);
        while (tp.measureText("CPO ILEGAL") > (w + 4 * SCALE) && cpoSize > 5.0f * SCALE) {
            cpoSize -= 0.5f * SCALE;
            tp.setTextSize(cpoSize);
        }
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText("CPO ILEGAL", x + w / 2, capY + capH * 0.65f, tp);
    }

    // [THEME 7] Ekskavator HGU (Alat Berat Pembalak)
    private void drawObstacleExcavator(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        // Caterpillar safety yellow steel beam
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFFE65100, 0xFFFFB300, 0xFFFFA000, 0xFFBF360C},
                new float[]{0, 0.35f, 0.70f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        // Chrome hydraulic piston rod in center
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFECEFF1);
        rf.set(x + w * 0.35f, y, x + w * 0.65f, y + h);
        c.drawRect(rf, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF90A4AE);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRect(rf, p);

        // Black warning hazard chevrons (///)
        p.setColor(0xDD000000);
        p.setStrokeWidth(3.0f * SCALE);
        float hstep = 26 * SCALE;
        for (float hy = (float)Math.floor(y / hstep) * hstep; hy < y + h; hy += hstep) {
            if (hy < y) continue;
            c.drawLine(x + 2, hy, x + w * 0.3f, hy + 10 * SCALE, p);
            c.drawLine(x + w * 0.7f, hy, x + w - 2, hy + 10 * SCALE, p);
        }

        // Heavy Excavator Bucket Lip with Sharp Teeth
        float capH = 24 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF37474F); // Dark forged steel
        rf.set(x - 6 * SCALE, capY, x + w + 6 * SCALE, capY + capH);
        c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFD54F);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);

        // Triangular bucket teeth protruding at opening
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFCFD8DC);
        float toothBaseY = isTop ? (capY + capH) : capY;
        float toothTipY = isTop ? (toothBaseY + 7 * SCALE) : (toothBaseY - 7 * SCALE);
        float toothW = (w + 12 * SCALE) / 4.0f;
        for (int i = 0; i < 4; i++) {
            float tx = (x - 6 * SCALE) + i * toothW;
            pa.reset();
            pa.moveTo(tx + 1 * SCALE, toothBaseY);
            pa.lineTo(tx + toothW / 2, toothTipY);
            pa.lineTo(tx + toothW - 1 * SCALE, toothBaseY);
            pa.close();
            c.drawPath(pa, p);
        }
    }

    // [THEME 8] Brankas Suap (Bunker Gratifikasi Pejabat)
    private void drawObstacleBrankas(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        // Heavy titanium vault steel
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFF263238, 0xFF455A64, 0xFF37474F, 0xFF102027},
                new float[]{0, 0.30f, 0.70f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        // Security inspection glass slot showing stacked banknotes
        float slotStep = 45 * SCALE;
        for (float sy = (float)Math.floor(y / slotStep) * slotStep; sy < y + h - 20 * SCALE; sy += slotStep) {
            if (sy < y) continue;
            // Glass window
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xAA000000);
            rf.set(x + w * 0.2f, sy + 4 * SCALE, x + w * 0.8f, sy + 22 * SCALE);
            c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);

            // Stacked green cash inside
            p.setColor(0xFF2E7D32);
            rf.set(x + w * 0.25f, sy + 8 * SCALE, x + w * 0.75f, sy + 18 * SCALE);
            c.drawRect(rf, p);
            tp.setTypeface(tfBlack);
            tp.setColor(0xFF81C784);
            tp.setTextSize(7.5f * SCALE);
            tp.setTextAlign(Paint.Align.CENTER);
            c.drawText("$$$$", x + w / 2, sy + 16 * SCALE, tp);

            // Steel bolts around window
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFCFD8DC);
            c.drawCircle(x + w * 0.12f, sy + 13 * SCALE, 1.4f * SCALE, p);
            c.drawCircle(x + w * 0.88f, sy + 13 * SCALE, 1.4f * SCALE, p);
        }

        // Vault Lip Cap with Keypad LED
        float capH = 24 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF102027);
        rf.set(x - 5 * SCALE, capY, x + w + 5 * SCALE, capY + capH);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF90A4AE);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        // Glowing green digital keypad dot
        float pulse = (float)Math.sin(timeMs * 0.008f);
        p.setStyle(Paint.Style.FILL);
        p.setColor(pulse > 0 ? 0xFF00E676 : 0xFF00B0FF);
        c.drawCircle(x + 5 * SCALE, capY + capH / 2, 2.5f * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(0xFFFFD54F);
        float tipikorSize = 7.0f * SCALE;
        tp.setTextSize(tipikorSize);
        while (tp.measureText("TIPIKOR") > (w - 6 * SCALE) && tipikorSize > 4.5f * SCALE) {
            tipikorSize -= 0.5f * SCALE;
            tp.setTextSize(tipikorSize);
        }
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText("TIPIKOR", x + w / 2 + 2.5f * SCALE, capY + capH * 0.65f, tp);
    }

    // [THEME 9] Sel Terali Tipikor (Penjara Khusus Koruptor)
    private void drawObstaclePenjara(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        // Cold dark prison background behind bars
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1A1A1A);
        c.drawRect(x, y, x + w, y + h, p);

        // Vertical cold iron prison bars
        p.setStyle(Paint.Style.FILL);
        float barSpacing = w / 4.0f;
        for (int i = 0; i < 4; i++) {
            float bx = x + i * barSpacing + barSpacing / 2 - 2.5f * SCALE;
            p.setShader(new LinearGradient(bx, 0, bx + 5 * SCALE, 0,
                    new int[]{0xFF424242, 0xFF9E9E9E, 0xFF212121}, null, Shader.TileMode.CLAMP));
            c.drawRect(bx, y, bx + 5 * SCALE, y + h, p);
        }
        p.setShader(null);

        // Horizontal steel crossbars
        float crossStep = 45 * SCALE;
        p.setColor(0xFF303030);
        for (float cy = (float)Math.floor(y / crossStep) * crossStep; cy < y + h; cy += crossStep) {
            if (cy < y) continue;
            c.drawRect(x, cy, x + w, cy + 6 * SCALE, p);
        }

        // Heavy padlock chained to center bar
        float lockY = isTop ? (y + h - 38 * SCALE) : (y + 14 * SCALE);
        p.setColor(0xFF795548); // Rusted brass/iron padlock
        rf.set(x + w * 0.35f, lockY, x + w * 0.65f, lockY + 16 * SCALE);
        c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFBCAAA4);
        p.setStrokeWidth(1.8f * SCALE);
        c.drawArc(rf, 180, 180, false, p);

        // Prison Beam Cap with Emergency Strobe
        float capH = 24 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF212121);
        rf.set(x - 5 * SCALE, capY, x + w + 5 * SCALE, capY + capH);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF757575);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 4 * SCALE, 4 * SCALE, p);

        // Blinking Red Emergency Beacon
        float beaconAlpha = 0.5f + 0.5f * (float)Math.sin(timeMs * 0.010f);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb((int)(255 * beaconAlpha), 255, 23, 68));
        c.drawCircle(x + 6 * SCALE, capY + capH / 2, 3.0f * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(0xFFEEEEEE);
        float sukamiskinSize = 6.8f * SCALE;
        tp.setTextSize(sukamiskinSize);
        while (tp.measureText("SUKAMISKIN") > (w - 4 * SCALE) && sukamiskinSize > 4.5f * SCALE) {
            sukamiskinSize -= 0.5f * SCALE;
            tp.setTextSize(sukamiskinSize);
        }
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText("SUKAMISKIN", x + w / 2 + 2.5f * SCALE, capY + capH * 0.65f, tp);
    }

    // [THEME 10] Pohon Hayat Rimba (Monolit Ulin Kalimantan)
    private void drawObstaclePohonHayat(Canvas c, float x, float y, float w, float h, boolean isTop, long timeMs) {
        // Ancient deep bronze Ironwood (Kayu Ulin)
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x, 0, x + w, 0,
                new int[]{0xFF1F1206, 0xFF3E2714, 0xFF2A1708, 0xFF140A03},
                new float[]{0, 0.35f, 0.70f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(x, y, x + w, y + h, p);
        p.setShader(null);

        // Glowing Emerald Dayak Sacred Spiral Carvings & Bioluminescent Moss
        float mossPulse = 0.75f + 0.25f * (float)Math.sin(timeMs * 0.005f);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.argb((int)(200 * mossPulse), 0, 230, 118));
        p.setStrokeWidth(1.8f * SCALE);

        float spiralStep = 36 * SCALE;
        for (float sy = (float)Math.floor(y / spiralStep) * spiralStep; sy < y + h; sy += spiralStep) {
            if (sy < y) continue;
            pa.reset();
            pa.moveTo(x + 4 * SCALE, sy);
            pa.quadTo(x + w * 0.5f, sy + 10 * SCALE, x + w - 4 * SCALE, sy);
            pa.quadTo(x + w * 0.5f, sy + 20 * SCALE, x + 6 * SCALE, sy + 14 * SCALE);
            c.drawPath(pa, p);
        }

        // Hanging sacred vines & moss tendrils
        p.setColor(0xFF1B5E20);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawLine(x + w * 0.25f, y, x + w * 0.25f, y + h, p);
        c.drawLine(x + w * 0.75f, y, x + w * 0.75f, y + h, p);

        // Canopy Crown Lip Cap wrapped in sacred vines
        float capH = 24 * SCALE;
        float capY = isTop ? (y + h - capH) : y;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(x - 6 * SCALE, 0, x + w + 6 * SCALE, 0,
                new int[]{0xFF1B5E20, 0xFF2E7D32, 0xFF0D3310}, null, Shader.TileMode.CLAMP));
        rf.set(x - 6 * SCALE, capY, x + w + 6 * SCALE, capY + capH);
        c.drawRoundRect(rf, 6 * SCALE, 6 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFF81C784);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 6 * SCALE, 6 * SCALE, p);

        // Sacred golden rune emblem on cap
        tp.setTypeface(tfBlack);
        tp.setColor(0xFFFFF59D);
        tp.setTextSize(9 * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText("RIMBA ABADI", x + w / 2, capY + capH * 0.65f, tp);
    }

    // ========================================================
    //  GROUND LAYER (Lush Peatland & Oil Palm Plantation Earth)
    // ========================================================
    private void drawGround(Canvas c) {
        float groundY = H - groundH;
        float gh = groundH;
        long timeMs = System.currentTimeMillis();

        // 1. Rich Earth Base Gradient (Warm Fertile Chocolate & Peat Loam - NO PITCH BLACK)
        p.setStyle(Paint.Style.FILL);
        if (groundShader != null) {
            p.setShader(groundShader);
        } else {
            p.setShader(new LinearGradient(0, groundY, 0, H,
                    new int[]{0xFF5A2A14, 0xFF421C0B, 0xFF321406, 0xFF240D04},
                    new float[]{0.0f, 0.25f, 0.65f, 1.0f}, Shader.TileMode.CLAMP));
        }
        c.drawRect(0, groundY, W, H, p);
        p.setShader(null);

        // 2. Earth Geological Strata (Sedimentary Horizontal Waves moving with groundOffset)
        float waveLen = 90 * SCALE;
        for (int s = 1; s <= 3; s++) {
            float sY = groundY + gh * (s == 1 ? 0.28f : (s == 2 ? 0.58f : 0.82f));
            pa.reset();
            pa.moveTo(-20 * SCALE, sY);
            for (float sx = -20 * SCALE; sx <= W + 40 * SCALE; sx += 35 * SCALE) {
                float wy = (float)Math.sin((sx - groundOffset * 0.8f) / waveLen * Math.PI * 2 + s * 1.5f) * (3.0f * SCALE);
                pa.lineTo(sx, sY + wy);
            }
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.8f * SCALE);
            p.setColor(0xFF1E0A03);
            c.drawPath(pa, p);

            // Subtle warm highlight on top of strata
            p.setColor(0x24FFA726);
            p.setStrokeWidth(1.0f * SCALE);
            pa.offset(0, -1.2f * SCALE);
            c.drawPath(pa, p);
        }

        // 3. Embedded Natural Earth Details (Stones, Palm Roots & Peat Embers)
        float patternW = 140 * SCALE;
        float startX = -(groundOffset % patternW);

        for (float bx = startX - patternW; bx < W + patternW; bx += patternW) {
            // [A] Palm Roots (Akar Serabut Kelapa Sawit Alami & Berkelanjutan)
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);

            // Wavy undulating taproot across topsoil - seamless continuous join
            pa.reset();
            pa.moveTo(bx, groundY + 14 * SCALE);
            pa.cubicTo(bx + 35 * SCALE, groundY + 22 * SCALE, bx + 65 * SCALE, groundY + 16 * SCALE, bx + 95 * SCALE, groundY + 24 * SCALE);
            pa.quadTo(bx + 120 * SCALE, groundY + 18 * SCALE, bx + patternW, groundY + 14 * SCALE);
            p.setColor(0xFF5D381E);
            p.setStrokeWidth(2.4f * SCALE);
            c.drawPath(pa, p);

            // Secondary deeper curved roots & tendrils
            pa.reset();
            pa.moveTo(bx + 25 * SCALE, groundY + 20 * SCALE);
            pa.quadTo(bx + 40 * SCALE, groundY + gh * 0.45f, bx + 52 * SCALE, groundY + gh * 0.65f);
            pa.moveTo(bx + 40 * SCALE, groundY + gh * 0.45f);
            pa.quadTo(bx + 58 * SCALE, groundY + gh * 0.52f, bx + 70 * SCALE, groundY + gh * 0.58f);

            pa.moveTo(bx + 85 * SCALE, groundY + 22 * SCALE);
            pa.quadTo(bx + 100 * SCALE, groundY + gh * 0.40f, bx + 115 * SCALE, groundY + gh * 0.60f);
            p.setColor(0xFF452610);
            p.setStrokeWidth(1.6f * SCALE);
            c.drawPath(pa, p);

            // Subtle warm fiber highlights
            pa.reset();
            pa.moveTo(bx + 5 * SCALE, groundY + 13 * SCALE);
            pa.cubicTo(bx + 35 * SCALE, groundY + 20.5f * SCALE, bx + 65 * SCALE, groundY + 15 * SCALE, bx + 95 * SCALE, groundY + 22.5f * SCALE);
            p.setColor(0x558D5B38);
            p.setStrokeWidth(1.0f * SCALE);
            c.drawPath(pa, p);

            // [B] Embedded River Stones & Peat Pebbles (Batu Kerikil Alami)
            drawEarthPebble(c, bx + 22 * SCALE, groundY + gh * 0.34f, 9 * SCALE, 5.5f * SCALE, -8, 0xFF6D4C41);
            drawEarthPebble(c, bx + 56 * SCALE, groundY + gh * 0.68f, 12 * SCALE, 7.0f * SCALE, 14, 0xFF54382E);
            drawEarthPebble(c, bx + 86 * SCALE, groundY + gh * 0.20f, 7 * SCALE, 4.5f * SCALE, 5, 0xFF795548);
            drawEarthPebble(c, bx + 116 * SCALE, groundY + gh * 0.76f, 10 * SCALE, 6.0f * SCALE, -12, 0xFF4E342E);
            drawEarthPebble(c, bx + 132 * SCALE, groundY + gh * 0.44f, 6 * SCALE, 4.0f * SCALE, 20, 0xFF8D6E63);

            // [C] Glowing Peat Soil Fissures (Urat Bara Api Gambut)
            float pulse = 0.5f + 0.5f * (float)Math.sin(timeMs * 0.005f + bx * 0.1f);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb((int)(130 * pulse), 255, 100, 0));
            rf.set(bx + 40 * SCALE, groundY + gh * 0.48f, bx + 48 * SCALE, groundY + gh * 0.48f + 2.5f * SCALE);
            c.drawRoundRect(rf, 1.2f * SCALE, 1.2f * SCALE, p);
            rf.set(bx + 96 * SCALE, groundY + gh * 0.36f, bx + 105 * SCALE, groundY + gh * 0.36f + 2.2f * SCALE);
            c.drawRoundRect(rf, 1.2f * SCALE, 1.2f * SCALE, p);

            // [D] Surface Grass & Tropical Peat Vegetation (Rerumputan & Pakis Sawit)
            drawGrassTuft(c, bx + 6 * SCALE, groundY, 11 * SCALE, -1);
            drawGrassTuft(c, bx + 26 * SCALE, groundY, 15 * SCALE, 1);
            drawGrassTuft(c, bx + 48 * SCALE, groundY, 12 * SCALE, 0);
            drawGrassTuft(c, bx + 68 * SCALE, groundY, 16 * SCALE, -1);
            drawGrassTuft(c, bx + 90 * SCALE, groundY, 13 * SCALE, 1);
            drawGrassTuft(c, bx + 112 * SCALE, groundY, 15 * SCALE, -1);
            drawGrassTuft(c, bx + 130 * SCALE, groundY, 11 * SCALE, 0);
        }

        // 4. Ground Surface Border & Light Rim
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3.0f * SCALE);
        p.setColor(0xFFD84315); // Rich burnt orange earth rim
        c.drawLine(0, groundY, W, groundY, p);

        p.setStrokeWidth(1.4f * SCALE);
        p.setColor(0xFFFFD54F); // Golden sun rim highlight
        c.drawLine(0, groundY - 0.8f * SCALE, W, groundY - 0.8f * SCALE, p);

        // 5. Surface Flickering Smoldering Ground Fires
        for (GroundFire gf : groundFires) {
            float gx = ((gf.x - groundOffset) % (W + 200 * SCALE));
            if (gx < -50 * SCALE) gx += W + 200 * SCALE;
            float pulse = 0.5f + 0.5f * (float)Math.sin(timeMs * 0.006f + gf.phase);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb((int)(120 * pulse), 255, 75, 0));
            rf.set(gx - gf.size, groundY + 4 * SCALE, gx + gf.size, groundY + 4 * SCALE + gf.size * 0.5f);
            c.drawOval(rf, p);

            if (pulse > 0.5f && graphicsLevel >= 1) {
                float flameH = gf.size * 0.95f * pulse;
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.argb((int)(180 * pulse), 255, 109, 0));
                c.drawCircle(gx, groundY - flameH * 0.25f, flameH * 0.5f, p);
                p.setColor(Color.argb((int)(240 * pulse), 255, 224, 130));
                c.drawCircle(gx, groundY - flameH * 0.25f, flameH * 0.25f, p);
            }
        }
    }

    private void drawEarthPebble(Canvas c, float x, float y, float w, float h, float rotDeg, int color) {
        c.save();
        c.translate(x, y);
        c.rotate(rotDeg);

        // Pebble drop shadow
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x55000000);
        rf.set(-w / 2, -h / 2 + 1.8f * SCALE, w / 2, h / 2 + 1.8f * SCALE);
        c.drawOval(rf, p);

        // Pebble body
        p.setColor(color);
        rf.set(-w / 2, -h / 2, w / 2, h / 2);
        c.drawOval(rf, p);

        // Pebble top highlight
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x35FFFFFF);
        p.setStrokeWidth(0.9f * SCALE);
        rf.set(-w / 2 + 0.8f * SCALE, -h / 2 + 0.6f * SCALE, w / 2 - 0.8f * SCALE, 0);
        c.drawOval(rf, p);

        c.restore();
    }

    private void drawGrassTuft(Canvas c, float x, float groundY, float h, int lean) {
        pa.reset();
        pa.moveTo(x - 5.5f * SCALE, groundY + 2 * SCALE);
        pa.quadTo(x - 3 * SCALE + lean * 4 * SCALE, groundY - h * 0.7f, x + lean * 7 * SCALE, groundY - h);
        pa.quadTo(x + 2 * SCALE + lean * 3 * SCALE, groundY - h * 0.5f, x + 5.5f * SCALE, groundY + 2 * SCALE);
        pa.close();

        p.setStyle(Paint.Style.FILL);
        p.setShader(null);
        p.setColor(0xFF2E7D32);
        c.drawPath(pa, p);

        // Sunlit green blade highlight
        pa.reset();
        pa.moveTo(x - 1.5f * SCALE, groundY + 1 * SCALE);
        pa.quadTo(x + lean * 3 * SCALE, groundY - h * 0.6f, x + lean * 6 * SCALE, groundY - h + 1 * SCALE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.4f * SCALE);
        p.setColor(0xFFFFCA28);
        c.drawPath(pa, p);
    }

    private void drawGroundDropShadow(Canvas c) {
        float groundY = H - groundH;
        float bh = BIRD_H * SCALE;
        float heightAbove = Math.max(0, groundY - (birdY + bh));
        float factor = Math.max(0.2f, 1 - heightAbove / (playableH * 0.85f));
        int alpha = (int)(100 * factor);
        float sw = (BIRD_W * SCALE * 0.9f) * factor, sh = (12 * SCALE) * factor;
        float sx = birdX + (BIRD_W * SCALE - sw) / 2;
        sp.setStyle(Paint.Style.FILL);
        sp.setColor(Color.argb(alpha, 10, 3, 0));
        rf.set(sx, groundY - sh * 0.4f, sx + sw, groundY + sh * 0.6f);
        c.drawOval(rf, sp);
    }

    // ========================================================
    //  SETTINGS MODAL & HUD
    // ========================================================
    private void drawSettingsGearBtn(Canvas c) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x55000000);
        c.drawCircle(settingsBtnX + settingsBtnSize / 2, settingsBtnY + settingsBtnSize / 2, settingsBtnSize / 2, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x88FFFFFF);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawCircle(settingsBtnX + settingsBtnSize / 2, settingsBtnY + settingsBtnSize / 2, settingsBtnSize / 2, p);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(17 * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText("⚙", settingsBtnX + settingsBtnSize / 2, settingsBtnY + settingsBtnSize * 0.72f, tp);
    }

    private void drawSettingsScreen(Canvas c) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xCC000000);
        c.drawRect(0, 0, W, H, p);

        float cardW = Math.min(320 * SCALE, W * 0.88f);
        float cardH = Math.min(410 * SCALE, playableH * 0.90f);
        float cardX = (W - cardW) / 2;
        float cardY = safeTop + (playableH - cardH) / 2;

        p.setShader(new LinearGradient(cardX, cardY, cardX, cardY + cardH,
                new int[]{0xFF2A1408, 0xFF140803}, null, Shader.TileMode.CLAMP));
        rf.set(cardX, cardY, cardX + cardW, cardY + cardH);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFF9100);
        p.setStrokeWidth(2 * SCALE);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);

        draw3DText(c, "PENGATURAN", W / 2, cardY + 40 * SCALE, 24 * SCALE, tfBlack, 0xFFFFE082, 0xFFFF6D00, 0xFF7F1D1D);

        float rowPad = 20 * SCALE;
        float rowW = cardW - rowPad * 2;
        float rowH = 54 * SCALE;
        float rowY = cardY + 68 * SCALE;

        drawSettingRow(c, cardX + rowPad, rowY, rowW, rowH, "SUARA SFX", soundEnabled ? "AKTIF" : "MATI", soundEnabled ? 0xFF81C784 : 0xFFE57373);
        rowY += rowH + 14 * SCALE;
        String gfxLabel = graphicsLevel == 2 ? "TINGGI" : (graphicsLevel == 1 ? "SEDANG" : "HEMAT");
        drawSettingRow(c, cardX + rowPad, rowY, rowW, rowH, "GRAFIK", gfxLabel, 0xFFFFD54F);
        rowY += rowH + 14 * SCALE;
        drawSettingRow(c, cardX + rowPad, rowY, rowW, rowH, "TARGET FPS", fpsTarget + " FPS", 0xFF80D8FF);

        float btnW = cardW - 56 * SCALE;
        float btnH = 46 * SCALE;
        float btnX = cardX + 28 * SCALE;
        float btnY = cardY + cardH - btnH - 18 * SCALE;

        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(btnX, btnY, btnX + btnW, btnY + btnH,
                new int[]{0xFFFF9100, 0xFFFF3D00}, null, Shader.TileMode.CLAMP));
        rf.set(btnX, btnY, btnX + btnW, btnY + btnH);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFD54F);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(17 * SCALE);
        tp.setFakeBoldText(true);
        tp.setShadowLayer(4 * SCALE, 0, 2 * SCALE, 0xFF000000);
        c.drawText("SIMPAN & KEMBALI", W / 2, btnY + 29 * SCALE, tp);
        tp.clearShadowLayer();
    }

    private void drawSettingRow(Canvas c, float x, float y, float w, float h, String label, String value, int valueColor) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x40000000);
        rf.set(x, y, x + w, y + h);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x33FFFFFF);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 12 * SCALE, 12 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFECEFF1);
        tp.setTextSize(15 * SCALE);
        tp.setTextAlign(Paint.Align.LEFT);
        tp.setFakeBoldText(true);
        c.drawText(label, x + 14 * SCALE, y + h * 0.62f, tp);

        float valW = 100 * SCALE;
        float valH = 34 * SCALE;
        float valX = x + w - valW - 10 * SCALE;
        float valY = y + (h - valH) / 2;

        p.setStyle(Paint.Style.FILL);
        p.setColor(0x55000000);
        rf.set(valX, valY, valX + valW, valY + valH);
        c.drawRoundRect(rf, 10 * SCALE, 10 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(valueColor);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 10 * SCALE, 10 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(valueColor);
        tp.setTextSize(13.5f * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        c.drawText(value, valX + valW / 2, valY + valH * 0.68f, tp);
    }

    private void drawScoreHUD(Canvas c) {
        float cx = W / 2, cy = safeTop + 24 * SCALE;

        // Top-left Coin Counter Pill
        float coinPillX = 14 * SCALE;
        float coinPillY = safeTop + 4 * SCALE;
        float coinPillH = 28 * SCALE;
        float coinPillW = 84 * SCALE;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x77000000);
        rf.set(coinPillX, coinPillY, coinPillX + coinPillW, coinPillY + coinPillH);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xAAFFD54F);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);

        // Vector Mini Coin Icon
        drawMiniVectorCoin(c, coinPillX + 14 * SCALE, coinPillY + coinPillH / 2, 7.0f * SCALE);

        tp.setTypeface(tfBlack);
        tp.setColor(0xFFFFD54F);
        tp.setTextSize(13 * SCALE);
        tp.setTextAlign(Paint.Align.LEFT);
        tp.setFakeBoldText(true);
        c.drawText(String.valueOf(sawitCoins), coinPillX + 25 * SCALE, coinPillY + 19 * SCALE, tp);

        tp.setTypeface(tfBlack);
        tp.setTextSize(46 * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(true);

        tp.setColor(0xAA000000);
        c.drawText(String.valueOf(score), cx, cy + 5 * SCALE, tp);

        tp.setColor(0xFF7F1D1D);
        for (float d = 4 * SCALE; d >= 1 * SCALE; d -= 1 * SCALE) {
            c.drawText(String.valueOf(score), cx, cy + d, tp);
        }

        tp.setShader(new LinearGradient(cx, cy - 40 * SCALE, cx, cy,
                new int[]{0xFFFFF59D, 0xFFFFB74D, 0xFFFF6D00}, null, Shader.TileMode.CLAMP));
        c.drawText(String.valueOf(score), cx, cy, tp);
        tp.setShader(null);
    }

    private void drawGameOverScreen(Canvas c) {
        if (gameOverAnimProgress < 1.0f) {
            gameOverAnimProgress = Math.min(1.0f, gameOverAnimProgress + 0.05f);
        }
        float t = gameOverAnimProgress;
        float easeOut = 1.0f - (1.0f - t) * (1.0f - t);

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb((int)(0x99 * easeOut), 0, 0, 0));
        c.drawRect(0, 0, W, H, p);

        float cardW = Math.min(320 * SCALE, W * 0.88f);
        float cardH = 292 * SCALE;
        float cardX = (W - cardW) / 2;
        float cardTargetY = safeTop + (playableH - cardH) * 0.46f;
        float cardY = cardTargetY + (1.0f - easeOut) * (85 * SCALE);

        p.setShader(new LinearGradient(cardX, cardY, cardX, cardY + cardH,
                new int[]{0xFF3E1708, 0xFF1E0A02}, null, Shader.TileMode.CLAMP));
        rf.set(cardX, cardY, cardX + cardW, cardY + cardH);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFF5722);
        p.setStrokeWidth(2.5f * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        draw3DText(c, "MISI GAGAL!", W / 2, cardY + 38 * SCALE, 26 * SCALE, tfBlack, 0xFFFF8A80, 0xFFD50000, 0xFF4A0E00);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFCC80);
        float subSize = 12.0f * SCALE;
        tp.setTextSize(subSize);
        float maxSubW = cardW - 36 * SCALE;

        String failSubtitle;
        if (score == 0) {
            failSubtitle = "Kena prank pipa siluman di perbatasan HGU!";
        } else if (score < 6) {
            failSubtitle = "Terjebak asap pekat, anggaran reboisasi nihil!";
        } else if (score < 16) {
            failSubtitle = "Menabrak tiang mafia sawit tanpa izin lingkungan!";
        } else if (score < 31) {
            failSubtitle = "Lolos beberapa pos, tersenggol pipa pejabat!";
        } else {
            failSubtitle = "Luar biasa! Berhasil membongkar kebun ilegal!";
        }

        while (tp.measureText(failSubtitle) > maxSubW && subSize > 8.5f * SCALE) {
            subSize -= 0.5f * SCALE;
            tp.setTextSize(subSize);
        }
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(true);
        c.drawText(failSubtitle, W / 2, cardY + 58 * SCALE, tp);

        float scoreBoxW = cardW - 40 * SCALE;
        float scoreBoxH = 84 * SCALE;
        float scoreBoxX = (W - scoreBoxW) / 2;
        float scoreBoxY = cardY + 72 * SCALE;

        p.setStyle(Paint.Style.FILL);
        p.setColor(0x55000000);
        rf.set(scoreBoxX, scoreBoxY, scoreBoxX + scoreBoxW, scoreBoxY + scoreBoxH);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x44FFFFFF);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFB0BEC5);
        tp.setTextSize(11.5f * SCALE);
        c.drawText("RINTANGAN", scoreBoxX + scoreBoxW * 0.18f, scoreBoxY + 23 * SCALE, tp);
        c.drawText("DISITA", scoreBoxX + scoreBoxW * 0.50f, scoreBoxY + 23 * SCALE, tp);
        c.drawText("REKOR", scoreBoxX + scoreBoxW * 0.82f, scoreBoxY + 23 * SCALE, tp);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(24 * SCALE);
        c.drawText(String.valueOf(score), scoreBoxX + scoreBoxW * 0.18f, scoreBoxY + 54 * SCALE, tp);

        tp.setColor(0xFFFFD54F);
        c.drawText("+" + runCoins, scoreBoxX + scoreBoxW * 0.50f, scoreBoxY + 54 * SCALE, tp);

        tp.setColor(0xFFFFB74D);
        c.drawText(String.valueOf(highScore), scoreBoxX + scoreBoxW * 0.82f, scoreBoxY + 54 * SCALE, tp);

        // Total coin sub-badge (With Vector Coin)
        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFE082);
        tp.setTextSize(11 * SCALE);
        String totalCoinsTxt = "KAS ANTI-KORUPSI: " + sawitCoins;
        float tcW = tp.measureText(totalCoinsTxt);
        float tcCoinR = 5.5f * SCALE;
        float tcTotalW = tcW + tcCoinR * 2 + 5 * SCALE;
        float tcStartX = scoreBoxX + (scoreBoxW - tcTotalW) / 2;
        tp.setTextAlign(Paint.Align.LEFT);
        c.drawText(totalCoinsTxt, tcStartX, scoreBoxY + 79 * SCALE, tp);
        drawMiniVectorCoin(c, tcStartX + tcW + 3 * SCALE + tcCoinR, scoreBoxY + 75.5f * SCALE, tcCoinR);

        // --- BUTTON 1: COBA LAGI (Centered Horizontally & Vertically) ---
        goRetryBtnW = cardW - 40 * SCALE;
        goRetryBtnH = 46 * SCALE;
        goRetryBtnX = (W - goRetryBtnW) / 2;
        goRetryBtnY = scoreBoxY + scoreBoxH + 12 * SCALE;

        // Multi-stage soft drop shadow for Coba Lagi
        sp.setStyle(Paint.Style.FILL);
        sp.setColor(0x35000000);
        rf.set(goRetryBtnX - 1 * SCALE, goRetryBtnY + 4 * SCALE, goRetryBtnX + goRetryBtnW + 1 * SCALE, goRetryBtnY + goRetryBtnH + 6 * SCALE);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, sp);

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF7F1D1D);
        rf.set(goRetryBtnX, goRetryBtnY + 3 * SCALE, goRetryBtnX + goRetryBtnW, goRetryBtnY + goRetryBtnH + 3 * SCALE);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, p);

        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(goRetryBtnX, goRetryBtnY, goRetryBtnX + goRetryBtnW, goRetryBtnY + goRetryBtnH,
                new int[]{0xFFFF9100, 0xFFFF3D00}, null, Shader.TileMode.CLAMP));
        rf.set(goRetryBtnX, goRetryBtnY, goRetryBtnX + goRetryBtnW, goRetryBtnY + goRetryBtnH);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFFD54F);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(18 * SCALE);
        tp.setFakeBoldText(true);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setShadowLayer(4 * SCALE, 0, 2 * SCALE, 0xFF000000);
        float retryTextY = goRetryBtnY + goRetryBtnH / 2 - (tp.descent() + tp.ascent()) / 2;
        c.drawText("COBA LAGI", W / 2, retryTextY, tp);
        tp.clearShadowLayer();

        // --- BUTTON 2: MENU UTAMA (Centered Horizontally & Vertically) ---
        goMenuBtnW = goRetryBtnW;
        goMenuBtnH = 46 * SCALE;
        goMenuBtnX = (W - goMenuBtnW) / 2;
        goMenuBtnY = goRetryBtnY + goRetryBtnH + 10 * SCALE;

        // Multi-stage soft drop shadow for Menu Utama
        sp.setStyle(Paint.Style.FILL);
        sp.setColor(0x35000000);
        rf.set(goMenuBtnX - 1 * SCALE, goMenuBtnY + 4 * SCALE, goMenuBtnX + goMenuBtnW + 1 * SCALE, goMenuBtnY + goMenuBtnH + 6 * SCALE);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, sp);

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1E0E08);
        rf.set(goMenuBtnX, goMenuBtnY + 3 * SCALE, goMenuBtnX + goMenuBtnW, goMenuBtnY + goMenuBtnH + 3 * SCALE);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, p);

        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(goMenuBtnX, goMenuBtnY, goMenuBtnX + goMenuBtnW, goMenuBtnY + goMenuBtnH,
                new int[]{0xFF422114, 0xFF281109}, null, Shader.TileMode.CLAMP));
        rf.set(goMenuBtnX, goMenuBtnY, goMenuBtnX + goMenuBtnW, goMenuBtnY + goMenuBtnH);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xCCFFA726);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 23 * SCALE, 23 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(0xFFFFE0B2);
        tp.setTextSize(16.5f * SCALE);
        tp.setFakeBoldText(true);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setShadowLayer(3 * SCALE, 0, 2 * SCALE, 0xFF000000);
        float menuTextY = goMenuBtnY + goMenuBtnH / 2 - (tp.descent() + tp.ascent()) / 2;
        c.drawText("MENU UTAMA", W / 2, menuTextY, tp);
        tp.clearShadowLayer();
    }

    private void drawDizzyStars(Canvas c, float bx, float by) {
        long timeMs = System.currentTimeMillis();
        float headX = bx - 2 * SCALE;
        float headY = by - 6 * SCALE;
        for (int i = 0; i < 3; i++) {
            float starAngle = timeMs * 0.0055f + i * ((float)Math.PI * 2 / 3);
            float sx = headX + (float)Math.cos(starAngle) * (16 * SCALE);
            float sy = headY + (float)Math.sin(starAngle) * (6 * SCALE);
            tp.setTypeface(tfBlack);
            tp.setColor(0xFFFFD54F);
            tp.setTextSize(10 * SCALE);
            tp.setTextAlign(Paint.Align.CENTER);
            tp.setFakeBoldText(true);
            tp.setShadowLayer(2 * SCALE, 0, 1 * SCALE, 0xFF000000);
            c.drawText("★", sx, sy, tp);
            tp.clearShadowLayer();
        }
    }

    private void drawParticles(Canvas c) {
        for (Particle pr : particles) {
            float alpha = pr.life / pr.maxLife;
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb((int)(alpha * 230), Color.red(pr.color), Color.green(pr.color), Color.blue(pr.color)));
            c.drawCircle(pr.x, pr.y, pr.size * alpha, p);
        }
    }

    private void spawnFlapSparks(float x, float y, int skin) {
        int color = (skin == 2) ? 0xFFFF3D00 : (skin == 3 ? 0xFF00E5FF : 0xFFFFD54F);
        for (int i = 0; i < 7; i++) {
            Particle pr = new Particle();
            pr.x = x;
            pr.y = y;
            pr.vx = -(60 + rng.nextFloat() * 120) * SCALE;
            pr.vy = (-40 + rng.nextFloat() * 80) * SCALE;
            pr.size = (3 + rng.nextFloat() * 4) * SCALE;
            pr.life = pr.maxLife = 0.35f + rng.nextFloat() * 0.2f;
            pr.color = color;
            particles.add(pr);
        }
    }

    private void spawnExplosion(float x, float y) {
        for (int i = 0; i < 35; i++) {
            Particle pr = new Particle();
            pr.x = x;
            pr.y = y;
            float angle = rng.nextFloat() * (float)Math.PI * 2;
            float spd = (50 + rng.nextFloat() * 260) * SCALE;
            pr.vx = (float)Math.cos(angle) * spd;
            pr.vy = (float)Math.sin(angle) * spd;
            pr.size = (4 + rng.nextFloat() * 7) * SCALE;
            pr.life = pr.maxLife = 0.5f + rng.nextFloat() * 0.5f;
            pr.color = (rng.nextBoolean()) ? 0xFFFF5722 : (rng.nextBoolean() ? 0xFFFFD600 : 0xFF212121);
            particles.add(pr);
        }
    }

    private void spawnGroundDust(float x, float y) {
        for (int i = 0; i < 18; i++) {
            Particle pt = new Particle();
            pt.x = x + (rng.nextFloat() - 0.5f) * 24 * SCALE;
            pt.y = y - 2 * SCALE;
            float angle = -(float)Math.PI * 0.15f - rng.nextFloat() * ((float)Math.PI * 0.70f);
            float speed = (30 + rng.nextFloat() * 110) * SCALE;
            pt.vx = (float)Math.cos(angle) * speed;
            pt.vy = (float)Math.sin(angle) * speed;
            pt.size = (4 + rng.nextFloat() * 5) * SCALE;
            pt.maxLife = 0.45f + rng.nextFloat() * 0.35f;
            pt.life = pt.maxLife;
            pt.color = (rng.nextBoolean() ? 0xFF8D6E63 : 0xFF5D4037);
            particles.add(pt);
        }
    }

    private void spawnFeathersAndSparks(float x, float y, int skin) {
        int[] featherColors;
        switch (skin) {
            case 1:
                featherColors = new int[]{ 0xFFFF9100, 0xFFE65100, 0xFFFFD600, 0xFFFFFFFF };
                break;
            case 2:
                featherColors = new int[]{ 0xFFFF1744, 0xFFFF5252, 0xFFFFD600, 0xFFFFAB00 };
                break;
            case 3:
                featherColors = new int[]{ 0xFF00E5FF, 0xFFCFD8DC, 0xFF90A4AE, 0xFFFFD600 };
                break;
            case 4:
                featherColors = new int[]{ 0xFF263238, 0xFFECEFF1, 0xFF00E5FF, 0xFFD50000 };
                break;
            case 5:
                featherColors = new int[]{ 0xFF00E676, 0xFFFFD600, 0xFF2979FF, 0xFFFF6D00 };
                break;
            case 6:
                featherColors = new int[]{ 0xFFFF1744, 0xFFFF9100, 0xFFFFEA00, 0xFF212121 };
                break;
            case 0:
            default:
                featherColors = new int[]{ 0xFFFFB300, 0xFFFF8F00, 0xFFFFF9C4, 0xFF4E342E };
                break;
        }

        for (int i = 0; i < 24; i++) {
            Particle pt = new Particle();
            pt.x = x;
            pt.y = y;
            float angle = rng.nextFloat() * (float)Math.PI * 2;
            float speed = (60 + rng.nextFloat() * 200) * SCALE;
            pt.vx = (float)Math.cos(angle) * speed;
            pt.vy = (float)Math.sin(angle) * speed;
            pt.size = (3.5f + rng.nextFloat() * 4.5f) * SCALE;
            pt.maxLife = 0.55f + rng.nextFloat() * 0.45f;
            pt.life = pt.maxLife;
            pt.color = featherColors[rng.nextInt(featherColors.length)];
            particles.add(pt);
        }
    }

    private void spawnCoinSparkles(float x, float y) {
        int[] coinColors = { 0xFFFFD700, 0xFFFFEB3B, 0xFFFFA000, 0xFFFFFFFF };
        for (int i = 0; i < 14; i++) {
            Particle pt = new Particle();
            pt.x = x;
            pt.y = y;
            float angle = rng.nextFloat() * (float)Math.PI * 2;
            float speed = (40 + rng.nextFloat() * 110) * SCALE;
            pt.vx = (float)Math.cos(angle) * speed;
            pt.vy = (float)Math.sin(angle) * speed;
            pt.size = (3 + rng.nextFloat() * 3.5f) * SCALE;
            pt.maxLife = 0.45f + rng.nextFloat() * 0.35f;
            pt.life = pt.maxLife;
            pt.color = coinColors[rng.nextInt(coinColors.length)];
            particles.add(pt);
        }
    }

    private void spawnCelebrationParticles(float x, float y) {
        int[] celebColors = { 0xFFFFD700, 0xFFFF4081, 0xFF00E676, 0xFF00E5FF, 0xFFFFAB00, 0xFFFFFFFF };
        for (int i = 0; i < 36; i++) {
            Particle pt = new Particle();
            pt.x = x;
            pt.y = y;
            float angle = rng.nextFloat() * (float)Math.PI * 2;
            float speed = (50 + rng.nextFloat() * 200) * SCALE;
            pt.vx = (float)Math.cos(angle) * speed;
            pt.vy = (float)Math.sin(angle) * speed;
            pt.size = (3.5f + rng.nextFloat() * 4.5f) * SCALE;
            pt.maxLife = 0.7f + rng.nextFloat() * 0.5f;
            pt.life = pt.maxLife;
            pt.color = celebColors[rng.nextInt(celebColors.length)];
            particles.add(pt);
        }
    }

    // ========================================================
    //  INTERACTIVE TOUCH HANDLING (Accurate Hitboxes & Instant Restart)
    // ========================================================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return super.onTouchEvent(event);
        float tx = event.getX(), ty = event.getY();

        if (gameState == STATE_DYING) {
            return true;
        }

        if (gameState == STATE_SKINS) {
            handleSkinTouch(tx, ty);
            return true;
        }

        if (gameState == STATE_SETTINGS) {
            handleSettingsTouch(tx, ty);
            return true;
        }

        // Settings gear icon during gameplay
        if (gameState == STATE_PLAYING) {
            if (tx > settingsBtnX - 10 * SCALE && tx < settingsBtnX + settingsBtnSize + 10 * SCALE
                && ty > settingsBtnY - 10 * SCALE && ty < settingsBtnY + settingsBtnSize + 10 * SCALE) {
                prevState = STATE_PLAYING;
                gameState = STATE_SETTINGS;
                return true;
            }
        }

        if (gameState == STATE_MENU) {
            // [1] Play Button (with comfortable thumb tap tolerance)
            if (tx >= playBtnX - 8 * SCALE && tx <= playBtnX + playBtnW + 8 * SCALE
                    && ty >= playBtnY - 8 * SCALE && ty <= playBtnY + playBtnH + 8 * SCALE) {
                gameState = STATE_PLAYING;
                score = 0;
                runCoins = 0;
                pipes.clear();
                particles.clear();
                pipeSpawnTimer = 0.5f;
                worldSpeed = 160;
                resetBird();
                flap();
                return true;
            }

            // [2] Skin & Theme Button (Left)
            if (tx >= skinBtnX - 6 * SCALE && tx <= skinBtnX + skinBtnW + 6 * SCALE
                    && ty >= skinBtnY - 6 * SCALE && ty <= skinBtnY + skinBtnH + 6 * SCALE) {
                prevState = STATE_MENU;
                previewSkin = currentSkin;
                previewPipeSkin = currentPipeSkin;
                gameState = STATE_SKINS;
                playClickSound();
                return true;
            }

            // [3] Settings Button (Right)
            if (tx >= setBtnX - 6 * SCALE && tx <= setBtnX + setBtnW + 6 * SCALE
                    && ty >= setBtnY - 6 * SCALE && ty <= setBtnY + setBtnH + 6 * SCALE) {
                prevState = STATE_MENU;
                gameState = STATE_SETTINGS;
                playClickSound();
                return true;
            }

            // [4] Tap directly on Bird (Interactive satire speech bubble)
            float birdCenterX = W / 2;
            float birdCenterY = safeTop + playableH * 0.36f;
            float dist = (float)Math.hypot(tx - birdCenterX, ty - birdCenterY);
            if (dist < 60 * SCALE) {
                playFlapSound();
                spawnFlapSparks(birdCenterX - 10 * SCALE, birdCenterY, currentSkin);
                currentSatireQuote = SATIRE_QUOTES[rng.nextInt(SATIRE_QUOTES.length)];
                satireQuoteTimer = 2.8f;
                return true;
            }

            // Anywhere else on menu does nothing (must press PLAY button)
            return true;
        } else if (gameState == STATE_PLAYING) {
            flap();
        } else if (gameState == STATE_GAMEOVER) {
            handleGameOverTouch(tx, ty);
            return true;
        }
        return true;
    }

    private void handleGameOverTouch(float tx, float ty) {
        if (gameOverAnimProgress < 0.65f) return;

        // [1] COBA LAGI (Retry Button)
        if (tx >= goRetryBtnX - 8 * SCALE && tx <= goRetryBtnX + goRetryBtnW + 8 * SCALE
                && ty >= goRetryBtnY - 8 * SCALE && ty <= goRetryBtnY + goRetryBtnH + 8 * SCALE) {
            gameState = STATE_PLAYING;
            score = 0;
            runCoins = 0;
            pipes.clear();
            particles.clear();
            pipeSpawnTimer = 0.5f;
            worldSpeed = 160;
            resetBird();
            flap();
            return;
        }

        // [2] MENU UTAMA (Main Menu Button)
        if (tx >= goMenuBtnX - 8 * SCALE && tx <= goMenuBtnX + goMenuBtnW + 8 * SCALE
                && ty >= goMenuBtnY - 8 * SCALE && ty <= goMenuBtnY + goMenuBtnH + 8 * SCALE) {
            goToMenu();
            return;
        }
    }

    public void goToMenu() {
        gameState = STATE_MENU;
        score = 0;
        runCoins = 0;
        pipes.clear();
        particles.clear();
        pipeSpawnTimer = 0.5f;
        worldSpeed = 160;
        resetBird();
        playClickSound();
    }

    public boolean handleBackPressed() {
        if (gameState == STATE_SKINS || gameState == STATE_SETTINGS) {
            gameState = prevState;
            playClickSound();
            return true;
        } else if (gameState == STATE_GAMEOVER || gameState == STATE_DYING) {
            goToMenu();
            return true;
        }
        return false;
    }

    private void handleSkinTouch(float tx, float ty) {
        float cardW = Math.min(320 * SCALE, W * 0.88f);
        float cardH = Math.min(430 * SCALE, playableH * 0.90f);
        float cardX = (W - cardW) / 2;
        float cardY = safeTop + (playableH - cardH) / 2;

        // Tabs Check
        float tabRowY = cardY + 40 * SCALE;
        float tabH = 32 * SCALE;
        float tabGap = 8 * SCALE;
        float tabW = (cardW - 36 * SCALE - tabGap) / 2;
        float tab1X = cardX + 18 * SCALE;
        float tab2X = tab1X + tabW + tabGap;

        if (ty >= tabRowY - 6 * SCALE && ty <= tabRowY + tabH + 6 * SCALE) {
            if (tx >= tab1X && tx <= tab1X + tabW) {
                skinTab = 0;
                playClickSound();
                return;
            }
            if (tx >= tab2X && tx <= tab2X + tabW) {
                skinTab = 1;
                playClickSound();
                return;
            }
        }

        float stageCenterY = cardY + 132 * SCALE;
        float arrowSize = 36 * SCALE;
        float arrowY = stageCenterY - arrowSize / 2;
        float leftArrowX = cardX + 14 * SCALE;
        float rightArrowX = cardX + cardW - arrowSize - 14 * SCALE;

        // Left Arrow [◀]
        if (tx > leftArrowX - 10 * SCALE && tx < leftArrowX + arrowSize + 10 * SCALE
            && ty > arrowY - 10 * SCALE && ty < arrowY + arrowSize + 10 * SCALE) {
            if (skinTab == 0) {
                previewSkin = (previewSkin - 1 + TOTAL_SKINS) % TOTAL_SKINS;
            } else {
                previewPipeSkin = (previewPipeSkin - 1 + TOTAL_PIPE_SKINS) % TOTAL_PIPE_SKINS;
            }
            playClickSound();
            return;
        }

        // Right Arrow [▶]
        if (tx > rightArrowX - 10 * SCALE && tx < rightArrowX + arrowSize + 10 * SCALE
            && ty > arrowY - 10 * SCALE && ty < arrowY + arrowSize + 10 * SCALE) {
            if (skinTab == 0) {
                previewSkin = (previewSkin + 1) % TOTAL_SKINS;
            } else {
                previewPipeSkin = (previewPipeSkin + 1) % TOTAL_PIPE_SKINS;
            }
            playClickSound();
            return;
        }

        float btnW = cardW - 48 * SCALE;
        float btnH = 44 * SCALE;
        float btnX = cardX + 24 * SCALE;
        float equipBtnY = cardY + cardH - btnH * 2 - 18 * SCALE;

        // Equip / Unlock Button
        if (tx > btnX && tx < btnX + btnW && ty > equipBtnY && ty < equipBtnY + btnH) {
            if (skinTab == 0) {
                if (!isSkinUnlocked(previewSkin)) {
                    int cost = SKIN_COSTS[previewSkin];
                    if (sawitCoins >= cost) {
                        sawitCoins -= cost;
                        unlockSkin(previewSkin);
                        currentSkin = previewSkin;
                        saveCoins();
                        saveSettings();
                        playSuccessSound();
                        spawnCelebrationParticles(W / 2, equipBtnY + btnH / 2);
                        try {
                            if (vibrator != null) vibrator.vibrate(80);
                        } catch (Exception ignored) {}
                    } else {
                        playHitSound();
                        try {
                            if (vibrator != null) vibrator.vibrate(35);
                        } catch (Exception ignored) {}
                    }
                    return;
                }
                currentSkin = previewSkin;
            } else {
                if (!isPipeSkinUnlocked(previewPipeSkin)) {
                    int cost = PIPE_SKIN_COSTS[previewPipeSkin];
                    if (sawitCoins >= cost) {
                        sawitCoins -= cost;
                        unlockPipeSkin(previewPipeSkin);
                        currentPipeSkin = previewPipeSkin;
                        saveCoins();
                        saveSettings();
                        playSuccessSound();
                        spawnCelebrationParticles(W / 2, equipBtnY + btnH / 2);
                        try {
                            if (vibrator != null) vibrator.vibrate(80);
                        } catch (Exception ignored) {}
                    } else {
                        playHitSound();
                        try {
                            if (vibrator != null) vibrator.vibrate(35);
                        } catch (Exception ignored) {}
                    }
                    return;
                }
                currentPipeSkin = previewPipeSkin;
            }
            saveSettings();
            playSuccessSound();
            try {
                if (vibrator != null) vibrator.vibrate(60);
            } catch (Exception ignored) {}
            return;
        }

        // Back Button
        float backBtnY = equipBtnY + btnH + 10 * SCALE;
        if (tx > btnX && tx < btnX + btnW && ty > backBtnY && ty < backBtnY + btnH) {
            gameState = prevState;
            playClickSound();
        }
    }

    private void handleSettingsTouch(float tx, float ty) {
        float cardW = Math.min(320 * SCALE, W * 0.88f);
        float cardH = Math.min(410 * SCALE, playableH * 0.90f);
        float cardX = (W - cardW) / 2;
        float cardY = safeTop + (playableH - cardH) / 2;
        float rowPad = 20 * SCALE;
        float rowH = 54 * SCALE;
        float valW = 100 * SCALE;

        // Sound row
        float rowY = cardY + 68 * SCALE;
        float valX = cardX + rowPad + (cardW - rowPad * 2) - valW - 10 * SCALE;
        float valY = rowY + (rowH - 34 * SCALE) / 2;
        if (tx > valX && tx < valX + valW && ty > valY && ty < valY + 34 * SCALE) {
            soundEnabled = !soundEnabled;
            saveSettings();
            playClickSound();
            return;
        }

        // Graphics row
        rowY += rowH + 14 * SCALE;
        valY = rowY + (rowH - 34 * SCALE) / 2;
        if (tx > valX && tx < valX + valW && ty > valY && ty < valY + 34 * SCALE) {
            graphicsLevel = (graphicsLevel + 1) % 3;
            saveSettings();
            initEnvironment();
            playClickSound();
            return;
        }

        // FPS row
        rowY += rowH + 14 * SCALE;
        valY = rowY + (rowH - 34 * SCALE) / 2;
        if (tx > valX && tx < valX + valW && ty > valY && ty < valY + 34 * SCALE) {
            if (fpsTarget == 30) fpsTarget = 60;
            else if (fpsTarget == 60) fpsTarget = 120;
            else fpsTarget = 30;
            saveSettings();
            playClickSound();
            return;
        }

        // Back button
        float btnW = cardW - 56 * SCALE;
        float btnH = 46 * SCALE;
        float btnX = cardX + 28 * SCALE;
        float btnY = cardY + cardH - btnH - 18 * SCALE;
        if (tx > btnX && tx < btnX + btnW && ty > btnY && ty < btnY + btnH) {
            gameState = prevState;
            playClickSound();
        }
    }

    // ========================================================
    //  ARCADE AUDIO ENGINE (Native SoundPool 44.1kHz Studio SFX)
    // ========================================================
    private SoundPool soundPool;
    private int soundFlap = 0;
    private int soundPoint = 0;
    private int soundHit = 0;
    private int soundDie = 0;
    private int soundClick = 0;
    private int soundSuccess = 0;

    private void initModernAudio() {
        try {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(8)
                    .setAudioAttributes(attrs)
                    .build();
            Context ctx = getContext();
            soundFlap = soundPool.load(ctx, R.raw.sfx_flap, 1);
            soundPoint = soundPool.load(ctx, R.raw.sfx_point, 1);
            soundHit = soundPool.load(ctx, R.raw.sfx_hit, 1);
            soundDie = soundPool.load(ctx, R.raw.sfx_die, 1);
            soundClick = soundPool.load(ctx, R.raw.sfx_click, 1);
            soundSuccess = soundPool.load(ctx, R.raw.sfx_success, 1);
        } catch (Exception ignored) {}
    }

    private void playSfx(int soundId, float volume, float rate) {
        if (!soundEnabled || soundPool == null || soundId == 0) return;
        try {
            soundPool.play(soundId, volume, volume, 1, 0, rate);
        } catch (Exception ignored) {}
    }

    public void playFlapSound() {
        playSfx(soundFlap, 0.95f, 1.0f);
    }

    public void playScoreSound() {
        playSfx(soundPoint, 1.0f, 1.0f);
    }

    public void playHitSound() {
        playSfx(soundHit, 1.0f, 1.0f);
    }

    public void playDieSound() {
        playSfx(soundDie, 0.95f, 1.0f);
    }

    public void playClickSound() {
        playSfx(soundClick, 0.85f, 1.0f);
    }

    public void playSuccessSound() {
        playSfx(soundSuccess, 1.0f, 1.0f);
    }

    private void releaseModernAudio() {
        try {
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
        } catch (Exception ignored) {}
    }
}
