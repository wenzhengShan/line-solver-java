package StochLib;

import SimUtil.Distribution;

import java.io.Serializable;

public class ServiceProcess implements Serializable {
    private JobClass jobClass;
    private ServiceStrategy serviceStrategy;
    private Distribution distribution;
    public ServiceProcess(JobClass jobClass, ServiceStrategy serviceStrategy) {
        this.jobClass = jobClass;
        this.serviceStrategy = serviceStrategy;
    }
    public ServiceProcess(JobClass jobClass, ServiceStrategy serviceStrategy, Distribution distribution) {
        this (jobClass, serviceStrategy);
        this.distribution = distribution;
    }


    public final JobClass getJobClass() {
        return this.jobClass;
    }
    public final Distribution getDistribution() { return this.distribution; }
}
