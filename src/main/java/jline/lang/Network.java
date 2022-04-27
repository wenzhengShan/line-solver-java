package jline.lang;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jline.lang.constant.*;
import jline.lang.nodes.*;
import jline.lang.state.NetworkState;


public class Network extends Model implements Serializable {
    private boolean doChecks;
    private boolean hasState;
    private boolean logPath;
    private boolean usedFeatures;

    private List<Node> nodes;
    private List<JobClass> jobClasses;

    // caches
    private Map<Node, Map<JobClass, List<Node>>> classLinks;

    public Network(String modelName) {
        super(modelName);

        this.hasState = false;
        this.doChecks = true;

        this.nodes = new ArrayList<Node>();
        this.jobClasses = new ArrayList<JobClass>();

        this.classLinks = new HashMap<Node, Map<JobClass, List<Node>>>();
    }

    public void setDoChecks(boolean doChecks) {
        this.doChecks = doChecks;
    }

    public int[] getSize() {
        int[] outInt = new int[2];
        outInt[0] = this.getNumberOfNodes();
        outInt[1] = this.getNumberOfClasses();
        return outInt;
    }

    public boolean hasOpenClasses() {
        for (JobClass temp : this.jobClasses) {
            if (temp instanceof OpenClass) {
                return true;
            }
        }

        return false;
    }

    public int getJobClassIndex (JobClass jobClass) {
        return this.jobClasses.indexOf(jobClass);
    }

    public JobClass getJobClassFromIndex(int inIdx) {
        return this.jobClasses.get(inIdx);
    }

    public List<Integer> getIndexOpenClasses() {
        List<Integer> outList = new ArrayList<Integer>();
        for (int i = 0; i < this.jobClasses.size(); i++) {
            if (this.jobClasses.get(i) instanceof OpenClass) {
                outList.add(this.getJobClassIndex(this.jobClasses.get(i)));
            }
        }
        return outList;
    }

