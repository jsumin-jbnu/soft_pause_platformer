package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;

public class GameWorld {

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
    public static final int FINAL_STAGE = 9;
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
        }
    }

    public void createStage1() {
        blocks.add(new Block(blockTexture, -1200, 0, 3000, 100)); // 지면1
        blocks.add(new Block(blockTexture, -1200, 0, 800, 800)); // 좌측 벽

        blocks.add(new Block(blockTexture, 800, 0, 600, 200)); // 첫번째 언덕
        blocks.add(new Block(blockTexture, 1600, 0, 400, 300)); // 두번째 언덕

        blocks.add(new Block(blockTexture, 2200, 0, 400, 300)); // (낭떠러지1) 세번째 언덕
        blocks.add(new Block(blockTexture, 2800, 0, 550, 400)); // (낭떠러지2) 네번째 언덕

        blocks.add(new Block(blockTexture, 3700, 400, 500, 50)); // (플래그 존재) 공중 플랫폼
        blocks.add(new Block(blockTexture, 3300, 0, 1000, 100)); // 지면2
        blocks.add(new Block(blockTexture, 4100, 0, 200, 200)); // 계단1
        blocks.add(new Block(blockTexture, 4300, 0, 200, 300)); // 계단2
        blocks.add(new Block(blockTexture, 4500, 0, 200, 400)); // 계단3
        blocks.add(new Block(blockTexture, 4700, 0, 800, 800)); // 우측 벽

        flag = new Flag(flagTexture, 3750, 450, 100, 100); // 골인
    }
    public void createStage2() {
        blocks.add(new Block(blockTexture, -1200, 0, 3000, 100)); // 지면1
        blocks.add(new Block(blockTexture, -1200, 0, 800, 800)); // 좌측 벽

        flag = new Flag(flagTexture, 10000, 100, 100, 100);
    }
    public void createStage3() {
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
