package StochLib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dispatcher extends OutputSection implements Serializable {
    public Dispatcher(List<JobClass> customerClasses) {
        super("StochLib.Dispatcher");
    }
}
