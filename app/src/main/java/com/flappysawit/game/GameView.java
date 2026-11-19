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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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

    private int gameState = STATE_MENU;
    private int prevState = STATE_MENU;
    private boolean isPaused = false;
    private long lastTimeNs = 0;

    // ========== SETTINGS ==========
    private boolean soundEnabled = true;
    private int graphicsLevel = 2;  // 0=Rendah, 1=Sedang, 2=Tinggi
    private int fpsTarget = 60;     // 30, 60, 120
    private int fpsActual = 0;
    private int frameCounter = 0;
    private long fpsTimer = 0;

    // ========== CHARACTER SKINS SYSTEM ==========
    private int currentSkin = 0;   // 0=Garuda Relawan, 1=Satgas Damkar, 2=Phoenix Rimba, 3=Cyber Mecha
    private int previewSkin = 0;
    private static final int TOTAL_SKINS = 4;

    private static final String[] SKIN_NAMES = {
        "Garuda Relawan",
        "Satgas Damkar",
        "Phoenix Rimba",
        "Cyber Mecha"
    };

    private static final String[] SKIN_TITLES = {
        "RELAWAN N95",
        "SATGAS MANGGALA",
        "SANG PHOENIX",
        "DRONE PENYELAMAT"
    };

    private static final String[] SKIN_DESC = {
        "Elang pejuang memakai masker N95 penahan asap kabut pekat.",
        "Helm damkar pelindung api & rompi fosfor Manggala Agni.",
        "Burung mistis abadi, kebal panas & melibas bara api rimba!",
        "Drone futuristik anti-asap dengan jet plasma booster canggih."
    };

    // ========== OBSTACLE SKINS SYSTEM ==========
    private int currentPipeSkin = 0; // 0=Sawit Membara, 1=Cerobong PKS, 2=Tower Listrik, 3=Magma Obsidian
    private int previewPipeSkin = 0;
    private static final int TOTAL_PIPE_SKINS = 4;
    private int skinTab = 0; // 0 = Karakter, 1 = Rintangan

    private static final String[] PIPE_SKIN_NAMES = {
        "Sawit Membara",
        "Cerobong PKS",
        "Tower Listrik",
        "Magma Obsidian"
    };

    private static final String[] PIPE_SKIN_TITLES = {
        "KLASIK KEBUN SAWIT",
        "PABRIK INDUSTRI SAWIT",
        "TRANSMISI GAMBUT",
        "INFERNO PURBA"
    };

    private static final String[] PIPE_SKIN_DESC = {
        "Batang kelapa sawit terbakar dengan serat wajik dan bara api.",
        "Pipa baja pabrik sawit bergaris hazard kuning-hitam & suar merah.",
        "Menara baja transmisi dengan sengatan petir busur plasma biru.",
        "Pilar batu obsidian hitam kristal dengan urat lahar magma membara."
    };

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
        Pipe(float x, float t, float g) {
            this.x = x;
            topH = t;
            gap = g;
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

        // Calculate Menu Button coordinates
        playBtnW = Math.min(270 * SCALE, W * 0.82f);
        playBtnH = 56 * SCALE;
        playBtnX = (W - playBtnW) / 2;
        playBtnY = safeTop + playableH * 0.65f;

        float subRowY = playBtnY + playBtnH + 16 * SCALE;
        float subRowH = 48 * SCALE;
        float gap = 12 * SCALE;

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

        // Calculate Game Over Button coordinates
        float goCardW = Math.min(320 * SCALE, W * 0.88f);
        float goCardH = 312 * SCALE;
        float goCardX = (W - goCardW) / 2;
        float goCardY = safeTop + (playableH - goCardH) * 0.35f;
        float goScoreBoxY = goCardY + 88 * SCALE;
        float goScoreBoxH = 78 * SCALE;

        goRetryBtnW = goCardW - 40 * SCALE;
        goRetryBtnH = 46 * SCALE;
        goRetryBtnX = goCardX + 20 * SCALE;
        goRetryBtnY = goScoreBoxY + goScoreBoxH + 16 * SCALE;

        goMenuBtnW = goRetryBtnW;
        goMenuBtnH = 46 * SCALE;
        goMenuBtnX = goRetryBtnX;
        goMenuBtnY = goRetryBtnY + goRetryBtnH + 12 * SCALE;

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
        birdVy = 0;
        birdRotation = 0;
        wingAngle = 0;
    }

    private void flap() {
        birdVy = -410 * SCALE;
        wingAngle = -(float)Math.PI / 3;
        playFlapSound();
        spawnFlapSparks(birdX, birdY + (BIRD_H * 0.4f) * SCALE, currentSkin);
    }

    private void triggerGameOver() {
        gameState = STATE_GAMEOVER;
        playHitSound();
        if (score > highScore) {
            highScore = score;
            prefs.edit().putInt("high_score", highScore).apply();
        }
        spawnExplosion(birdX + (BIRD_W * SCALE) / 2, birdY + (BIRD_H * SCALE) / 2);
        try {
            if (vibrator != null) vibrator.vibrate(90);
        } catch (Exception ignored) {}
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

        // Rich Multi-Layer Atmosphere (Zero Empty Space)
        drawSkyAndSun(c);
        if (graphicsLevel >= 1) drawSmokeClouds(c);
        drawVolcanicRidge(c);
        drawContinuousPalmCanopy(c, dt);
        drawParallaxForest(c, farTrees, 0.2f, dt);
        drawParallaxForest(c, midTrees, 0.45f, dt);
        if (graphicsLevel >= 1) drawEmbers(c);
        drawPipes(c);
        if (graphicsLevel >= 2) drawParallaxForest(c, nearTrees, 0.7f, dt);
        drawGround(c);
        drawGroundDropShadow(c);
        drawParticles(c);

        if (gameState == STATE_PLAYING) {
            drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, isBlinking, currentSkin);
            drawScoreHUD(c);
            drawSettingsGearBtn(c);
        } else if (gameState == STATE_MENU) {
            drawIdleBirdWithStage(c);
            drawModernArcadeMenu(c);
        } else if (gameState == STATE_GAMEOVER) {
            drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, isBlinking, currentSkin);
            drawGameOverScreen(c);
        } else if (gameState == STATE_SETTINGS) {
            if (prevState == STATE_PLAYING) {
                drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, isBlinking, currentSkin);
            }
            drawSettingsScreen(c);
        } else if (gameState == STATE_SKINS) {
            drawSkinSelectionScreen(c);
        }

        if (graphicsLevel >= 1 && gameState == STATE_PLAYING) {
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
            triggerGameOver();
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
            if (!pp.passed && pp.x + pw < birdX) {
                pp.passed = true;
                score++;
                playScoreSound();
                if (score % 6 == 0 && worldSpeed < 260) worldSpeed += 12;
            }
            if (birdR > pp.x && birdL < pp.x + pw) {
                if (birdT < pp.topH || birdB > pp.topH + pp.gap) {
                    triggerGameOver();
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
    //  MODERN ARCADE MENU SCREEN
    // ========================================================
    private void drawModernArcadeMenu(Canvas c) {
        long timeMs = System.currentTimeMillis();

        // 1. TOP CAMPAIGN BADGE
        float badgeW = Math.min(290 * SCALE, W * 0.88f);
        float badgeH = 28 * SCALE;
        float badgeY = safeTop + 2 * SCALE;
        float badgeX = (W - badgeW) / 2;

        p.setStyle(Paint.Style.FILL);
        p.setColor(0x35000000);
        rf.set(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x99FF8C00);
        p.setStrokeWidth(1.2f * SCALE);
        c.drawRoundRect(rf, 14 * SCALE, 14 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFB74D);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTextSize(11.5f * SCALE);
        tp.setFakeBoldText(true);
        c.drawText("🚨 MISI DARURAT: SELAMATKAN RIMBA SAWIT", W / 2, badgeY + 18.5f * SCALE, tp);

        // 2. 3D GRAND ARCADE TITLE
        float titleY = safeTop + 62 * SCALE;
        draw3DText(c, "FLAPPY SAWIT", W / 2, titleY, 44 * SCALE, tfBlack, 0xFFFFF176, 0xFFFF3D00, 0xFF7F1D1D);

        tp.setTypeface(tfMedium);
        tp.setColor(0xFFFFCC80);
        tp.setTextSize(13.5f * SCALE);
        tp.setFakeBoldText(false);
        tp.setShadowLayer(4 * SCALE, 0, 2 * SCALE, 0xFF000000);
        c.drawText("Terbang Menembus Asap & Kobaran Hutan!", W / 2, titleY + 23 * SCALE, tp);
        tp.clearShadowLayer();

        // 3. TROPHY HIGH SCORE PILL
        float birdCenterY = safeTop + playableH * 0.38f;
        float trophyW = Math.min(200 * SCALE, W * 0.6f);
        float trophyH = 30 * SCALE;
        float trophyX = (W - trophyW) / 2;
        float trophyY = birdCenterY + (BIRD_H * 0.8f) * SCALE + 20 * SCALE;

        p.setStyle(Paint.Style.FILL);
        p.setColor(0x60000000);
        rf.set(trophyX, trophyY, trophyX + trophyW, trophyY + trophyH);
        c.drawRoundRect(rf, 15 * SCALE, 15 * SCALE, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xCCFFD54F);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 15 * SCALE, 15 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFE082);
        tp.setTextSize(13 * SCALE);
        tp.setFakeBoldText(true);
        c.drawText(highScore > 0 ? "🏆 REKOR TERTINGGI: " + highScore : "🏆 SKOR TERBAIK: 0", W / 2, trophyY + 20 * SCALE, tp);

        // 4. MAIN ACTION BUTTON ("▶ MULAI TERBANG")
        float pulse = 0.90f + (float)Math.sin(timeMs * 0.005f) * 0.10f;

        sp.setStyle(Paint.Style.FILL);
        sp.setColor(Color.argb((int)(60 * pulse), 255, 80, 0));
        rf.set(playBtnX - 6 * SCALE, playBtnY - 6 * SCALE, playBtnX + playBtnW + 6 * SCALE, playBtnY + playBtnH + 6 * SCALE);
        c.drawRoundRect(rf, 32 * SCALE, 32 * SCALE, sp);

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF7F1D1D);
        rf.set(playBtnX, playBtnY + 5 * SCALE, playBtnX + playBtnW, playBtnY + playBtnH + 5 * SCALE);
        c.drawRoundRect(rf, 28 * SCALE, 28 * SCALE, p);

        p.setShader(new LinearGradient(playBtnX, playBtnY, playBtnX, playBtnY + playBtnH,
                new int[]{0xFFFF9100, 0xFFFF3D00, 0xFFD50000}, null, Shader.TileMode.CLAMP));
        rf.set(playBtnX, playBtnY, playBtnX + playBtnW, playBtnY + playBtnH);
        c.drawRoundRect(rf, 28 * SCALE, 28 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x88FFFFFF);
        p.setStrokeWidth(1.8f * SCALE);
        rf.set(playBtnX + 16 * SCALE, playBtnY + 3 * SCALE, playBtnX + playBtnW - 16 * SCALE, playBtnY + 12 * SCALE);
        c.drawRoundRect(rf, 6 * SCALE, 6 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(21 * SCALE);
        tp.setFakeBoldText(true);
        tp.setShadowLayer(5 * SCALE, 0, 3 * SCALE, 0xFF000000);
        c.drawText("▶ MULAI TERBANG", W / 2, playBtnY + playBtnH * 0.67f, tp);
        tp.clearShadowLayer();

        // 5. SECONDARY CONTROL ROW ([ 🎭 PILIH SKIN ] and [ ⚙ PENGATURAN ])
        // [A] Skin Selection Button
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1E0E08);
        rf.set(skinBtnX, skinBtnY + 3 * SCALE, skinBtnX + skinBtnW, skinBtnY + skinBtnH + 3 * SCALE);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);

        p.setShader(new LinearGradient(skinBtnX, skinBtnY, skinBtnX, skinBtnY + skinBtnH,
                new int[]{0xFF4A1F3D, 0xFF280B20}, null, Shader.TileMode.CLAMP));
        rf.set(skinBtnX, skinBtnY, skinBtnX + skinBtnW, skinBtnY + skinBtnH);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFE040FB);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFF8BBD0);
        tp.setTextSize(14.5f * SCALE);
        tp.setFakeBoldText(true);
        tp.setShadowLayer(3 * SCALE, 0, 2 * SCALE, 0xFF000000);
        c.drawText("🎭 PILIH SKIN", skinBtnX + skinBtnW / 2, skinBtnY + skinBtnH * 0.66f, tp);
        tp.clearShadowLayer();

        // [B] Settings Button
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF1E0E08);
        rf.set(setBtnX, setBtnY + 3 * SCALE, setBtnX + setBtnW, setBtnY + setBtnH + 3 * SCALE);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);

        p.setShader(new LinearGradient(setBtnX, setBtnY, setBtnX, setBtnY + setBtnH,
                new int[]{0xFF422114, 0xFF281109}, null, Shader.TileMode.CLAMP));
        rf.set(setBtnX, setBtnY, setBtnX + setBtnW, setBtnY + setBtnH);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0x99FFA726);
        p.setStrokeWidth(1.5f * SCALE);
        c.drawRoundRect(rf, 24 * SCALE, 24 * SCALE, p);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFE0B2);
        tp.setTextSize(14.5f * SCALE);
        tp.setFakeBoldText(true);
        tp.setShadowLayer(3 * SCALE, 0, 2 * SCALE, 0xFF000000);
        c.drawText("⚙ PENGATURAN", setBtnX + setBtnW / 2, setBtnY + setBtnH * 0.66f, tp);
        tp.clearShadowLayer();
    }

    // ========== HERO CHARACTER STAGE IN MENU ==========
    private void drawIdleBirdWithStage(Canvas c) {
        long timeMs = System.currentTimeMillis();
        float birdCenterX = W / 2;
        float birdCenterY = safeTop + playableH * 0.38f;

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
        birdY = birdCenterY - (BIRD_H * SCALE) / 2 + (float)Math.sin(timeMs * 0.0035f) * (12 * SCALE);
        birdRotation = (float)Math.sin(timeMs * 0.0035f) * 6;
        drawBirdWithSkin(c, birdX, birdY, birdRotation, wingAngle, isBlinking, currentSkin);
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

        // 1. Header Title
        draw3DText(c, "WARDROBE & TEMA", W / 2, cardY + 28 * SCALE, 19 * SCALE, tfBlack, 0xFFFFF176, 0xFFFF5722, 0xFF880E4F);

        // 2. Tabs Row
        float tabRowY = cardY + 40 * SCALE;
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

        // 4. Dot Indicators
        float dotsY = cardY + 188 * SCALE;
        int activeIdx = (skinTab == 0) ? previewSkin : previewPipeSkin;
        int totalDots = (skinTab == 0) ? TOTAL_SKINS : TOTAL_PIPE_SKINS;
        for (int i = 0; i < totalDots; i++) {
            float dotX = W / 2 + (i - (totalDots - 1) / 2.0f) * (16 * SCALE);
            p.setStyle(Paint.Style.FILL);
            p.setColor(i == activeIdx ? (skinTab == 0 ? 0xFFE040FB : 0xFFFF9100) : 0x44FFFFFF);
            c.drawCircle(dotX, dotsY, i == activeIdx ? 4 * SCALE : 2.5f * SCALE, p);
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

        // 6. Description Text with Auto-Wrapping
        tp.setTypeface(tfMedium);
        tp.setColor(0xFFF8BBD0);
        tp.setTextSize(11 * SCALE);
        tp.setFakeBoldText(false);
        String desc = (skinTab == 0) ? SKIN_DESC[previewSkin] : PIPE_SKIN_DESC[previewPipeSkin];
        drawWrappedText(c, desc, W / 2, infoBoxY + 40 * SCALE, cardW - 36 * SCALE, tp, 15 * SCALE);

        // 7. Equip Button
        float btnW = cardW - 48 * SCALE;
        float btnH = 44 * SCALE;
        float btnX = cardX + 24 * SCALE;
        float equipBtnY = cardY + cardH - btnH * 2 - 18 * SCALE;

        boolean isEquipped = (skinTab == 0) ? (previewSkin == currentSkin) : (previewPipeSkin == currentPipeSkin);
        p.setStyle(Paint.Style.FILL);
        if (isEquipped) {
            p.setColor(0xFF2E7D32);
        } else {
            p.setShader(new LinearGradient(btnX, equipBtnY, btnX + btnW, equipBtnY + btnH,
                    new int[]{skinTab == 0 ? 0xFFAB47BC : 0xFFFF9100, skinTab == 0 ? 0xFF7B1FA2 : 0xFFFF3D00}, null, Shader.TileMode.CLAMP));
        }
        rf.set(btnX, equipBtnY, btnX + btnW, equipBtnY + btnH);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(isEquipped ? 0xFF81C784 : 0xFFFFD54F);
        p.setStrokeWidth(1.4f * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(15 * SCALE);
        tp.setFakeBoldText(true);
        tp.setShadowLayer(4 * SCALE, 0, 2 * SCALE, 0xFF000000);
        String btnText = isEquipped ? "✓ SEDANG DIGUNAKAN" : (skinTab == 0 ? "▶ GUNAKAN KARAKTER INI" : "▶ GUNAKAN TEMA RINTANGAN");
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
        c.save();
        c.translate(x + hw, y + hh);
        c.rotate(rot);

        switch (skinId) {
            case 1:
                drawBirdDamkar(c, hw, hh, wAngle, blink);
                break;
            case 2:
                drawBirdPhoenix(c, hw, hh, wAngle, blink);
                break;
            case 3:
                drawBirdMecha(c, hw, hh, wAngle);
                break;
            case 0:
            default:
                drawBirdGaruda(c, hw, hh, wAngle, blink);
                break;
        }
        c.restore();
    }

    // [SKIN 0] Garuda Relawan
    private void drawBirdGaruda(Canvas c, float hw, float hh, float wAngle, boolean blink) {
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

        drawBirdEye(c, hw, hh, blink);
        drawBirdBeak(c, hw);
    }

    // [SKIN 1] Satgas Damkar
    private void drawBirdDamkar(Canvas c, float hw, float hh, float wAngle, boolean blink) {
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

        drawBirdEye(c, hw, hh, blink);
        drawBirdBeak(c, hw);
    }

    // [SKIN 2] Phoenix Rimba
    private void drawBirdPhoenix(Canvas c, float hw, float hh, float wAngle, boolean blink) {
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

        drawBirdEye(c, hw, hh, blink);
        drawBirdBeak(c, hw);
    }

    // [SKIN 3] Cyber Mecha
    private void drawBirdMecha(Canvas c, float hw, float hh, float wAngle) {
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

        p.setColor(0xFF00E5FF);
        rf.set(hw * 0.15f, -hh * 0.35f, hw * 0.85f, -hh * 0.05f);
        c.drawRoundRect(rf, 3 * SCALE, 3 * SCALE, p);

        p.setColor(0xFFFFAB00);
        pa.reset();
        pa.moveTo(hw * 0.7f, -hh * 0.1f);
        pa.lineTo(hw + 12 * SCALE, 0);
        pa.lineTo(hw * 0.7f, hh * 0.3f);
        pa.close();
        c.drawPath(pa, p);

        p.setColor(0xFF00E5FF);
        c.drawCircle(-hw * 0.85f, 0, (4 + (float)Math.sin(System.currentTimeMillis() * 0.02f) * 2) * SCALE, p);
    }

    private void drawBirdEye(Canvas c, float hw, float hh, boolean blink) {
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
        p.setShader(new LinearGradient(0, 0, 0, H,
                new int[]{0xFF1C0502, 0xFF4A0E02, 0xFF8A1E00, 0xFFD84315, 0xFFFF7043, 0xFF5A1604},
                new float[]{0, 0.18f, 0.38f, 0.62f, 0.82f, 1.0f}, Shader.TileMode.CLAMP));
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
        }
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

    // ========================================================
    //  GROUND LAYER (Lush Peatland & Oil Palm Plantation Earth)
    // ========================================================
    private void drawGround(Canvas c) {
        float groundY = H - groundH;
        float gh = groundH;
        long timeMs = System.currentTimeMillis();

        // 1. Rich Earth Base Gradient (Warm Fertile Chocolate & Peat Loam - NO PITCH BLACK)
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, groundY, 0, H,
                new int[]{0xFF5A2A14, 0xFF421C0B, 0xFF321406, 0xFF240D04},
                new float[]{0.0f, 0.25f, 0.65f, 1.0f}, Shader.TileMode.CLAMP));
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
            // [A] Palm Roots (Akar Serabut Kelapa Sawit)
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);

            // Root 1: Meandering upper root
            pa.reset();
            pa.moveTo(bx + 8 * SCALE, groundY + 12 * SCALE);
            pa.quadTo(bx + 35 * SCALE, groundY + gh * 0.38f, bx + 70 * SCALE, groundY + gh * 0.44f);
            p.setColor(0xFF7D4E2A);
            p.setStrokeWidth(2.6f * SCALE);
            c.drawPath(pa, p);
            p.setColor(0xFFA06C42);
            p.setStrokeWidth(1.1f * SCALE);
            c.drawPath(pa, p);

            // Root 2: Deeper descending root with branch
            pa.reset();
            pa.moveTo(bx + 75 * SCALE, groundY + gh * 0.26f);
            pa.quadTo(bx + 105 * SCALE, groundY + gh * 0.55f, bx + 135 * SCALE, groundY + gh * 0.70f);
            p.setColor(0xFF6B4021);
            p.setStrokeWidth(2.2f * SCALE);
            c.drawPath(pa, p);
            // Root 2 branch
            pa.reset();
            pa.moveTo(bx + 100 * SCALE, groundY + gh * 0.50f);
            pa.quadTo(bx + 115 * SCALE, groundY + gh * 0.65f, bx + 126 * SCALE, groundY + gh * 0.85f);
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
                p.setShader(new RadialGradient(gx, groundY - flameH * 0.3f, flameH * 0.7f,
                        new int[]{0xEEFFE082, 0x99FF6D00, 0x00AA0000}, null, Shader.TileMode.CLAMP));
                c.drawCircle(gx, groundY - flameH * 0.3f, flameH * 0.7f, p);
                p.setShader(null);
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
        p.setShader(new LinearGradient(x, groundY, x, groundY - h,
                new int[]{0xFF2E7D32, 0xFF4CAF50, 0xFFFFCA28}, null, Shader.TileMode.CLAMP));
        c.drawPath(pa, p);
        p.setShader(null);
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
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x99000000);
        c.drawRect(0, 0, W, H, p);

        float cardW = Math.min(320 * SCALE, W * 0.88f);
        float cardH = 312 * SCALE;
        float cardX = (W - cardW) / 2;
        float cardY = safeTop + (playableH - cardH) * 0.35f;

        p.setShader(new LinearGradient(cardX, cardY, cardX, cardY + cardH,
                new int[]{0xFF3E1708, 0xFF1E0A02}, null, Shader.TileMode.CLAMP));
        rf.set(cardX, cardY, cardX + cardW, cardY + cardH);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(0xFFFF5722);
        p.setStrokeWidth(2.5f * SCALE);
        c.drawRoundRect(rf, 22 * SCALE, 22 * SCALE, p);

        draw3DText(c, "MISI GAGAL!", W / 2, cardY + 46 * SCALE, 30 * SCALE, tfBlack, 0xFFFF8A80, 0xFFD50000, 0xFF4A0E00);

        tp.setTypeface(tfCondensed);
        tp.setColor(0xFFFFCC80);
        tp.setTextSize(14 * SCALE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setFakeBoldText(true);
        c.drawText("Burungmu tertelan pekatnya asap kabut!", W / 2, cardY + 72 * SCALE, tp);

        float scoreBoxW = cardW - 40 * SCALE;
        float scoreBoxH = 78 * SCALE;
        float scoreBoxX = cardX + 20 * SCALE;
        float scoreBoxY = cardY + 88 * SCALE;

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
        tp.setTextSize(13 * SCALE);
        c.drawText("POHON DILEWATI", scoreBoxX + scoreBoxW * 0.28f, scoreBoxY + 27 * SCALE, tp);
        c.drawText("REKOR TERBAIK", scoreBoxX + scoreBoxW * 0.72f, scoreBoxY + 27 * SCALE, tp);

        tp.setTypeface(tfBlack);
        tp.setColor(Color.WHITE);
        tp.setTextSize(28 * SCALE);
        c.drawText(String.valueOf(score), scoreBoxX + scoreBoxW * 0.28f, scoreBoxY + 63 * SCALE, tp);

        tp.setColor(0xFFFFD54F);
        c.drawText(String.valueOf(highScore), scoreBoxX + scoreBoxW * 0.72f, scoreBoxY + 63 * SCALE, tp);

        // --- BUTTON 1: COBA LAGI ---
        goRetryBtnW = cardW - 40 * SCALE;
        goRetryBtnH = 46 * SCALE;
        goRetryBtnX = cardX + 20 * SCALE;
        goRetryBtnY = scoreBoxY + scoreBoxH + 16 * SCALE;

        // Shadow / 3D edge under retry button
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
        tp.setShadowLayer(4 * SCALE, 0, 2 * SCALE, 0xFF000000);
        c.drawText("COBA LAGI", W / 2, goRetryBtnY + goRetryBtnH * 0.67f, tp);
        tp.clearShadowLayer();

        // --- BUTTON 2: MENU UTAMA ---
        goMenuBtnW = goRetryBtnW;
        goMenuBtnH = 46 * SCALE;
        goMenuBtnX = goRetryBtnX;
        goMenuBtnY = goRetryBtnY + goRetryBtnH + 12 * SCALE;

        // Shadow / 3D edge under menu button
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
        tp.setShadowLayer(3 * SCALE, 0, 2 * SCALE, 0xFF000000);
        c.drawText("MENU UTAMA", W / 2, goMenuBtnY + goMenuBtnH * 0.67f, tp);
        tp.clearShadowLayer();
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

    // ========================================================
    //  INTERACTIVE TOUCH HANDLING (Accurate Hitboxes & Instant Restart)
    // ========================================================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return super.onTouchEvent(event);
        float tx = event.getX(), ty = event.getY();

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
            // [1] Play Button
            if (tx > playBtnX && tx < playBtnX + playBtnW && ty > playBtnY && ty < playBtnY + playBtnH) {
                gameState = STATE_PLAYING;
                score = 0;
                pipes.clear();
                particles.clear();
                pipeSpawnTimer = 0.5f;
                worldSpeed = 160;
                resetBird();
                flap();
                return true;
            }

            // [2] Skin & Theme Button (Left)
            if (tx > skinBtnX && tx < skinBtnX + skinBtnW && ty > skinBtnY && ty < skinBtnY + skinBtnH) {
                prevState = STATE_MENU;
                previewSkin = currentSkin;
                previewPipeSkin = currentPipeSkin;
                gameState = STATE_SKINS;
                playClickSound();
                return true;
            }

            // [3] Settings Button (Right)
            if (tx > setBtnX && tx < setBtnX + setBtnW && ty > setBtnY && ty < setBtnY + setBtnH) {
                prevState = STATE_MENU;
                gameState = STATE_SETTINGS;
                playClickSound();
                return true;
            }

            // [4] Tap directly on Bird (Interactive fun)
            float birdCenterX = W / 2;
            float birdCenterY = safeTop + playableH * 0.38f;
            float dist = (float)Math.hypot(tx - birdCenterX, ty - birdCenterY);
            if (dist < 55 * SCALE) {
                playFlapSound();
                spawnFlapSparks(birdCenterX - 10 * SCALE, birdCenterY, currentSkin);
                return true;
            }

            // Anywhere else starts the game
            gameState = STATE_PLAYING;
            score = 0;
            pipes.clear();
            particles.clear();
            pipeSpawnTimer = 0.5f;
            worldSpeed = 160;
            resetBird();
            flap();
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
        // [1] COBA LAGI (Retry Button)
        if (tx >= goRetryBtnX - 8 * SCALE && tx <= goRetryBtnX + goRetryBtnW + 8 * SCALE
                && ty >= goRetryBtnY - 8 * SCALE && ty <= goRetryBtnY + goRetryBtnH + 8 * SCALE) {
            gameState = STATE_PLAYING;
            score = 0;
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
        } else if (gameState == STATE_GAMEOVER) {
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

        // Equip Button
        if (tx > btnX && tx < btnX + btnW && ty > equipBtnY && ty < equipBtnY + btnH) {
            if (skinTab == 0) {
                currentSkin = previewSkin;
            } else {
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
    //  MODERN PROCEDURAL AUDIO ENGINE (Zero Latency Static AudioTrack)
    // ========================================================
    private static final int AUDIO_SAMPLE_RATE = 22050;
    private AudioTrack[] flapTracks = new AudioTrack[2];
    private int flapTrackIdx = 0;
    private AudioTrack scoreTrack;
    private AudioTrack hitTrack;
    private AudioTrack clickTrack;
    private AudioTrack successTrack;

    private void initModernAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Dual-Voice Modern Flap (Aerodynamic Flutter + Warm Pop)
                    short[] flapBuf = generateFlapBuffer(AUDIO_SAMPLE_RATE);
                    flapTracks[0] = createStaticTrack(flapBuf, AUDIO_SAMPLE_RATE);
                    flapTracks[1] = createStaticTrack(flapBuf, AUDIO_SAMPLE_RATE);

                    // 2. Modern Score Crystal Bell (Harmonic Duo-Tone Chime)
                    short[] scoreBuf = generateScoreBuffer(AUDIO_SAMPLE_RATE);
                    scoreTrack = createStaticTrack(scoreBuf, AUDIO_SAMPLE_RATE);

                    // 3. Modern Cinematic Impact (Sub-Bass Thud + Crunch Debris)
                    short[] hitBuf = generateHitBuffer(AUDIO_SAMPLE_RATE);
                    hitTrack = createStaticTrack(hitBuf, AUDIO_SAMPLE_RATE);

                    // 4. Modern UI Tap / Bubble Pop
                    short[] clickBuf = generateClickBuffer(AUDIO_SAMPLE_RATE);
                    clickTrack = createStaticTrack(clickBuf, AUDIO_SAMPLE_RATE);

                    // 5. Modern Success / Celebration Arpeggio Chime
                    short[] successBuf = generateSuccessBuffer(AUDIO_SAMPLE_RATE);
                    successTrack = createStaticTrack(successBuf, AUDIO_SAMPLE_RATE);
                } catch (Exception ignored) {}
            }
        }).start();
    }

    private AudioTrack createStaticTrack(short[] buffer, int sampleRate) {
        try {
            int numBytes = buffer.length * 2;
            AudioTrack track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    numBytes,
                    AudioTrack.MODE_STATIC
            );
            track.write(buffer, 0, buffer.length);
            return track;
        } catch (Exception e) {
            return null;
        }
    }

    private void playSoundTrack(AudioTrack track) {
        if (!soundEnabled || track == null) return;
        try {
            track.stop();
            track.reloadStaticData();
            track.play();
        } catch (Exception ignored) {}
    }

    public void playFlapSound() {
        if (!soundEnabled) return;
        try {
            AudioTrack t = flapTracks[flapTrackIdx];
            flapTrackIdx = (flapTrackIdx + 1) % flapTracks.length;
            if (t != null) {
                t.stop();
                t.reloadStaticData();
                t.play();
            }
        } catch (Exception ignored) {}
    }

    public void playScoreSound() {
        playSoundTrack(scoreTrack);
    }

    public void playHitSound() {
        playSoundTrack(hitTrack);
    }

    public void playClickSound() {
        playSoundTrack(clickTrack);
    }

    public void playSuccessSound() {
        playSoundTrack(successTrack);
    }

    private void playTone(final int startFreq, final int endFreq, final int durationMs) {
        if (!soundEnabled) return;
        if (startFreq < endFreq) {
            playScoreSound();
        } else if (durationMs > 120) {
            playHitSound();
        } else {
            playClickSound();
        }
    }

    private short[] generateFlapBuffer(int sampleRate) {
        int numSamples = (int)(0.085f * sampleRate);
        short[] buf = new short[numSamples];
        double phase = 0;
        Random r = new Random(42);

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            float pitch = 170.0f + 190.0f * (float)Math.sin(t * Math.PI);
            phase += 2.0 * Math.PI * pitch / sampleRate;

            float tone = (float)Math.sin(phase) + 0.35f * (float)Math.sin(phase * 2.0);
            float noise = (r.nextFloat() * 2.0f - 1.0f) * (1.0f - t);

            float mix = tone * 0.72f + noise * 0.28f;
            float env = (t < 0.07f) ? (t / 0.07f) : (float)Math.pow(1.0f - (t - 0.07f) / 0.93f, 2.2);

            int val = (int)(mix * env * 24000);
            if (val > 32767) val = 32767;
            else if (val < -32767) val = -32767;
            buf[i] = (short) val;
        }
        return buf;
    }

    private short[] generateScoreBuffer(int sampleRate) {
        int numSamples = (int)(0.22f * sampleRate);
        short[] buf = new short[numSamples];
        double p1 = 0, p2 = 0, p3 = 0;

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            p1 += 2.0 * Math.PI * 1046.5 / sampleRate;
            p2 += 2.0 * Math.PI * 1318.5 / sampleRate;
            p3 += 2.0 * Math.PI * 2093.0 / sampleRate;

            float bell = 0.50f * (float)Math.sin(p1)
                       + 0.35f * (float)Math.sin(p2)
                       + 0.15f * (float)Math.sin(p3);

            float env = (t < 0.012f) ? (t / 0.012f) : (float)Math.exp(-t * 13.0f);

            int val = (int)(bell * env * 22000);
            if (val > 32767) val = 32767;
            else if (val < -32767) val = -32767;
            buf[i] = (short) val;
        }
        return buf;
    }

    private short[] generateHitBuffer(int sampleRate) {
        int numSamples = (int)(0.24f * sampleRate);
        short[] buf = new short[numSamples];
        double phase = 0;
        Random r = new Random(101);

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            float pitch = 150.0f * (float)Math.exp(-t * 16.0f) + 38.0f;
            phase += 2.0 * Math.PI * pitch / sampleRate;

            float bass = (float)Math.sin(phase);
            float noise = (r.nextFloat() * 2.0f - 1.0f) * (float)Math.exp(-t * 24.0f);

            float mix = (float)Math.tanh((bass * 0.75f + noise * 0.40f) * 1.5f);
            float env = (t < 0.01f) ? (t / 0.01f) : (float)Math.pow(1.0f - t, 2.0);

            int val = (int)(mix * env * 28000);
            if (val > 32767) val = 32767;
            else if (val < -32767) val = -32767;
            buf[i] = (short) val;
        }
        return buf;
    }

    private short[] generateClickBuffer(int sampleRate) {
        int numSamples = (int)(0.038f * sampleRate);
        short[] buf = new short[numSamples];
        double phase = 0;

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            float pitch = 680.0f - 180.0f * t;
            phase += 2.0 * Math.PI * pitch / sampleRate;

            float tone = 0.85f * (float)Math.sin(phase) + 0.15f * (float)Math.sin(phase * 2.0);
            float env = (t < 0.08f) ? (t / 0.08f) : (float)Math.exp(-t * 42.0f);

            int val = (int)(tone * env * 20000);
            if (val > 32767) val = 32767;
            else if (val < -32767) val = -32767;
            buf[i] = (short) val;
        }
        return buf;
    }

    private short[] generateSuccessBuffer(int sampleRate) {
        int numSamples = (int)(0.30f * sampleRate);
        short[] buf = new short[numSamples];
        double phase = 0;

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            float freq = (t < 0.33f) ? 523.25f : ((t < 0.66f) ? 659.25f : 783.99f);
            phase += 2.0 * Math.PI * freq / sampleRate;

            float tone = 0.70f * (float)Math.sin(phase) + 0.30f * (float)Math.sin(phase * 2.0);
            float subT = (t % 0.33f) / 0.33f;
            float env = (float)Math.exp(-subT * 5.5f);

            int val = (int)(tone * env * 21000);
            if (val > 32767) val = 32767;
            else if (val < -32767) val = -32767;
            buf[i] = (short) val;
        }
        return buf;
    }

    private void releaseModernAudio() {
        try {
            for (int i = 0; i < flapTracks.length; i++) {
                if (flapTracks[i] != null) {
                    flapTracks[i].release();
                    flapTracks[i] = null;
                }
            }
            if (scoreTrack != null) { scoreTrack.release(); scoreTrack = null; }
            if (hitTrack != null) { hitTrack.release(); hitTrack = null; }
            if (clickTrack != null) { clickTrack.release(); clickTrack = null; }
            if (successTrack != null) { successTrack.release(); successTrack = null; }
        } catch (Exception ignored) {}
    }
}
