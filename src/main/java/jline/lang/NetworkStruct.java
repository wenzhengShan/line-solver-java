package jline.lang;

import java.io.Serializable;
import java.util.HashMap;

import jline.lang.constant.SchedStrategy;

public class NetworkStruct implements Serializable {
    public int nStateful;
    public int nClasses;
    public SchedStrategy[] schedStrategies;
    public int[][] capacities;
    public int[] nodeCapacity;
    public int[] numberOfServers;
}
