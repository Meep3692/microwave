package ca.awoo.microwave.breakout;

public class Vec2 {
    public final double x;
    public final double y;

    public Vec2(double x, double y){
        this.x = x;
        this.y = y;
    }

    public Vec2(Vec2 other){
        this.x = other.x;
        this.y = other.y;
    }

    public Vec2 plus(Vec2 other){
        return new Vec2(x + other.x, y + other.y);
    }

    public Vec2 plus(double x, double y){
        return new Vec2(this.x + x, this.y + y);
    }

    public Vec2 minus(Vec2 other){
        return new Vec2(x - other.x, y - other.y);
    }

    public Vec2 normalized(){
        double m = magnitude();
        return new Vec2(x/m, y/m);
    }

    public Vec2 times(double scalar){
        return new Vec2(x*scalar, y*scalar);
    }

    public double dot(Vec2 other){
        return x*other.x + y*other.y;
    }

    public double magnitude(){
        return Math.sqrt(x*x+y*y);
    }

    public Vec2 reflect(Vec2 normal){
        return this.minus(normal.times(2*this.dot(normal)));
    }

    public double distance(Vec2 other){
        double dx = x-other.x;
        double dy = y-other.y;
        return Math.sqrt(dx*dx+dy*dy);
    }

    @Override
    public String toString() {
        return "<" + x + ", " + y + ">";
    }
    
}
