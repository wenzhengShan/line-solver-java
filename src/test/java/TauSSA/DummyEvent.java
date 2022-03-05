package jline.solvers.ssa;//package SolverSSA;

import jline.lang.*;
import jline.lang.*;

import java.util.Random;

public class DummyEvent extends Event {
    private Distribution distribution;
    private int nodeIdx;
    private int classIdx;
    private int stateDiff;
    public DummyEvent(Distribution distribution, int nodeIdx, int classIdx, int stateDiff) {
        this.distribution = distribution;
        this.nodeIdx = nodeIdx;
        this.classIdx = classIdx;
        this.stateDiff = stateDiff;
    }

    @Override
    public double getRate(StateMatrix stateMatrix) {
        if (this.distribution instanceof Immediate) {
            return Double.POSITIVE_INFINITY;
        } else if (this.distribution instanceof DisabledDistribution) {
            return Double.NaN;
        }
        return this.distribution.getRate();
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        stateMatrix.setState(this.nodeIdx, this.classIdx, stateMatrix.getState(this.nodeIdx, this.classIdx)+this.stateDiff);
        return true;
    }
}
