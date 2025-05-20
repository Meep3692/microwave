package ca.awoo.microwave.hell;

public class Delay implements Scheduler {
    private Runnable run;
    public double delay;

    public Delay(double delay, Runnable run){
        this.delay = delay;
        this.run = run;
    }

    @Override
    public Runnable getRun() {
        return run;
    }

    @Override
    public void setRun(Runnable r) {
        this.run = r;
    }
    
}
