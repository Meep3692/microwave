package ca.awoo.microwave.hell;

import static java.lang.Math.PI;

import java.awt.Color;

import ca.awoo.microwave.Game;
import ca.awoo.microwave.breakout.Vec2;

public class Pawns implements Wave {
    private final int amount;
    private final PawnGun gun;
    private final PieceSprite sprite;
    
    public Pawns(Game game, int amount) {
        this.amount = amount;
        gun = new PawnGun(game.getImageMasked("/com/screamingbrainstudio/breakout/Balls/Shiny/Ball_Orange_Shiny-16x16.png", Color.MAGENTA));
        sprite = new PieceSprite(game, PieceSprite.Type.PAWN, PieceSprite.Team.WHITE, PieceSprite.Variant.MARBLE);
    }

    @Override
    public int points() {
        return 10*amount;
    }

    @Override
    public void spawn(ECS ecs, double width, double height) {
        for(int i = 0; i < amount; i++){
            double x = width/(amount+1)*(i+1);
            Vec2 pos = new Vec2(x, -100);
            long pawn = ecs.createEntity();
            ecs.addComponent(pawn, new Transform(pos, PI/2));
            ecs.addComponent(pawn, sprite);
            ecs.addComponent(pawn, new Shoot(0.4, gun.copy()));
            ecs.addComponent(pawn, new Enemy(10));
            ZigZag z = new ZigZag(x, 100, 100);
            z.ratio = 0.5;
            ecs.addComponent(pawn, z);
        }
        
    }
}
