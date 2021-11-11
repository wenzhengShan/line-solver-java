package StochLib;

import java.io.Serializable;
import java.util.List;
import TauSSA.JoinOutputEvent;

public class Joiner extends OutputSection implements Serializable {
    protected List<JobClass> jobClasses;
    public Joiner(Network model) {
        super("Joiner");
        this.jobClasses = model.getClasses();
    }

    @Override
    public void setOutputStrategy(JobClass jobClass, RoutingStrategy routingStrategy, Node destination, double probability) {
        for (OutputStrategy outputStrategy : this.outputStrategies) {
            if ((outputStrategy.getJobClass() == jobClass) && (outputStrategy.getDestination() == destination)) {
                outputStrategy.setRoutingStrategy(routingStrategy);
                outputStrategy.setProbability(probability);
                this.probabilityUpdate();
                return;
            }
        }

        OutputStrategy outputStrategy = new OutputStrategy(jobClass, routingStrategy, destination, probability);
        outputStrategies.add(outputStrategy);
        outputEvents.put(outputStrategy, new JoinOutputEvent(this, destination, jobClass));
        this.probabilityUpdate();
    }
}
