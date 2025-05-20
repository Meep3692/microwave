package ca.awoo.microwave.hell;

public class BoardClear implements Scheduler {
    private Runnable run;

    public BoardClear(Runnable r){
        this.run = r;
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
