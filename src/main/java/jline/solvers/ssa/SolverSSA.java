package jline.solvers.ssa;

import jline.lang.*;
import jline.lang.constant.DropStrategy;
import jline.lang.constant.SchedStrategy;
import jline.lang.nodes.Node;
import jline.lang.nodes.StatefulNode;
import jline.lang.nodes.Station;
import jline.solvers.ssa.events.DepartureEvent;
import jline.solvers.ssa.events.Event;
import jline.solvers.ssa.events.EventStack;
import jline.solvers.ssa.state.StateMatrix;
import jline.solvers.ssa.strategies.TauLeapingStateStrategy;
import jline.util.Interval;

import java.util.*;

public class SolverSSA {
    public class SSAOptions {
        /*
            Internal class for setting the configuration of SolverSSA.
         */

        // simulation parameters
        public int samples;
        public int seed;
        public Interval timeInterval;
        public Map<Node, Map<JobClass, Double>> cutoffMatrix;
        public Map<Node, Double> nodeCutoffMatrix;
        public Double cutoff;
        public double timeout;

        // tau leaping configuration
        public TauLeapingType tauLeapingType;
        public boolean useTauLeap;

        // metrics configurations
        public boolean useMSER5;
        public boolean useR5;
        public int r5value;
        public boolean recordMetricTimeline;
        public double steadyStateTime;
        public boolean disableResTime;

        public SSAOptions() {
            this.disableResTime = false;
            this.samples = 10000;
            this.seed = 1;
            this.timeout = Double.POSITIVE_INFINITY;
            this.timeInterval = new Interval(0, Double.POSITIVE_INFINITY);
            cutoff = Double.POSITIVE_INFINITY;
            cutoffMatrix = new HashMap<Node,Map<JobClass, Double>>();

            this.tauLeapingType = null;
            this.useTauLeap = false;
            this.useMSER5 = false;
            this.useR5 = false;
            this.recordMetricTimeline = true;
            this.r5value = 19;
            this.steadyStateTime = -1;
        }

        public SSAOptions samples(int samples) {
            this.samples = samples;
            return this;
        }

        public SSAOptions seed(int seed) {
            this.seed = seed;
            return this;
        }

        public SSAOptions MSER5() {
            this.useMSER5 = true;
            this.useR5 = false;
            return this;
        }

        public SSAOptions R5(int k) {
            this.useR5 = true;
            this.useMSER5 = false;
            this.r5value = k;
            return this;
        }

        public SSAOptions recordMetricTimeline(boolean recordMetricTimeline) {
            this.recordMetricTimeline = recordMetricTimeline;

            return this;
        }

        public SSAOptions setTimeInterval(Interval timeInterval) {
            this.timeInterval = timeInterval;
            return this;
        }

        public SSAOptions setTimeout(double timeout) {
            this.timeout = timeout;
            return this;
        }

        public SSAOptions setStartTime(double startTime) {
            this.timeInterval.setLeft(startTime);
            return this;
        }

        public SSAOptions setEndTime(double endTime) {
            this.timeInterval.setRight(endTime);
            return this;
        }

        public SSAOptions steadyStateTime(double sTime) {
            this.steadyStateTime = sTime;
            return this;
        }

        public void setCutoff(Node node, JobClass jobClass, Double cutoff) {
            if (!this.cutoffMatrix.containsKey(node)) {
                this.cutoffMatrix.put(node, new HashMap<JobClass, Double>());
            }
            this.cutoffMatrix.get(node).put(jobClass, cutoff);
        }

        public void setCutoff(Double cutoff) {
            this.cutoff = cutoff;
            this.cutoffMatrix = new HashMap<Node, Map<JobClass, Double>>();
        }

        public void setCutoff(Node node, Double cutoff) {
            this.nodeCutoffMatrix.put(node, cutoff);
        }

        public int getCutoff(Node node, JobClass jobClass) {
            if (this.cutoffMatrix.containsKey(node)) {
                if (this.cutoffMatrix.get(node).containsKey(jobClass)) {
                    return this.cutoffMatrix.get(node).get(jobClass).intValue();
                }
            }

            return cutoff.intValue();
        }

        public void configureTauLeap (TauLeapingType tauLeapingType) {
            this.tauLeapingType = tauLeapingType;
            this.useTauLeap = true;
        }

        public void disableTauLeap() {
            this.useTauLeap = false;
        }
    }

