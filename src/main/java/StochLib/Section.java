package StochLib;

import java.io.Serializable;

public abstract class Section extends NetworkElement implements Serializable {
    String className;
    public Section(String className) {
        super("StochLib.Section");
        this.className = className;
    }
}
