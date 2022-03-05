package Line;

import java.io.Serializable;

public abstract class Section extends NetworkElement implements Serializable {
    String className;
    public Section(String className) {
        super("Line.Section");
        this.className = className;
    }
}
