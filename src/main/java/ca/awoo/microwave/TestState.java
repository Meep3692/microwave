package ca.awoo.microwave;

import java.awt.Graphics;
import java.util.Optional;

public class TestState extends State<String>{
    private double t = 0;

    @Override
    public Optional<String> update(Game game, double dt) {
        t += dt;
        return Optional.empty();
    }

    @Override
    public void paint(Graphics g) {
        double pos = Math.sin(t);
        int w = getWidth();
        int h = getHeight();
        int x = (int) (pos*w/2+w/2);
        g.drawString("Hello world", x, h/2);
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
    
}
