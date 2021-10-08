package SimUtil;

import SimUtil.Distribution;
import StochLib.Interval;

import java.io.Serializable;

public abstract class DiscreteDistribution extends Distribution implements Serializable {
    public DiscreteDistribution(String name, int numParam, Interval support) {
        super(name, numParam, support);
    }
}
