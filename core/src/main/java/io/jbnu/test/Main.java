package io.jbnu.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {

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
    private BitmapFont clearFont;

    // 카메라
    private OrthographicCamera camera;
    // 고정 카메라 (STAGE CLEAR!)
    private OrthographicCamera staticCamera;
    private Viewport viewport;

    private ShapeRenderer shapeRenderer;


    @Override
    public void create() {
        batch = new SpriteBatch();
        playerTexture = new Texture("char.png");
        blockTexture = new Texture("block.png");
        spikeTexture = new Texture("spike.png");
        jumpPadTexture = new Texture("jump_pad.png");
        sawTexture = new Texture("saw.png");
        flagTexture = new Texture("flag.png");

        world = new GameWorld(playerTexture, blockTexture, spikeTexture,
            jumpPadTexture, sawTexture, flagTexture);

        hardPauseTexture = new Texture("hard_pause.png");
        softPauseTexture = new Texture("soft_pause.png");


        clearFont = new BitmapFont();
        clearFont.getData().setScale(10);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.setToOrtho(false, 1280, 720);

        staticCamera = new OrthographicCamera();
        staticCamera.setToOrtho(false, 1280, 720);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
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
        clearFont.dispose();

        shapeRenderer.dispose();
    }

    public void input() {
        // ESC로 일시정지 토글
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                world.toggleHardPause();
        }

        if(world.isHardPause()) {
            return;
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

        }

        if(world.isSoftPause()) {
            return;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            world.onPlayerJump();
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

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        for (Saw saw : world.getSaws()) {
            shapeRenderer.rect(saw.hitbox.x, saw.hitbox.y, saw.hitbox.width, saw.hitbox.height);
        }
        shapeRenderer.end();


        batch.setProjectionMatrix(staticCamera.combined);
        if(world.getGameState() == GameState.STAGE_CLEAR) {
            batch.begin();
            clearFont.draw(batch, "STAGE CLEAR !!", 60, 440);
            batch.end();
        }
        else if(world.getGameState() == GameState.GAME_CLEAR) {
            batch.begin();
            clearFont.draw(batch, "GAME CLEAR !!!", 80, 440);
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
