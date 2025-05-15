package ca.awoo.microwave.breakout;

public class Hit {
    public final Vec2 location;
    public final double distance;
    public final Vec2 normal;
    public final AABB cause;
    public Hit(Vec2 location, double distance, Vec2 normal, AABB cause) {
        this.location = location;
        this.distance = distance;
        this.normal = normal;
        this.cause = cause;
    }
    @Override
    public String toString() {
        return "Hit [location=" + location + ", distance=" + distance + ", normal=" + normal + "]";
    }
    
}
