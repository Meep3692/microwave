package ca.awoo.microwave.hell;

import ca.awoo.microwave.breakout.Vec2;

public class MoveTo {
    public final Vec2 dest;
    public final double speed;
    public Object then;

    public MoveTo(Vec2 dest, double speed, Object then) {
        this.dest = dest;
        this.speed = speed;
        this.then = then;
    }

    public MoveTo(Vec2 dest, double speed){
        this(dest, speed, null);
    }

    public static MoveTo path(double speed, Vec2... nodes){
        MoveTo[] moves = new MoveTo[nodes.length];
        for(int i = 0; i < nodes.length; i++){
            moves[i] = new MoveTo(nodes[i], speed);
            if(i > 0){
                moves[i-1].then = moves[i];
            }
        }
        return moves[0];
    }

    public static MoveTo loop(double speed, Vec2... nodes){
        MoveTo[] moves = new MoveTo[nodes.length];
        for(int i = 0; i < nodes.length; i++){
            moves[i] = new MoveTo(nodes[i], speed);
            if(i > 0){
                moves[i-1].then = moves[i];
            }
        }
        moves[nodes.length - 1].then = moves[0];
        return moves[0];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dest == null) ? 0 : dest.hashCode());
        long temp;
        temp = Double.doubleToLongBits(speed);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        // result = prime * result + ((then == null) ? 0 : then.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MoveTo other = (MoveTo) obj;
        if (dest == null) {
            if (other.dest != null)
                return false;
        } else if (!dest.equals(other.dest))
            return false;
        if (Double.doubleToLongBits(speed) != Double.doubleToLongBits(other.speed))
            return false;
        if (then == null) {
            if (other.then != null)
                return false;
        } else if (!then.equals(other.then))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MoveTo [dest=" + dest + ", speed=" + speed + ", then=" + then + "]";
    }
    
    
}
