package ca.awoo.microwave;

public class FPSReport {
    public double fps;
    public double low;
    public FPSReport(double fps, double low) {
        this.fps = fps;
        this.low = low;
    }
    @Override
    public String toString() {
        return "FPSReport [fps=" + (int)fps + ", low=" + (int)low + "]";
    }
}
