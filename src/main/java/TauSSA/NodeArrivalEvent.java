package TauSSA;

import StochLib.JobClass;
import StochLib.Node;

import java.util.Random;

public class NodeArrivalEvent extends ArrivalEvent {
    public NodeArrivalEvent(Node node, JobClass jobClass) {
        super(node, jobClass);
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        OutputEvent nodeOutputEvent = node.getOutputEvent(jobClass, random);
        return nodeOutputEvent.stateUpdate(stateMatrix, random, timeline);
    }
    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        OutputEvent nodeOutputEvent = node.getOutputEvent(jobClass, random);
        return nodeOutputEvent.stateUpdateN(n, stateMatrix, random, timeline);
    }
}
