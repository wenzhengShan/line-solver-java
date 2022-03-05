package jline.lang.nodes;

import java.io.Serializable;

import jline.lang.*;
import jline.lang.sections.Joiner;

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
