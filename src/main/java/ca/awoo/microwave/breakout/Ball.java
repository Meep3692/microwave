package ca.awoo.microwave.breakout;

public class Ball {
    public Vec2 pos = new Vec2(0, 0);
    public Vec2 vel = new Vec2(0, 0);
    public final double r;
    
    public Ball(double r) {
        this.r = r;
    }

    public Vec2 destPos(double dt){
        return pos.plus(vel.times(dt));
    }

    @Override
    public String toString() {
        return "Ball [pos=" + pos + ", vel=" + vel + ", r=" + r + "]";
    }
    
}
