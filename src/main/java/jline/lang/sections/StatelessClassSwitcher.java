package jline.lang.sections;

import java.io.Serializable;
import java.util.List;
import jline.lang.*;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;

public class StatelessClassSwitcher extends ClassSwitcher implements Serializable {
    public StatelessClassSwitcher(List<JobClass> jobClasses, Object csMatrix) {
        super(jobClasses, "jline.StatelessClassSwitcher");
    }
}
