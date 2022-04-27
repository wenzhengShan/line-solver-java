package jline.solvers.ssa.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PhaseList {
    protected int nPhases;
    protected List<Integer> phases;
    protected int phase;
    protected Random random;

    public PhaseList(int nPhases, Random random) {
        this.nPhases = nPhases;
        this.phases = new ArrayList<Integer>();
        this.random = random;
        phase = 0;
    }

    public PhaseList(int nPhases) {
        this(nPhases,new Random());
    }

    public boolean updatePhase(int serverCt) {
        int phaseIdx = random.nextInt(serverCt);
        if (phaseIdx >= this.phases.size()) {
            if (this.nPhases == 1) {
                return true;
            }
            this.phases.add(1);
            return false;
        }

        int curPhase = this.phases.get(phaseIdx);
        if (curPhase == (nPhases-1)) {
            int disam = this.phases.remove(phaseIdx);
            return true;
        }
        this.phases.set(phaseIdx, curPhase+1);
        return false;
    }

    public void setPhase(int newPhase) {
        this.phase = newPhase;
    }

    public int getPhase() {
        return this.phase;
    }

    public int getListSize() {
        return this.phases.size();
    }

    public boolean updatePhase() {
        int nServers = this.phases.size();
        return updatePhase(nServers+1);
    }

    public List<Integer> getActivePhases(int serverCt) {
        return this.phases.subList(0, serverCt);
    }
}
