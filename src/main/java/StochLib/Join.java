package StochLib;

import java.io.Serializable;

public class Join extends Node implements Serializable {
    protected Network model;
    public Join(Network model) {
        super("Join");
        model.addNode(this);
        this.model = model;
        this.output = new Joiner(model);
    }

    @Override
    public Network getModel() {
        return this.model;
    }
}
