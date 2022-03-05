package Line;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSwitcher extends ServiceSection implements Serializable {
    protected List<JobClass> jobClasses;
    public ClassSwitcher(List<JobClass> jobClasses, String name) {
        super(name);

        this.jobClasses = jobClasses;
        this.numberOfServers = 1;
        this.serviceProcesses = new HashMap<JobClass, ServiceProcess>();
    }
}
