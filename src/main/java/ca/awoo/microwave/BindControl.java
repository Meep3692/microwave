package ca.awoo.microwave;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;


public class BindControl extends JPanel {
    private final JPanel topPanel;
    private final JLabel inputLabel;
    private final JLabel keyLabel;
    private final JPanel controls;
    private static String keyNames(Set<Integer> keys){
        if(keys.isEmpty()) return "Unbound";
        StringBuilder sb = new StringBuilder();
        for(int b : keys){
            sb.append(KeyEvent.getKeyText(b));
            sb.append(", ");
        }
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
}
    public BindControl(Game game, int input, Set<Integer> keys){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        inputLabel = new JLabel(Input.getName(input));
        keyLabel = new JLabel(keyNames(keys));
        controls = new JPanel();
        controls.setDoubleBuffered(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        inputLabel.setHorizontalAlignment(SwingConstants.LEFT);
        keyLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        Font normalFont = inputLabel.getFont();
        Font bigFont = normalFont.deriveFont(Font.BOLD).deriveFont(normalFont.getSize2D()*1.5f);
        inputLabel.setFont(bigFont);
        keyLabel.setFont(bigFont);

        controls.setAlignmentX(0.0f);
        topPanel.setAlignmentX(0.0f);
        topPanel.add(inputLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(keyLabel);
        add(topPanel);
        add(controls);
        setDoubleBuffered(false);
        topPanel.setDoubleBuffered(false);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener((e) -> {
            game.clearBinding(input);
            keyLabel.setText(keyNames(game.getBindings(input)));
        });
        JButton bindButton = new JButton("Bind");
        bindButton.addActionListener((e) -> {
            JOptionPane option = new JOptionPane(new JLabel("Press a key to bind"), JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{"Cancel"}, "Cancel");
            JDialog dialog = option.createDialog(this, "Bind a key");
            game.setKeyHog(false);
            KeyEventDispatcher ked = new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent ke) {
                    if(ke.getID() == KeyEvent.KEY_PRESSED){
                        game.bindInput(input, ke.getKeyCode());
                        keyLabel.setText(keyNames(game.getBindings(input)));
                        dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
                        game.setKeyHog(true);
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                        return true;
                    }
                    return false;
                }
            };
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ked);
            dialog.setVisible(true);
        });

        controls.add(clearButton);
        controls.add(bindButton);
    }

    public void setKeysText(String text){
        keyLabel.setText(text);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int lineY = topPanel.getHeight()/2 + 10;
        int lineStart = inputLabel.getWidth() + 3 + 10;
        int lineEnd = getWidth()-keyLabel.getWidth()-3 - 10;
        Graphics2D g2d = (Graphics2D)g;
        Stroke original = g2d.getStroke();
        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
        g2d.setStroke(dashed);
        g2d.setColor(Color.GRAY);
        g2d.drawLine(lineStart, lineY, lineEnd, lineY);
        g2d.setStroke(original);
    }
}
