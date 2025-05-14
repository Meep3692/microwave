package ca.awoo.microwave;

import java.awt.Graphics;
import java.util.Optional;

public interface State<Result> {
    public Optional<Result> update();
    public void paint(Graphics g, int w, int h);
    public boolean isTransparent();
}
