package Line;

import TauSSA.SinkArrivalEvent;
import TauSSA.ArrivalEvent;

import java.io.Serializable;

public class Sink extends Node implements Serializable {
    protected SchedStrategy schedStrategy;
    protected TauSSA.ArrivalEvent arrivalEvent;

    public Sink(Network model, String name) {
        super(name);


        if (model != null) {
            this.setModel(model);

            this.server = new ServiceSection("JobSink");
            this.model.addNode(this);
            this.setModel(model);
            this.schedStrategy = SchedStrategy.EXT;
        }

        this.arrivalEvent = new SinkArrivalEvent(this);
    }

    @Override
    public void printSummary() {
        System.out.format("Line.Sink: %s\n", this.getName());
    }


    @Override
    public ArrivalEvent getArrivalEvent(JobClass jobClass) {
        return this.arrivalEvent;
    }
}
