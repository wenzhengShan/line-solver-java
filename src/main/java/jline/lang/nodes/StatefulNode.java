package jline.lang.nodes;
import java.io.Serializable;
import jline.lang.*;
import jline.solvers.ssa.events.ArrivalEvent;

public class StatefulNode extends Node implements Serializable {
    private Integer statefulIndex;
    public StatefulNode(String name) {
        super(name);
        statefulIndex = null;
    }

    protected void clearState()  {
        throw new RuntimeException("Not Implemented!");
    }

    public int getStatefulIndex() {
        if (this.statefulIndex == null) {
            this.statefulIndex = this.model.getStatefulNodeIndex((Node)this);
        }
        return this.statefulIndex;
    }

    public int getNumberOfServers() {
        return 1;
    }

    @Override
    public ArrivalEvent getArrivalEvent(JobClass jobClass) {
        if (!this.arrivalEvents.containsKey(jobClass)) {
            this.arrivalEvents.put(jobClass, new ArrivalEvent(this, jobClass));
        }
        return this.arrivalEvents.get(jobClass);
    }
}
