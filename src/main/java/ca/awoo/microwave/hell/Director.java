package ca.awoo.microwave.hell;

public class Director {
    public Wave[] waves;
    public double target;
    public double mult;
    public double fullSpawnTime;
    public Director(double target, double mult, Wave... waves){
        this.waves = waves;
        this.target = target;
        this.mult = mult;
    }
}
