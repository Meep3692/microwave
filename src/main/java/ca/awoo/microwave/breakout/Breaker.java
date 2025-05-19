package ca.awoo.microwave.breakout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.sound.midi.Sequence;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import ca.awoo.microwave.Game;
import ca.awoo.microwave.Input;
import ca.awoo.microwave.State;

public class Breaker extends State<Integer> {
    private final Game game;
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

    private final Set<Ball> balls = new HashSet<>();
    private Image ballSprite;

    private String hitSound = "/io/itch/brackeys/sound/hurt.wav";
    private String levelSound = "/io/itch/brackeys/sound/power_up.wav";

    private final List<Powerup> powerups = new ArrayList<>();
    private final Set<Item> items = new HashSet<>();

    private final Random random = new Random();

    private final Vec2 gravity = new Vec2(0, 100);

    private int score = 0;
    private int mult = 1;
    private JLabel scoreLabel;

    public Breaker(Game game){
        this.game = game;
        Sequence music = game.getSequence("/io/itch/surtr/dungeon_forest.mid");
        game.playSequence(music);

        Image paddle = game.getImageMasked("/com/screamingbrainstudio/breakout/Paddles/Style B/Paddle_B_Purple_64x28.png", Color.MAGENTA);
        bg = game.getImage("/com/screamingbrainstudio/planetSurfaceBg2/Lava_01-640x480.png");
        brick = game.getImage("/com/screamingbrainstudio/breakout/Bricks/Textured/Textured_Brick_01-64x32.png");
        ballSprite = game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Shiny/Ball_Blue_Shiny-16x16.png", Color.MAGENTA);
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

        Powerup multiball = new Powerup(game.getImageMasked("/ca/awoo/microwave/breaker/multiball.png", Color.MAGENTA), (b) -> {
            Set<Vec2> newBalls = new HashSet<>();
            for(Ball ball : balls){
                newBalls.add(ball.pos);
            }
            for(Vec2 pos : newBalls){
                double vx = random.nextDouble()-0.5;
                double vy = vx + vx * random.nextDouble();
                spawnBall(pos, new Vec2(vx, vy).normalized().times(ballSpeed));
            }
        });
        powerups.add(multiball);

        setLayout(new BorderLayout());
        JPanel topBar = new JPanel();
        topBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(topBar, BorderLayout.SOUTH);
        topBar.setPreferredSize(new Dimension(getWidth(), 16));
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        scoreLabel = new JLabel("Score: " + score);
        add(topBar, BorderLayout.NORTH);
        topBar.add(scoreLabel);
        topBar.setDoubleBuffered(false);
    }

    public void spawnBall(Vec2 pos, Vec2 vel){
        Ball ball = new Ball(ballSprite.getWidth(null)/2);
        ball.pos = pos;
        ball.vel = vel;
        balls.add(ball);
    }

    public Set<Ball> getBalls(){
        return balls;
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
            int by = brickMargin+16;
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
            balls.clear();
            items.clear();
            game.playSound(levelSound);
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
            double paddleY = h - paddleLeft.getHeight(null);
            double paddleCenter = paddlePos + paddleWidth/2;
            Vec2 pos = new Vec2(paddleCenter, paddleY-8);
            Vec2 vel = new Vec2(0, -ballSpeed);
            spawnBall(pos, vel);
        }
        if(input.isHeld(Input.EXIT)){
            return Optional.of(0);
        }
        if(!launching){
            Set<Ball> deadBalls = new HashSet<>();
            for(Ball ball : balls){
                //Collision
                Vec2 ballDest = ball.destPos(dt);
                if(ballDest.x < ball.r){
                    ball.pos = new Vec2(ball.r, ball.pos.y);
                    ball.vel = new Vec2(Math.abs(ball.vel.x), ball.vel.y);
                    game.playSound(hitSound);
                }
                if(ballDest.x > w-ball.r){
                    ball.pos = new Vec2(w-ball.r, ball.pos.y);
                    ball.vel = new Vec2(-Math.abs(ball.vel.x), ball.vel.y);
                    game.playSound(hitSound);
                }
                if(ballDest.y < ball.r+16){
                    ball.pos = new Vec2(ball.pos.x, ball.r+16);
                    ball.vel = new Vec2(ball.vel.x, Math.abs(ball.vel.y));
                    game.playSound(hitSound);
                }
                if(ballDest.y > h+ball.r){
                    deadBalls.add(ball);
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
                    game.playSound(hitSound);
                    mult = 1;
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
                    if(random.nextInt(10) < 1){
                        int i = random.nextInt(powerups.size());
                        Powerup powerup = powerups.get(i);
                        double brickx = (hitBrick%fieldWidth*brickw)+brickw/2+brickMargin;
                        double bricky = (hitBrick/fieldWidth*brickh)+brickh/2+brickMargin+16;
                        Item item = new Item(powerup, new Vec2(brickx, bricky));
                        items.add(item);
                    }
                    score += mult++;
                }
                if(closest != null){
                    lastHit = closest;
                    ball.vel = ball.vel.reflect(closest.normal);
                    ball.pos = closest.location;
                    game.playSound(hitSound);
                }else{
                    ball.pos = ballPath.b;
                }
            }
            balls.removeAll(deadBalls);
            if(balls.isEmpty()){
                launching = true;
                mult = 1;
            }
            Set<Item> deadItems = new HashSet<>();
            for(Item item : items){
                item.pos = item.pos.plus(gravity.times(dt));
                if(item.pos.y > h+28){
                    deadItems.add(item);
                }
                if(item.pos.y > h-28 && item.pos.x > paddlePos && item.pos.x < paddlePos + paddleWidth){
                    deadItems.add(item);
                    item.powerup.run.accept(this);
                }
            }
            items.removeAll(deadItems);
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
        int x = (int) (aabb.min.x - 8);
        int y = (int) (aabb.min.y - 8);
        int w = (int) (aabb.max.x-aabb.min.x+8*2);
        int h = (int) (aabb.max.y-aabb.min.y+8*2);
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
        for(Ball ball : balls){
            int ballx = (int) (ball.pos.x - ball.r);
            int bally = (int) (ball.pos.y - ball.r);
            g.drawImage(ballSprite, ballx, bally, null);
        }
        if(launching){
            int paddleY = getHeight() - paddleLeft.getHeight(null);
            int paddleCenter = (int) (paddlePos + paddleWidth/2);
            g.drawImage(ballSprite, paddleCenter-8, paddleY-16, null);
        }
        for(Item item : items){
            int x = (int) (item.pos.x - item.powerup.sprite.getWidth(null)/2);
            int y = (int) (item.pos.y - item.powerup.sprite.getHeight(null)/2);
            g.drawImage(item.powerup.sprite, x, y, null);
        }
        if(game.isDebugView() && lastHit != null){
            drawHit(lastHit, g);
        }

        scoreLabel.setText("Score: " + score);

        super.paint(g);
    }

    @Override
    public Action[] getActions() {
        return null;
    }
}
