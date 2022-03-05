package Line;

import SimUtil.Exp;

import java.io.Serializable;
import java.util.List;

public class Server extends ServiceSection implements Serializable {
    public Server(List<JobClass> jobClasses) {
        super("Line.Server");
        this.numberOfServers = 1;
    }

    private void initServers(List<JobClass> jobClasses) {
        for (JobClass jobClass : jobClasses) {
            this.serviceProcesses.put(jobClass, new ServiceProcess(jobClass, ServiceStrategy.LI, new Exp(0)));
        }
    }

}
