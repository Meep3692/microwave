package ca.awoo.microwave.hell;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Optional;
import java.util.Random;

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
    public Hell(Game game){
        this.ecs = new ECS();
        Random random = new Random();
        Type[] types = Type.values();
        Variant[] variants = Variant.values();
        for(int i = 0; i < 16; i++){
            long entity = ecs.createEntity();
            Transform t = new Transform(new Vec2(random.nextInt(640), random.nextInt(480)));
            PieceSprite s = new PieceSprite(game, types[random.nextInt(types.length)], Team.BLACK, variants[random.nextInt(variants.length)]);
            ecs.addComponent(entity, t);
            ecs.addComponent(entity, s);
        }
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
        if(game.getInput().isPressed(Input.EXIT)){
            return Optional.of(0);
        }
        return Optional.empty();
    }
    @Override
    public boolean isTransparent() {
        return false;
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            Sprite s = (Sprite) os[1];
            int x = (int) (t.position.x - s.image.getWidth(null)/2);
            int y = (int) (t.position.y - s.image.getHeight(null)/2);
            g.drawImage(s.image, x, y, null);
        }, Transform.class, Sprite.class);
        ecs.query((e, os) -> {
            Transform t = (Transform) os[0];
            PieceSprite s = (PieceSprite) os[1];
            int x = (int) (t.position.x - 64);
            int y = (int) (t.position.y - 64);
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
            s.draw(g, dir, x, y);
            g.drawString(String.format("TRot: %.2f, Rot: %d, Dir: %s", t.rotation, rot, dir.toString()), x, y);
        }, Transform.class, PieceSprite.class);
    }
    
}
