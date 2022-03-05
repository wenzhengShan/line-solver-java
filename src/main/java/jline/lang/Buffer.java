package jline.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Buffer extends InputSection implements Serializable {
    protected int size;
    public Buffer(List<JobClass> classes) {
        super("Buffer");

        this.size = -1;
        this.schedPolicy = SchedStrategyType.NP;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
