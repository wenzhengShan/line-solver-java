package jline.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dispatcher extends OutputSection implements Serializable {
    public Dispatcher(List<JobClass> customerClasses) {
        super("jline.Dispatcher");
    }
}