    protected class NetworkData {
        /*
            Yet another class for handling network information
         */
        public EventStack eventStack;
        public NetworkStruct networkStruct;

        public NetworkData(Network network) {
            if (this.networkStruct == null) {
                this.networkStruct = new NetworkStruct();
            }
            this.networkStruct.nStateful = network.getNumberOfStatefulNodes();
            List<JobClass> classes = network.getClasses();
            this.networkStruct.nClasses = classes.size();

            this.eventStack = new EventStack();

            // find capacities
            this.networkStruct.capacities = new int[this.networkStruct.nStateful][this.networkStruct.nClasses];
            this.networkStruct.nodeCapacity = new int[this.networkStruct.nStateful];

            this.networkStruct.numberOfServers = new int[this.networkStruct.nStateful];
            this.networkStruct.schedStrategies = new SchedStrategy[this.networkStruct.nStateful];


            // loop through each node and add active events to the eventStack
            ListIterator<Node> nodeIter = network.getNodes().listIterator();
            int nodeIdx = -1;
            while (nodeIter.hasNext()) {
                Node node = nodeIter.next();
                if (!(node instanceof StatefulNode)) {
                    continue;
                }
                nodeIdx++;
                Iterator<JobClass> jobClassIter = network.getClasses().listIterator();
                while (jobClassIter.hasNext()) {
                    JobClass jobClass = jobClassIter.next();
                    int jobClassIdx = jobClass.getJobClassIdx();
                    if (network.getVisitCount(node, jobClass) == 0) {
                        this.networkStruct.capacities[nodeIdx][jobClassIdx] = 0;
                    } else {
                        double jobCap = jobClass.getNumberOfJobs();
                        jobCap = Math.min(jobCap, node.getClassCap(jobClass));
                        if ((jobCap == Double.POSITIVE_INFINITY) || (node.getDropStrategy() == DropStrategy.WaitingQueue)) {
                            this.networkStruct.capacities[nodeIdx][jobClassIdx] = Integer.MAX_VALUE;
                        } else {
                            this.networkStruct.capacities[nodeIdx][jobClassIdx] = (int)jobCap;
                        }
                    }
                    Event dEvent = DepartureEvent.fromNodeAndClass((StatefulNode) node, jobClass);
                    this.eventStack.addEvent(dEvent);
                    if (dEvent instanceof DepartureEvent) {
                        if (((DepartureEvent)dEvent).getPhaseEvent() != null) {
                            this.eventStack.addEvent(((DepartureEvent)dEvent).getPhaseEvent());
                        }
                    }
                }

                double nodeCap = node.getCap();
                if (nodeCap == Double.POSITIVE_INFINITY) {
                    this.networkStruct.nodeCapacity[nodeIdx] = Integer.MAX_VALUE;
                } else {
                    this.networkStruct.nodeCapacity[nodeIdx] = (int) nodeCap;
                }
            }


            // update server counts
            for (int i = 0; i < networkStruct.nStateful; i++) {
                Node nodeIter2 = network.getNodeByStatefulIndex(i);
                if (nodeIter2 instanceof Station) {
                    Station stationIter = (Station) nodeIter2;
                    networkStruct.numberOfServers[i] = stationIter.getNumberOfServers();
                    if (nodeIter2 instanceof HasSchedStrategy) {
                        networkStruct.schedStrategies[i] = ((HasSchedStrategy)nodeIter2).getSchedStrategy();
                    } else {
                        networkStruct.schedStrategies[i] = SchedStrategy.FCFS;
                    }
                } else {
                    networkStruct.numberOfServers[i] = 1;
                    networkStruct.schedStrategies[i] = SchedStrategy.FCFS;
                }
            }
        }

        public void applyCutoff(SSAOptions ssaOptions, Network network) {
            for (int i = 0; i < this.networkStruct.nStateful; i++) {
                Node nodeIter = network.getStatefulNodeFromIndex(i);
                for (int j = 0; j < this.networkStruct.nClasses; j++) {
                    JobClass jobClassIter = network.getJobClassFromIndex(j);
                    double cutoff = Math.min(ssaOptions.cutoffMatrix.get(nodeIter).get(jobClassIter), ssaOptions.cutoff);
                    if (cutoff != Double.POSITIVE_INFINITY) {
                        this.networkStruct.capacities[i][j] = Math.min(this.networkStruct.capacities[i][j], (int)cutoff);
                    }
                }
                double nodeCutoff = Math.min(ssaOptions.nodeCutoffMatrix.get(nodeIter), ssaOptions.cutoff);

                if (nodeCutoff != Double.POSITIVE_INFINITY) {
                    this.networkStruct.nodeCapacity[i] = Math.min(this.networkStruct.nodeCapacity[i], (int)nodeCutoff);
                }
            }
        }

