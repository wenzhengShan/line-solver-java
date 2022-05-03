package jline.lang.nodes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import jline.lang.*;
import jline.solvers.ssa.events.DepartureEvent;

public class Station extends StatefulNode implements Serializable {
    protected int numberOfServers;
    protected double cap; // double to allow infinite values.
    protected int stationIndex;
    protected Map<JobClass, Double> classCap;
    protected double lldScaling;
    protected double lcdScaling;

    protected Map<JobClass, DepartureEvent> departureEvents;

    public Station(String name) {
        super(name);

        this.classCap = new HashMap<JobClass, Double>();
        this.departureEvents = new HashMap<JobClass, DepartureEvent>();

        this.cap = Double.POSITIVE_INFINITY;
    }

    public void setNumberOfServers(int numberOfServers) {
        this.numberOfServers = numberOfServers;
    }

    @Override
    public int getNumberOfServers() {
        return this.numberOfServers;
    }

    public void setCap(double cap) {
        this.cap = cap;
    }

    public void setCap(int cap) {
        this.cap = (double) cap;
    }

    public void setClassCap(JobClass jobClass, double cap) {
        this.classCap.put(jobClass, cap);
    }
    public void setClassCap(JobClass jobClass, int cap) {
        this.classCap.put(jobClass, (double)cap);
    }

    public void setChainCapacity() {

    }

    @Override
    public double getClassCap(JobClass jobClass) {
        if (classCap.containsKey(jobClass)) {
            return Math.min(this.classCap.get(jobClass), cap);
        }

        return cap;
    }

    @Override
    public double getCap() {
        return cap;
    }

    public boolean[] isServiceDefined() {
        throw new RuntimeException("Not Implemented!");
    }

    public boolean isServiceDefined(JobClass j_class)  {
        throw new RuntimeException("Not Implemented!");
    }

    public boolean[] isServiceDisabled()  {
        throw new RuntimeException("Not Implemented!");
    }

    public boolean isServiceDisabled(JobClass j_class)  {
        throw new RuntimeException("Not Implemented!");
    }

    public int[] getMarkovianSourceRates()  {
        throw new RuntimeException("Not Implemented!");

        //int[] outRates = {0, 0, 0};
    }

    public int[] getMarkovianServiceRates()  {
        throw new RuntimeException("Not Implemented!");

        //int[] outRates = {0, 0, 0};
    }
    public DepartureEvent getDepartureEvent(JobClass jobClass) {
        if (!this.departureEvents.containsKey(jobClass)) {
            this.departureEvents.put(jobClass, new DepartureEvent(this, jobClass));
        }
        return this.departureEvents.get(jobClass);
    }

    @Override
    public boolean isRefstat() {
        for (JobClass jobClass : this.model.getClasses()) {
            if (jobClass instanceof ClosedClass) {
                if (((ClosedClass)jobClass).getRefstat() == this) {
                    return true;
                }
            }
        }

        return false;
    }
    
    public void setLimitedLoadDependence(double alpha) {
    	this.lldScaling = alpha;
    }
    
    public void setLimitedClassDependence(double gamma) {
    	this.lcdScaling = gamma;
    }
}
