package StochLib;

import SimUtil.Distribution;
import SolverSSA.DepartureEvent;

public interface HasSchedStrategy {
    public SchedStrategy getSchedStrategy();
    public Distribution getServiceProcess(JobClass jobClass);
    public double minRate();
    public double maxRate();
    public double avgRate();
    public int rateCt();
}
