package ca.awoo.microwave;

import java.awt.Graphics;
import java.util.Optional;

public class TestState implements State<String>{

    @Override
    public Optional<String> update() {
        return Optional.empty();
    }

    @Override
    public void paint(Graphics g, int w, int h) {
        g.drawString("Hello world", 0, h/2);
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
    
}
