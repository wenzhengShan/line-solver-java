package jline.solvers.ssa;

import jline.lang.NetworkStruct;
import jline.lang.SchedStrategy;

import static org.junit.jupiter.api.Assertions.*;

class TimelineTest {
    Timeline timeline;
    NetworkStruct networkStruct;
    StateMatrix stateMatrix;
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        int[][] capacityMatrix = new int[3][3];
        int[] nodeCapacities = new int[3];
        int[] servers = new int[3];
        capacityMatrix[0][0] = 10;
        capacityMatrix[0][1] = 9;
        capacityMatrix[0][2] = 8;
        capacityMatrix[1][0] = 9;
        capacityMatrix[1][1] = 8;
        capacityMatrix[1][2] = 7;
        capacityMatrix[2][0] = 8;
        capacityMatrix[2][1] = 7;
        capacityMatrix[2][2] = 6;
        nodeCapacities[0] = 13;
        nodeCapacities[1] = 12;
        nodeCapacities[2] = 1;
        servers[0] = 1;
        servers[1] = 1;
        servers[2] = 2;
        SchedStrategy[] schedStrategies = new SchedStrategy[3];
        schedStrategies[0] = SchedStrategy.FCFS;
        schedStrategies[1] = SchedStrategy.LCFS;
        schedStrategies[2] = SchedStrategy.LCFS;
        this.networkStruct = new NetworkStruct();
        this.networkStruct.nStateful = 3;
        this.networkStruct.nClasses = 3;
        this.networkStruct.schedStrategies = schedStrategies;
        this.networkStruct.capacities = capacityMatrix;
        this.networkStruct.nodeCapacity = nodeCapacities;
        this.networkStruct.numberOfServers = servers;
        this.stateMatrix = new StateMatrix(networkStruct);
        this.timeline = new Timeline(networkStruct, CutoffStrategy.None);
    }

    @org.junit.jupiter.api.Test
    void testRecord() {
    }

    @org.junit.jupiter.api.Test
    void testPreRecord() {
    }

    @org.junit.jupiter.api.Test
    void testClearCache() {
    }

    @org.junit.jupiter.api.Test
    void testRecordCache() {
    }
}