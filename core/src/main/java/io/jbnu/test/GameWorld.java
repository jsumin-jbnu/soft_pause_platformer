package io.jbnu.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;

public class GameWorld {

    // 중력 가속도 값
    public final float WORLD_GRAVITY = -9.8f * 130;

    // 플레이어의 X축 이동값
    private final float MOVE_SPEED = 420f;
    // 스테이지 클리어 후 딜레이 시간
    private final float CLEAR_DELAY = 2f;

    // 낭떠러지 높이
    private final float DEATH_Y = -100;

    // 마지막 스테이지
    private final int FINAL_STAGE = 3;
    private final gameCharacter player;
    private final Array<Block> blocks;
    private final Array<Spike> spikes;

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
    private Texture flagTexture;


    public GameWorld(Texture playerTexture, Texture blockTexture, Texture spikeTexture, Texture flagTexture) {
        this.blockTexture = blockTexture;
        this.spikeTexture = spikeTexture;
        this.flagTexture = flagTexture;

        player = new gameCharacter(playerTexture, 0, 100);

        // blockTexture를 이용해서 나중에 여러 Block을 서로 다른 위치에 생성
        blocks = new Array<>();
        spikes = new Array<>();
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
                resetPlayerToSpawn();
                return;
            }
        }
        // 플래그와 충돌 시 클리어 처리
        if (playerBounds.overlaps(flag.bounds)) {
            gameState = GameState.STAGE_CLEAR;
        }

        // 소프트 퍼즈
        if(softPause) {
            return;
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

        if(player.position.y < DEATH_Y) {
            resetPlayerToSpawn();
        }



        player.syncSpriteToPosition();
    }



    public void loadStage(int stage) {
        currentStage = stage;

        blocks.clear();
        spikes.clear();
        flag = null;

        createLevel();
    }

    public void createLevel() {
        if (currentStage == 1) {
            blocks.add(new Block(blockTexture, -1000, 0, 4000, 100));
            blocks.add(new Block(blockTexture, 500, 0, 80, 200));
            blocks.add(new Block(blockTexture, 300, 250, 60, 100));

            spikes.add(new Spike(spikeTexture, 200, 100, 60, 60));

            flag = new Flag(flagTexture, 1000, 200, 100, 100);
        }

        else if(currentStage == 2) {
            blocks.add(new Block(blockTexture, -1000, 0, 10000, 100));
            blocks.add(new Block(blockTexture, -1000, 600, 4000, 100));

            flag = new Flag(flagTexture, 1000, 100, 100, 100);
        }

        else if(currentStage == 3) {
            blocks.add(new Block(blockTexture, -100, 0, 200, 100));
            flag = new Flag(flagTexture, 200, 100, 100, 100);
        }
    }

    public Array<Block> getBlocks() {
        return blocks;
    }

    public Array<Spike> getSpikes() {return spikes;}

    public Flag getFlag() { return flag;}

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

    public GameState getGameState() {
        return gameState;
    }

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
        softPause = !softPause;
    }
}
