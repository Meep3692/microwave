package ca.awoo.microwave;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
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

    private boolean[] keys = new boolean[Input.getInputLength()];
    private final Input input = new Input();

    public Game(){
        setLayout(new GameLayout());
        missingImage = new BufferedImage(16, 16, BufferedImage.TYPE_3BYTE_BGR);
        Graphics missingGraphics = missingImage.getGraphics();
        missingGraphics.setColor(Color.BLACK);
        missingGraphics.fillRect(0, 0, 8, 8);
        missingGraphics.fillRect(8, 8, 8, 8);
        missingGraphics.setColor(Color.MAGENTA);
        missingGraphics.fillRect(0, 8, 8, 8);
        missingGraphics.fillRect(0, 8, 8, 8);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if(e.getID() != KeyEvent.KEY_PRESSED && e.getID() != KeyEvent.KEY_RELEASED){
                return false;
            }
            boolean set = e.getID() == KeyEvent.KEY_PRESSED;
            synchronized(keys){
                switch(e.getKeyCode()){
                    case KeyEvent.VK_W:
                        keys[Input.UP] = set;
                        break;
                    case KeyEvent.VK_S:
                        keys[Input.DOWN] = set;
                        break;
                    case KeyEvent.VK_A:
                        keys[Input.LEFT] = set;
                        break;
                    case KeyEvent.VK_D:
                        keys[Input.RIGHT] = set;
                        break;
                    case KeyEvent.VK_ESCAPE:
                        keys[Input.EXIT] = set;
                        break;
                }
            }
            return false;
        });
    }

    private final Image missingImage;

    public Image getImage(String name){
        try {
            URL location = getClass().getResource(name);
            if(location == null){
                return missingImage;
            }
            return ImageIO.read(location);
        } catch (IOException e) {
            return missingImage;
        }
    }

    public Input getInput(){
        return input;
    }

    private void clearPressed(){
        for(int i = 0; i < Input.getInputLength(); i++){
            input.setPressed(i, false);
        }
    }

    public <T> T runState(State<T> state){
        double ddelta = 0.016;
        if(!states.isEmpty()){
            State<?> lastState = states.peek();
            lastState.setEnabled(false);
            remove(lastState);
        }
        states.push(state);
        if(!state.isTransparent()){
            topOpaque.push(states.size()-1);
        }
        add(state);
        Optional<T> result;
        clearPressed();
        while(!(result = state.update(this, ddelta)).isPresent()){
            //Input
            synchronized(keys){
                for(int i = 0; i < Input.getInputLength(); i++){
                    input.setPressed(i, keys[i] && !input.isHeld(i));
                    input.setHeld(i, keys[i]);
                }
            }
            //Render
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

            //Timing
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
        if(!states.isEmpty()){
            State<?> lastState = states.peek();
            lastState.setEnabled(true);
            add(lastState);
        }
        clearPressed();
        return result.get();
    }

    @Override
    public void paint(Graphics g) {
        if(!states.empty()){
            for(int i = topOpaque.peek(); i < states.size(); i++){
                State<?> state = states.get(i);
                state.setSize(getSize());
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
