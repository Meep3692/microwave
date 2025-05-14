package ca.awoo.microwave;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

public class Microwave extends JApplet{
    private Game game;
    private JLabel statusLabel;

    @Override
    public void init() {
        System.out.println("init pl0x");
        try {
            SwingUtilities.invokeAndWait(() -> {
                setLayout(new BorderLayout());
                this.game = new Game();
                game.setTargetFps(60);
                add(game, BorderLayout.CENTER);
                JPanel statusPanel = new JPanel();
                statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
                add(statusPanel, BorderLayout.SOUTH);
                statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
                statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
                statusLabel = new JLabel();
                statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
                statusPanel.add(statusLabel);
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
        game.onSwingFrame((game) -> {
            statusLabel.setText(String.format("FPS: %d", (int)game.getAvgFps()));
        });
    }
    
}
