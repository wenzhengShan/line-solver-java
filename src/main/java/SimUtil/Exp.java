package SimUtil;

import SimUtil.MarkovianDistribution;
import StochLib.Interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.exp;
import static java.lang.Math.log;

public class Exp extends MarkovianDistribution  implements Serializable {
    public Exp(double lambda) {
        super("Exponential", 1);
        this.setParam(1, "lambda", lambda);
    }

    public List<Double> sample(int n, Random random)  {
        List<Double> outList = new ArrayList<Double>();
        double lambda = (double) this.getParam(1).getValue();
        for (int i = 0; i < n; i++) {
            outList.add(Math.log(1-random.nextDouble())/(-lambda));
        }
        return outList;
    }

    public long getNumberOfPhases() {
        return 1;
    }

    public double evalCDF(double t) {
        double lambda = (double) this.getParam(1).getValue();
        return 1-exp(-lambda*t);
    }

    public Interval getPH() {
        double lambda = (double) this.getParam(1).getValue();
        return new Interval(-lambda, lambda);
    }

    public double evalLST(double s) {
        double lambda = (double) this.getParam(1).getValue();
        return (lambda/(lambda+s));
    }

    public double getSCV() {
        return 1;
    }

    public double getRate() {
        return (double) this.getParam(1).getValue();
    }

    public double getMean() {
        return 1/getRate();
    }

    public double getVar() {
        return 1/(Math.pow(getRate(),2));
    }

    public double getSkew() {
        return 2;
    }

    public String toString() {
        return String.format("SimUtil.Exp(%f)", this.getRate());
    }
}
