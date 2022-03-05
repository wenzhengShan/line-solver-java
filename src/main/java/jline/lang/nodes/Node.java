package jline.lang.nodes;

import jline.lang.JobClass;
import jline.lang.Network;
import jline.lang.NetworkElement;
import jline.lang.OutputStrategy;
import jline.lang.constant.DropStrategy;
import jline.lang.constant.RoutingStrategy;
import jline.lang.sections.InputSection;
import jline.lang.sections.OutputSection;
import jline.lang.sections.ServiceSection;
import jline.solvers.ssa.events.ArrivalEvent;
import jline.solvers.ssa.events.NodeArrivalEvent;
import jline.solvers.ssa.events.OutputEvent;

import java.io.Serializable;
import java.util.*;

public class Node extends NetworkElement implements Serializable {
    public Network model;
    protected InputSection input;
    protected OutputSection output;
    protected ServiceSection server;
    protected DropStrategy dropStrategy;

    protected Map<JobClass, ArrivalEvent> arrivalEvents;

    protected int statefulIdx;

    public Node(String nodeName) {
        super(nodeName);
        this.arrivalEvents = new HashMap<JobClass, ArrivalEvent>();

        this.output = new OutputSection("Generic Output");
        this.input = new InputSection("Generic Input");
        this.dropStrategy = DropStrategy.Drop;
        this.statefulIdx = -1;
    }

    public void setModel(Network model) {
        this.model = model;
    }

    public Network getModel() {
        return this.model;
    }

    public void setRouting(JobClass jobClass, RoutingStrategy routingStrategy, Node destination, double probability) {
        this.output.setOutputStrategy(jobClass, routingStrategy, destination, probability);
    }

    public void resetRouting() {
        this.output.resetRouting();
    }

    public RoutingStrategy getRoutingStrategy(JobClass jobClass) {
        for (OutputStrategy outputStrategy : this.output.getOutputStrategies()) {
            if (outputStrategy.getDestination() != null) {
                continue;
            }

            if (outputStrategy.getJobClass() == jobClass) {
                return outputStrategy.getRoutingStrategy();
            }
        }

        return RoutingStrategy.RAND;
    }

    public void printSummary() {
        System.out.format("jline.Node: %s\n", this.getName());
        this.output.printSummary();
    }

    public double getClassCap(JobClass jobClass) {
        return Double.POSITIVE_INFINITY;
    }

    public double getCap() { return Double.POSITIVE_INFINITY; }

    public ArrivalEvent getArrivalEvent(JobClass jobClass) {
        if (!this.arrivalEvents.containsKey(jobClass)) {
            this.arrivalEvents.put(jobClass, new NodeArrivalEvent(this, jobClass));
        }
        return this.arrivalEvents.get(jobClass);
    }

    public OutputEvent getOutputEvent(JobClass jobClass, Random random) {
        return this.output.getOutputEvent(jobClass, random);
    }

    public List<OutputStrategy> getOutputStrategies() {
        return this.output.getOutputStrategies();
    }

    public DropStrategy getDropStrategy() {
        return this.dropStrategy;
    }

    public boolean isRefstat() { return false; }

    public int getStatefulIdx() {
        if (this.statefulIdx == -1) {
            this.statefulIdx = this.model.getStatefulNodeIndex(this);
        }

        return this.statefulIdx;
    }
}
