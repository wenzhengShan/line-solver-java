package jline.tests;

import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Distribution;
import jline.lang.distributions.Exp;
import jline.lang.distributions.Immediate;
import jline.lang.nodes.Node;
import jline.lang.nodes.Source;
import jline.lang.nodes.StatefulNode;

import java.util.List;
import java.util.Map;
import java.util.Random;
import jline.solvers.ssa.*;
import jline.util.Cdf;
import jline.util.Pair;

public class CombinedDepartureEvent extends Event implements NodeEvent {
    private int statefulIndex;
    private int classIndex;
    private boolean useBuffer;
    private SchedStrategy schedStrategy;
    private boolean isSource;
    private Distribution[] serviceProcesses;
    private Double[] serviceRates;
    private List<JobClass> jobClasses;
    private Cdf<JobClass> jobClassCdf;
    protected Node node;
    protected Random random;

    public CombinedDepartureEvent(Node node, Random random) {
        super();
        this.node = node;
        this.random = random;

        if (node instanceof StatefulNode) {
            this.statefulIndex = ((StatefulNode)this.node).getStatefulIndex();
        } else {
            this.statefulIndex = -1;
        }

        this.isSource = node instanceof Source;
        this.useBuffer = !this.isSource;
        this.schedStrategy = SchedStrategy.FCFS;

        this.jobClasses = node.getModel().getClasses();
        this.serviceRates = new Double[this.jobClasses.size()];
        this.serviceProcesses = new Distribution[this.jobClasses.size()];
        this.jobClassCdf = new Cdf<JobClass>(random);

        if (node instanceof HasSchedStrategy) {
            HasSchedStrategy nodeS = (HasSchedStrategy)node;
            this.schedStrategy = ((HasSchedStrategy)node).getSchedStrategy();

            int i = 0;
            for (JobClass jobClass : this.jobClasses) {
                this.serviceProcesses[i] = nodeS.getServiceProcess(jobClass);
                if ((this.serviceProcesses[i] instanceof Exp) ||
                        (this.serviceProcesses[i] instanceof Immediate)) {
                    throw new RuntimeException("Invalid distribution for combined departure.");
                }
                i++;
            }
        } else {
            for (int i = 0; i < this.jobClasses.size(); i++) {
                this.serviceProcesses[i] = new Immediate();
            }
        }
    }

    @Override
    public double getRate(StateMatrix stateMatrix) {
        double totalRate = 0;

        if (this.node instanceof StatefulNode) {
            for (int i = 0; i < this.serviceProcesses.length; i++) {
                int activeServers = 1;
                if (!(this.node instanceof Source)) {
                    activeServers = stateMatrix.inProcess(this.statefulIndex, this.classIndex);
                }
                if (activeServers != 0) {
                    if (this.serviceProcesses[i] instanceof Immediate) {
                        return Double.POSITIVE_INFINITY;
                    }
                    this.serviceRates[i] = this.serviceProcesses[i].getRate()*activeServers;
                    totalRate += this.serviceRates[i];
                }
            }
        }

        int serviceRateIter = 0;
        for (JobClass jobClass : this.jobClasses) {
            this.jobClassCdf.addElement(jobClass, this.serviceRates[serviceRateIter]/totalRate);
        }

        if (totalRate == 0) {
            return Double.NaN;
        }

        return totalRate;
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        JobClass jobClass = this.jobClassCdf.generate();
        if (this.node instanceof Source) {
            return this.node.getOutputEvent(jobClass, random).stateUpdate(stateMatrix, random, timeline);
        }

        boolean res = stateMatrix.stateDeparture(this.statefulIndex, classIndex);
        if (!res) {
            return false;
        }

        this.node.getOutputEvent(jobClass, random).stateUpdate(stateMatrix, random, timeline);
        return true;
    }

    @Override
    public void printSummary() {
        System.out.format("Combined departure event at %s\n", this.node.getName());
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        int res = 0;

        if (this.node instanceof Source) {
            for (int i = 0; i < n; i++) {
                if (!this.node.getOutputEvent(this.jobClassCdf.generate(), random).stateUpdate(stateMatrix, random, timeline)) {
                    res++;
                }
            }
        } else {
            Pair<Map<Integer, Integer>, Integer> outputRes = stateMatrix.stateDepartureN(n, this.statefulIndex);

            for (int jobClassIdx : outputRes.getLeft().keySet()) {
                int numDepartures = outputRes.getLeft().get(jobClassIdx);
                this.node.getOutputEvent(this.jobClasses.get(jobClassIdx), random).stateUpdateN(numDepartures, stateMatrix, random, timeline);
            }
            res = outputRes.getRight();
        }

        return res;
    }

    @Override
    public int getMaxRepetitions(StateMatrix stateMatrix) {
        if (this.node instanceof Source) {
            return Integer.MAX_VALUE;
        }

        return stateMatrix.getState(this.statefulIndex, this.classIndex);
    }

    public Node getNode() {
        return this.node;
    }

    public int getNodeStatefulIdx() {
        return this.statefulIndex;
    }
    public int getClassIdx() {
        return this.classIndex;
    }

    public boolean isStateful() {
        return this.statefulIndex != -1;
    }
}
