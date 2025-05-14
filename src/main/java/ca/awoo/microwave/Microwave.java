package ca.awoo.microwave;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class Microwave extends JApplet{

    @Override
    public void init() {
        System.out.println("init pl0x");
        try {
            SwingUtilities.invokeAndWait(() -> {
                JLabel label = new JLabel("Hello applet");
                add(label);
            });
        } catch (Exception e) {
            System.err.println("Oh no");
        }
    }
    
}
