package ca.awoo.microwave.hell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Optional;
import ca.awoo.microwave.Game;
import ca.awoo.microwave.Input;
import ca.awoo.microwave.State;
import ca.awoo.microwave.breakout.Vec2;
import ca.awoo.microwave.hell.PieceSprite.Direction;
import ca.awoo.microwave.hell.PieceSprite.Team;
import ca.awoo.microwave.hell.PieceSprite.Type;
import ca.awoo.microwave.hell.PieceSprite.Variant;

public class Hell extends State<Integer>{
    private final ECS ecs;
    private final Sprite dot;
    public Hell(Game game){
        this.ecs = new ECS();
        dot = new Sprite(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Glossy/Ball_Red_Glossy-16x16.png", Color.MAGENTA));
        dot.layer = 1;
        //Create player
        long player = ecs.createEntity();
        ecs.addComponent(player, new Transform(new Vec2(320, 240)));
        ecs.addComponent(player, new PieceSprite(game, Type.KNIGHT, Team.WHITE, Variant.MARBLE));
        ecs.addComponent(player, new Player());
        game.playSequence(game.getSequence("/io/itch/chisech/naranoiston/B01 - Viagem ao Setor Magenta.mid"));
    }

    
    @Override
    public Optional<Integer> update(Game game, double dt) {
        Point mouse = getMousePosition();
        
        if(mouse != null){
            ecs.query((e, os) -> {
                Transform t = (Transform) os[0];
                double dx = mouse.getX() - t.position.x;
                double dy = mouse.getY() - t.position.y;
                double rot = Math.atan2(dy, dx);
                if(rot < 0) rot += 2*Math.PI;
                t.rotation = rot;
            }, Transform.class);
        }
        ecs.query((e, os) -> {
            Transform   t = (Transform)   os[0];
            // PieceSprite s = (PieceSprite) os[1];
            Player      p = (Player)      os[2];
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
            
            t.position = t.position.plus(new Vec2(dx, dy));
        }, Transform.class, PieceSprite.class, Player.class);
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
            int x = (int) (t.position.x - s.image.getWidth(null)/2);
            int y = (int) (t.position.y - s.image.getHeight(null)/2);
            drawStack.add(() -> {
                g.drawImage(s.image, x, y, null);
            }, s.layer);
        }, Transform.class, Sprite.class);
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            PieceSprite s = (PieceSprite) os[1];
            int x = (int) (t.position.x - 64);
            int y = (int) (t.position.y - 64 - 16);
            int rot = (int) ((t.rotation * (4/Math.PI))+0.5) % 8;
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
                s.draw(g, dir, x, y);
                g.drawString(String.format("TRot: %.2f, Rot: %d, Dir: %s", t.rotation, rot, dir.toString()), x, y);
            }, s.layer);

            for(Runnable r : drawStack){
                r.run();
            }
        }, Transform.class, PieceSprite.class);
    }
    
}
