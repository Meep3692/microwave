package ca.awoo.microwave.hell;

import java.awt.Image;

public class KnightGun implements BulletGenerator{
    private final Image sprite;
    
    public KnightGun(Image sprite) {
        this.sprite = sprite;
    }

    @Override
    public void gen(long entityId, Transform t, ECS ecs) {
        ecs.addComponent(entityId, t.copy());
        ecs.addComponent(entityId, new Bullet(Bullet.Team.ENEMY));
        ecs.addComponent(entityId, new StraightMovement(300));
        ecs.addComponent(entityId, new Sprite(sprite, 2));
    }

}
