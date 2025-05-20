package ca.awoo.microwave.hell;

import ca.awoo.microwave.breakout.Vec2;

public class MoveToPlayer {
    public Vec2[] dirs;
    public double speed;

    public MoveToPlayer(double speed, Vec2... dirs) {
        this.speed = speed;
        this.dirs = dirs;
    }
    
}
