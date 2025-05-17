package ca.awoo.microwave.hell;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Optional;
import ca.awoo.microwave.Game;
import ca.awoo.microwave.Input;
import ca.awoo.microwave.State;
import ca.awoo.microwave.breakout.Vec2;
import ca.awoo.microwave.hell.PieceSprite.Direction;
import ca.awoo.microwave.hell.PieceSprite.Team;
import ca.awoo.microwave.hell.PieceSprite.Type;
import ca.awoo.microwave.hell.PieceSprite.Variant;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.PI;

public class Hell extends State<Integer>{
    private final ECS ecs;
    private final Sprite dot;
    private double renderScale = 0.5;
    public Hell(Game game){
        this.ecs = new ECS();
        dot = new Sprite(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Glossy/Ball_Red_Glossy-16x16.png", Color.MAGENTA));
        dot.layer = 1;
        //Create player
        long player = ecs.createEntity();
        ecs.addComponent(player, new Transform(new Vec2(320, 240), -PI/2));
        ecs.addComponent(player, new PieceSprite(game, Type.KNIGHT, Team.WHITE, Variant.MARBLE));
        ecs.addComponent(player, new Player());
        game.playSequence(game.getSequence("/io/itch/chisech/naranoiston/B01 - Viagem ao Setor Magenta.mid"));
    }

    
    @Override
    public Optional<Integer> update(Game game, double dt) {
        double w = getWidth()/renderScale;
        double h = getHeight()/renderScale;
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
            }
            if(input.isPressed(Input.SHIFT)){
                ecs.addComponent(e, dot);
            }
            if(input.isReleased(Input.SHIFT)){
                ecs.removeComponent(e, dot);
            }
            
            t.position = t.position.plus(dx, dy);
        }, Transform.class, PieceSprite.class, Player.class);
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            StraightMovement m = (StraightMovement) os[1];
            double deltax = cos(t.rotation)*m.speed*dt;
            double deltay = sin(t.rotation)*m.speed*dt;
            t.position = t.position.plus(deltax, deltay);
        }, Transform.class, StraightMovement.class);
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            double margin = 16;
            if(t.position.x < 0-margin || t.position.x > w+margin || t.position.y < 0-margin || t.position.y > h+margin){
                ecs.removeEntity(e);
            }
        }, Transform.class, Bullet.class);
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
            int rot = (int) ((drot * (4/Math.PI))+0.5) % 8;
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
                g.drawString(String.format("TRot: %.2f, Rot: %d, Dir: %s", t.rotation, rot, dir.toString()), x, y);
            }, s.layer);
        }, Transform.class, PieceSprite.class);
        for(Runnable r : drawStack){
            r.run();
        }
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            g.drawString("Entity: " + e, (int)(t.position.x*renderScale), (int)(t.position.y*renderScale));
        }, Transform.class);
    }
    
}
