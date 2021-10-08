package SolverSSA;

import SimUtil.MAPProcess;

import java.util.Random;

public class MAPPhaseEvent extends PhaseEvent {
    MAPProcess mapProcess;
    public MAPPhaseEvent(MAPProcess mapProcess) {
        super();
        this.mapProcess = mapProcess;
    }

    @Override
    public long getNPhases() {
        return mapProcess.getNumberOfPhases();
    }

    @Override
    public double getRate(StateMatrix stateMatrix) {
        return (long)mapProcess.getTotalPhaseRate(stateMatrix.getPhase(this));
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        timeline.record(this, stateMatrix);
        return stateMatrix.phaseUpdate(this, this.mapProcess.getNextPhase(stateMatrix.getPhase(this), random));
    }

    @Override
    public int stateUpdateN(int n,StateMatrix stateMatrix, Random random, Timeline timeline) {
        int res = n;
        for (int i = 0; i < n; i++) {
            if(stateMatrix.phaseUpdate(this, this.mapProcess.getNextPhase(stateMatrix.getPhase(this), random))) {
                res--;
            }
        }

        timeline.preRecord(this, stateMatrix, n-res);

        return res;
    }
}
