package ca.awoo.microwave.hell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.awoo.microwave.Game;
import ca.awoo.microwave.Input;
import ca.awoo.microwave.State;

public class GameOverState extends State<GameOverState.Result> {
    public static enum Result{
        EXIT,
        CONTINUE
    }

    public GameOverState(Game game){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setDoubleBuffered(false);
        titlePanel.setLayout(null);

        String text = "GAME OVER";

        JLabel titleLabel = new JLabel(text);
        Font font = titleLabel.getFont();
        font = font.deriveFont(4.0f*font.getSize2D()).deriveFont(Font.BOLD);
        titleLabel.setFont(font);
        titleLabel.setForeground(Color.WHITE);
        
        JLabel titleShadow = new JLabel(text);
        titleShadow.setFont(font);
        titleShadow.setForeground(Color.RED);
        titlePanel.add(titleLabel);
        titlePanel.add(titleShadow);
        Dimension labelSize = titleLabel.getPreferredSize();
        titleLabel.setBounds(0, 0, labelSize.width, labelSize.height);
        titleShadow.setBounds(3, 3, labelSize.width, labelSize.height);
        add(titlePanel);
        titlePanel.setMinimumSize(new Dimension(labelSize.width+5, labelSize.height+5));
        titlePanel.setSize(new Dimension(labelSize.width+5, labelSize.height+5));
        titlePanel.setPreferredSize(new Dimension(labelSize.width+5, labelSize.height+5));
        titlePanel.setAlignmentX(0.0f);

        JLabel instructions = new JLabel("Press fire to continue, press exit to exit");
        Font instFont = instructions.getFont();
        instFont = instFont.deriveFont(2.0f*instFont.getSize2D()).deriveFont(Font.BOLD);
        instructions.setFont(instFont);
        instructions.setForeground(Color.WHITE);
        instructions.setAlignmentX(0.0f);
        add(instructions);
        game.playSequence(game.getSequence("/io/itch/chisech/naranoiston/B04 - Conclusão Tempestuosa na Terra.mid"));
    }

    @Override
    public Optional<Result> update(Game game, double dt) {
        Input input = game.getInput();
        if(input.isPressed(Input.EXIT)){
            return Optional.of(Result.EXIT);
        }
        if(input.isPressed(Input.FIRE)){
            return Optional.of(Result.CONTINUE);
        }
        return Optional.empty();
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public Action[] getActions() {
        return null;
    }

    @Override
    public void paint(Graphics g) {
        // TODO Auto-generated method stub
        super.paint(g);
    }
    
}
