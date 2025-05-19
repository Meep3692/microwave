package ca.awoo.microwave.hell;

public class Shoot {
    public final double rate;
    public  double coolDown = 0;
    public final int max;
    public int fired = 0;
    public final BulletGenerator bulletGenerator;
    public Shoot(double rate, int max, BulletGenerator bulletGenerator) {
        this.rate = rate;
        this.max = max;
        this.bulletGenerator = bulletGenerator;
    }

    public Shoot(double rate, BulletGenerator bulletGenerator) {
        this(rate, -1, bulletGenerator);
    }
    
}
