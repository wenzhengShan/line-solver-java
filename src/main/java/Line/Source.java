package Line;

import SimUtil.DisabledDistribution;
import SimUtil.Distribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Source extends Station implements HasSchedStrategy, Serializable {
    protected List<ServiceProcess> serviceProcesses;
    protected SchedStrategy schedStrategy;

    public Source(String name) {
        super(name);
        this.numberOfServers = 1;
    }
    public Source(Network model, String name) {
        this(name);

        List<JobClass> jobClasses = model.getClasses();
        this.output = new Dispatcher(jobClasses);
        this.server = new ServiceTunnel();
        this.input = new RandomSource(jobClasses);
        this.schedStrategy = SchedStrategy.EXT;
        this.setModel(model);
        this.model.addNode(this);
        this.serviceProcesses = new ArrayList<ServiceProcess>();

        for (JobClass jobClass : jobClasses) {
            this.classCap.put(jobClass, Double.POSITIVE_INFINITY);
            // HACK!!!!!!!!!!!!!!
            this.setArrivalDistribution(jobClass, new DisabledDistribution());
        }
    }

    public void setArrivalDistribution(JobClass jobClass, Distribution distribution) {
        ServiceProcess arrivalProcess = new ServiceProcess(jobClass, ServiceStrategy.LI, distribution);
        this.input.setServiceProcess(arrivalProcess);
        this.setServiceProcess(arrivalProcess);
        if (distribution == null) {
            this.classCap.put(jobClass, 0.0);
        } else {
            this.classCap.put(jobClass, Double.POSITIVE_INFINITY);
        }
    }

    protected void removeServiceProcess(JobClass jobClass) {
        Iterator<ServiceProcess> serviceProcessIterator = this.serviceProcesses.iterator();
        while (serviceProcessIterator.hasNext()) {
            if (serviceProcessIterator.next().getJobClass() == jobClass) {
                serviceProcessIterator.remove();
            }
        }
    }

    public void setServiceProcess(ServiceProcess arrivalProcess) {
        removeServiceProcess(arrivalProcess.getJobClass());
        serviceProcesses.add(arrivalProcess);
    }

    public final Distribution getServiceProcess(JobClass jobClass) {
        for (ServiceProcess serviceProcess : this.serviceProcesses) {
            if (serviceProcess.getJobClass() == jobClass) {
                return serviceProcess.getDistribution();
            }
        }

        return new DisabledDistribution();
    }

    public Distribution getArrivalDistribution(JobClass jobClass) {
        for (ServiceProcess serviceProcess : this.serviceProcesses) {
            if (serviceProcess.getJobClass() == jobClass) {
                return serviceProcess.getDistribution();
            }
        }
        return new DisabledDistribution();

    }
    @Override
    public void printSummary() {
        System.out.format("Line.Source:\n");
        System.out.format("--Name: %s\n", this.getName());
        System.out.format("--Arrival Processes:\n");
        for (JobClass jobClass : this.model.getClasses()) {
            System.out.format("----%s: %s\n", jobClass.getName(), this.getArrivalDistribution(jobClass).toString());
        }

        this.output.printSummary();
    }
    public SchedStrategy getSchedStrategy() {
        return this.schedStrategy;
    }

    public double minRate() {
        double acc = Double.POSITIVE_INFINITY;
        for (ServiceProcess serviceProcess : this.serviceProcesses) {
            double dRate = serviceProcess.getDistribution().getRate();
            if (dRate != 0) {
                acc = Math.min(acc, dRate);
            }
        }
        return acc;
    }
    public double maxRate() {
        double acc = 0;
        for (ServiceProcess serviceProcess : this.serviceProcesses) {
            if (serviceProcess.getDistribution().getRate() == Double.POSITIVE_INFINITY) {
                continue;
            }
            acc = Math.max(acc, serviceProcess.getDistribution().getRate());
        }
        return acc;
    }
    public double avgRate() {
        double acc = 0;
        for (ServiceProcess serviceProcess : this.serviceProcesses) {
            double accVal = serviceProcess.getDistribution().getRate();
            if ((accVal == Double.POSITIVE_INFINITY) || (accVal == 0)) {
                continue;
            }
            acc += accVal;
        }
        return acc;
    }

    public int rateCt() {
        int acc = 0;
        for (ServiceProcess serviceProcess : this.serviceProcesses) {
            double accVal = serviceProcess.getDistribution().getRate();
            if ((accVal == Double.POSITIVE_INFINITY) || (accVal == 0)) {
                continue;
            }
            acc++;
        }
        return acc;
    }
}
