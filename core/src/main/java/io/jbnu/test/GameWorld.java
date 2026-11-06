package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;

public class GameWorld {

    // 디버그용 설정값
    private final boolean DEBUG = true;
    private final int DEBUG_STAGE = 5;
    private final int DEBUG_X = -300;
    private final int DEBUG_Y = 600;

    public boolean isDEBUG() {
        return DEBUG;
    }



    // 중력 가속도 값
    public final float WORLD_GRAVITY = -9.8f * 140;

    // 플레이어의 X축 이동값
    private final float MOVE_SPEED = 420f;
     // 점프패드 속도
    private final float JUMP_PAD_VELOCITY = 900f;
    // 스테이지 클리어 후 딜레이 시간
    private final float CLEAR_DELAY = 2f;

    // 낭떠러지 높이
    private final float DEATH_Y = -100;

    // 마지막 스테이지
    public static final int FINAL_STAGE = 7;
    // 소프트 퍼즈 해금 스테이지
    private final int SOFT_PAUSE_UNLOCK_STAGE = 3;
    private final gameCharacter player;
    private final Array<Block> blocks;
    private final Array<Spike> spikes;
    private final Array<JumpPad> jumpPads;
    private final Array<Saw> saws;

    private Flag flag;

    private GameState gameState = GameState.PLAYING;

    // 게임 전체를 멈추는 일시정지 기능 (true일시 하드 퍼즈)
    private boolean hardPause = false;
    // 중력 등 게임 요소를 멈추는 게임 기믹 (true일시 소프트 퍼즈)
    private boolean softPause = false;
    private final Rectangle groundSensor;

    private int currentStage = 1;

    private float clearTimer = 0f;

    private Texture blockTexture;
    private Texture spikeTexture;
    private Texture jumpPadTexture;
    private Texture sawTexture;
    private Texture flagTexture;



    public GameWorld(Texture playerTexture, Texture blockTexture, Texture spikeTexture,
                     Texture jumpPadTexture, Texture sawTexture, Texture flagTexture) {
        this.blockTexture = blockTexture;
        this.spikeTexture = spikeTexture;
        this.jumpPadTexture = jumpPadTexture;
        this.sawTexture = sawTexture;
        this.flagTexture = flagTexture;

        player = new gameCharacter(playerTexture, 0, 100);

        // blockTexture를 이용해서 나중에 여러 Block을 서로 다른 위치에 생성
        blocks = new Array<>();
        spikes = new Array<>();
        jumpPads = new Array<>();
        saws = new Array<>();
        groundSensor = new Rectangle();

        if(DEBUG) {
            loadStage(DEBUG_STAGE);
            resetPlayerToSpawn();
            return;
        }

        loadStage(1);
    }

    public gameCharacter getPlayer()
    {
        return player;
    }




