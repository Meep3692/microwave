package ca.awoo.microwave.hell;

import static java.lang.Math.PI;

import java.awt.Color;

import ca.awoo.microwave.Game;
import ca.awoo.microwave.breakout.Vec2;

public class Queens implements Wave {
    private final int amount;
    private final BulletGenerator gun;
    private final PieceSprite sprite;
    
    public Queens(Game game, int amount) {
        this.amount = amount;
        gun = new RadialGun(8, 0, game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Shiny/Ball_Orange_Shiny-16x16.png", Color.MAGENTA));
        sprite = new PieceSprite(game, PieceSprite.Type.QUEEN, PieceSprite.Team.WHITE, PieceSprite.Variant.MARBLE);
    }
    
    @Override
    public int points() {
        return 90*amount;
    }

    @Override
    public void spawn(ECS ecs, double width, double height) {
        for(int i = 0; i < amount; i++){
            double x = width/(amount+1)*(i+1);
            Vec2 pos = new Vec2(x, -100);
            long queen = ecs.createEntity();
            ecs.addComponent(queen, new Transform(pos, PI/2));
            ecs.addComponent(queen, sprite);
            ecs.addComponent(queen, new Shoot(0.8, gun));
            ecs.addComponent(queen, new Enemy(90));
            ecs.addComponent(queen, new MoveToPlayer(150, new Vec2(1, 1).normalized(), new Vec2(-1, 1).normalized(), new Vec2(1, -1).normalized(), new Vec2(-1, -1).normalized(), new Vec2(0, 1), new Vec2(1, 0), new Vec2(0, -1), new Vec2(-1, 0)));
        }
    }
}
