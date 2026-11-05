package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Spike {

    public Vector2 position;
    public Sprite sprite;
    public Rectangle hitbox;



    public Spike(Texture texture, float x, float y, float size, boolean flipped) {
        this.position = new Vector2(x, y);

        this.sprite = new Sprite(texture);
        this.sprite.setPosition(x, y);
        this.sprite.setSize(size, size);

        float hitX = x + size * 0.2f;
        float hitY = y + size * 0.2f;
        float hitW = size * 0.6f;
        float hitH = size * 0.6f;

        if (flipped) {
            sprite.flip(false, true);
        }
        this.hitbox = new Rectangle(hitX, hitY, hitW, hitH);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

}
