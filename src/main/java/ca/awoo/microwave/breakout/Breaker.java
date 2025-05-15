package ca.awoo.microwave.breakout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Optional;

import ca.awoo.microwave.Game;
import ca.awoo.microwave.Input;
import ca.awoo.microwave.State;

public class Breaker extends State<Integer> {
    
    private double paddlePos;
    private double paddleWidth = 128;
    private double paddleSpeed = 500;
    private final Image paddleLeft;
    private final Image paddleRight;
    private final Image paddleMiddle;

    private final Image bg;

    private Brick[] bricks;
    private int fieldWidth;
    private Image brick;
    private int brickw, brickh;
    private int minBrickMargin = 10;
    private int brickMargin = 10;
    private double ballSpeed = 200;

    private Ball ball;
    private Image ballSprite;

    private boolean debug = false;

    public Breaker(Game game){
        Image paddle = game.getImageMasked("/com/screamingbrainstudio/breakout/Paddles/Style B/Paddle_B_Purple_64x28.png", Color.MAGENTA);
        bg = game.getImage("/com/screamingbrainstudio/planetSurfaceBg2/Lava_01-640x480.png");
        brick = game.getImage("/com/screamingbrainstudio/breakout/Bricks/Textured/Textured_Brick_01-64x32.png");
        ballSprite = game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Shiny/Ball_Blue_Shiny-16x16.png", Color.MAGENTA);
        ball = new Ball(ballSprite.getWidth(null)/2.0);
        brickw = brick.getWidth(null);
        brickh = brick.getHeight(null);
        
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

    private boolean launching = true;
    private Hit lastHit;
    @Override
    public Optional<Integer> update(Game game, double dt) {
        int w = getWidth();
        int h = getHeight();
        //Init field
        boolean genField = false;
        if(bricks == null){
            genField = true;
        }else{
            boolean bricksLeft = false;
            for(Brick b : bricks){
                if(b != null){
                    bricksLeft = true;
                }
            }
            genField = !bricksLeft;
        }
        if(genField){
            int maxw = (w - minBrickMargin*2)/brickw;
            int maxh = (h/2 - minBrickMargin*2)/brickh;
            brickMargin = (w - maxw*brickw)/2;
            fieldWidth = maxw;
            int maxbricks = maxw*maxh;
            bricks = new Brick[maxbricks];
            int bx = brickMargin;
            int by = brickMargin;
            int ix = 0;
            for(int i = 0; i < bricks.length; i++){
                bricks[i] = new Brick(new AABB(new Vec2(bx, by), new Vec2(bx+brickw, by+brickh)), brick);
                bx += brickw;
                ix++;
                if(ix >= fieldWidth){
                    ix = 0;
                    bx = brickMargin;
                    by += brickh;
                }
            }
            launching = true;
        }
        Input input = game.getInput();
        if(input.isHeld(Input.LEFT)){
            paddlePos -= dt*paddleSpeed;
            if(paddlePos < 0){
                paddlePos = 0;
            }
        }
        if(input.isHeld(Input.RIGHT)){
            paddlePos += dt*paddleSpeed;
            if(paddlePos > w-paddleWidth){
                paddlePos = w-paddleWidth;
            }
        }
        if(launching && input.isPressed(Input.FIRE)){
            launching = false;
        }
        if(input.isHeld(Input.EXIT)){
            return Optional.of(0);
        }
        if(!launching){
            //Collision
            Vec2 ballDest = ball.destPos(dt);
            if(ballDest.x < 0){
                ball.pos = new Vec2(0, ball.pos.y);
                ball.vel = new Vec2(Math.abs(ball.vel.x), ball.vel.y);
            }
            if(ballDest.x > w){
                ball.pos = new Vec2(w, ball.pos.y);
                ball.vel = new Vec2(-Math.abs(ball.vel.x), ball.vel.y);
            }
            if(ballDest.y < 0){
                ball.pos = new Vec2(ball.pos.x, 0);
                ball.vel = new Vec2(ball.vel.x, Math.abs(ball.vel.y));
            }
            if(ballDest.y > h){
                launching = true;
            }
            Line ballPath = new Line(ball.pos, ball.destPos(dt));
            AABB paddle = new AABB(paddlePos, h-paddleLeft.getHeight(null), paddlePos+paddleWidth, h);
            Hit closest = paddle.castCircle(ballPath, ball.r);
            if(closest != null){
                //Paddle behaves weird
                double offset = closest.location.x - (paddlePos+paddleWidth/2);
                Vec2 newDir = new Vec2(offset, -paddleWidth/2).normalized();
                ball.vel = newDir.times(ballSpeed);
                ball.pos = closest.location;
                lastHit = closest;
                closest = null;
            }
            int hitBrick = -1;
            for(int i = 0; i < bricks.length; i++){
                Brick brick = bricks[i];
                if(brick != null){
                    Hit hit = brick.collider.castCircle(ballPath, ball.r);
                    if(hit != null && (closest == null || hit.distance < closest.distance)){
                        closest = hit;
                        hitBrick = i;
                    }
                }
            }
            if(hitBrick >= 0){
                bricks[hitBrick] = null;
            }
            if(closest != null){
                lastHit = closest;
                ball.vel = ball.vel.reflect(closest.normal);
                ball.pos = closest.location;
            }else{
                ball.pos = ballPath.b;
            }
        }else{
            double paddleY = h - paddleLeft.getHeight(null);
            double paddleCenter = paddlePos + paddleWidth/2;
            ball.pos = new Vec2(paddleCenter, paddleY-ball.r);
            ball.vel = new Vec2(0, -ballSpeed);
        }

        return Optional.empty();
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    private void drawPaddle(Graphics g){
        int sw = getWidth();
        int sh = getHeight();
        g.drawImage(bg, 0, 0, sw, sh, null);
        int px = (int)paddlePos;
        int pw = (int)paddleWidth;
        int paddleY = sh - paddleLeft.getHeight(null);
        g.drawImage(paddleLeft, px, paddleY, null);
        g.drawImage(paddleMiddle, px+paddleLeft.getWidth(null), paddleY, pw-paddleLeft.getWidth(null)-paddleRight.getWidth(null), paddleMiddle.getHeight(null), null);
        g.drawImage(paddleRight, px+pw-paddleRight.getWidth(null), paddleY, null);
    }

    private void drawAABB(AABB aabb, Graphics g){
        int x = (int) (aabb.min.x - ball.r);
        int y = (int) (aabb.min.y - ball.r);
        int w = (int) (aabb.max.x-aabb.min.x+ball.r*2);
        int h = (int) (aabb.max.y-aabb.min.y+ball.r*2);
        g.drawRect(x, y, w, h);
    }

    private void drawHit(Hit hit, Graphics g){
        g.setColor(Color.RED);
        g.fillOval((int)(hit.location.x - 4), (int)(hit.location.y - 4), 8, 8);
        g.setColor(Color.YELLOW);
        g.drawLine((int)(hit.location.x), (int)(hit.location.y), (int)(hit.location.x+hit.normal.x*16), (int)(hit.location.y+hit.normal.y*16));
        g.setColor(Color.BLUE);
        drawAABB(hit.cause, g);
    }

    @Override
    public void paint(Graphics g) {
        drawPaddle(g);

        for(Brick b : bricks){
            if(b != null){
                g.drawImage(b.sprite, b.x(), b.y(), null);
            }
        }

        int ballx = (int) (ball.pos.x - ball.r);
        int bally = (int) (ball.pos.y - ball.r);
        g.drawImage(ballSprite, ballx, bally, null);

        if(debug && lastHit != null){
            drawHit(lastHit, g);
        }

        super.paint(g);
    }
}
