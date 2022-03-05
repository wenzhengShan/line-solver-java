package jline.lang;

import jline.lang.Distribution;
import jline.lang.Interval;

import java.io.Serializable;

public abstract class DiscreteDistribution extends Distribution implements Serializable {
    public DiscreteDistribution(String name, int numParam, Interval support) {
        super(name, numParam, support);
    }
}
