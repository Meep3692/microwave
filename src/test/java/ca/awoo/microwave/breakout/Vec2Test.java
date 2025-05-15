package ca.awoo.microwave.breakout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Vec2Test {
    @Test
    public void testNormalize(){
        Vec2 initial = new Vec2(1, 4);
        Vec2 normal = initial.normalized();
        assertEquals(1.0, normal.magnitude(), 0.001);
    }

    @Test
    public void testReflect(){
        Vec2 normal = new Vec2(0, 1);
        Vec2 d = new Vec2(0, -1);
        Vec2 reflect = d.reflect(normal);
        assertEquals(0, reflect.distance(new Vec2(0, 1)), 0.001);
    }
    @Test
    public void testReflectAngle(){
        Vec2 normal = new Vec2(0, 1);
        Vec2 d = new Vec2(1, -1);
        Vec2 reflect = d.reflect(normal);
        assertEquals(0, reflect.distance(new Vec2(1, 1)), 0.001);
    }

    @Test
    public void testReflectAngleNeg(){
        Vec2 normal = new Vec2(0, -1);
        Vec2 d = new Vec2(1, 1);
        Vec2 reflect = d.reflect(normal);
        assertEquals(0, reflect.distance(new Vec2(1, -1)), 0.001);
    }

    @Test
    public void testReflectNormalAng(){
        Vec2 normal = new Vec2(1, 2).normalized();
        Vec2 d = new Vec2(0, -1);
        Vec2 reflect = d.reflect(normal);
        assertTrue(reflect.y > 0);
        assertTrue(reflect.x > 0);
    }
}
