package SolverSSA;

import StochLib.JobClass;
import StochLib.OutputSection;
import StochLib.Node;

import java.util.Random;

public class OutputEvent extends Event {
    protected OutputSection outputSection;
    protected Node targetNode;
    protected JobClass jobClass;
    protected boolean isClassSwitched;
    protected int jobClassIdx;

    public OutputEvent(OutputSection outputSection, Node targetNode, JobClass jobClass) {
        super();
        this.jobClass = jobClass;
        this.targetNode = targetNode;
        this.isClassSwitched = false;
        this.jobClassIdx = jobClass.getJobClassIdx();
    }

    public OutputEvent(OutputSection outputSection, Node targetNode, JobClass jobClass, boolean isClassSwitched) {
        this(outputSection, targetNode, jobClass);
        this.isClassSwitched = isClassSwitched;
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        return this.targetNode.getArrivalEvent(this.jobClass).stateUpdate(stateMatrix, random, timeline);
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        return this.targetNode.getArrivalEvent(this.jobClass).stateUpdateN(n, stateMatrix, random, timeline);
    }

    public boolean isClassSwitched() {
        return this.isClassSwitched;
    }

    public int getClassIdx() {
        return this.jobClassIdx;
    }
}
