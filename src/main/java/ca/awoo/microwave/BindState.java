package ca.awoo.microwave;

import java.awt.BorderLayout;
import java.util.Optional;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class BindState extends State<Integer>{

    public BindState(Game game){
        setLayout(new BorderLayout());
        JPanel bindPanel = new JPanel();
        bindPanel.setDoubleBuffered(false);
        bindPanel.setLayout(new BoxLayout(bindPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(bindPanel);
        scrollPane.setDoubleBuffered(false);
        add(scrollPane, BorderLayout.CENTER);
        for(int i = 0; i < Input.getInputLength(); i++){
            Set<Integer> bindings = game.getBindings(i);
            BindControl control = new BindControl(game, i, bindings);
            bindPanel.add(control);
        }
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
