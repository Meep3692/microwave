package ca.awoo.microwave;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
    private boolean exit = false;
    private final Queue<Consumer<Game>> toRun;

    public MenuState(Image background, MenuItem... items){
        this.bg = background;
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
            menuPanel.add(Box.createVerticalStrut(8));
        }
        JButton exitButton = new JButton("Exit");
        Dimension maxSize = exitButton.getMaximumSize();
        exitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) maxSize.getHeight()));
        exitButton.setHorizontalAlignment(SwingConstants.LEFT);
        exitButton.addActionListener((e) -> {
            exit = true;
        });
        menuPanel.add(exitButton);
        menuPanel.add(Box.createVerticalGlue());
        add(menuPanel, BorderLayout.WEST);

    }

    @Override
    public Optional<Integer> update(Game game, double dt) {
        if(exit){
            return Optional.of(0);
        }else{
            Consumer<Game> run;
            while((run = toRun.poll()) != null){
                run.accept(game);
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
