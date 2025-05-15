package ca.awoo.microwave.breakout;

import java.awt.Image;

public class Brick {
    public final AABB collider;
    public final Image sprite;
    public Brick(AABB collider, Image sprite) {
        this.collider = collider;
        this.sprite = sprite;
    }
    public Vec2 position(){
        return collider.min;
    }

    public int x(){
        return (int) collider.min.x;
    }
    public int y(){
        return (int) collider.min.y;
    }
}
