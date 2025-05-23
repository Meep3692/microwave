package ca.awoo.microwave;

import java.awt.BorderLayout;
import java.util.Optional;

import javax.swing.Action;
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
        creditsPanel.add(new Credit("Music", "Surt R.", "CC BY 4.0"));
        creditsPanel.add(new Credit("Music", "Chisech", "CC0/Public Domain"));
        creditsPanel.add(new Credit("Sound effects", "Brackeys, Asbjørn Thirslund", "CC0"));
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

    @Override
    public Action[] getActions() {
        return null;
    }
    
}
