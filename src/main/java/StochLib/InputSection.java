package StochLib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InputSection extends Section  implements Serializable {
    protected SchedStrategyType schedPolicy;
    protected List<InputJobProcess> inputJobProcesses;

    public InputSection(String className) {
        super(className);
        this.inputJobProcesses = new ArrayList<InputJobProcess>();
    }

    public void setInputJobProcess(InputJobProcess process) {
        removeInputJobProcess(process.getJobClass());
        inputJobProcesses.add(process);
    }

    protected void removeInputJobProcess(JobClass jobClass) {
        Iterator<InputJobProcess> inputJobProcessIter = this.inputJobProcesses.iterator();
        while (inputJobProcessIter.hasNext()) {
            if (inputJobProcessIter.next().getJobClass() == jobClass) {
                inputJobProcessIter.remove();
            }
        }
    }

    public void setServiceProcess(ServiceProcess serviceProcess) {

    }
}
