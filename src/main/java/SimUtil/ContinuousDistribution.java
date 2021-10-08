package SimUtil;

import SimUtil.Distribution;
import StochLib.Interval;

import java.io.Serializable;

abstract public class ContinuousDistribution extends Distribution implements Serializable {
    public ContinuousDistribution(String name, int numParam, Interval support) {
        super(name, numParam, support);
    }
}
