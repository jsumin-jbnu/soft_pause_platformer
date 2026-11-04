package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Saw {

    public Vector2 position;
    public Sprite sprite;
    public Rectangle hitbox;

    // 왕복할 두 지점 (startX, startY), (endX, endY)
    private Vector2 start, end;
    // 톱니의 크기
    private float size;

    // 초당 이동 거리
    private float speed;

    // 진행도 0~1 (0.5 : 50%, 1 : 100%)
    private float t = 0f;

    // 양수(1) : 시작점 → 끝점 , 음수(-1) : 끝점 → 시작점
    private float direction = 1;

    private final float ratio = 0.7f;

    public Saw(Texture texture, float startX, float startY,
               float endX, float endY, float size, float speed) {
        this.position = new Vector2(startX, startY);
        this.start = new Vector2(startX, startY);
        this.end = new Vector2(endX, endY);
        this.speed = speed;
        this.size = size;

        this.sprite = new Sprite(texture);
        this.sprite.setPosition(startX, startY);
        this.sprite.setSize(size, size);

        this.hitbox = new Rectangle(startX+size*(1-ratio)/2, startY+size*(1-ratio)/2,
            size*ratio, size*ratio);
    }

    public void update(float delta) {
        // start와 end 두 점 사이의 거리
        float distance = start.dst(end);

        // 두 점이 같은 위치면 움직임 실행하지 않음 (정지 상태)
        if (distance == 0) {
            return;
        }

        // 초당 speed 만큼 진행, 톱니가 어떤 distance여도 깉은 속력을 유지하도록
        t += direction * speed * delta / distance;

        // 끝점에 도착하면 direction을 음수로 변경 (끝점 → 시작점)
        if (t >= 1f) {
            t = 1f;
            direction = -1;
        }
        // 시작점에 도착하면 direction을 양수로 변경 (시작점 → 끝점)
        else if (t <= 0f) {
            t = 0f;
            direction = 1;
        }

        // 시작점 + (전체 이동량 × 비율)로 이동
        position.x = start.x + (end.x - start.x) * t;
        position.y = start.y + (end.y - start.y) * t;

        // sprite와 hitbox도 같이 이동
        this.sprite.setPosition(position.x, position.y);
        this.hitbox.setPosition(position.x+ size*(1-ratio)/2,
            position.y+ size*(1-ratio)/2);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

}
