package ca.awoo.microwave.hell;

import java.awt.Image;

public class Sprite {
    public Image image;
    public int layer;

    public Sprite(Image image) {
        this.image = image;
    }

    public Sprite(Image image, int layer) {
        this.image = image;
        this.layer = layer;
    }
    
}
