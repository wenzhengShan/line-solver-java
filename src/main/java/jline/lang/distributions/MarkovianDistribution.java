package jline.lang.distributions;

import java.io.Serializable;

import jline.util.Interval;

abstract public class MarkovianDistribution extends Distribution implements Serializable {
    public MarkovianDistribution(String name, int numParam) {
        super(name, numParam, new Interval(0, Double.POSITIVE_INFINITY));
    }

    public abstract long getNumberOfPhases();
}