        public NetworkData(NetworkStruct networkStruct) {
            this.networkStruct = networkStruct;
        }
    }

    protected SSAOptions ssaOptions;
    protected Network network;
    protected NetworkStruct networkStruct;
    protected NetworkData networkCache;
    protected Random random;

    public SolverSSA() {
        this.network = null;
        this.networkStruct = null;
        this.ssaOptions = new SSAOptions();
        this.random = new Random();
    }

    public SSAOptions setOptions() {
        return this.ssaOptions;
    }

    public void compile(Network network) {
        this.network = network;
        this.networkCache = new NetworkData(this.network);
    }

    public void compile(NetworkStruct networkStruct) {
        this.networkStruct = networkStruct;
        this.networkCache = new NetworkData(this.networkStruct);
        throw new RuntimeException("Network structs not supported");
    }

    public Timeline solve() {
        if (this.networkCache == null) {
            if (this.networkStruct == null) {
                this.compile(this.networkStruct);
            } else if (this.network == null) {
                this.compile(this.network);
            } else {
                throw new RuntimeException("Network data not provided!");
            }
        }

        this.random = new Random(this.ssaOptions.seed);
        int samplesCollected = 1;
        int maxSamples = ssaOptions.samples;
        double curTime = ssaOptions.timeInterval.getLeft();
        double maxTime = ssaOptions.timeInterval.getRight();

        // Add ClosedClass instances to the reference station
        StateMatrix stateMatrix = new StateMatrix(this.networkCache.networkStruct);
        for (JobClass jobClass : this.network.getClasses()) {
            if (jobClass instanceof ClosedClass) {
                int classIdx = this.network.getJobClassIndex(jobClass);
                ClosedClass cClass = (ClosedClass) jobClass;
                int stationIdx = this.network.getStatefulNodeIndex(cClass.getRefstat());
                stateMatrix.setState(stationIdx, classIdx, (int)cClass.getPopulation());
                for (int i = 0; i < cClass.getPopulation(); i++) {
                    stateMatrix.addToBuffer(stationIdx, classIdx);
                }
            }
        }

        Timeline timeline = new Timeline(this.networkCache.networkStruct);

        if (ssaOptions.disableResTime) {
            timeline.disableResidenceTime();
        }

        if (ssaOptions.useMSER5) {
            timeline.useMSER5();
        } else if (ssaOptions.useR5) {
            timeline.useR5(ssaOptions.r5value);
        }

        if (!ssaOptions.recordMetricTimeline) {
            timeline.setMetricRecord(false);
        }

        if (ssaOptions.useTauLeap) {
            this.networkCache.eventStack.configureTauLeap(ssaOptions.tauLeapingType);
            if ((ssaOptions.tauLeapingType.stateStrategy == TauLeapingStateStrategy.TimeWarp) ||
                    (ssaOptions.tauLeapingType.stateStrategy == TauLeapingStateStrategy.TauTimeWarp)) {
                timeline.cacheRecordings();
            }
        }

        double sysTime = 0;
        double startTime = System.currentTimeMillis();

        boolean beforeSState = false;

        // collect samples and update states
        while ((samplesCollected < maxSamples) && (curTime < maxTime) && (sysTime < this.ssaOptions.timeout)) {
            beforeSState = curTime < this.ssaOptions.steadyStateTime;

            if (ssaOptions.useTauLeap) {
                curTime = this.networkCache.eventStack.tauLeapUpdate(stateMatrix, timeline, curTime, random);
            } else {
                curTime = this.networkCache.eventStack.updateState(stateMatrix, timeline, curTime, random);
            }

            if (beforeSState && (curTime > this.ssaOptions.steadyStateTime)) {
                timeline.resetHistory();
            }

            samplesCollected++;
            sysTime = (System.currentTimeMillis() - startTime)/1000.0;

        }

        //System.out.format("Solver finished. %d samples in %f time\n", samplesCollected, curTime);

        timeline.taper(curTime);
        //timeline.printSummary(this.network);

        return timeline;
    }
}
