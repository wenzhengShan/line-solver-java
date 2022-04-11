package jline.lang;

import java.io.Serializable;

import jline.lang.constant.JobClassType;
import jline.lang.nodes.Node;

public class JobClass extends NetworkElement implements Serializable {
    protected JobClassType type;
    protected int priority;
    protected boolean completes;
    protected Node reference;

    public JobClass(JobClassType type, String name) {
        super(name);
        this.priority = 0;
        this.reference = new Node("Unallocated");
        this.type = type;
        this.completes = true;
    }

    public void setReference(Node source) throws Exception {
        this.reference = source;
    }

    public boolean isReferenceStation(Node node) {
        return name.equals(node.getName());
    }

    public void printSummary() {
        System.out.format("Job Class: %s\n",this.getName());
        System.out.println("");
    }

    public double getNumberOfJobs() {
        return Double.POSITIVE_INFINITY;
    }

    public int getJobClassIdx() {
        return -1;
    }
}
