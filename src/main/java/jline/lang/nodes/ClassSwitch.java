package jline.lang.nodes;

import jline.solvers.ssa.events.ArrivalEvent;
import jline.solvers.ssa.events.ClassSwitchArrivalEvent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jline.lang.*;
import jline.lang.constant.RoutingStrategy;
import jline.lang.constant.SchedStrategy;
import jline.lang.constant.SchedStrategyType;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;

public class ClassSwitch extends Node implements Serializable {
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
    
    public void setCsMatrix(JobClass originClass, JobClass targetClass, double probability) {
    	Map<JobClass, Double> map = csMatrix.getOrDefault(originClass, new HashMap<JobClass, Double>());
    	double p = map.getOrDefault(targetClass, 0.0);
    	map.put(targetClass, p + probability);
    	this.csMatrix.put(originClass, map);
    }

    @Override
    public ArrivalEvent getArrivalEvent(JobClass jobClass) {
        if (!this.arrivalEvents.containsKey(jobClass)) {
            this.arrivalEvents.put(jobClass, new ClassSwitchArrivalEvent(this, jobClass, this.csMatrix));
        }
        return this.arrivalEvents.get(jobClass);
    }
}