    public void update(float delta) {
        // 하드 퍼즈
        if(hardPause) {
            return;
        }

        if(gameState == GameState.STAGE_CLEAR) {
            clearTimer += delta;

            // 클리어 타이머가 CLEAR_DELAY만큼 지나면 다음 스테이지 이동
            if(clearTimer >= CLEAR_DELAY) {
                if(currentStage >= FINAL_STAGE) {
                    gameState = GameState.GAME_CLEAR;
                    softPause = false;
                }

                else {
                    currentStage++;
                    loadStage(currentStage);

                    gameState = GameState.PLAYING;
                    clearTimer = 0f;

                    resetPlayerToSpawn();
                }
            }

            return;
        }

        else if(gameState == GameState.GAME_CLEAR) {
            if(clearTimer != 0f) {
                clearTimer = 0f;
            }
            return;
        }

        Rectangle playerBounds = player.sprite.getBoundingRectangle();

        // 가시와 충돌 시 리트라이
        for(Spike spike : spikes) {
            if (playerBounds.overlaps(spike.hitbox)) {
                playerDeath();
                return;
            }
        }

        // 점프패드와 충돌 시 슈퍼점프
        for(JumpPad jumpPad : jumpPads) {
            if (playerBounds.overlaps(jumpPad.bounds)) {
                player.velocity.y = JUMP_PAD_VELOCITY;
                player.isGrounded = false;
            }
        }

        // 톱니와 충돌 시 리트라이
        for(Saw saw : saws) {
            if (playerBounds.overlaps(saw.hitbox)) {
                playerDeath();
                return;
            }
        }




        // 플래그와 충돌 시 클리어 처리
        if (playerBounds.overlaps(flag.bounds)) {
            gameState = GameState.STAGE_CLEAR;
        }

        // ★ 소프트 퍼즈
        if(softPause) {
            return;
        }

        // 톱니가 움직이도록 함
        for(Saw saw : saws) {
            saw.update(delta);
        }

        // 바닥에 블럭이 있는지 판정하는 센서
        groundSensor.set(player.position.x+1,
            player.position.y-2,
            player.sprite.getWidth()-2,
            2);

        // grounded 상태에서 발밑에 더 이상 블럭이 없으면 공중 판정
        // = 블럭 위에서 점프 없이 걸어나가 공중에서 떨어짐
            if (player.isGrounded) {
                boolean onBlock = false;
                // 모든 블럭 위에 있는 지 검사
                for(Block block : blocks) {
                    if (groundSensor.overlaps(block.bounds)) {
                        onBlock = true;
                        break;
                    }
                }
                // 어느 블럭 위에도 없다면 공중 판정
                if (!onBlock) {
                    player.isGrounded = false;
                }
            }

        if(!player.isGrounded) {
            player.velocity.y += WORLD_GRAVITY * delta;
            movePlayerY(player.velocity.y * delta);
        }

        // 플레이어가 낭떠러지에 떨어질 시 리스폰
        if(player.position.y < DEATH_Y) {
            playerDeath();
            return;
        }

        player.syncSpriteToPosition();
    }



    public void loadStage(int stage) {
        currentStage = stage;

        blocks.clear();
        spikes.clear();
        jumpPads.clear();
        saws.clear();
        flag = null;

        createLevel();
    }

    public void createLevel() {
        switch (currentStage) {
            case 1:
                createStage1();
                break;
            case 2:
                createStage2();
                break;
            case 3:
                createStage3();
                break;
            case 4:
                createStage4();
                break;
            case 5:
                createStage5();
                break;
            case 6:
                createStage6();
                break;
            case 7:
                createStage7();
                break;
        }
    }

