package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class gameCharacter {

    // 캐릭터의 위치
    public Vector2 position;
    // 캐릭터의 속도
    public Vector2 velocity;

    // 캐릭터가 화면에 어떻게 보이는지
    public Sprite sprite;

    // 현재 땅에 있는지 여부
    public boolean isGrounded = false;

    public gameCharacter(Texture texture, float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);

        this.sprite = new Sprite(texture);
        this.sprite.setSize(50, 50);
        this.sprite.setPosition(position.x, position.y);
    }

    // 현재 position.x, position.y를 sprite에 반영
    public void syncSpriteToPosition() {
        this.sprite.setPosition(position.x, position.y);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void jump() {
        if (isGrounded) {
            velocity.y = 600f;
            isGrounded = false;
        }
    }

}
