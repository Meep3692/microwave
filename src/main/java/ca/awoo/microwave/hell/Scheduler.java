package ca.awoo.microwave.hell;

public interface Scheduler {
    public Runnable getRun();
    public void setRun(Runnable r);
}