    public void createStage1() {
        blocks.add(new Block(blockTexture, -1200, 0, 3000, 100)); // 지면1
        blocks.add(new Block(blockTexture, -1200, 0, 800, 800)); // 좌측 벽1

        blocks.add(new Block(blockTexture, 800, 0, 600, 200)); // 언덕1_1
        blocks.add(new Block(blockTexture, 1600, 0, 400, 300)); // 언덕1_2

        blocks.add(new Block(blockTexture, 2200, 0, 400, 300)); // 언덕1_낭떠러지1
        blocks.add(new Block(blockTexture, 2800, 0, 550, 400)); // 언덕1_낭떠러지2

        blocks.add(new Block(blockTexture, 3700, 400, 500, 50)); // (플래그 존재) 공중 플랫폼2
        blocks.add(new Block(blockTexture, 3300, 0, 1000, 100)); // 지면2
        blocks.add(new Block(blockTexture, 4100, 0, 200, 200)); // 계단2_1
        blocks.add(new Block(blockTexture, 4300, 0, 200, 300)); // 계단2_2
        blocks.add(new Block(blockTexture, 4500, 0, 200, 400)); // 계단2_3
        blocks.add(new Block(blockTexture, 4700, 0, 800, 800)); // 우측 벽2

        flag = new Flag(flagTexture, 3750, 450, 100, 100); // 골인2
    }
    public void createStage2() {
        blocks.add(new Block(blockTexture, -1200, 0, 2400, 100)); // 지면1
        blocks.add(new Block(blockTexture, -1200, 0, 800, 800)); // 좌측 벽1

        jumpPads.add(new JumpPad(jumpPadTexture, 300, 100, 60, 30)); // 점프패드1
        blocks.add(new Block(blockTexture, 400, 0, 800, 350)); // 언덕1
        spikes.add(new Spike(spikeTexture, 775, 350, 50, false)); // 가시1

        blocks.add(new Block(blockTexture, 1200, 0, 800, 200)); // 언덕2
        jumpPads.add(new JumpPad(jumpPadTexture, 1540, 200, 60, 30)); // 점프패드2
        spikes.add(new Spike(spikeTexture, 1600, 200, 50, false)); // 가시2_1
        spikes.add(new Spike(spikeTexture, 1650, 200, 50, false)); // 가시2_2
        spikes.add(new Spike(spikeTexture, 1700, 200, 50, false)); // 가시2_3
        spikes.add(new Spike(spikeTexture, 1750, 200, 50, false)); // 가시2_4
        spikes.add(new Spike(spikeTexture, 1800, 200, 50, false)); // 가시2_5

        blocks.add(new Block(blockTexture, 2200, 0, 1600, 150)); // 언덕3
        blocks.add(new Block(blockTexture, 2300, 550, 1300, 400)); // 천장3
        jumpPads.add(new JumpPad(jumpPadTexture, 2540, 150, 60, 30)); // 점프패드2_1
        jumpPads.add(new JumpPad(jumpPadTexture, 2940, 150, 60, 30)); // 점프패드2_2
        jumpPads.add(new JumpPad(jumpPadTexture, 3340, 150, 60, 30)); // 점프패드2_3
        jumpPads.add(new JumpPad(jumpPadTexture, 3740, 150, 60, 30)); // 점프패드2_4
        spikes.add(new Spike(spikeTexture, 2300, 450, 100, true)); // 가시3_01
        spikes.add(new Spike(spikeTexture, 2400, 450, 100, true)); // 가시3_02
        spikes.add(new Spike(spikeTexture, 2500, 450, 100, true)); // 가시3_03
        spikes.add(new Spike(spikeTexture, 2600, 450, 100, true)); // 가시3_04
        spikes.add(new Spike(spikeTexture, 2700, 450, 100, true)); // 가시3_05
        spikes.add(new Spike(spikeTexture, 2800, 450, 100, true)); // 가시3_06
        spikes.add(new Spike(spikeTexture, 2900, 450, 100, true)); // 가시3_07
        spikes.add(new Spike(spikeTexture, 3000, 450, 100, true)); // 가시3_08
        spikes.add(new Spike(spikeTexture, 3100, 450, 100, true)); // 가시3_09
        spikes.add(new Spike(spikeTexture, 3200, 450, 100, true)); // 가시3_10
        spikes.add(new Spike(spikeTexture, 3300, 450, 100, true)); // 가시3_11
        spikes.add(new Spike(spikeTexture, 3400, 450, 100, true)); // 가시3_12
        spikes.add(new Spike(spikeTexture, 3500, 450, 100, true)); // 가시3_13

        blocks.add(new Block(blockTexture, 4000, 0, 300, 350)); // 언덕4_1
        blocks.add(new Block(blockTexture, 4300, 0, 1800, 150)); // 언덕4_2
        saws.add(new Saw(sawTexture, 4500, 150, 4500, 500, 200, 300f)); // 톱니4_1
        saws.add(new Saw(sawTexture, 5000, 500, 5000, 150, 200, 300f)); // 톱니4_2
        saws.add(new Saw(sawTexture, 5500, 150, 5500, 500, 200, 500f)); // 톱니4_3

        blocks.add(new Block(blockTexture, 6300, 0, 800, 200)); // 언덕5_위1
        blocks.add(new Block(blockTexture, 7100, 0, 1900, 100)); // 언덕5_아래
        blocks.add(new Block(blockTexture, 7400, 0, 400, 200)); // 언덕5_위2
        blocks.add(new Block(blockTexture, 8100, 0, 400, 200)); // 언덕5_위3
        blocks.add(new Block(blockTexture, 6700, 600, 1800, 300)); // 천장5
        blocks.add(new Block(blockTexture, 9000, 0, 800, 800)); // 우측 벽5

        saws.add(new Saw(sawTexture, 6600, 150, 8100, 150, 500, 300f)); // 톱니5
        jumpPads.add(new JumpPad(jumpPadTexture, 8500 + 250 - 30, 100, 60, 30)); // 점프패드5

        flag = new Flag(flagTexture, 8500 + 250 - 30, 400, 100, 100); // 골인5
    }
    public void createStage3() {
        blocks.add(new Block(blockTexture, -1200, 0, 3000, 100)); // 지면1
        blocks.add(new Block(blockTexture, -1200, 0, 800, 800)); // 좌측 벽1

        jumpPads.add(new JumpPad(jumpPadTexture, 340, 100, 60, 30)); // 점프패드1
        spikes.add(new Spike(spikeTexture, 400, 100, 50, false)); // 가시1_a1
        spikes.add(new Spike(spikeTexture, 450, 100, 50, false)); // 가시1_a2
        spikes.add(new Spike(spikeTexture, 500, 100, 50, false)); // 가시1_a3
        spikes.add(new Spike(spikeTexture, 550, 100, 50, false)); // 가시1_a4
        spikes.add(new Spike(spikeTexture, 600, 100, 50, false)); // 가시1_a5
        spikes.add(new Spike(spikeTexture, 650, 100, 50, false)); // 가시1_a6
        spikes.add(new Spike(spikeTexture, 700, 100, 50, false)); // 가시1_a7

        blocks.add(new Block(blockTexture, 900, 300, 400, 600)); // 천장1

        spikes.add(new Spike(spikeTexture, 1450, 100, 50, false)); // 가시1_a1
        spikes.add(new Spike(spikeTexture, 1500, 100, 50, false)); // 가시1_a2
        spikes.add(new Spike(spikeTexture, 1550, 100, 50, false)); // 가시1_a3
        spikes.add(new Spike(spikeTexture, 1600, 100, 50, false)); // 가시1_a4
        spikes.add(new Spike(spikeTexture, 1650, 100, 50, false)); // 가시1_a5
        spikes.add(new Spike(spikeTexture, 1700, 100, 50, false)); // 가시1_a6
        spikes.add(new Spike(spikeTexture, 1750, 100, 50, false)); // 가시1_a7

        blocks.add(new Block(blockTexture, 1900, 0, 800, 100)); // 지면2_1
        jumpPads.add(new JumpPad(jumpPadTexture, 2020, 100, 60, 30)); // 점프패드2_1
        jumpPads.add(new JumpPad(jumpPadTexture, 2080, 100, 60, 30)); // 점프패드2_2
        jumpPads.add(new JumpPad(jumpPadTexture, 2140, 100, 60, 30)); // 점프패드2_3
        blocks.add(new Block(blockTexture, 2700, 0, 800, 350)); // 지면2_2
        blocks.add(new Block(blockTexture, 3200, 650, 300, 350)); // 천장2_2
        spikes.add(new Spike(spikeTexture, 3200, 350, 100, false)); // 가시2_a1
        spikes.add(new Spike(spikeTexture, 3300, 350, 100, false)); // 가시2_a2
        spikes.add(new Spike(spikeTexture, 3400, 350, 100, false)); // 가시2_a3
        spikes.add(new Spike(spikeTexture, 3200, 550, 100, true)); // 가시2_b1
        spikes.add(new Spike(spikeTexture, 3300, 550, 100, true)); // 가시2_b2
        spikes.add(new Spike(spikeTexture, 3400, 550, 100, true)); // 가시2_b3

        blocks.add(new Block(blockTexture, 3500, 0, 800, 100)); // 지면2_2
        blocks.add(new Block(blockTexture, 3700, 450, 400, 450)); // 천장2_2
        spikes.add(new Spike(spikeTexture, 3700, 100, 100, false)); // 가시2_a1
        spikes.add(new Spike(spikeTexture, 3800, 100, 100, false)); // 가시2_a2
        spikes.add(new Spike(spikeTexture, 3900, 100, 100, false)); // 가시2_a3
        spikes.add(new Spike(spikeTexture, 3700, 350, 100, true)); // 가시2_b1
        spikes.add(new Spike(spikeTexture, 3800, 350, 100, true)); // 가시2_b2
        spikes.add(new Spike(spikeTexture, 3900, 350, 100, true)); // 가시2_b3
        spikes.add(new Spike(spikeTexture, 4000, 350, 100, true)); // 가시2_b4

        blocks.add(new Block(blockTexture, 4100, 200, 900, 700)); // 벽3_1
        blocks.add(new Block(blockTexture, 5000, 0, 800, 800)); // 벽3_2


        flag = new Flag(flagTexture, 4600, 100, 100, 100); // 골인4
    }
    public void createStage4() {
        blocks.add(new Block(blockTexture, -1200, 0, 2400, 100)); // 지면1
        blocks.add(new Block(blockTexture, -1000, 0, 900, 500)); // 좌측 벽1_1
        blocks.add(new Block(blockTexture, -1000, 0, 800, 1000)); // 좌측 벽1_2
        blocks.add(new Block(blockTexture, 400, 0, 200, 250)); // 우측 벽1_1
        blocks.add(new Block(blockTexture, 600, 0, 600, 600)); // 우측 벽1_2
        jumpPads.add(new JumpPad(jumpPadTexture, 140, 100, 60, 30)); // 점프패드1_1
        jumpPads.add(new JumpPad(jumpPadTexture, 540, 250, 60, 30)); // 점프패드1_2
        blocks.add(new Block(blockTexture, -200, 700, 6600, 200)); // 천장1

        spikes.add(new Spike(spikeTexture, 1200, 500, 100, false)); // 가시1_1
        blocks.add(new Block(blockTexture, 1200, 450, 100, 50)); // 블럭1_1

        spikes.add(new Spike(spikeTexture, 1400, 200, 100, false)); // 가시1_3
        spikes.add(new Spike(spikeTexture, 1300, 200, 100, false)); // 가시1_4
        blocks.add(new Block(blockTexture, 1300, 150, 200, 50)); // 블럭1_2

        blocks.add(new Block(blockTexture, 1500, 150, 200, 700)); // 천장2_1
        blocks.add(new Block(blockTexture, 1200, 0, 1400, 100)); // 지면2_1
        jumpPads.add(new JumpPad(jumpPadTexture, 1740, 100, 60, 30)); // 점프패드2_01
        jumpPads.add(new JumpPad(jumpPadTexture, 1800, 100, 60, 30)); // 점프패드2_02
        jumpPads.add(new JumpPad(jumpPadTexture, 1860, 100, 60, 30)); // 점프패드2_03
        jumpPads.add(new JumpPad(jumpPadTexture, 1920, 100, 60, 30)); // 점프패드2_04
        jumpPads.add(new JumpPad(jumpPadTexture, 1980, 100, 60, 30)); // 점프패드2_05
        jumpPads.add(new JumpPad(jumpPadTexture, 2040, 100, 60, 30)); // 점프패드2_06
        jumpPads.add(new JumpPad(jumpPadTexture, 2100, 100, 60, 30)); // 점프패드2_07
        jumpPads.add(new JumpPad(jumpPadTexture, 2160, 100, 60, 30)); // 점프패드2_08
        jumpPads.add(new JumpPad(jumpPadTexture, 2220, 100, 60, 30)); // 점프패드2_09
        jumpPads.add(new JumpPad(jumpPadTexture, 2280, 100, 60, 30)); // 점프패드2_10
        spikes.add(new Spike(spikeTexture, 1700, 200, 100, true)); // 가시2_a1
        spikes.add(new Spike(spikeTexture, 1800, 200, 100, true)); // 가시2_a2
        spikes.add(new Spike(spikeTexture, 1900, 200, 100, true)); // 가시2_a3
        spikes.add(new Spike(spikeTexture, 2000, 200, 100, true)); // 가시2_a4
        spikes.add(new Spike(spikeTexture, 2100, 200, 100, true)); // 가시2_a5
        spikes.add(new Spike(spikeTexture, 2200, 200, 100, true)); // 가시2_a6
        spikes.add(new Spike(spikeTexture, 2300, 200, 100, true)); // 가시2_a7
        blocks.add(new Block(blockTexture, 1700, 300, 700, 50)); // 블럭2_a1

        spikes.add(new Spike(spikeTexture, 1700, 500, 100, true)); // 가시2_b1
        spikes.add(new Spike(spikeTexture, 1800, 500, 100, true)); // 가시2_b2
        spikes.add(new Spike(spikeTexture, 1900, 500, 100, true)); // 가시2_b3
        spikes.add(new Spike(spikeTexture, 2000, 500, 100, true)); // 가시2_b4
        spikes.add(new Spike(spikeTexture, 2100, 500, 100, true)); // 가시2_b5
        spikes.add(new Spike(spikeTexture, 2200, 500, 100, true)); // 가시2_b6
        spikes.add(new Spike(spikeTexture, 2300, 500, 100, true)); // 가시2_b7
        blocks.add(new Block(blockTexture, 1700, 600, 700, 200)); // 블럭2_a1
        jumpPads.add(new JumpPad(jumpPadTexture, 1740, 350, 60, 30)); // 점프패드2_b1

        blocks.add(new Block(blockTexture, 2600, 0, 1600, 250)); // 지면3_1
        blocks.add(new Block(blockTexture, 2700, 0, 400, 600)); // 지면3_2
        spikes.add(new Spike(spikeTexture, 3500, 400, 100, false)); // 가시3_c1
        blocks.add(new Block(blockTexture, 3500, 250, 100, 150)); // 블럭3_1
        blocks.add(new Block(blockTexture, 3300, 500, 100, 300)); // 블럭3_2
        blocks.add(new Block(blockTexture, 3700, 500, 100, 300)); // 블럭3_2
        blocks.add(new Block(blockTexture, 4100, 500, 200, 300)); // 블럭3_3
        blocks.add(new Block(blockTexture, 4100, 250, 100, 250)); // 블럭3_4
        blocks.add(new Block(blockTexture, 4200, 0, 600, 1000)); // 우측 벽3
        jumpPads.add(new JumpPad(jumpPadTexture, 3120, 250, 60, 30)); // 점프패드3_1
        jumpPads.add(new JumpPad(jumpPadTexture, 3620, 250, 60, 30)); // 점프패드2_b1

        flag = new Flag(flagTexture, 3900, 500, 100, 100); // 골인4

    }
    public void createStage5() {
        blocks.add(new Block(blockTexture, -250, 0, 1500, 100)); // 지면1_1
        blocks.add(new Block(blockTexture, -150, 300, 400, 50)); // 천장1_2
        spikes.add(new Spike(spikeTexture, 250, 100, 100, false)); // 가시1_a1
        spikes.add(new Spike(spikeTexture, 350, 100, 100, false)); // 가시1_a2
        spikes.add(new Spike(spikeTexture, 450, 100, 100, false)); // 가시1_a3
        spikes.add(new Spike(spikeTexture, 550, 100, 100, false)); // 가시1_a4
        spikes.add(new Spike(spikeTexture, 650, 100, 100, false)); // 가시1_a5
        spikes.add(new Spike(spikeTexture, 750, 100, 100, false)); // 가시1_a6
        spikes.add(new Spike(spikeTexture, 850, 100, 100, false)); // 가시1_a7
        spikes.add(new Spike(spikeTexture, 950, 100, 100, false)); // 가시1_a8

        spikes.add(new Spike(spikeTexture, 1150, 200, 100, true)); // 가시1_b1
        spikes.add(new Spike(spikeTexture, 1250, 200, 100, true)); // 가시1_b2
        spikes.add(new Spike(spikeTexture, 1350, 200, 100, true)); // 가시1_b3
        spikes.add(new Spike(spikeTexture, 1450, 200, 100, true)); // 가시1_b4
        spikes.add(new Spike(spikeTexture, 1550, 200, 100, true)); // 가시1_b5
        spikes.add(new Spike(spikeTexture, 1650, 200, 100, true)); // 가시1_b6
        spikes.add(new Spike(spikeTexture, 1750, 200, 100, true)); // 가시1_b7
        spikes.add(new Spike(spikeTexture, 1850, 200, 100, true)); // 가시1_b8

        blocks.add(new Block(blockTexture, 250, 300, 1700, 50)); // 지면2_1
        blocks.add(new Block(blockTexture, -550, 700, 2700, 100)); // 천장2_1

        blocks.add(new Block(blockTexture, 2100, 0, 800, 1000)); // 우측 벽2
        jumpPads.add(new JumpPad(jumpPadTexture, 2040, 0, 60, 30)); // 점프패드2_1
        jumpPads.add(new JumpPad(jumpPadTexture, 2040, 200, 60, 30)); // 점프패드2_2
        jumpPads.add(new JumpPad(jumpPadTexture, 420, 350, 60, 30)); // 점프패드2_3

        saws.add(new Saw(sawTexture, 1550, 300, 1550, 550, 200, 500f)); // 톱니2_1
        saws.add(new Saw(sawTexture, 1400, 300, 1400, 550, 200, 500f)); // 톱니2_2

        saws.add(new Saw(sawTexture, 1100, 550, 1100, 300, 200, 550f)); // 톱니2_3
        saws.add(new Saw(sawTexture, 950, 550, 950, 300, 200, 550f)); // 톱니2_4

        saws.add(new Saw(sawTexture, 500, 300, 500, 550, 200, 600f)); // 톱니2_5
        saws.add(new Saw(sawTexture, 350, 550, 350, 300, 200, 600f)); // 톱니2_6
        saws.add(new Saw(sawTexture, 200, 300, 200, 550, 200, 600f)); // 톱니2_7
        blocks.add(new Block(blockTexture, -550, 0, 400, 550)); // 지면2_2

        saws.add(new Saw(sawTexture, -850, -100, -850, 550, 300, 650f)); // 톱니3_1
        blocks.add(new Block(blockTexture, -1150, 0, 300, 250)); // 지면3_1
        blocks.add(new Block(blockTexture, -1150, 450, 300, 500)); // 천장3_1

        saws.add(new Saw(sawTexture, -1450, 550, -1450, -100, 300, 700f)); // 톱니3_2
        blocks.add(new Block(blockTexture, -1750, 0, 300, 350)); // 지면3_2
        blocks.add(new Block(blockTexture, -1750, 550, 300, 400)); // 천장3_2

        saws.add(new Saw(sawTexture, -2050, -100, -2050, 550, 300, 750f)); // 톱니3_3
        blocks.add(new Block(blockTexture, -2350, 0, 300, 100)); // 지면3_3
        blocks.add(new Block(blockTexture, -2350, 300, 300, 600)); // 천장3_3
        jumpPads.add(new JumpPad(jumpPadTexture, -2225, 250, 60, 30)); // 점프패드3

        blocks.add(new Block(blockTexture, -2950, 0, 300, 400)); // 지면3_4
        blocks.add(new Block(blockTexture, -2950, 600, 300, 400)); // 천장3_4
        saws.add(new Saw(sawTexture, -2650, -100, -2650, 550, 300, 250f)); // 톱니3_4

        blocks.add(new Block(blockTexture, -3850, 0, 900, 900)); // 좌측 벽3



        flag = new Flag(flagTexture, -2850, 450, 100, 100); // 골인3
    }

