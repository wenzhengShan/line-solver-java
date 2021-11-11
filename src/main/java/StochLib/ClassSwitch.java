package StochLib;

import TauSSA.ArrivalEvent;
import TauSSA.ClassSwitchArrivalEvent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ClassSwitch extends Station implements Serializable {
    protected SchedStrategyType schedPolicy;
    protected SchedStrategy schedStrategy;

    protected Map<JobClass, Map<JobClass, Double>> csMatrix;

    public ClassSwitch(Network model, String name, Map<JobClass, Map<JobClass, Double>> csMatrix) {
        super(name);

        List<JobClass> jobClasses = model.getClasses();
        this.input = new Buffer(jobClasses);
        this.output = new ClassSwitchOutputSection(jobClasses);

        this.csMatrix = csMatrix;

        this.setModel(model);
        this.model.addNode(this);

        this.schedPolicy = SchedStrategyType.NP;
        this.schedStrategy = SchedStrategy.FCFS;
        this.server = new StatelessClassSwitcher(jobClasses, csMatrix);
    }

    public void setProbRouting(JobClass jobClass, Node destination, double probability) {
        this.setRouting(jobClass, RoutingStrategy.PROB, destination, probability);
    }

    @Override
    public ArrivalEvent getArrivalEvent(JobClass jobClass) {
        if (!this.arrivalEvents.containsKey(jobClass)) {
            this.arrivalEvents.put(jobClass, new ClassSwitchArrivalEvent(this, jobClass, this.csMatrix));
        }
        return this.arrivalEvents.get(jobClass);
    }
}
