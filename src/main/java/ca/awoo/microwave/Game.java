package ca.awoo.microwave;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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
    private boolean hogKeys = true;

    private Sequencer sequencer;

    private boolean muteMusic = false;
    private boolean muteSound = false;
    
    @SuppressWarnings("unchecked")
    private final Set<Integer>[] bindings = new Set[Input.getInputLength()];

    public void bindInput(int binding, int key){
        bindings[binding].add(key);
    }

    public void unbindInput(int binding, int key){
        bindings[binding].remove(key);
    }

    public Set<Integer> getBindings(int binding){
        return bindings[binding];
    }

    public void clearBinding(int binding){
        bindings[binding].clear();
    }

    public void setKeyHog(boolean hog){
        hogKeys = hog;
    }

    public Game(){
        setLayout(new GameLayout());
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.open();
        } catch (MidiUnavailableException e) {
            System.err.println("No midi sequencer avaliable, midi playback will not work");
        }
        for(int i = 0; i < clipPool.length; i++){
            try {
                Clip clip = AudioSystem.getClip();
                clipPool[i] = clip;
                clipPool[i].addLineListener(event -> {
                    if(event.getType().equals(LineEvent.Type.STOP)){
                        clip.close();
                    }
                });
            } catch (LineUnavailableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        missingImage = new BufferedImage(16, 16, BufferedImage.TYPE_3BYTE_BGR);
        Graphics missingGraphics = missingImage.getGraphics();
        missingGraphics.setColor(Color.BLACK);
        missingGraphics.fillRect(0, 0, 8, 8);
        missingGraphics.fillRect(8, 8, 8, 8);
        missingGraphics.setColor(Color.MAGENTA);
        missingGraphics.fillRect(0, 8, 8, 8);
        missingGraphics.fillRect(8, 0, 8, 8);

        for(int i = 0; i < Input.getInputLength(); i++){
            bindings[i] = new HashSet<>();
        }
        bindings[Input.UP].add(KeyEvent.VK_W);
        bindings[Input.DOWN].add(KeyEvent.VK_S);
        bindings[Input.LEFT].add(KeyEvent.VK_A);
        bindings[Input.RIGHT].add(KeyEvent.VK_D);
        bindings[Input.EXIT].add(KeyEvent.VK_ESCAPE);
        bindings[Input.FIRE].add(KeyEvent.VK_SPACE);
        bindings[Input.SHIFT].add(KeyEvent.VK_SHIFT);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if(e.getID() != KeyEvent.KEY_PRESSED && e.getID() != KeyEvent.KEY_RELEASED){
                return false;
            }
            boolean set = e.getID() == KeyEvent.KEY_PRESSED;
            synchronized(keys){
                for(int i = 0; i < Input.getInputLength(); i++){
                    if(bindings[i].contains(e.getKeyCode())){
                        keys[i] = set;
                    }
                }
            }
            return hogKeys;
        });
    }

    private final Image missingImage;
    private final Map<String, Image> imageCache = new HashMap<>();

    public Image getImage(String name){
        if(imageCache.containsKey(name)){
            return imageCache.get(name);
        }
        try {
            URL location = getClass().getResource(name);
            if(location == null){
                imageCache.put(name, missingImage);
                return missingImage;
            }
            imageCache.put(name, ImageIO.read(location));
            return imageCache.get(name);
        } catch (IOException e) {
            imageCache.put(name, missingImage);
            return missingImage;
        }
    }
    
    public void muteMusic(boolean muted){
        muteMusic = muted;
        if(muted){
            sequencer.stop();
        }else{
            sequencer.start();
        }
    }

    public void muteSound(boolean muted){
        muteSound = muted;
    }

    public Image getImageMasked(String name, Color mask){
        int maskPixel = mask.getRGB();
        Image base = getImage(name);
        int w = base.getWidth(null);
        int h = base.getHeight(null);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        img.getGraphics().drawImage(base, 0, 0, null);
        int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);
        for(int i = 0; i < pixels.length; i++){
            int pixel = pixels[i];
            if(pixel == maskPixel){
                pixels[i] = 0;
            }
        }
        img.setRGB(0, 0, w, h, pixels, 0, w);
        return img;
    }

    public Input getInput(){
        return input;
    }

    private final Map<String, Sequence> sequenceCache = new HashMap<>();

    public Sequence getSequence(String name){
        if(sequenceCache.containsKey(name)){
            return sequenceCache.get(name);
        }
        URL location = getClass().getResource(name);
        if(location == null){
            return null;
        }
        try {
            Sequence sequence = MidiSystem.getSequence(location);
            sequenceCache.put(name, sequence);
            return sequence;
        } catch (InvalidMidiDataException | IOException e) {
            //TODO: handle missing sequences better
            return null;
        }
    }

    public void playSequence(Sequence sequence) {
        if(sequencer != null){
            if(sequencer.getSequence() == sequence){
                return;
            }
            
            try {
                sequencer.stop();
                sequencer.setSequence(sequence);
                if(!muteMusic)
                    sequencer.start();
            } catch (InvalidMidiDataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private final Clip[] clipPool = new Clip[16];
    private final Map<String, byte[]> soundCache = new HashMap<>();

    public void playSound(String name){
        if(muteSound) return;
        if(!soundCache.containsKey(name)){
            byte[] bytes = new byte[1024];
            InputStream is = getClass().getResourceAsStream(name);
            int bottom = 0;
            int read = 0;
            try {
                while((read = is.read(bytes, bottom, bytes.length-bottom)) > 0){
                    bottom += read;
                    if(bottom == bytes.length){
                        bytes = Arrays.copyOf(bytes, bytes.length + 1024);
                    }
                }
                if(bottom != bytes.length){
                    bytes = Arrays.copyOf(bytes, bottom);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            soundCache.put(name, bytes);
        }
        byte[] sound = soundCache.get(name);
        Clip clip = null;
        for(int i = 0; i < clipPool.length; i++){
            if(!clipPool[i].isOpen()){
                clip = clipPool[i];
                break;
            }
        }
        if(clip == null){
            //No line avaliable
            return;
        }
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(sound));
            clip.open(stream);
            clip.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
                    input.setReleased(i, !keys[i] && input.isHeld(i));
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

    public void start(State<?> state){
        Thread thread = new Thread(() -> {
            runState(state);
            for(Consumer<Game> listener : endListeners){
                listener.accept(this);
            }
        }, "Game");
        thread.start();
    }

    private Set<Consumer<Game>> endListeners = new HashSet<>();

    public void onGameEnd(Consumer<Game> listener){
        endListeners.add(listener);
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
