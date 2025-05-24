package ca.awoo.microwave.hell;

import static java.lang.Math.PI;

import java.awt.Image;

public class PawnGun implements BulletGenerator {
    private int angle = 0;
    private final Image sprite;
    
    public PawnGun(Image sprite) {
        this.sprite = sprite;
    }

    @Override
    public void gen(long entityId, Transform t, ECS ecs) {
        Transform tb = t.copy();
        if(angle == 0){
            tb.rotation = PI/4;
            angle = 1;
        }else {
            tb.rotation = PI/4*3;
            angle = 0;
        }
        ecs.addComponent(entityId, tb);
        ecs.addComponent(entityId, new Bullet(Bullet.Team.ENEMY));
        ecs.addComponent(entityId, new StraightMovement(300));
        ecs.addComponent(entityId, new Sprite(sprite, 2));
        ecs.addComponent(entityId, new Delay(1.0, () -> {
            ecs.removeEntity(entityId);
        }));
    }

    public PawnGun copy(){
        return new PawnGun(sprite);
    }
    
}
