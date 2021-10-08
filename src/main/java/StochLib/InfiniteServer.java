package StochLib;

import java.io.Serializable;
import java.util.List;

public class InfiniteServer extends Server implements Serializable {
    public InfiniteServer(List<JobClass> jobClasses) {
        super(jobClasses);
        this.numberOfServers = Double.POSITIVE_INFINITY;
        this.className = "StochLib.InfiniteServer";
    }
}
