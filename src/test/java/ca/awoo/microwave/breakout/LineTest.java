package ca.awoo.microwave.breakout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LineTest {
    @Test
    public void doesIntersect(){
        Line line1 = new Line(new Vec2(-1, 0), new Vec2(1, 0));
        Line line2 = new Line(new Vec2(0, -1), new Vec2(0, 1));
        Vec2 intersect = line1.intersect(line2);
        assertNotNull(intersect);
        assertEquals(0.0, intersect.x, 0.01);
        assertEquals(0.0, intersect.y, 0.01);
    }

    @Test
    public void doesNotIntersect(){
        Line line1 = new Line(new Vec2(-1, 0), new Vec2(-0.5, 0));
        Line line2 = new Line(new Vec2(0, -1), new Vec2(0, 1));
        Vec2 intersect = line1.intersect(line2);
        assertNull(intersect);
    }

    @Test
    public void parallel(){
        Line line1 = new Line(new Vec2(-1, -1), new Vec2(1, -1));
        Line line2 = new Line(new Vec2(-1, 1), new Vec2(1, 1));
        Vec2 intersect = line1.intersect(line2);
        assertNull(intersect);
    }

    @Test
    public void vertMiss(){
        Line line1 = new Line(0, 0, 0, 1);
        Line line2 = new Line(1, 2, -1, 1);
        Vec2 intersect = line1.intersect(line2);
        assertNull(intersect);
    }

    @Test
    public void rightSide(){
        double r = 10;
        Line line = new Line(220, 50, 150, 75);
        AABB aabb = new AABB(100, 100, 200, 120);
        Vec2 tr = new Vec2(aabb.max.x+r, aabb.min.y-r);
        Vec2 br = new Vec2(aabb.max.x+r, aabb.max.y+r);
        Line right = new Line(tr, br);
        Vec2 intersection = right.intersect(line);
        assertNull(intersection);
    }

    @Test
    public void rightSide2(){
        double r = 10;
        Line line = new Line(220, 50, 150, 75);
        AABB aabb = new AABB(100, 100, 200, 120);
        Vec2 tr = new Vec2(aabb.max.x+r, aabb.min.y-r);
        Vec2 br = new Vec2(aabb.max.x+r, aabb.max.y+r);
        Line right = new Line(tr, br);
        Vec2 a = right.a;
        Vec2 b = right.b;
        Vec2 c = line.a;
        Vec2 d = line.b;
        double t = ((a.x-c.x)*(c.y-d.y)-(a.y-c.y)*(c.x-d.x))/((a.x-b.x)*(c.y-d.y)-(a.y-b.y)*(c.x-d.x));
        double u = -((a.x-b.x)*(a.y-c.y)-(a.y-b.y)*(a.x-c.x))/((a.x-b.x)*(c.y-d.y)-(a.y-b.y)*(c.x-d.x));
        assertTrue(t < 0 || t > 1 || u < 0 || u > 1);
    }
}
