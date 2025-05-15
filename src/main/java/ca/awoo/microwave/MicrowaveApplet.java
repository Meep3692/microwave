package ca.awoo.microwave;

import java.awt.BorderLayout;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;

public class MicrowaveApplet extends JApplet{
    private Microwave game;

    @Override
    public void init() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                setLayout(new BorderLayout());
                this.game = new Microwave(false);
                GameView view = new GameView(game);
                add(view, BorderLayout.CENTER);
                game.onGameEnd((g) -> {
                    //In the inlikely event
                    game.start();
                });
            });
        } catch (Exception e) {
            System.err.println("Oh no");
        }
    }

    @Override
    public void start() {
        game.start();
    }
    
}
