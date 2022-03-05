package Line;

import TauSSA.ForkOutputEvent;

import java.util.List;

public class Forker extends OutputSection {
    protected List<JobClass> jobClasses;
    public Forker(Network model) {
        super("Forker");
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
        outputEvents.put(outputStrategy, new ForkOutputEvent(this, destination, jobClass));
        this.probabilityUpdate();
    }
}
