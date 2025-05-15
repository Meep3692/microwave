package ca.awoo.microwave.breakout;

public class Item {
    public final Powerup powerup;
    public Vec2 pos;
    public Item(Powerup powerup, Vec2 pos) {
        this.powerup = powerup;
        this.pos = pos;
    }
    
}
