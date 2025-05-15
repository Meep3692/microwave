package ca.awoo.microwave.breakout;

public class AABB {
    public final Vec2 min;
    public final Vec2 max;

    public AABB(Vec2 min, Vec2 max){
        this.min = min;
        this.max = max;
    }

    public AABB(double minx, double miny, double maxx, double maxy){
        this(new Vec2(minx, miny), new Vec2(maxx, maxy));
    }

    public Hit castCircle(Vec2 from, Vec2 to, double r){
        return castCircle(new Line(from, to), r);
    }

    public Hit castCircle(Line line, double r){
        Vec2 dir = line.b.minus(line.a);
        Hit minHit = null;
        Vec2 tl = new Vec2(min.x-r, min.y-r);
        Vec2 bl = new Vec2(min.x-r, max.y+r);
        Vec2 tr = new Vec2(max.x+r, min.y-r);
        Vec2 br = new Vec2(max.x+r, max.y+r);
        if(dir.x < 0){
            Line right = new Line(tr, br);
            Vec2 intersection = right.intersect(line);
            if(intersection != null){
                double dist = line.a.minus(intersection).magnitude();
                if(minHit == null || minHit.distance > dist){
                    minHit = new Hit(intersection, dist, new Vec2(1, 0), this);
                }
            }
        }else if(dir.x > 0){
            Line left = new Line(tl, bl);
            Vec2 intersection = left.intersect(line);
            if(intersection != null){
                double dist = line.a.minus(intersection).magnitude();
                if(minHit == null || minHit.distance > dist){
                    minHit = new Hit(intersection, dist, new Vec2(-1, 0), this);
                }
            }
        }
        if(dir.y < 0){
            Line bottom = new Line(bl, br);
            Vec2 intersection = bottom.intersect(line);
            if(intersection != null){
                double dist = line.a.minus(intersection).magnitude();
                if(minHit == null || minHit.distance > dist){
                    minHit = new Hit(intersection, dist, new Vec2(0, 1), this);
                }
            }
        }
        if(dir.y > 0){
            Line top = new Line(tl, tr);
            Vec2 intersection = top.intersect(line);
            if(intersection != null){
                double dist = line.a.minus(intersection).magnitude();
                if(minHit == null || minHit.distance > dist){
                    minHit = new Hit(intersection, dist, new Vec2(0, -1), this);
                }
            }
        }
        return minHit;
    }
}
