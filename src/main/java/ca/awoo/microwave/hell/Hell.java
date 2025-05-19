package ca.awoo.microwave.hell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Optional;
import ca.awoo.microwave.Game;
import ca.awoo.microwave.Input;
import ca.awoo.microwave.Ref;
import ca.awoo.microwave.State;
import ca.awoo.microwave.breakout.Vec2;
import ca.awoo.microwave.hell.PieceSprite.Direction;
import ca.awoo.microwave.hell.PieceSprite.Team;
import ca.awoo.microwave.hell.PieceSprite.Type;
import ca.awoo.microwave.hell.PieceSprite.Variant;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.min;

public class Hell extends State<Integer>{
    private final ECS ecs;
    private final Sprite dot;
    private double renderScale = 0.5;
    private double bgScale = 1;

    private double bgx, bgy;
    private Image bgi;

    private Runnable makeEnemy;

    private boolean debug = false;

    public Hell(Game game){
        this.ecs = new ECS();
        dot = new Sprite(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Glossy/Ball_Red_Glossy-16x16.png", Color.MAGENTA));
        dot.layer = 1;

        bgi = game.getImage("/com/screamingbrainstudio/space/Purple Nebula/Purple_Nebula_01-512x512.png");
        //Create player
        long player = ecs.createEntity();
        ecs.addComponent(player, new Transform(new Vec2(320, 240), -PI/2));
        ecs.addComponent(player, new PieceSprite(game, Type.KNIGHT, Team.BLACK, Variant.MARBLE));
        ecs.addComponent(player, new Player());
        game.playSequence(game.getSequence("/io/itch/chisech/naranoiston/B01 - Viagem ao Setor Magenta.mid"));

        makeEnemy = () -> {
            long enemy = ecs.createEntity();
            ecs.addComponent(enemy, new Transform(new Vec2(320, 50), PI/2));
            ecs.addComponent(enemy, new PieceSprite(game, Type.BISHOP, Team.WHITE, Variant.MARBLE));
            ecs.addComponent(enemy, MoveTo.loop(200, new Vec2(320, 240), new Vec2(640, 480), new Vec2(960, 240)));
            ecs.addComponent(enemy, new Shoot(0.5, (e, t, ecs) -> {
                ecs.addComponent(e, t.copy());
                ecs.addComponent(e, new Bullet(Bullet.Team.ENEMY));
                ecs.addComponent(e, new StraightMovement(300));
                ecs.addComponent(e, new Sprite(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Shiny/Ball_Orange_Shiny-16x16.png", Color.MAGENTA), 2));
            }));
            Enemy enComp = new Enemy();
            ecs.addComponent(enemy, enComp);
            ecs.onRemove(enComp, makeEnemy);
        };
        makeEnemy.run();
        
    }

    
    @Override
    public Optional<Integer> update(Game game, double dt) {
        double w = getWidth()/renderScale;
        double h = getHeight()/renderScale;
        //Player movement
        ecs.query((e, os) -> {
            Transform   t = (Transform)   os[0];
            // PieceSprite s = (PieceSprite) os[1];
            Player      p = (Player)      os[2];
            p.fireCooldown -= dt;
            // if(p.fireCooldown < 0) p.fireCooldown = 0;
            Input input = game.getInput();
            double dy = 0;
            double dx = 0;
            double speedMult = 1.0;
            if(input.isHeld(Input.SHIFT)){
                speedMult = 0.5;
            }
            if(input.isHeld(Input.UP)){
                dy -= p.speed*dt*speedMult;
            }
            if(input.isHeld(Input.DOWN)){
                dy += p.speed*dt*speedMult;
            }
            if(input.isHeld(Input.LEFT)){
                dx -= p.speed*dt*speedMult;
            }
            if(input.isHeld(Input.RIGHT)){
                dx += p.speed*dt*speedMult;
            }
            if(input.isHeld(Input.FIRE) && p.fireCooldown <= 0){
                p.fireCooldown = p.fireRate;
                long bullet = ecs.createEntity();
                ecs.addComponent(bullet, new Transform(t.position, -PI/2));
                ecs.addComponent(bullet, new Bullet(Bullet.Team.PLAYER));
                ecs.addComponent(bullet, new StraightMovement(300));
                ecs.addComponent(bullet, new Sprite(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Glass/Ball_Chrome_Glass-16x16.png", Color.MAGENTA), 2));
                Ref<Transform> enemy = new Ref<>(null);
                ecs.query((en, eos) -> {
                    Transform et = (Transform)eos[0];
                    if(enemy.contents == null || t.position.distance(et.position) < t.position.distance(enemy.contents.position)){
                        enemy.contents = et;
                    }
                }, Transform.class, Enemy.class);
                if(enemy.contents != null){
                    Homing homing = new Homing(enemy.contents);
                    ecs.addComponent(bullet, homing);
                    ecs.onRemove(enemy.contents, () -> {
                        ecs.removeComponent(bullet, homing);
                    });
                }
            }
            if(input.isPressed(Input.SHIFT)){
                ecs.addComponent(e, dot);
            }
            if(input.isReleased(Input.SHIFT)){
                ecs.removeComponent(e, dot);
            }
            
            t.position = t.position.plus(dx, dy);
        }, Transform.class, PieceSprite.class, Player.class);
        //Bullet homing
        ecs.query((e, os) -> {
            Transform t = (Transform)os[0];
            Homing home = (Homing)os[1];
            Vec2 delta = home.target.position.minus(t.position);
            double closeness = 600/delta.magnitude()+1;
            double dir = atan2(delta.y, delta.x);
            double rot = t.rotation;
            while(rot < -PI){
                rot += 2*PI;
            }
            while(rot > PI){
                rot -= 2*PI;
            }
            double diff = dir - rot;
            if(diff > PI) diff -= 2*PI;
            if(diff < -PI) diff += 2*PI;
            t.rotation += diff*dt*closeness;
        }, Transform.class, Homing.class);
        //Bullet movement
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            StraightMovement m = (StraightMovement) os[1];
            double deltax = cos(t.rotation)*m.speed*dt;
            double deltay = sin(t.rotation)*m.speed*dt;
            t.position = t.position.plus(deltax, deltay);
        }, Transform.class, StraightMovement.class);
        //Remove offscreen bullets
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            double margin = 16;
            if(t.position.x < 0-margin || t.position.x > w+margin || t.position.y < 0-margin || t.position.y > h+margin){
                ecs.removeEntity(e);
            }
        }, Transform.class, Bullet.class);
        
        //Kill player
        ecs.query((player, os) -> {
            Transform et = (Transform) os[0];
            ecs.query((bullet, bos) -> {
                Transform bt = (Transform) bos[0];
                Bullet b = (Bullet)bos[1];
                if(b.team == Bullet.Team.ENEMY && et.position.distance(bt.position) < 16){
                    game.playSound("/io/itch/brackeys/sound/hurt.wav");
                    ecs.removeEntity(bullet);
                }
            }, Transform.class, Bullet.class);
        }, Transform.class, Player.class);

        //Kill enemies
        ecs.query((enemy, os) -> {
            Transform et = (Transform) os[0];
            ecs.query((bullet, bos) -> {
                Transform bt = (Transform) bos[0];
                Bullet b = (Bullet)bos[1];
                if(b.team == Bullet.Team.PLAYER && et.position.distance(bt.position) < 32){
                    game.playSound("/io/itch/brackeys/sound/hurt.wav");
                    ecs.removeEntity(enemy);
                    ecs.removeEntity(bullet);
                }
            }, Transform.class, Bullet.class);
        }, Transform.class, Enemy.class);
        //Move enemy
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            MoveTo m = (MoveTo) os[1];
            Vec2 delta = m.dest.minus(t.position);
            double dist = delta.magnitude();
            if(dist == 0){
                ecs.removeComponent(e, m);
                if(m.then != null){
                    ecs.addComponent(e, m.then);
                }
            }else{
                Vec2 dir = delta.normalized();
                t.position = t.position.plus(dir.times(min(dt*m.speed, dist)));
            }
        }, Transform.class, MoveTo.class);
        //Enemy shoot
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            Shoot s = (Shoot) os[1];
            s.coolDown -= dt;
            if(s.coolDown <= 0){
                long bullet = ecs.createEntity();
                s.bulletGenerator.gen(bullet, t, ecs);
                s.coolDown = s.rate;
                s.fired++;
                if(s.max > 0 && s.fired >= s.max){
                    ecs.removeComponent(e, s);
                }
            }
        }, Transform.class, Shoot.class);
        
        bgy += 100*dt;

        if(game.getInput().isPressed(Input.EXIT)){
            return Optional.of(0);
        }
        return Optional.empty();
    }
    @Override
    public boolean isTransparent() {
        return false;
    }
    private final PrioritySet<Runnable> drawStack = new PrioritySet<>();
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int sw = getWidth();
        int sh = getHeight();
        int bgx = (int) (this.bgx*bgScale);
        int bgyr = (int) (this.bgy*bgScale);
        int basebgw = bgi.getWidth(null);
        int basebgh = bgi.getHeight(null);
        int bgw = (int) (basebgw*bgScale);
        int bgh = (int) (basebgh*bgScale);
        while(bgx > 0){
            bgx -= bgw;
        }
        while(bgyr > 0){
            bgyr -= bgh;
        }
        while(bgx < sw){
            int bgy = bgyr;
            while(bgy < sh){
                g.drawImage(bgi, bgx, bgy, bgw, bgh, null);
                bgy += bgh;
            }
            bgx += bgw;
        }
        drawStack.clear();
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            Sprite s = (Sprite) os[1];
            int x = (int) ((t.position.x - s.image.getWidth(null)/2)*renderScale);
            int y = (int) ((t.position.y - s.image.getHeight(null)/2)*renderScale);
            int w = (int) (s.image.getWidth(null)*renderScale);
            int h = (int) (s.image.getHeight(null)*renderScale);
            drawStack.add(() -> {
                g.drawImage(s.image, x, y, w, h, null);
            }, s.layer);
        }, Transform.class, Sprite.class);
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            PieceSprite s = (PieceSprite) os[1];
            int x = (int) ((t.position.x - 64)*renderScale);
            int y = (int) ((t.position.y - 64 - 16)*renderScale);
            double drot = t.rotation;
            while(drot < 0){
                drot += 2*PI;
            }
            while(drot > 2*PI){
                drot -= 2*PI;
            }
            int rot = (int) ((drot * (4/PI))+0.5) % 8;
            Direction dir;
            switch(rot){
                case 0:
                    dir = Direction.EAST;
                    break;
                case 1:
                    dir = Direction.SOUTH_EAST;
                    break;
                case 2:
                    dir = Direction.SOUTH;
                    break;
                case 3:
                    dir = Direction.SOUTH_WEST;
                    break;
                case 4:
                    dir = Direction.WEST;
                    break;
                case 5:
                    dir = Direction.NORTH_WEST;
                    break;
                case 6:
                    dir = Direction.NORTH;
                    break;
                case 7:
                    dir = Direction.NORTH_EAST;
                    break;
                default:
                    java.lang.System.out.println("Oh nay, wrong rot: " + rot);
                    dir = Direction.NORTH;
                    break;
            }
            drawStack.add(() -> {
                s.draw(g, dir, x, y, renderScale);
                // g.drawString(String.format("TRot: %.2f, Rot: %d, Dir: %s", t.rotation, rot, dir.toString()), x, y);
            }, s.layer);
        }, Transform.class, PieceSprite.class);
        for(Runnable r : drawStack){
            r.run();
        }
        if(debug){
            ecs.query((e, os) -> {
                Transform t = (Transform) os[0];
                int x = (int)(t.position.x*renderScale);
                int y = (int)(t.position.y*renderScale);
                int dirx = (int) (x + cos(t.rotation)*16);
                int diry = (int) (y + sin(t.rotation)*16);
                g.setColor(Color.BLACK);
                g.drawString("Entity: " + e, x, y);
                g.setColor(Color.RED);
                g.drawLine(x, y, dirx, diry);
            }, Transform.class);
    
            ecs.query((e, os) -> {
                Transform t = (Transform)os[0];
                Homing home = (Homing)os[1];
                Vec2 delta = home.target.position.minus(t.position);
                double dir = atan2(delta.y, delta.x);
                double rot = t.rotation;
                while(rot < -PI){
                    rot += 2*PI;
                }
                while(rot > PI){
                    rot -= 2*PI;
                }
                double diff = dir - rot;
                if(diff > PI) diff -= 2*PI;
                if(diff < -PI) diff += 2*PI;
    
                int x = (int)(t.position.x*renderScale);
                int y = (int)(t.position.y*renderScale);
                int dirx = (int) (x + cos(dir)*16);
                int diry = (int) (y + sin(dir)*16);
    
                int rotx = (int) (x + cos(rot)*16);
                int roty = (int) (y + sin(rot)*16);
    
                int diffx = (int) (x + cos(diff*0.5+rot)*16);
                int diffy = (int) (y + sin(diff*0.5+rot)*16);
    
                g.setColor(Color.GREEN);
                g.drawLine(x, y, dirx, diry);
                g.setColor(Color.RED);
                g.drawLine(x, y, rotx, roty);
                g.setColor(Color.BLUE);
                g.drawLine(x, y, diffx, diffy);
            }, Transform.class, Homing.class);
        }
    }
    
}
