package StochLib;

import java.io.Serializable;

public class InputJobProcess implements Serializable {
    protected JobClass jobClass;
    protected SchedStrategyType schedPolicy;
    protected DropStrategy dropStrategy;
    public InputJobProcess(JobClass jobClass, SchedStrategyType schedPolicy, DropStrategy dropStrategy) {
        this.jobClass = jobClass;
        this.schedPolicy = schedPolicy;
        this.dropStrategy = dropStrategy;
    }

    public final JobClass getJobClass() {
        return this.jobClass;
    }
}
