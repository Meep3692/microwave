package ca.awoo.microwave;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class Breakout extends State<Integer> {
    // private int paddlePos = 0;
    // private int paddleWidth = 80;
    private double paddlePos;
    private double paddleWidth = 0.25;
    private final Image paddleLeft;
    private final Image paddleRight;
    private final Image paddleMiddle;


    public Breakout(Game game){
        Image paddle = game.getImage("/com/screamingbrainstudio/breakout/Paddles/Style B/Paddle_B_Purple_64x28.png");
        paddleLeft = new BufferedImage(29, 28, BufferedImage.TYPE_4BYTE_ABGR);
        paddleRight = new BufferedImage(29, 28, BufferedImage.TYPE_4BYTE_ABGR);
        paddleMiddle = new BufferedImage(6, 28, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = paddleLeft.getGraphics();
        g.drawImage(paddle, 0, 0, 29, 28, 0, 0, 29, 28, null);
        g = paddleRight.getGraphics();
        g.drawImage(paddle, 0, 0, 29, 28, 35, 0, 63, 28, null);
        g = paddleMiddle.getGraphics();
        g.drawImage(paddle, 0, 0, 6, 28, 29, 0, 34, 28, null);
    }

    @Override
    public Optional<Integer> update(Game game, double dt) {
        Input input = game.getInput();
        if(input.isHeld(Input.LEFT)){
            paddlePos -= dt;
            if(paddlePos < 0){
                paddlePos = 0;
            }
        }
        if(input.isHeld(Input.RIGHT)){
            paddlePos += dt;
            if(paddlePos > 1-paddleWidth){
                paddlePos = 1-paddleWidth;
            }
        }
        if(input.isHeld(Input.EXIT)){
            return Optional.of(0);
        }
        return Optional.empty();
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    private void drawPaddle(Graphics g){
        int sw = getWidth();
        int sh = getHeight();
        int px = (int) (paddlePos * sw);
        int pw = (int) (paddleWidth * sw);
        int paddleY = sh - paddleLeft.getHeight(null);
        g.drawImage(paddleLeft, px, paddleY, null);
        g.drawImage(paddleMiddle, px+paddleLeft.getWidth(null), paddleY, pw-paddleLeft.getWidth(null)-paddleRight.getWidth(null), paddleMiddle.getHeight(null), null);
        g.drawImage(paddleRight, px+pw-paddleRight.getWidth(null), paddleY, null);
    }

    @Override
    public void paint(Graphics g) {
        drawPaddle(g);
        super.paint(g);
    }
    
    
}
