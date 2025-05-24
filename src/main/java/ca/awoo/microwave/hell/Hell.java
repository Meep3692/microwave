package ca.awoo.microwave.hell;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.Optional;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.Action;
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
    private final Game game;
    private final ECS ecs;
    private final Sprite dot;
    private double renderScale = 0.5;
    private double bgScale = 1;

    private double bgx, bgy;
    private Image bgi;

    private Random random = new Random();

    private int uiWidth = 148;
    private int score = 0;
    private int lives = 5;
    private Image[] scoreDigits = new Image[10];
    private final PieceSprite life;

    public Hell(Game game){
        this.game = game;
        this.ecs = new ECS();
        setSize(640, 480);
        dot = new Sprite(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Glossy/Ball_Red_Glossy-16x16.png", Color.MAGENTA));
        dot.layer = 1;

        bgi = game.getImage("/com/screamingbrainstudio/space/Purple Nebula/Purple_Nebula_01-512x512.png");

        for(int i = 0; i < 10; i++){
            scoreDigits[i] = game.getImage("/ca/awoo/microwave/numbers/" + i + ".png");
        }

        life = new PieceSprite(game, Type.KNIGHT, Team.BLACK, Variant.MARBLE);
        //Create player
        long player = ecs.createEntity();
        Transform pt = new Transform(new Vec2(640-uiWidth/2, 480), -PI/2);
        ecs.addComponent(player, pt);
        ecs.addComponent(player, new Delay(0.5, () -> {
            pt.position = new Vec2(scaledWidth()/2, scaledHeight()/2);
        }));
        ecs.addComponent(player, new PieceSprite(game, Type.KNIGHT, Team.BLACK, Variant.MARBLE));
        ecs.addComponent(player, new Player());
        ecs.addComponent(player, new Shoot(0.2, (e, t, ecs) -> {
            ecs.addComponent(e, t.copy());
            ecs.addComponent(e, new Bullet(Bullet.Team.PLAYER));
            ecs.addComponent(e, new StraightMovement(300));
            ecs.addComponent(e, new Sprite(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Glass/Ball_Chrome_Glass-16x16.png", Color.MAGENTA), 2));
        }));


        game.playSequence(game.getSequence("/io/itch/chisech/naranoiston/B01 - Viagem ao Setor Magenta.mid"));
        Wave[] waves = new Wave[]{
            new Pawns(game, 8),
            new Rooks(game, 2),
            new Knights(game, 2),
            new Bishops(game, 2),
            new Queens(game, 1),
        };
        Scheduler[] sched = new Scheduler[waves.length+2];
        sched[0] = new Delay(1.0, ()->{});
        for(int i = 0; i < waves.length; i++){
            final int index = i;
            sched[i+1] = new BoardClear(() -> {waves[index].spawn(ecs, scaledWidth(), scaledHeight());});
        }
        sched[waves.length+1] = new BoardClear(() -> {
            long director = ecs.createEntity();
            ecs.addComponent(director, new Director(100, 1.25,
                new Pawns(game, 8),
                new Rooks(game, 2),
                new Knights(game, 2),
                new Bishops(game, 2),
                new Queens(game, 1)
            ));
        });
        sequence(sched);
    }

    private double scaledWidth(){
        return (getWidth()-uiWidth)/renderScale;
    }
    private double scaledHeight(){
        return getHeight()/renderScale;
    }

    private void particle(Vec2 pos, Image image){
        double rot = random.nextDouble()*2*PI;
        Vec2 dest = new Vec2(cos(rot), sin(rot)).times(random.nextDouble()*100).plus(pos);
        long entity = ecs.createEntity();
        ecs.addComponent(entity, new Transform(pos, rot));
        ecs.addComponent(entity, new MoveTo(dest, (random.nextDouble()+1)*100));
        ecs.addComponent(entity, new Speen((random.nextDouble()-0.5)*20));
        ecs.addComponent(entity, new Sprite(image));
        ecs.addComponent(entity, new Delay(random.nextDouble(), () -> {
            ecs.removeEntity(entity);
        }));
    }

    private void particles(Vec2 pos, Image image, int c){
        for(int i = 0; i < c; i++){
            particle(pos, image);
        }
    }

    private void sequence(Scheduler... items){
        long entity = ecs.createEntity();
        for(int i = 0; i < items.length - 1; i++){
            Runnable run = items[i].getRun();
            final int j = i;
            Runnable newRun = () -> {
                run.run();
                ecs.addComponent(entity, items[j+1]);
            };
            items[i].setRun(newRun);
        }
        ecs.addComponent(entity, items[0]);
    }

    
    @Override
    public Optional<Integer> update(Game game, double dt) {
        double w = scaledWidth();
        double h = scaledHeight();
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
            if(input.isPressed(Input.SHIFT)){
                ecs.addComponent(e, dot);
            }
            if(input.isReleased(Input.SHIFT)){
                ecs.removeComponent(e, dot);
            }
            double newx = t.position.x + dx;
            double newy = t.position.y + dy;
            if(newx < 0){
                newx = 0;
            }
            if(newy < 0){
                newy = 0;
            }
            if(newx > w){
                newx = w;
            }
            if(newy > h){
                newy = h;
            }
            t.position = new Vec2(newx, newy);
        }, Transform.class, PieceSprite.class, Player.class);
        //Player shoot
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            Shoot s = (Shoot) os[1];
            Input input = game.getInput();
            s.coolDown -= dt;
            if(input.isHeld(Input.FIRE) && s.coolDown <= 0){
                long bullet = ecs.createEntity();
                s.bulletGenerator.gen(bullet, t, ecs);
                s.coolDown = s.rate;
                s.fired++;
                if(s.max > 0 && s.fired >= s.max){
                    ecs.removeComponent(e, s);
                }
            }
        }, Transform.class, Shoot.class, Player.class);
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
            Player p = (Player) os[1];
            if(p.iframes > 0){
                p.iframes -= dt;
            }else{
                ecs.query((bullet, bos) -> {
                    Transform bt = (Transform) bos[0];
                    Bullet b = (Bullet)bos[1];
                    if(b.team == Bullet.Team.ENEMY && et.position.distance(bt.position) < 16){
                        game.playSound("/io/itch/brackeys/sound/explosion.wav");
                        ecs.removeEntity(bullet);
                        particles(et.position, game.getImage("/ca/awoo/microwave/hell/card_small_blue.png"), 10);
                        p.iframes = 2.0;
                        lives--;
                    }
                }, Transform.class, Bullet.class);
            }
        }, Transform.class, Player.class);

        //Flash player
        ecs.query((e, os) -> {
            Player p = (Player) os[0];
            PieceSprite s = (PieceSprite) os[1];
            if(p.iframes > 0){
                s.visible = p.iframes%0.2<0.1;
            }else{
                s.visible = true;
            }
        }, Player.class, PieceSprite.class);

        //Kill enemies
        ecs.query((enemy, os) -> {
            Transform et = (Transform) os[0];
            Enemy en = (Enemy) os[1];
            ecs.query((bullet, bos) -> {
                Transform bt = (Transform) bos[0];
                Bullet b = (Bullet)bos[1];
                if(b.team == Bullet.Team.PLAYER && et.position.distance(bt.position) < 32){
                    game.playSound("/io/itch/brackeys/sound/hurt.wav");
                    ecs.removeEntity(enemy);
                    ecs.removeEntity(bullet);
                    particles(et.position, game.getImage("/ca/awoo/microwave/hell/card_small_red.png"), 10);
                    score += en.points;
                }
            }, Transform.class, Bullet.class);
        }, Transform.class, Enemy.class);
        //Move enemy
        //MoveTo
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
                double rot = atan2(dir.y, dir.x);
                t.position = t.position.plus(dir.times(min(dt*m.speed, dist)));
                t.rotation = rot;
            }
        }, Transform.class, MoveTo.class);

        //ZigZag
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            ZigZag z = (ZigZag) os[1];
            double newy = t.position.y + z.speed*dt*z.ratio;
            double newx;
            if(z.right){
                newx = t.position.x + z.speed*dt;
            }else{
                newx = t.position.x - z.speed*dt;
            }
            if(newx > z.center+z.width){
                z.right = false;
            }else if(newx < z.center-z.width){
                z.right = true;
            }
            if(newy > h+100){
                newy = -100;
            }
            t.position = new Vec2(newx, newy);
        }, Transform.class, ZigZag.class);
        //Orbit
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            Orbit o = (Orbit) os[1];
            o.angle += dt*o.speed;
            Vec2 pos = new Vec2(cos(o.angle), sin(o.angle)).times(o.distance).plus(o.target.position);
            t.position = pos;
        }, Transform.class, Orbit.class);
        //MoveToPlayer
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            MoveToPlayer mtp = (MoveToPlayer) os[1];
            Ref<Transform> rp = new Ref<>(null);
            ecs.query((e2, os2) -> {
                rp.contents = (Transform) os2[0];
            }, Transform.class, Player.class);
            Transform pt = rp.contents;
            if(pt == null) return;
            Vec2 diff = pt.position.minus(t.position);
            Vec2 min = mtp.dirs[0];
            double dist = 10000;
            for(int i = 0; i < mtp.dirs.length; i++){
                Vec2 dir = mtp.dirs[i];
                double thisDist = dir.dot(diff);
                if(thisDist < dist){
                    dist = thisDist;
                    min = dir;
                }
            }
            ecs.addComponent(e, new MoveTo(t.position.plus(min.times(dist)), mtp.speed, mtp));
            ecs.removeComponent(e, mtp);
        }, Transform.class, MoveToPlayer.class);
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
        }, Transform.class, Shoot.class, Enemy.class);
        
        //Speen
        ecs.query((e, os) -> {
            Transform t = (Transform)os[0];
            Speen s = (Speen)os[1];
            t.rotation += s.v*dt;
        }, Transform.class, Speen.class);

        //Delay
        ecs.query((e, os) -> {
            Delay d = (Delay)os[0];
            d.delay -= dt;
            if(d.delay <= 0){
                d.getRun().run();
                ecs.removeComponent(e, d);
            }
        }, Delay.class);

        //Board clear
        Ref<Boolean> enemiesLeft = new Ref<Boolean>(false);
        ecs.query((e, os) -> {
            enemiesLeft.contents = true;
        }, Enemy.class);
        if(!enemiesLeft.contents){
            ecs.query((e, os) -> {
                BoardClear b = (BoardClear)os[0];
                b.getRun().run();
                ecs.removeComponent(e, b);
            }, BoardClear.class);
        }

        //Director
        if(!enemiesLeft.contents){
            ecs.query((e, os) -> {
                Director director = (Director)os[0];
                double sofar = 0;
                while(sofar < director.target){
                    int i = random.nextInt(director.waves.length);
                    Wave wave = director.waves[i];
                    sofar += wave.points();
                    wave.spawn(ecs, scaledWidth(), scaledHeight());
                }
                director.target *= director.mult;
            }, Director.class);
        }

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

    private void textureArea(Graphics g, int x, int y, int x2, int y2, Image image){
        int by = y;
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
        while(x < x2){
            y = by;
            while(y < y2){
                int rx = x2-x;
                int ry = y2-y;
                int dw = min(rx, iw);
                int dh = min(ry, ih);
                g.drawImage(image, x, y, x+dw, y+dh, 0, 0, dw, dh, null);
                y+=ih;
            }
            x+=iw;
        }
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
                Graphics2D g2d = (Graphics2D)g;
                AffineTransform orig = g2d.getTransform();
                AffineTransform a = AffineTransform.getRotateInstance(t.rotation, x+w/2, y+h/2);
                g2d.setTransform(a);
                g.drawImage(s.image, x, y, w, h, null);
                g2d.setTransform(orig);
            }, s.layer);
        }, Transform.class, Sprite.class);
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            PieceSprite s = (PieceSprite) os[1];
            if(!s.visible) return;
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
        //UI
        int uisx = getWidth()-uiWidth;
        int uisy = 0;
        textureArea(g, uisx, uisy, getWidth(), getHeight(), game.getImage("/com/screamingbrainstudio/basematerials/Stone/Mat_Stone_Black_02-128x128.png"));
        Font uiFont = new Font("monospace", Font.BOLD, 32);
        g.setFont(uiFont);
        g.setColor(Color.WHITE);
        g.drawString("Score", uisx+8, uisy+32);
        int digits = 10;
        int sx = uisx+8;
        int sy = uisy+40;
        g.draw3DRect(sx, sy, 13*digits+1, 22, getFocusTraversalKeysEnabled());
        for(int i = 0; i < digits; i++){
            int mag = (int) Math.pow(10, i);
            int digit = (score/mag)%10;
            int dx = sx+13*digits-13*(i+1);
            g.drawImage(scoreDigits[digit], dx+1, sy+1, null);
        }

        int ly = uisy+40+22+8;
        int lx = sx;
        int lw = uiWidth-16;
        for(int i = 0; i < lives; i++){
            double split = lw/((double)lives+1.0)*(i+1);
            int x = (int) (lx + split);
            life.draw(g, Direction.EAST, (int)(x - 64*renderScale), ly, renderScale);
        }

        //Debug
        if(game.isDebugView()){
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


    @Override
    public Action[] getActions() {
        return new Action[]{
            new AbstractAction("Spawn Pawn") {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    new Pawns(game, 1).spawn(ecs, scaledWidth(), scaledHeight());
                }
            },
            new AbstractAction("Spawn Rook") {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    new Rooks(game, 1).spawn(ecs, scaledWidth(), scaledHeight());
                }
            },
            new AbstractAction("Spawn Knight") {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    new Knights(game, 1).spawn(ecs, scaledWidth(), scaledHeight());
                }
            },
            new AbstractAction("Spawn Bishop") {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    new Bishops(game, 1).spawn(ecs, scaledWidth(), scaledHeight());
                }
            },
            new AbstractAction("Spawn Queen") {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    new Queens(game, 1).spawn(ecs, scaledWidth(), scaledHeight());
                }
            }
        };
    }
    
}
