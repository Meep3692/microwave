package ca.awoo.microwave.breakout;

public class Line {
    public final Vec2 a;
    public final Vec2 b;
    public Line(Vec2 a, Vec2 b) {
        this.a = a;
        this.b = b;
    }

    public Line(double x1, double y1, double x2, double y2){
        this(new Vec2(x1, y1), new Vec2(x2, y2));
    }
    
    public Vec2 intersect(Line other){
        Vec2 c = other.a;
        Vec2 d = other.b;
        double t = ((a.x-c.x)*(c.y-d.y)-(a.y-c.y)*(c.x-d.x))/((a.x-b.x)*(c.y-d.y)-(a.y-b.y)*(c.x-d.x));
        if(t < 0 || t > 1){
            return null;
        }
        double u = -((a.x-b.x)*(a.y-c.y)-(a.y-b.y)*(a.x-c.x))/((a.x-b.x)*(c.y-d.y)-(a.y-b.y)*(c.x-d.x));
        if(u < 0 || u > 1){
            return null;
        }
        double x = a.x+t*(b.x-a.x);
        double y = a.y+t*(b.y-a.y);
        return new Vec2(x, y);
    }

    @Override
    public String toString() {
        return "Line [a=" + a + ", b=" + b + "]";
    }
    
}