    public void createStage6() {
        blocks.add(new Block(blockTexture, -100, 0, 200, 100));
        flag = new Flag(flagTexture, 200, 100, 100, 100);
    }

    public void createStage7() {
        blocks.add(new Block(blockTexture, -100, 0, 200, 100));
        flag = new Flag(flagTexture, 200, 100, 100, 100);
    }

    public Array<Block> getBlocks() {return blocks;}

    public Array<Spike> getSpikes() {return spikes;}
    public Array<JumpPad> getJumpPads() {return jumpPads;}
    public Array<Saw> getSaws() {return saws;}

    public Flag getFlag() {return flag;}

    public void restartGame() {
        loadStage(1);
        gameState = GameState.PLAYING;

        resetPlayerToSpawn();
        }


    public void resetPlayerToSpawn() {
        softPause = false;

        player.position.set(0, 100);
        player.velocity.set(0, 0);
        player.isGrounded = true;

        if(DEBUG && currentStage == DEBUG_STAGE) {
            player.position.set(DEBUG_X, DEBUG_Y);
        }

        player.syncSpriteToPosition();


    }
    public void playerDeath() {
        loadStage(currentStage);
        resetPlayerToSpawn();
    }


    public GameState getGameState() {
        return gameState;
    }
    public int getCurrentStage() { return currentStage; }

