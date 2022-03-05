package SimUtil;

import SimUtil.Distribution;
import Line.Interval;

import java.io.Serializable;

abstract public class MarkovianDistribution extends Distribution implements Serializable {
    public MarkovianDistribution(String name, int numParam) {
        super(name, numParam, new Interval(0, Double.POSITIVE_INFINITY));
    }

    public abstract long getNumberOfPhases();
}
