package Line;

import java.io.Serializable;
import java.util.List;

public class StatelessClassSwitcher extends ClassSwitcher implements Serializable {
    public StatelessClassSwitcher(List<JobClass> jobClasses, Object csMatrix) {
        super(jobClasses, "Line.StatelessClassSwitcher");
    }
}
