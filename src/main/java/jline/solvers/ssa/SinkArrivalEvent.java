package jline.solvers.ssa;

import java.util.Random;

import jline.lang.nodes.Node;

public class SinkArrivalEvent extends ArrivalEvent{
    public SinkArrivalEvent(Node node) {
        super(node, null);
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        return true;
    }
    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        return 0;
    }
}
