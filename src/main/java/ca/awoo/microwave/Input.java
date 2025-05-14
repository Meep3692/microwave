package ca.awoo.microwave;

public class Input {
    private static int iota = 0;
    public static final int LEFT = iota++;
    public static final int RIGHT = iota++;
    public static final int UP = iota++;
    public static final int DOWN = iota++;
    public static final int EXIT = iota++;
    private final boolean[] held = new boolean[iota];
    private final boolean[] pressed = new boolean[iota];

    public boolean isPressed(int input){
        return pressed[input];
    }

    public boolean isHeld(int input){
        return held[input];
    }

    public void setPressed(int input, boolean value){
        pressed[input] = value;
    }

    public void setHeld(int input, boolean value){
        held[input] = value;
    }

    public static int getInputLength(){
        return iota;
    }
}
