package ca.awoo.microwave.hell;

import static java.lang.Math.PI;

import java.awt.Color;

import ca.awoo.microwave.Game;
import ca.awoo.microwave.breakout.Vec2;

public class Rooks implements Wave {
    private final int amount;
    private final BulletGenerator gun;
    private final PieceSprite sprite;
    
    public Rooks(Game game, int amount) {
        this.amount = amount;
        gun = new RadialGun(4, 0, game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Shiny/Ball_Orange_Shiny-16x16.png", Color.MAGENTA));
        sprite = new PieceSprite(game, PieceSprite.Type.ROOK, PieceSprite.Team.WHITE, PieceSprite.Variant.MARBLE);
    }
    
    @Override
    public int points() {
        return 50*amount;
    }

    @Override
    public void spawn(ECS ecs, double width, double height) {
        for(int i = 0; i < amount; i++){
            double x = width/(amount+1)*(i+1);
            Vec2 pos = new Vec2(x, -100);
            long rook = ecs.createEntity();
            ecs.addComponent(rook, new Transform(pos, PI/2));
            ecs.addComponent(rook, sprite);
            ecs.addComponent(rook, new Shoot(0.8, gun));
            ecs.addComponent(rook, new Enemy(50));
            ecs.addComponent(rook, new MoveToPlayer(150, new Vec2(0, 1), new Vec2(1, 0), new Vec2(0, -1), new Vec2(-1, 0)));
        }
    }
    
}
