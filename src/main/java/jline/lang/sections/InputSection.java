package jline.lang.sections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jline.lang.*;
import jline.lang.constant.SchedStrategyType;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;


public class InputSection extends Section  implements Serializable {
    protected SchedStrategyType schedPolicy;
    protected List<Inputs> inputJobProcesses;

    public InputSection(String className) {
        super(className);
        this.inputJobProcesses = new ArrayList<Inputs>();
    }

    public void setInputJobProcess(Inputs process) {
        removeInputJobProcess(process.getJobClass());
        inputJobProcesses.add(process);
    }

    protected void removeInputJobProcess(JobClass jobClass) {
        Iterator<Inputs> inputJobProcessIter = this.inputJobProcesses.iterator();
        while (inputJobProcessIter.hasNext()) {
            if (inputJobProcessIter.next().getJobClass() == jobClass) {
                inputJobProcessIter.remove();
            }
        }
    }

    public void setServiceProcess(ServiceProcess serviceProcess) {

    }
}
