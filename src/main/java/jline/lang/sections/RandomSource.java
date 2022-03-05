package jline.lang.sections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jline.lang.*;
import jline.lang.constant.ServiceStrategy;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;

public class RandomSource extends InputSection implements Serializable {
    protected List<ServiceProcess> serviceProcesses;
    public RandomSource(List<JobClass> jobClasses) {
        super("jline.RandomSource");
        serviceProcesses = new ArrayList<ServiceProcess>();

        for (JobClass jobClass : jobClasses) {
            serviceProcesses.add(new ServiceProcess(jobClass, ServiceStrategy.LI));
        }
    }
    public void setServiceProcess(ServiceProcess serviceProcess) {
        removeServiceProcess(serviceProcess.getJobClass());
        serviceProcesses.add(serviceProcess);
    }

    public void removeServiceProcess(JobClass jobClass) {
        Iterator<ServiceProcess> serviceProcessIterator = this.serviceProcesses.iterator();
        while (serviceProcessIterator.hasNext()) {
            if (serviceProcessIterator.next().getJobClass() == jobClass) {
                serviceProcessIterator.remove();
            }
        }
    }
}
