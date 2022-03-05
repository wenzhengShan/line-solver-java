package jline.lang;

import java.io.Serializable;

public abstract class Section extends NetworkElement implements Serializable {
    String className;
    public Section(String className) {
        super("jline.Section");
        this.className = className;
    }
}
