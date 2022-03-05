package jline.lang;

import jline.solvers.ssa.SinkArrivalEvent;
import jline.solvers.ssa.ArrivalEvent;

import java.io.Serializable;

public class Sink extends Node implements Serializable {
    protected SchedStrategy schedStrategy;
    protected jline.solvers.ssa.ArrivalEvent arrivalEvent;

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
        System.out.format("jline.Sink: %s\n", this.getName());
    }


    @Override
    public ArrivalEvent getArrivalEvent(JobClass jobClass) {
        return this.arrivalEvent;
    }
}
