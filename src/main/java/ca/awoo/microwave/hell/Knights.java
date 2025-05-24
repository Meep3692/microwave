package ca.awoo.microwave.hell;

import static java.lang.Math.PI;
import static java.lang.Math.signum;

import java.awt.Color;

import ca.awoo.microwave.Game;
import ca.awoo.microwave.breakout.Vec2;

public class Knights implements Wave {
    private final int amount;
    private final BulletGenerator gun;
    private final PieceSprite sprite;
    
    public Knights(Game game, int amount) {
        this.amount = amount;
        gun = new KnightGun(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Shiny/Ball_Orange_Shiny-16x16.png", Color.MAGENTA));
        sprite = new PieceSprite(game, PieceSprite.Type.KNIGHT, PieceSprite.Team.WHITE, PieceSprite.Variant.MARBLE);
    }
    @Override
    public int points() {
        return 30*amount;
    }

    @Override
    public void spawn(ECS ecs, double width, double height) {
        for(int i = 0; i < amount; i++){
            double x = width/(amount+1)*(i+1);
            Vec2 pos = new Vec2(x, -100);
            long knight = ecs.createEntity();
            ecs.addComponent(knight, new Transform(pos, PI/2));
            ecs.addComponent(knight, sprite);
            ecs.addComponent(knight, new Shoot(0.2, gun));
            ecs.addComponent(knight, new Enemy(30));
            MoveTo root = new MoveTo(new Vec2(x, 100), 150);
            double y = 100;
            double side = signum(x - width/2);
            MoveTo last = root;
            while(y < height+100){
                MoveTo next = new MoveTo(new Vec2(x+200*side, y), 150);
                last.then = next;
                last = next;
                next = new MoveTo(new Vec2(x+200*side, y+100), 150);
                last.then = next;
                last = next;
                next = new MoveTo(new Vec2(x, y+100), 150);
                last.then = next;
                last = next;
                next = new MoveTo(new Vec2(x, y+200), 150);
                last.then = next;
                last = next;
                y += 200;
            }
            MoveTo reset = new MoveTo(new Vec2(x, -100), Double.POSITIVE_INFINITY);
            last.then = reset;
            reset.then = root;
            ecs.addComponent(knight, root);
        }
    }
    
}
