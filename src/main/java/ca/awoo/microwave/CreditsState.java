package ca.awoo.microwave;

import java.awt.BorderLayout;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CreditsState extends State<Integer>{

    public CreditsState(){
        setLayout(new BorderLayout());
        JPanel creditsPanel = new JPanel();
        creditsPanel.setDoubleBuffered(false);
        creditsPanel.setLayout(new BoxLayout(creditsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(creditsPanel);
        scrollPane.setDoubleBuffered(false);
        add(scrollPane, BorderLayout.CENTER);
        // scrollPane.add(creditsPanel);
        creditsPanel.add(new Credit("Textures", "Screaming Brain Studios", "CC0/Public Domain"));
        add(new JLabel("Press ESC to exit"), BorderLayout.SOUTH);
    }

    @Override
    public Optional<Integer> update(Game game, double dt) {
        if(game.getInput().isPressed(Input.EXIT)){
            return Optional.of(0);
        }
        return Optional.empty();
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
    
}
