package Line;

import java.io.Serializable;

public class Delay extends Queue implements Serializable {
    public Delay(Network model, String name) {
        super(model, name, SchedStrategy.INF);
        this.numberOfServers = Integer.MAX_VALUE;
    }
}
