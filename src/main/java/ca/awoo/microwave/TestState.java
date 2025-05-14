package ca.awoo.microwave;

import java.awt.Graphics;
import java.util.Optional;

public class TestState implements State<String>{
    private double t = 0;

    @Override
    public Optional<String> update(Game game, double dt) {
        t += dt;
        return Optional.empty();
    }

    @Override
    public void paint(Graphics g, int w, int h) {
        double pos = Math.sin(t);
        int x = (int) (pos*w/2+w/2);
        g.drawString("Hello world", x, h/2);
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
    
}
