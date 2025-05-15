package ca.awoo.microwave.breakout;

import java.awt.Image;
import java.util.function.Consumer;

public class Powerup {
    public final Image sprite;
    public final Consumer<Breaker> run;
    public Powerup(Image sprite, Consumer<Breaker> run) {
        this.sprite = sprite;
        this.run = run;
    }
    
}
