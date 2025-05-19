package ca.awoo.microwave.hell;

public class Orbit {
    public final Transform target;
    public double distance;
    public double angle;
    public double speed;
    public Orbit(Transform target, double distance, double angle, double speed) {
        this.target = target;
        this.distance = distance;
        this.angle = angle;
        this.speed = speed;
    }
    
}
