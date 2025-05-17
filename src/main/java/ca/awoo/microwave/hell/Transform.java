package ca.awoo.microwave.hell;

import ca.awoo.microwave.breakout.Vec2;

public class Transform {
    public Vec2 position;
    public double rotation;

    public Transform(Vec2 position) {
        this.position = position;
    }

    public Transform(Vec2 position, double rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Transform copy(){
        return new Transform(position, rotation);
    }
}