    public boolean isHardPause() {
        return hardPause;
    }

    public boolean isSoftPause() {
        return softPause;
    }

    // X축 블럭 충돌계산
    public void movePlayerX(float moveAmountX) {
        // 다음에 갈 위치 계산
        float expectedX = player.position.x + moveAmountX;

        // 계산한 위치에 충돌 영역 만들기
        player.sprite.setX(expectedX);

        Rectangle playerBoundsX = player.sprite.getBoundingRectangle();

        boolean collisionX = false;

        // 충돌 검사 (블록이 하나라도 겹치면 collisionX = true)
        for(Block block : blocks) {
           if (playerBoundsX.overlaps(block.bounds)) {
               collisionX = true;

               // 오른쪽 이동 중 충돌
               if(moveAmountX > 0) {
                   player.position.x =
                       block.bounds.x - player.sprite.getWidth();
               }

               // 왼쪽 이동 중 충돌
               if(moveAmountX < 0) {
                   player.position.x =
                       block.bounds.x + block.bounds.width;
               }
               break;
           }
        }

        // 충돌하지 않았을 때만 실제 position.x가 반영 됨
        if (!collisionX) {
            player.position.x = expectedX;
        }

        // expectedX로 가정만 했던 위치를 확정지어 스프라이트 이동
        player.syncSpriteToPosition();
    }

