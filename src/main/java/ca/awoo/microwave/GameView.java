package ca.awoo.microwave;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

public class GameView extends JPanel{
    public GameView(Game game){
        setLayout(new BorderLayout());
        add(game, BorderLayout.CENTER);
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);
        game.onSwingFrame((g) -> {
            statusLabel.setText(g.getAvgFps().toString());
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu debugMenu = new JMenu("Debug");
        JCheckBoxMenuItem debugView = new JCheckBoxMenuItem("Debug view", false);
        debugView.addActionListener((e) -> {
            game.setDebugView(debugView.getState());
        });
        debugMenu.add(debugView);

        JMenu audioMenu = new JMenu("Audio");
        JCheckBoxMenuItem musicItem = new JCheckBoxMenuItem("Music", true);
        JCheckBoxMenuItem soundItem = new JCheckBoxMenuItem("Sound", true);
        musicItem.addActionListener((e) -> {
            game.muteMusic(!musicItem.getState());
        });
        soundItem.addActionListener((e) -> {
            game.muteSound(!soundItem.getState());
        });
        audioMenu.add(musicItem);
        audioMenu.add(soundItem);

        menuBar.add(debugMenu);
        menuBar.add(audioMenu);

        Ref<JMenu> stateMenu = new Ref<JMenu>(null);
        game.onStateChange((g) -> {
            SwingUtilities.invokeLater(() -> {
                if(stateMenu.contents != null){
                    menuBar.remove(stateMenu.contents);
                    validate();
                    repaint();
                }
                
                State<?> state = g.getActiveState();
                Action[] actions = state.getActions();
                if(actions == null || actions.length == 0){
                    stateMenu.contents = null;
                    return;
                }
                stateMenu.contents = new JMenu(state.getClass().getSimpleName());
                for(Action action : actions){
                    JMenuItem menuItem = new JMenuItem(action);
                    stateMenu.contents.add(menuItem);
                }
                menuBar.add(stateMenu.contents);
                validate();
                repaint();
            });
        });

        add(menuBar, BorderLayout.NORTH);
    }
}
