package ca.awoo.microwave;

import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class Game extends JComponent{
    private final Stack<State<?>> states = new Stack<>();
    private final Stack<Integer> topOpaque = new Stack<>();
    public <T> T runState(State<T> state){
        states.push(state);
        if(!state.isTransparent()){
            topOpaque.push(states.size()-1);
        }
        Optional<T> result;
        while(!(result = state.update()).isPresent()){
            try {
                SwingUtilities.invokeAndWait(() -> {
                    //God I hope this works
                    repaint();
                });
            } catch (InvocationTargetException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        states.pop();
        if(!state.isTransparent()){
            topOpaque.pop();
        }
        return result.get();
    }
    @Override
    public void paint(Graphics g) {
        for(int i = topOpaque.peek(); i < states.size(); i++){
            State<?> state = states.get(i);
            state.paint(g, getWidth(), getHeight());
        }
    }
}
