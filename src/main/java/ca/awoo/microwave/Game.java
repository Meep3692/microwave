package ca.awoo.microwave;

import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class Game extends JComponent{
    private final Stack<State<?>> states = new Stack<>();
    private final Stack<Integer> topOpaque = new Stack<>();

    private long lastTime = -1;
    private long[] times = new long[100];
    private int timesIndex = 0;
    private long targetTime = -1;
    private long nextTime = -1;

    public Game(){
        setLayout(new GameLayout());
    }

    public <T> T runState(State<T> state){
        double ddelta = 0.016;
        states.push(state);
        if(!state.isTransparent()){
            topOpaque.push(states.size()-1);
        }
        add(state);
        Optional<T> result;
        while(!(result = state.update(this, ddelta)).isPresent()){
            try {
                SwingUtilities.invokeAndWait(() -> {
                    //God I hope this works
                    repaint();
                    for(Consumer<Game> listener : swingFrameListeners){
                        listener.accept(this);
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            long currentTime = System.nanoTime();
            if(lastTime < 0){
                lastTime = currentTime - 16000;
            }
            long deltaTime = currentTime - lastTime;
            times[timesIndex++] = deltaTime;
            if(timesIndex >= times.length){
                timesIndex = 0;
            }
            ddelta = (double)deltaTime / 1_000_000_000;
            lastTime = currentTime;

            if(targetTime > 0){
                if(nextTime < 0){
                    nextTime = currentTime;
                }
                nextTime += targetTime;
                long sleepTime = nextTime - currentTime;
                if(sleepTime >= 0){
                    try {
                        Thread.sleep(sleepTime/1_000_000, (int) (sleepTime % 1_000_000));
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }else{
                    //TDOD: do something about falling behind
                }
            }

            
        }
        states.pop();
        if(!state.isTransparent()){
            topOpaque.pop();
        }
        remove(state);
        return result.get();
    }
    @Override
    public void paint(Graphics g) {
        if(!states.empty()){
            for(int i = topOpaque.peek(); i < states.size(); i++){
                State<?> state = states.get(i);
                state.paint(g);
            }
        }
    }
    

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    public void setTargetFps(double target){
        double nanos = 1/target * 1_000_000_000;
        targetTime = (long)nanos;
    }

    private Set<Consumer<Game>> swingFrameListeners = new HashSet<>();

    public void onSwingFrame(Consumer<Game> listener){
        swingFrameListeners.add(listener);
    }

    public double getAvgFps(){
        double avgTime = 0;
        for(long time : times){
            avgTime += time;
        }
        avgTime /= times.length;
        avgTime /= 1_000_000_000;//Unit conversion
        double avgFrames = 1/avgTime;
        return avgFrames;
    }
}
