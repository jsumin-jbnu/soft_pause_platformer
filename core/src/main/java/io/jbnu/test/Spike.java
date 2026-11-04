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


    public Spike(Texture texture, float x, float y, float width, float height) {
        this.position = new Vector2(x, y);

        this.sprite = new Sprite(texture);
        this.sprite.setPosition(x, y);
        this.sprite.setSize(width, height);

        this.hitbox = new Rectangle(x + 10, y + height / 2, width - 20, height/2);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

}
