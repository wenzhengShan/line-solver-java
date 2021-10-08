package StochLib;

import java.io.Serializable;
import java.util.HashMap;

public class NetworkStruct implements Serializable {
    public int nStateful;
    public int nClasses;
    public SchedStrategy[] schedStrategies;
    public int[][] capacities;
    public int[] nodeCapacity;
    public int[] numberOfServers;
}
