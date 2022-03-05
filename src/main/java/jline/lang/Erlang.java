package jline.lang;

import jline.lang.Interval;

import java.io.Serializable;
import java.util.List;

public class Erlang extends MarkovianDistribution implements Serializable {
    /*public static List<List<Double>> mapErlang(double mean, long k) {
        double mu = k/mean;
        List<List<Double>> map = new ArrayList<List<Double>>();

        for (int i = 0; i < k; i++) {
            map.set(i,new ArrayList<Double>());
            for (int j = 0; j < k; j++) {
                map.get(i).set(j,0);
            }
        }

        for (int i = 0; i < (k-1); i++) {
            map.get(i).set(i+1, mu);
        }
        map.

        return map;
    }*/
    public Erlang(double phaseRate, long nPhases) {
        super("jline.Erlang", 2);
        this.setParam(1, "alpha", phaseRate);
        this.setParam(2, "r", nPhases);
    }

    public List<Double> sample(int n)  {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        //return exprnd(1/lambda, n, 1);
        throw new RuntimeException("Not Implemented!");
    }

    public long getNumberOfPhases() {
        return (long) this.getParam(2).getValue();
    }

    public double getMean() {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        return r/alpha;
    }

    public double getVar() {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        return r/Math.pow(alpha,2);
    }

    public double getSkew() {
        long r = (long) this.getParam(2).getValue();
        return 2/Math.sqrt(r);
    }

    public double getSCV() {
        long r = (long) this.getParam(2).getValue();
        return 1/r;
    }

    public double getRate() {
        return (double)this.getParam(1).getValue();
    }

    public double evalCDF(double t) {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        double ft = 1;

        for (int j = 0; j < r; j++) {
            int fac_j = 1;
            for (int k = 2; k <= j; k++) {
                fac_j *= k;
            }
            ft -= Math.exp(-alpha*t)*(alpha*t)*j/fac_j;
        }

        return ft;
    }

    public Interval getPH()  {
        throw new RuntimeException("Not Implemented!");
    }
    public double evalLST(double s) {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        return Math.pow(alpha/(alpha+s), r);
    }

    public static Erlang fitMeanAndSCV(double mean, double scv) {
        double r = Math.ceil(scv);
        double alpha = r/mean;
        return new Erlang(alpha, (long)r);
    }

    public static Erlang fitMeanAndStdDev(double mean, double stdDev) {
        return Erlang.fitMeanAndSCV(mean, (mean/Math.pow(stdDev,2)));
    }
}
