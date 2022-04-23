package jline.solvers.ssa.events;

import jline.lang.JobClass;
import jline.lang.nodes.Node;
import jline.lang.sections.OutputSection;
import jline.solvers.ssa.Timeline;
import jline.solvers.ssa.state.StateMatrix;

import java.util.Random;

public class JoinOutputEvent extends OutputEvent {
    protected boolean oneWaiting;
    public JoinOutputEvent(OutputSection outputSection, Node targetNode, JobClass jobClass) {
        super(outputSection, targetNode, jobClass);
        this.oneWaiting = false;
    }
    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        if (this.oneWaiting) {
            this.oneWaiting = false;
            return this.targetNode.getArrivalEvent(this.jobClass).stateUpdateN(1, stateMatrix, random, timeline) == 0;
        }

        this.oneWaiting = true;
        return true;
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        if (this.oneWaiting) {
            n += 1;
        }

        if ((n % 2) == 1) {
            this.oneWaiting = true;
            n -= 1;
        }
        return this.targetNode.getArrivalEvent(this.jobClass).stateUpdateN(n/2, stateMatrix, random, timeline);
    }
}