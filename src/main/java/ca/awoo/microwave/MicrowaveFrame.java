package ca.awoo.microwave;

import javax.swing.JFrame;

public class MicrowaveFrame extends JFrame {
    public MicrowaveFrame(){
        Microwave game = new Microwave(true);
        GameView view = new GameView(game);
        add(view);
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        game.onGameEnd((g) -> {
            System.exit(0);
        });
        game.start();
    }

    public static void main(String[] args){
        new MicrowaveFrame();
    }
}
