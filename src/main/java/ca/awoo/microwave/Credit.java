package ca.awoo.microwave;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Credit extends JPanel {
    private final JPanel topPanel;
    private final JLabel assetLabel;
    private final JLabel creditLabel;
    private final JLabel licenseLabel;
    public Credit(String asset, String credit, String license){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        assetLabel = new JLabel(asset);
        creditLabel = new JLabel(credit);
        licenseLabel = new JLabel(license);

        assetLabel.setHorizontalAlignment(SwingConstants.LEFT);
        creditLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        licenseLabel.setHorizontalAlignment(SwingConstants.LEFT);

        Font normalFont = assetLabel.getFont();
        Font bigFont = normalFont.deriveFont(Font.BOLD).deriveFont(normalFont.getSize2D()*1.5f);
        assetLabel.setFont(bigFont);
        creditLabel.setFont(bigFont);

        licenseLabel.setAlignmentX(0.0f);
        topPanel.setAlignmentX(0.0f);
        topPanel.add(assetLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(creditLabel);
        add(topPanel);
        add(licenseLabel);
        setDoubleBuffered(false);
        topPanel.setDoubleBuffered(false);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int lineY = topPanel.getHeight()/2 + 10;
        int lineStart = assetLabel.getWidth() + 3 + 10;
        int lineEnd = getWidth()-creditLabel.getWidth()-3 - 10;
        Graphics2D g2d = (Graphics2D)g;
        Stroke original = g2d.getStroke();
        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
        g2d.setStroke(dashed);
        g2d.setColor(Color.GRAY);
        g2d.drawLine(lineStart, lineY, lineEnd, lineY);
        g2d.setStroke(original);
    }

    
}
