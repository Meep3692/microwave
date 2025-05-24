package ca.awoo.microwave.hell;

import static java.lang.Math.PI;

import java.awt.Color;

import ca.awoo.microwave.Game;
import ca.awoo.microwave.breakout.Vec2;

public class Bishops implements Wave {
    private final int amount;
    private final BulletGenerator gun;
    private final PieceSprite sprite;
    
    public Bishops(Game game, int amount) {
        this.amount = amount;
        gun = new RadialGun(4, PI/4, game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Shiny/Ball_Orange_Shiny-16x16.png", Color.MAGENTA));
        sprite = new PieceSprite(game, PieceSprite.Type.BISHOP, PieceSprite.Team.WHITE, PieceSprite.Variant.MARBLE);
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
            long bishop = ecs.createEntity();
            ecs.addComponent(bishop, new Transform(pos, PI/2));
            ecs.addComponent(bishop, sprite);
            ecs.addComponent(bishop, new Shoot(0.8, gun));
            ecs.addComponent(bishop, new Enemy(30));
            ecs.addComponent(bishop, new MoveToPlayer(150, new Vec2(1, 1).normalized(), new Vec2(-1, 1).normalized(), new Vec2(1, -1).normalized(), new Vec2(-1, -1).normalized()));
        }
    }
    
}
