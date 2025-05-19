package ca.awoo.microwave;

import java.util.Optional;

import javax.swing.Action;
import javax.swing.JComponent;

public abstract class State<Result> extends JComponent {
    public abstract Optional<Result> update(Game game, double dt);
    public abstract boolean isTransparent();
    public abstract Action[] getActions();

    public State(){
        setOpaque(!isTransparent());
    }
}