    public boolean hasClosedClasses() {
        for (JobClass temp : this.jobClasses) {
            if (temp instanceof ClosedClass) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getIndexClosedClasses() {
        List<Integer> outList = new ArrayList<Integer>();
        for (int i = 0; i < this.jobClasses.size(); i++) {
            if (this.jobClasses.get(i) instanceof ClosedClass) {
                outList.add(this.getJobClassIndex(this.jobClasses.get(i)));
            }
        }
        return outList;
    }

    public boolean hasClasses() {
        return !this.jobClasses.isEmpty();
    }

    public List<JobClass> getClasses() {
        return this.jobClasses;
    }

    public List<Node> getNodes() { return this.nodes; }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void setInitialized(boolean initStatus) {
        this.hasState = initStatus;
    }

    public int getNumberOfNodes() {
        return this.nodes.size();
    }

    public int getNumberOfStatefulNodes() {
        int ct = 0;
        for (Node node : this.nodes) {
            if (node instanceof StatefulNode) {
                ct++;
            }
        }
        return ct;
    }

    public int getNumberOfClasses() {
        return this.jobClasses.size();
    }

    public JobClass getClassByName(String name) {
        for (JobClass jobClass : this.jobClasses) {
            if (jobClass.getName().equals(name)) {
                return jobClass;
            }
        }
        return null;
    }

    public JobClass getClassByIndex(int index) {
        for (JobClass jobClass : this.jobClasses) {
            if (this.getJobClassIndex(jobClass) == index) {
                return jobClass;
            }
        }
        return null;
    }

    public void addJobClass(JobClass jobClass) {
        this.jobClasses.add(jobClass);
    }

    public Node getNodeByName(String name) {
        for (Node node : this.nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }

        return null;
    }

    public Node getNodeByStatefulIndex(int idx) {
        int nodesPassed = 0;
        for (Node nodeIter : this.nodes) {
            if (nodeIter instanceof StatefulNode) {
                if (nodesPassed == idx) {
                    return nodeIter;
                }
                nodesPassed++;
            }
        }

        return null;
    }

    public int getNodeIndex(Node node) {
        return this.nodes.indexOf(node);
    }

    public int getStatefulNodeIndex(Node node) {
        int outIdx = 0;
        for (Node nodeIter : this.nodes) {
            if (nodeIter == node) {
                return outIdx;
            } else if (nodeIter instanceof StatefulNode) {
                outIdx++;
            }
        }

        return -1;
    }

    public Node getStatefulNodeFromIndex(int inIdx) {
        int outIdx = 0;
        for (Node nodeIter : this.nodes) {
            if (outIdx == inIdx) {
                return nodeIter;
            } else if (nodeIter instanceof StatefulNode) {
                outIdx++;
            }
        }

        return null;
    }

    public static RoutingMatrix serialRouting(List<JobClass> jobClasses, Node... nodes) {
        if (nodes.length == 0) {
            return new RoutingMatrix();
        }

        Network network = nodes[0].model;
        RoutingMatrix outMatrix = new RoutingMatrix(jobClasses, network.nodes);

        for (int i = 1; i < nodes.length; i++) {
            //System.out.format("Loading connection %s->%s\n", nodes[i-1].getName(), nodes[i].getName());
            outMatrix.addConnection(nodes[i-1], nodes[i]);
        }

        if(!(nodes[nodes.length-1] instanceof Sink)) {
            outMatrix.addConnection(nodes[nodes.length-1], nodes[0]);
        }

        return outMatrix;
    }

    public static RoutingMatrix serialRouting(JobClass jobClass, Node... nodes) {
        List<JobClass> jobClasses = new ArrayList<JobClass>();
        jobClasses.add(jobClass);

        return Network.serialRouting(jobClasses, nodes);
    }

    public static RoutingMatrix serialRouting(Node... nodes) {
        if (nodes.length == 0) {
            return new RoutingMatrix();
        }
        Network network = nodes[0].model;
        return Network.serialRouting(network.jobClasses, nodes);
    }

    public void link(RoutingMatrix routing) {
        /*
             Input:
                routing: row: source, column: dest
         */

        routing.setRouting(this);
    }

    public void unLink() {
        for (Node node : this.nodes) {
            node.resetRouting();
        }
    }

    public NetworkState getState() {
        if (!this.hasState) {
            this.initDefault();
        }
        return new NetworkState();
    }

    public void initDefault() {
        // 1. open classes are empty
        // 2. closed classes are initialized at reference station
        // 3. running jobs are allocated in class id
        // 4. servers are busy

        // For each node..
        //     ..if it's a station..
        //
        //     ..else..
    }

    public void printSummary() {
        System.out.format("jline.Network model: %s\n", this.getName());
        System.out.format("--------------------------------------------------------\n");
        System.out.format("Job classes: \n");
        for (JobClass jobClass : this.jobClasses) {
            jobClass.printSummary();
        }
        System.out.format("--------------------------------------------------------\n");
        System.out.format("Nodes: \n");
        for (Node node : this.nodes) {
            node.printSummary();
            System.out.format("--------\n");
        }
    }

    public void clearCaches() {
        this.classLinks = new HashMap<Node, Map<JobClass, List<Node>>>();
    }

    protected void generateClassLinks() {
        this.classLinks = new HashMap<Node, Map<JobClass, List<Node>>>();
        for (Node node : this.nodes) {
            Map<JobClass, List<Node>> nodeMap = new HashMap<JobClass, List<Node>>();

            for (JobClass jobClass : this.jobClasses) {
                nodeMap.put(jobClass, new ArrayList<Node>());
            }
            classLinks.put(node, nodeMap);
        }

        for (Node node : this.nodes) {
            for (final OutputStrategy outputStrategy : node.getOutputStrategies()) {
                Node destNode = outputStrategy.getDestination();
                if (destNode == null) {
                    continue;
                }
                JobClass jobClass = outputStrategy.getJobClass();
                this.classLinks.get(destNode).get(jobClass).add(node);
            }
        }
    }

    public int getClassLinks(Node node, JobClass jobClass) {
        if (this.classLinks.isEmpty()) {
            this.generateClassLinks();
        }
        return this.classLinks.get(node).get(jobClass).size();
    }

    public double minRate() {
        double acc = Double.POSITIVE_INFINITY;
        for (Node node : this.nodes) {
            if (node instanceof HasSchedStrategy) {
                double accVal = ((HasSchedStrategy)node).minRate();
                if (accVal != 0) {
                    acc = Math.min(acc, accVal);
                }
            }
        }
        return acc;
    }

    public double maxRate() {
        double acc = 0;
        for (Node node : this.nodes) {
            if (node instanceof HasSchedStrategy) {
                double accVal = ((HasSchedStrategy)node).maxRate();
                if (accVal != Double.POSITIVE_INFINITY) {
                    acc = Math.max(acc, accVal);
                }
            }
        }
        return acc;
    }

    public double avgRate() {
        double acc = 0;
        int accCt = 0;
        for (Node node : this.nodes) {
            if (node instanceof HasSchedStrategy) {
                double accVal = ((HasSchedStrategy)node).avgRate();
                int valCt = ((HasSchedStrategy)node).rateCt();
                if ((accVal != Double.POSITIVE_INFINITY) && (accVal != 0)) {
                    acc += accVal;
                    accCt += valCt;
                }
            }
        }
        return acc/accCt;
    }
}
