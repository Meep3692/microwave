package ca.awoo.microwave.hell;

public interface ECSSystem {
    public void run(long entity, Object[] components);
}
