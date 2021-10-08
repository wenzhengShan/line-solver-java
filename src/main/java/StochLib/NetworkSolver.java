package StochLib;

import StochLib.Network;

import java.io.Serializable;

public class NetworkSolver implements Serializable {
    protected Network model;
    protected String name;

    public NetworkSolver(Network model, String name, Object options) {
        this.model = model;
        this.name = name;

    }
}
