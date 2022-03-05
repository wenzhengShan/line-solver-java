package Line;

import java.io.Serializable;

public class Fork extends Node implements Serializable {
    protected Network model;
    public Fork(Network model) {
        super("Fork");
        model.addNode(this);
        this.model = model;
        this.output = new Forker(model);
    }

    @Override
    public Network getModel() {
        return this.model;
    }
}
