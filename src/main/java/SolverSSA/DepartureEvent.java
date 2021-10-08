package SolverSSA;

import SimUtil.*;
import StochLib.*;

import javax.swing.plaf.nimbus.State;
import java.util.Random;

public class DepartureEvent extends Event implements NodeEvent {
    protected int statefulIndex;
    protected int classIndex;
    protected boolean useBuffer;
    protected SchedStrategy schedStrategy;
    protected boolean isSource;
    protected final Distribution serviceProcess;
    protected Node node;
    protected JobClass jobClass;
    protected PhaseEvent phaseEvent;
    protected boolean isMAP;
    protected boolean isReference;

    public static Event fromNodeAndClass(Node node, JobClass jobClass) {
        if (node instanceof HasSchedStrategy) {
            Distribution serviceDist = ((HasSchedStrategy)node).getServiceProcess(jobClass);
            if (serviceDist instanceof Erlang) {
                DepartureEvent depEvent = new DepartureEvent(node, jobClass);
                ErlangPhaseEvent ePhase = new ErlangPhaseEvent(node, jobClass, depEvent);
                depEvent.setPhaseEvent(ePhase);
                return ePhase;
            } else if (serviceDist instanceof MAPProcess) {
                MAPPhaseEvent mapPhaseEvent = new MAPPhaseEvent((MAPProcess) serviceDist);
                return new DepartureEvent(node, jobClass, mapPhaseEvent);
            }
        }

        return new DepartureEvent(node, jobClass);
    }

    public DepartureEvent(Node node, JobClass jobClass) {
        super();
        this.node = node;
        this.jobClass = jobClass;


        if (node instanceof StatefulNode) {
            this.statefulIndex = ((StatefulNode)this.node).getStatefulIndex();
        } else {
            this.statefulIndex = -1;
        }

        if (node instanceof Source) {
            this.isReference = true;
        } else if (node instanceof ClassSwitch) {
            this.isReference = true;
        } else if (jobClass instanceof ClosedClass) {
            if (node == ((ClosedClass) jobClass).getRefstat()) {
                this.isReference = true;
            }
        } else {
            this.isReference = false;
        }

        this.classIndex = this.node.getModel().getJobClassIndex(this.jobClass);

        this.isSource = node instanceof Source;
        this.useBuffer = !this.isSource;
        this.schedStrategy = SchedStrategy.FCFS;
        if (node instanceof HasSchedStrategy) {
            this.schedStrategy = ((HasSchedStrategy)node).getSchedStrategy();
            this.serviceProcess = ((HasSchedStrategy)node).getServiceProcess(this.jobClass);
        } else {
            this.serviceProcess = new Immediate();
        }

        this.phaseEvent = null;
        this.isMAP = (this.serviceProcess) instanceof MAPProcess;
    }

    public DepartureEvent(Node node, JobClass jobClass, PhaseEvent phaseEvent) {
        this(node, jobClass);
        this.phaseEvent = phaseEvent;
    }

    public void setPhaseEvent(PhaseEvent phaseEvent) {
        this.phaseEvent = phaseEvent;
    }

    public PhaseEvent getPhaseEvent() {
        return this.phaseEvent;
    }

    @Override
    public double getRate(StateMatrix stateMatrix) {
        int activeServers = 1;

        if (this.node instanceof StatefulNode) {
            activeServers = stateMatrix.inProcess(this.statefulIndex, this.classIndex);
            if (this.node instanceof Source) {
                activeServers = 1;
            } else if (activeServers == 0) {
                return Double.NaN;
            }
        }

        if (this.serviceProcess instanceof Immediate) {
            return Double.POSITIVE_INFINITY;
        } else if (this.serviceProcess instanceof Exp) {
            return this.serviceProcess.getRate()*activeServers;
        } else if (this.serviceProcess instanceof DisabledDistribution) {
            return Double.NaN;
        } else if (this.serviceProcess instanceof Erlang) {
            // Rate logic should be handled by PhaseEvent
            return Double.NaN;
        } else if (this.serviceProcess instanceof MAPProcess) {
            return ((MAPProcess)this.serviceProcess).getDepartureRate(stateMatrix.getPhase(this.phaseEvent))*activeServers;
        }

        return Double.NaN;
    }

    @Override
    public boolean stateUpdate(StateMatrix stateMatrix, Random random, Timeline timeline) {
        if (this.isMAP) {
            MAPProcess mapProcess = (MAPProcess)(this.serviceProcess);
            int nextPhase = mapProcess.getNextPhaseAfterDeparture(stateMatrix.getPhase(this.phaseEvent), random);
            stateMatrix.phaseUpdate(this.phaseEvent, nextPhase);
        }

        if (this.node instanceof Source) {
            if (this.node.getOutputEvent(this.jobClass, random).stateUpdate(stateMatrix, random, timeline)) {
                timeline.record(this, stateMatrix);
                return true;
            }
            return false;
        }

        boolean res = stateMatrix.stateDeparture(this.statefulIndex, classIndex);
        if (!res) {
            return false;
        }

        this.node.getOutputEvent(this.jobClass, random).stateUpdate(stateMatrix, random, timeline);


        timeline.record(this, stateMatrix);

        return true;
    }

    @Override
    public void printSummary() {
        System.out.format("Departure event for %s at %s\n", this.jobClass.getName(), this.node.getName());
    }

    @Override
    public int stateUpdateN(int n, StateMatrix stateMatrix, Random random, Timeline timeline) {
        int res = 0;

        if (this.isMAP) {
            MAPProcess mapProcess = (MAPProcess)(this.serviceProcess);
            int nextPhase = mapProcess.getNextPhaseAfterDeparture(stateMatrix.getPhase(this.phaseEvent), random);
            stateMatrix.phaseUpdate(this.phaseEvent, nextPhase);
        }

        if (this.node instanceof Source) {
            res = this.node.getOutputEvent(this.jobClass, random).stateUpdateN(n, stateMatrix, random, timeline);
        } else {
            res = stateMatrix.stateDepartureN(n, this.statefulIndex, classIndex);
            this.node.getOutputEvent(this.jobClass, random).stateUpdateN(n-res, stateMatrix, random, timeline);
        }

        timeline.preRecord(this, stateMatrix, n-res);

        return res;
    }

    @Override
    public int getMaxRepetitions(StateMatrix stateMatrix) {
        if (this.node instanceof Source) {
            return Integer.MAX_VALUE;
        }

        return stateMatrix.getState(this.statefulIndex, this.classIndex);
    }

    public Node getNode() {
        return this.node;
    }

    public JobClass getJobClass() { return this.jobClass; }

    public int getNodeStatefulIdx() {
        return this.statefulIndex;
    }
    public int getClassIdx() {
        return this.classIndex;
    }

    public boolean isStateful() {
        return this.statefulIndex != -1;
    }

    public boolean isReference() {
        return this.isReference;
    }
}
