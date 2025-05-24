package ca.awoo.microwave.hell;

import static java.lang.Math.PI;

import java.awt.Image;

public class RadialGun implements BulletGenerator {
    private final int amount;
    private final double offset;
    private final Image sprite;

    public RadialGun(int amount, double offset, Image sprite) {
        this.amount = amount;
        this.offset = offset;
        this.sprite = sprite;
    }


    @Override
    public void gen(long entityId, Transform t, ECS ecs) {
        for(int i = 0; i < amount; i++){
            double rot = ((2*PI)/amount)*i;
            rot += offset;
            long e = ecs.createEntity();
            Transform tb = t.copy();
            tb.rotation = rot;
            ecs.addComponent(e, tb);
            ecs.addComponent(e, new Bullet(Bullet.Team.ENEMY));
            ecs.addComponent(e, new StraightMovement(300));
            ecs.addComponent(e, new Sprite(sprite, 2));
        }
    }
    
}
