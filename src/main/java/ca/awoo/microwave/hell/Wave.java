package ca.awoo.microwave.hell;

public interface Wave {
    public int points();
    public void spawn(ECS ecs, double width, double height);
}