    // Y축 블럭 충돌계산
    public void movePlayerY(float moveAmountY) {
        // 다음에 갈 위치 계산
        float expectedY = player.position.y + moveAmountY;

        // 계산한 위치에 충돌 영역 만들기
        player.sprite.setY(expectedY);
        Rectangle playerBoundsY = player.sprite.getBoundingRectangle();

        boolean collisionY = false;

        // 충돌 검사 (블록이 하나라도 겹치면 collisionY = true)
        for(Block block : blocks) {
            if (playerBoundsY.overlaps(block.bounds)) {
                collisionY = true;

                // 위로 이동 중 충돌 (블록 아랫면에 머리 닿음)
                if(moveAmountY > 0) {
                    player.position.y =
                        block.bounds.y - player.sprite.getHeight();
                    player.velocity.y = 0;
                }

                // 아래로 이동 중 충돌 (블록 윗면에 착지)
                if(moveAmountY < 0) {
                    player.position.y =
                        block.bounds.y + block.bounds.height;
                    player.velocity.y = 0;
                    player.isGrounded = true;
                }
                break;
            }
        }

        // 충돌하지 않았을 때만 실제 position.y가 반영 됨
        if (!collisionY) {
            player.position.y = expectedY;
        }

        // expectedY로 가정만 했던 위치를 확정지어 스프라이트 이동
        player.syncSpriteToPosition();

    }


    public void onPlayerRight(float delta) {
        movePlayerX(MOVE_SPEED * delta);
    }

    public void onPlayerLeft(float delta) {
        movePlayerX(-MOVE_SPEED * delta);
    }

    public void onPlayerJump() {
        player.jump();
    }

    public void toggleHardPause() {
        hardPause = !hardPause;
    }

    public void toggleSoftPause() {
        // SOFT_PAUSE_UNLOCK_STAGE 이전엔 소프트 퍼즈 금지
        if(currentStage >= SOFT_PAUSE_UNLOCK_STAGE) {
            softPause = !softPause;
        }
    }
}
