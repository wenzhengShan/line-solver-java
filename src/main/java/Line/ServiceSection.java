package Line;

import SimUtil.Distribution;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServiceSection extends Section implements Serializable {
    protected double numberOfServers;
    protected Map<JobClass, ServiceProcess> serviceProcesses;

    public ServiceSection(String className) {
        super(className);

        this.serviceProcesses = new HashMap<JobClass, ServiceProcess>();
    }

    public ServiceProcess getServiceProcess(JobClass jobClass) {
        return this.serviceProcesses.get(jobClass);
    }

    public Distribution getServiceDistribution(JobClass jobClass) {
        if (!this.serviceProcesses.containsKey(jobClass)) {
            return new Immediate();
        }
        return this.serviceProcesses.get(jobClass).getDistribution();
    }

    public void setServiceProcesses(ServiceProcess serviceProcess) {
        this.serviceProcesses.put(serviceProcess.getJobClass(), serviceProcess);
    }
}