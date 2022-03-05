package Line;

import java.io.Serializable;
import java.util.List;

public class ClassSwitchOutputSection extends Dispatcher implements Serializable {
    public ClassSwitchOutputSection(List<JobClass> customerClasses) {
        super(customerClasses);
        this.className = "ClassSwitchDispatcher";
        this.isClassSwitch = true;
    }
}
