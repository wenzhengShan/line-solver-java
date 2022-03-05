package TauSSA;

import Line.JobClass;
import Line.Node;
import Line.OutputSection;

import java.util.Random;

public class ForkOutputEvent extends OutputEvent {
    public ForkOutputEvent(OutputSection outputSection, Node targetNode, JobClass jobClass) {
        super(outputSection, targetNode, jobClass);
    }
    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        return this.targetNode.getArrivalEvent(this.jobClass).stateUpdateN(2, stateMatrix, random, timeline) == 0;
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        return this.targetNode.getArrivalEvent(this.jobClass).stateUpdateN(n*2, stateMatrix, random, timeline);
    }
}
