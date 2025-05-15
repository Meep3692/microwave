package ca.awoo.microwave.breakout;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class AABBTest {
    @Test
    public void diagMiss(){
        AABB aabb = new AABB(0, 0, 1, 1);
        Hit hit = aabb.castCircle(new Vec2(0, -1), new Vec2(1, -0.5), 0.25);
        assertNull(hit);
    }

    @Test
    public void downRightMiss(){
        AABB aabb = new AABB(100, 100, 200, 120);
        Hit hit = aabb.castCircle(new Vec2(220, 50), new Vec2(150, 75), 10);
        assertNull(hit);
    }
}
