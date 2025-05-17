package ca.awoo.microwave;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import javax.sound.midi.Sequence;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MenuState extends State<Integer>{
    public static class MenuItem{
        public final String label;
        public final Consumer<Game> run;
        public MenuItem(String label, Consumer<Game> run) {
            this.label = label;
            this.run = run;
        }
    }
    
    private final Image bg;
    private final Sequence bgm;
    private boolean exit = false;
    private final Queue<Consumer<Game>> toRun;

    public MenuState(Game game,String title, String background, String bgm, boolean addExit, MenuItem... items){
        this.bg = game.getImage(background);
        this.bgm = game.getSequence(bgm);
        game.playSequence(this.bgm);
        toRun = new ConcurrentLinkedQueue<>();
        setLayout(new BorderLayout());
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setDoubleBuffered(false);
        menuPanel.setOpaque(false);
        menuPanel.add(Box.createVerticalGlue());
        for(MenuItem item : items){
            JButton button = new JButton(item.label);
            button.addActionListener((e) -> {
                toRun.add(item.run);
            });
            Dimension maxSize = button.getMaximumSize();
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) maxSize.getHeight()));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            menuPanel.add(button);
            if(addExit || item != items[items.length-1])
                menuPanel.add(Box.createVerticalStrut(8));
        }
        if(addExit){
            JButton exitButton = new JButton("Exit");
            Dimension maxSize = exitButton.getMaximumSize();
            exitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) maxSize.getHeight()));
            exitButton.setHorizontalAlignment(SwingConstants.LEFT);
            exitButton.addActionListener((e) -> {
                exit = true;
            });
            menuPanel.add(exitButton);
        }
        menuPanel.add(Box.createVerticalGlue());
        add(menuPanel, BorderLayout.WEST);


        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setDoubleBuffered(false);
        titlePanel.setLayout(null);

        JLabel titleLabel = new JLabel(title);
        Font font = titleLabel.getFont();
        font = font.deriveFont(4.0f*font.getSize2D()).deriveFont(Font.BOLD);
        titleLabel.setFont(font);
        titleLabel.setForeground(new Color(153, 153, 204));
        
        JLabel titleShadow = new JLabel(title);
        titleShadow.setFont(font);
        titleShadow.setForeground(new Color(102, 102, 153));
        titlePanel.add(titleLabel);
        titlePanel.add(titleShadow);
        Dimension labelSize = titleLabel.getPreferredSize();
        titleLabel.setBounds(0, 0, labelSize.width, labelSize.height);
        titleShadow.setBounds(3, 3, labelSize.width, labelSize.height);
        add(titlePanel, BorderLayout.NORTH);
        titlePanel.setMinimumSize(new Dimension(labelSize.width+5, labelSize.height+5));
        titlePanel.setSize(new Dimension(labelSize.width+5, labelSize.height+5));
        titlePanel.setPreferredSize(new Dimension(labelSize.width+5, labelSize.height+5));

    }

    @Override
    public Optional<Integer> update(Game game, double dt) {
        if(exit){
            return Optional.of(0);
        }else{
            Consumer<Game> run;
            while((run = toRun.poll()) != null){
                run.accept(game);
                //In case the menu item ran a state which changed the music
                game.playSequence(bgm);
            }
            return Optional.empty();
        }
    }
    @Override
    public boolean isTransparent() {
        return false;
    }
    @Override
    public void paint(Graphics g) {
        g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        super.paint(g);
    }
    
}
