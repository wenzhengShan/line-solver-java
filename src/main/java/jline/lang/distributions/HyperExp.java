package jline.lang.distributions;

import jline.util.Interval;

import java.io.Serializable;
import java.util.List;

import static java.lang.Math.exp;

public class HyperExp extends MarkovianDistribution  implements Serializable {
    int nPhases;
    public HyperExp(List<Double> p, List<Double> lambda) {
        super("HyperExponential", 1);
        this.setParam(1, "p", p);
        this.setParam(2, "lambda", lambda);

        nPhases = lambda.size();
    }

    public List<Double> sample(int n)  {
        throw new RuntimeException("Not Implemented!");
    }

    public long getNumberOfPhases() {
        return ((List)this.getParam(2)).size();
    }

    public double evalCDF(double t) {
        throw new RuntimeException("Not Implemented!");
    }

    public Interval getPH() {
        throw new RuntimeException("Not Implemented!");
    }

    public double evalLST(double s) {
        throw new RuntimeException("Not Implemented!");
    }

    public double getSCV() {
        return 1;
    }

    public double getRate() {
        throw new RuntimeException("Not Implemented!");
    }

    public double getMean() {
        throw new RuntimeException("Not Implemented!");
    }

    public double getVar() {
        throw new RuntimeException("Not Implemented!");
    }

    public double getSkew() {
        throw new RuntimeException("Not Implemented!");
    }

    public String toString() {
        return String.format("jline.HyperExp(%f)", this.getRate());
    }

    public double getRateFromPhase(int phase) {
        return ((List<Double>)this.getParam(2)).get(phase);
    }
}
