package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Block {

    public Vector2 position;
    public Sprite sprite;
    public Rectangle bounds;


    public Block(Texture texture, float x, float y, float width, float height) {
        this.position = new Vector2(x, y);

        this.sprite = new Sprite(texture);

        this.sprite.setRegion(0f, 0f, width / 100f, height / 100f);
        this.sprite.setRegion(0f, 0f, width / 100f, height / 100f);

        this.sprite.setPosition(x, y);
        this.sprite.setSize(width, height);

        this.bounds = new Rectangle(x, y, width, height);
    }





    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

}
