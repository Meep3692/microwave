package ca.awoo.microwave;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class GameView extends JPanel{
    public GameView(Game game){
        setLayout(new BorderLayout());
        add(game, BorderLayout.CENTER);
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);
        game.onSwingFrame((g) -> {
            statusLabel.setText(String.format("FPS: %d", (int)g.getAvgFps()));
        });
        statusPanel.add(Box.createHorizontalGlue());
        JToggleButton musicMute = new JToggleButton("Mute Music");
        musicMute.addActionListener((e) -> {
            game.muteMusic(musicMute.isSelected());
        });
        statusPanel.add(musicMute);
        JToggleButton soundMute = new JToggleButton("Mute Sound");
        soundMute.addActionListener((e) -> {
            game.muteSound(soundMute.isSelected());
        });
        statusPanel.add(soundMute);
    }
}
