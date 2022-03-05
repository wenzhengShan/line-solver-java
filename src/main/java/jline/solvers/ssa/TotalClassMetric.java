package jline.solvers.ssa;

public class TotalClassMetric extends Metric<Double, Double>{
    int classIdx;
    int totalCt = 0;

    public TotalClassMetric(int classIdx) {
        super("Total Class Count");
        this.shortName = "TC";
        this.classIdx = classIdx;
        this.totalCt = 0;
    }

    protected void addSample(double currentTime, Double metric) {
    }

    public Double getMetric() {
        return (double) this.totalCt;
    }

    public void fromStateMatrix(double t, StateMatrix stateMatrix) {
    }

    public void fromEvent(double t, Event e) {
    }

    public void fromEvent(double t, Event e, int n) {

    }

    public void increment() {
        this.totalCt++;
    }
    public void increment(int n) {
        this.totalCt += n;
    }
}
