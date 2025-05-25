package ca.awoo.microwave.hell;

public class Bullet {
    public static enum Team{
        PLAYER,
        ENEMY
    }
    public final Team team;
    public boolean dead = false;
    public Bullet(Team team) {
        this.team = team;
    }
    
}
