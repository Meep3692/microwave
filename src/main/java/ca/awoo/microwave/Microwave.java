package ca.awoo.microwave;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

public class Microwave extends JApplet{
    private Game game;

    @Override
    public void init() {
        System.out.println("init pl0x");
        try {
            SwingUtilities.invokeAndWait(() -> {
                // JLabel label = new JLabel("Hello applet");
                // add(label);
                this.game = new Game();
                add(game);
            });
        } catch (Exception e) {
            System.err.println("Oh no");
        }
    }

    @Override
    public void start() {
        Thread thread = new Thread(() -> {
            game.runState(new TestState());
        });
        thread.start();
    }
    
}
