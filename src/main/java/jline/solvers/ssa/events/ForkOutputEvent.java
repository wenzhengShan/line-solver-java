package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.lang.sections.OutputSection;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;

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
