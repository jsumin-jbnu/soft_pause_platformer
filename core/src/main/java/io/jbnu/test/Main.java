package io.jbnu.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {

    // 텍스처
    private SpriteBatch batch;
    GameWorld world;
    private Texture playerTexture;
    private Texture blockTexture;
    private Texture spikeTexture;
    private Texture sawTexture;
    private Texture flagTexture;

    private Texture hardPauseTexture;
    private Texture softPauseTexture;
    private Texture jumpPadTexture;
    private BitmapFont stageFont, stageFontBlack;
    private BitmapFont clearFont, clearFontBlack;
    private GlyphLayout layout;

    // 사운드
    private Sound soundJump;
    private Sound soundJumpPad;
    private Sound soundDeath;
    private Sound soundHardPauseOn;
    private Sound soundHardPauseOff;
    private Sound soundSoftPauseOn;
    private Sound soundSoftPauseOff;
    private Sound soundStageClear;
    private Sound soundGameClear;

    private Music earlyStageBGM;
    private Music lateStageBGM;


    // 카메라
    private OrthographicCamera camera;
    // 고정 카메라 (STAGE CLEAR!)
    private OrthographicCamera staticCamera;
    private Viewport viewport;

    private ShapeRenderer shapeRenderer;


    @Override
    public void create() {
        // 이미지
        batch = new SpriteBatch();
        blockTexture = new Texture("textures/block.png");
        blockTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        playerTexture = new Texture("textures/char.png");
        spikeTexture = new Texture("textures/spike.png");
        jumpPadTexture = new Texture("textures/jump_pad.png");
        sawTexture = new Texture("textures/saw.png");
        flagTexture = new Texture("textures/flag.png");

        world = new GameWorld(playerTexture, blockTexture, spikeTexture,
            jumpPadTexture, sawTexture, flagTexture);

        hardPauseTexture = new Texture("textures/hard_pause.png");
        softPauseTexture = new Texture("textures/soft_pause.png");

        // 글꼴 적용
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("font/Gaegu-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.color = Color.WHITE;
        parameter.size = 40;
        stageFont = generator.generateFont(parameter);
        parameter.color = Color.BLACK;
        stageFontBlack = generator.generateFont(parameter);

        parameter.color = Color.WHITE;
        parameter.size = 150;
        clearFont = generator.generateFont(parameter);
        parameter.color = Color.BLACK;
        clearFontBlack = generator.generateFont(parameter);
        generator.dispose();

        // 가운데 정렬을 위한 GlyphLayout
        layout = new GlyphLayout();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.setToOrtho(false, 1280, 720);

        staticCamera = new OrthographicCamera();
        staticCamera.setToOrtho(false, 1280, 720);

        shapeRenderer = new ShapeRenderer();

        // 사운드
        soundJump = Gdx.audio.newSound(Gdx.files.internal("sounds/jump.ogg"));
        soundJumpPad = Gdx.audio.newSound(Gdx.files.internal("sounds/jump_pad.ogg"));
        soundDeath = Gdx.audio.newSound(Gdx.files.internal("sounds/death.ogg"));
        soundHardPauseOn = Gdx.audio.newSound(Gdx.files.internal("sounds/hard_pause_on.ogg"));
        soundHardPauseOff = Gdx.audio.newSound(Gdx.files.internal("sounds/hard_pause_off.ogg"));
        soundSoftPauseOn = Gdx.audio.newSound(Gdx.files.internal("sounds/soft_pause_on.ogg"));
        soundSoftPauseOff = Gdx.audio.newSound(Gdx.files.internal("sounds/soft_pause_off.ogg"));
        soundStageClear = Gdx.audio.newSound(Gdx.files.internal("sounds/stage_clear.ogg"));
        soundGameClear = Gdx.audio.newSound(Gdx.files.internal("sounds/game_clear.ogg"));

        earlyStageBGM = Gdx.audio.newMusic(Gdx.files.internal("musics/early_stage.mp3"));
        lateStageBGM = Gdx.audio.newMusic(Gdx.files.internal("musics/late_stage.mp3"));;
    }

    @Override
    public void render() {
        if(world.getCurrentStage() == 1 || world.getCurrentStage() == 2) {
            ScreenUtils.clear(1f, 1f, 1f, 1f);

        }
        else if(world.getCurrentStage() == 3 || world.getCurrentStage() == 4) {
            ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1f);
        }
        else if(world.getCurrentStage() == 5) {
            ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1f);
        }

        Music currentBGM, otherBGM;

        if(world.getCurrentStage() <= 3) {
            currentBGM = earlyStageBGM;
        }
        else {
            currentBGM = lateStageBGM;
        }
        otherBGM = lateStageBGM;
        otherBGM.stop();
        currentBGM.setVolume(0.9f);
        currentBGM.setLooping(true);

        // 하드 퍼즈 중일 때 BGM 멈추기
        if(world.isHardPause()) {
            currentBGM.pause();
        } // 하드 퍼즈를 취소할 때 (현재 BGM이 플레이 중이 아니라면), 재생
        else if (!currentBGM.isPlaying()) {
            currentBGM.play();
        }

        // 소프트 퍼즈 중일 때 BGM 볼륨 줄이기
        if(world.isSoftPause()) {
            currentBGM.setVolume(0.3f);
        }
        else {
            currentBGM.setVolume(0.9f);
        }


        input();
        logic();
        draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        blockTexture.dispose();
        spikeTexture.dispose();
        sawTexture.dispose();
        flagTexture.dispose();

        hardPauseTexture.dispose();
        softPauseTexture.dispose();
        jumpPadTexture.dispose();
        stageFont.dispose();
        clearFont.dispose();
        stageFontBlack.dispose();
        clearFontBlack.dispose();

        shapeRenderer.dispose();

        soundJump.dispose();
        soundJumpPad.dispose();
        soundDeath.dispose();
        soundHardPauseOn.dispose();
        soundHardPauseOff.dispose();
        soundSoftPauseOn.dispose();
        soundSoftPauseOff.dispose();
        soundStageClear.dispose();
        soundGameClear.dispose();
        earlyStageBGM.dispose();
        lateStageBGM.dispose();
    }

    public void input() {
        // ESC로 일시정지 토글
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                world.toggleHardPause();
            if(world.isHardPause()) {
                soundHardPauseOn.play(0.5f);
            }
            else {
                soundHardPauseOff.play(0.5f);
            }
        }

        if(world.isHardPause()) {
            return;
        }

        // 점프패드에 닿으면 효과음 재생
        if(world.isJumpPadTriggered()) {
            soundJumpPad.play();
            world.resetJumpPadTriggered();
        }

        // 플레이어가 죽으면 효과음 재생
        if(world.isPlayerDeath())  {
            soundDeath.play();
            world.resetPlayerDeath();
        }

        // 스테이지 클리어 시 재생
        if(world.isFlagTouched()) {
            soundStageClear.play();
            world.resetFlagTouched();
        }

        // 게임 클리어 시 재생
        if(world.isGameCleared()) {
            soundGameClear.play();
            world.resetGameCleared();
        }

        // GAME_CLEAR시 ENTER로 리스타트
        if(world.getGameState() == GameState.GAME_CLEAR) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                world.restartGame();
            }
        }

        // STAGE_CLEAR 또는 GAME_CLEAR 시 방향키, 점프 조작 불가
        if(world.getGameState() != GameState.PLAYING) {
            return;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            world.onPlayerRight(Gdx.graphics.getDeltaTime());
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            world.onPlayerLeft(Gdx.graphics.getDeltaTime());
        }

        // Shift로 소프트 퍼즈 기믹 사용
         if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
             || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)){
            world.toggleSoftPause();

            if(world.getCurrentStage() >= world.SOFT_PAUSE_UNLOCK_STAGE) {
                if (world.isSoftPause()) {
                    soundSoftPauseOn.play(0.5f);
                } else {
                    soundSoftPauseOff.play(0.5f);
                }
            }
        }

        if(world.isSoftPause()) {
            return;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if(world.getPlayer().isGrounded) {
                world.onPlayerJump();
                soundJump.play();
            }
        }


    }

    public void logic() {
        world.update(Gdx.graphics.getDeltaTime());
        camera.position.x = world.getPlayer().position.x;
        camera.update();
    }

    public void draw() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        for (Block block : world.getBlocks()) {
            block.draw(batch);
        }

        for (Spike spike : world.getSpikes()) {
            spike.draw(batch);
        }

        for (JumpPad jumpPad : world.getJumpPads()) {
            jumpPad.draw(batch);
        }

        for (Saw saw : world.getSaws()) {
            saw.draw(batch);
        }

        world.getFlag().draw(batch);
        world.getPlayer().draw(batch);

        batch.end();

        if(world.isDEBUG()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);

            for (Spike spike : world.getSpikes()) {
                shapeRenderer.rect(spike.hitbox.x, spike.hitbox.y, spike.hitbox.width, spike.hitbox.height);
            }

            for (Saw saw : world.getSaws()) {
                shapeRenderer.rect(saw.hitbox.x, saw.hitbox.y, saw.hitbox.width, saw.hitbox.height);
            }
            shapeRenderer.end();
        }


        batch.setProjectionMatrix(staticCamera.combined);
        batch.begin();
        String stageText = "Stage " + world.getCurrentStage() + " / " + GameWorld.FINAL_STAGE;

        layout.setText(stageFont, stageText); // 이번 문장 길이 측정
        float stage_x = 640 - layout.width / 2; // 가운데 정렬 x값

        stageFont.draw(batch, stageText, stage_x-2, 698);
        stageFontBlack.draw(batch, stageText, stage_x, 700);
        batch.end();




        if(world.getGameState() == GameState.STAGE_CLEAR) {
            batch.begin();
            layout.setText(clearFont, "STAGE CLEAR !!");
            float clear_x = 640 - layout.width / 2;

            clearFont.draw(batch, "STAGE CLEAR !!", clear_x - 5, 435);
            clearFontBlack.draw(batch, "STAGE CLEAR !!", clear_x, 440);
            batch.end();
        }
        else if(world.getGameState() == GameState.GAME_CLEAR) {
            batch.begin();
            layout.setText(clearFont, "GAME CLEAR !!!");
            float clear_x = 640 - layout.width / 2;

            clearFont.draw(batch, "GAME CLEAR !!!", clear_x - 5, 435);
            clearFontBlack.draw(batch, "GAME CLEAR !!!", clear_x, 440);
            batch.end();
        }

        if(world.isHardPause()) {
            batch.begin();
            batch.draw(hardPauseTexture, 1160, 600, 120, 120);
            batch.end();
        }

        if(world.isSoftPause()) {
            batch.begin();
            batch.draw(softPauseTexture, 0, 600, 120, 120);
            batch.end();
        }

    }
}
