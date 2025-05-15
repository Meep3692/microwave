package ca.awoo.microwave;

import ca.awoo.microwave.breakout.Breaker;

public class Microwave extends Game {
    private final boolean exitButton;
    public Microwave(boolean exitButton){
        super();
        this.exitButton = exitButton;
        setTargetFps(60);
    }

    public void start(){
        start(new MenuState( this, "/com/screamingbrainstudio/planetSurfaceBg2/Rocky_01-640x480.png", "/io/itch/surtr/dungeon_castle.mid", exitButton,
            new MenuState.MenuItem("Breaker", (game) -> {game.runState(new Breaker(game));}),
            new MenuState.MenuItem("Credits", (game) -> {game.runState(new CreditsState());})));
    }
}
