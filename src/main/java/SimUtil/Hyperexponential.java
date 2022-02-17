package SimUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Hyperexponential extends MarkovianDistribution implements Serializable {
    public  Hyperexponential(int nPhases, List<Double> startingProbabilities, List<Double> phaseRates) {
        super("Hyperexponential", 3);
        this.setParam(1, "n", nPhases);
        this.setParam(2, "p", startingProbabilities);
        this.setParam(3, "rates", phaseRates);
    }

    public long getNumberOfPhases() {
        return (long) this.getParam(1).getValue();
    }

    public double getMean() {
        long nPhases = this.getNumberOfPhases();
        List<Double> startingProbabilities = (List<Double>) this.getParam(2);
        List<Double> phaseRates = (List<Double>) this.getParam(3);
        double acc = 0;
        for (int i = 0; i < nPhases; i++) {
            acc += startingProbabilities.get(i)/phaseRates.get(i);
        }
        return acc;
    }

    public List<Double> sample(int n, Random random)  {
        long nPhases = this.getNumberOfPhases();
        List<Double> startingProbabilities = (List<Double>) this.getParam(2);
        List<Double> phaseRates = (List<Double>) this.getParam(3);
        List<Double> outList = new ArrayList<Double>();

        Cdf<Integer> startingPhaseCdf = new Cdf<Integer>(random);
        for (int j = 0; j < nPhases; j++) {
            startingPhaseCdf.addElement(j, startingProbabilities.get(j));
        }

        for (int i = 0; i < n; i++) {
            int startingPhase = startingPhaseCdf.generate();
            double processRate = phaseRates.get(startingPhase);
            outList.add(Math.log(1-random.nextDouble())/(-processRate));

        }
        return outList;
    }

    public double getSCV() {
        return this.getVar()/Math.pow(this.getMean(),2);
    }

    public double getRate() {
        /* Return the total adjusted rate rather than an individual rate */
        double acc = 0;
        long nPhases = this.getNumberOfPhases();
        List<Double> startingProbabilities = (List<Double>) this.getParam(2);
        List<Double> phaseRates = (List<Double>) this.getParam(3);

        for (int i = 0; i < nPhases; i++) {
            acc += startingProbabilities.get(i)*phaseRates.get(i);
        }
        return acc;
    }

    public double getVar() {
        long nPhases = this.getNumberOfPhases();
        List<Double> startingProbabilities = (List<Double>) this.getParam(2);
        List<Double> phaseRates = (List<Double>) this.getParam(3);

        double acc = 0;
        for (int i = 0; i < nPhases; i++) {
            acc += startingProbabilities.get(i)/phaseRates.get(i);
        }

        acc = Math.pow(acc, 2);


        for (int i = 0; i < nPhases; i++) {
            double i_prob = startingProbabilities.get(i);
            double i_rate = phaseRates.get(i);
            for (int j = 0; j < nPhases; j++) {
                acc += (i_prob*startingProbabilities.get(j))*Math.pow((1/i_rate) - (1/phaseRates.get(j)), 2);
            }
        }

        return acc;
    }

    public double getSkew() {
        throw new RuntimeException("Not Implemented!");
    }

    public String toString() {
        return String.format("SimUtil.Exp(%f)", this.getRate());
    }
    public double evalLST(double s) {
        throw new RuntimeException("Not Implemented!");
    }

    public double evalCDF(double t) {
        throw new RuntimeException("Not Implemented!");
    }
}
