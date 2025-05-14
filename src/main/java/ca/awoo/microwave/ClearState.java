package ca.awoo.microwave;

import java.util.Optional;

public class ClearState<T> extends State<T>{
    private final State<T> toRun;
    private Optional<T> returnVal;

    public ClearState(State<T> toRun){
        this.toRun = toRun;
        returnVal = Optional.empty();
    }

    @Override
    public Optional<T> update(Game game, double dt) {
        if(!returnVal.isPresent()){
            returnVal = Optional.of(game.runState(toRun));
        }
        if(game.getInput().isPressed(Input.EXIT)){
            return returnVal;
        }
        return Optional.empty();
    }

    @Override
    public boolean isTransparent() {
        return true;
    }
    
}
