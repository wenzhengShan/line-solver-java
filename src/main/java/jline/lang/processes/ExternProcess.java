package jline.lang.processes;

import jline.lang.distributions.MarkovianDistribution;
import jline.util.Cdf;
import jline.util.Interval;

import java.util.List;
import java.util.Random;

public class ExternProcess extends MarkovianDistribution {
    protected RateController rateController;
    public ExternProcess(RateController rateController) {
        super("jline.ExternProcess", 1);

        this.rateController = rateController;
    }

    public long getNumberOfPhases() {
        throw new RuntimeException("Not Implemented!");
    }

    public double getMean() {
        throw new RuntimeException("Not Implemented!");
    }

    public List<Double> sample(int n)  {
        throw new RuntimeException("Not Implemented!");
    }

    public double getVar() {
        throw new RuntimeException("Not Implemented!");
    }

    public double getSkew() {
        throw new RuntimeException("Not Implemented!");
    }

    public double getSCV() { throw new RuntimeException("Not Implemented!"); }

    public double getRate() {
        return  this.rateController.getRate();
    }

    public double evalCDF(double t) {
        throw new RuntimeException("Not Implemented!");
    }

    public Interval getPH()  {
        throw new RuntimeException("Not Implemented!");
    }
    public double evalLST(double s) {
        throw new RuntimeException("Not Implemented!");
    }
}
