package Line;

import java.io.Serializable;
import java.util.*;

public class RoutingMatrix implements Serializable {
    private List<List<List<Double>>> routingArray;
    private List<JobClass> jobClasses;
    private List<Node> nodes;
    private Map<JobClass, Integer> classIndexMap;
    private Map<Node, Integer> nodeIndexMap;
    private boolean hasUnappliedConnections;

    private List<List<Double>> generateEmptyRouting() {
        int nClasses = jobClasses.size();
        List<List<Double>> outArray = new ArrayList<List<Double>>();
        for (int i = 0; i < nClasses; i++) {
            List<Double> rowArray = new ArrayList<Double>();
            outArray.add(rowArray);
            for (int j = 0; j < nClasses; j++) {
                rowArray.add(0.0);
            }
        }

        return outArray;
    }

    public RoutingMatrix() {
        this.routingArray = new ArrayList<List<List<Double>>>();
        this.jobClasses = new ArrayList<JobClass>();
        this.nodes = new ArrayList<Node>();
        this.hasUnappliedConnections = false;
    }

    public RoutingMatrix(List<JobClass> jobClasses, List<Node> nodes) {
        int nJobClasses = jobClasses.size();
        int nNodes = nodes.size();
        this.jobClasses = new ArrayList<JobClass>(jobClasses);
        this.nodes = new ArrayList<Node>(nodes);
        this.routingArray = new ArrayList<List<List<Double>>>(nJobClasses);
        this.classIndexMap = new HashMap<JobClass, Integer>();
        this.nodeIndexMap = new HashMap<Node, Integer>();

        for (int i = 0; i < nJobClasses; i++) {
            List<List<Double>> classFrame = new ArrayList<List<Double>>(nNodes);
            for (int j = 0; j < nNodes; j++) {
                Double[] rowData = new Double[nNodes];
                Arrays.fill(rowData, new Double(0.0));
                List<Double> rowFrame = Arrays.asList(rowData);
                classFrame.add(rowFrame);
            }
            this.routingArray.add(classFrame);
            this.classIndexMap.put(this.jobClasses.get(i), i);
        }
        for (int j = 0; j < this.nodes.size(); j++) {
            this.nodeIndexMap.put(this.nodes.get(j), j);
        }
    }



    public void addClass(JobClass jobClass) {
        if (this.jobClasses.contains(jobClass)) {
            // idempotent
            return;
        }

        int classIdx = this.jobClasses.size();
        this.jobClasses.add(jobClass);
        this.routingArray.add(this.generateEmptyRouting());
        this.classIndexMap.put(jobClass, classIdx);
    }

    public int getClassIndex(JobClass jobClass) {
        return this.classIndexMap.get(jobClass);
    }

    public int getNodeIndex(Node node) {
        return this.nodeIndexMap.get(node);
    }

    public void addNode(Node node) {
        if (this.nodes.contains(node)) {
            return;
        }

        int nodeIdx = this.nodes.size();

        this.nodes.add(node);
        for (List<List<Double>> classArray : this.routingArray) {
            List<Double> newRow = new ArrayList<Double>();
            for (List<Double> row : classArray) {
                row.add(0.0);
                newRow.add(0.0);
            }
            newRow.add(0.0);
            classArray.add(newRow);
        }

        this.nodeIndexMap.put(node, nodeIdx);
    }


    public void addConnection(Node sourceNode, Node destNode, JobClass jobClass, double probability) {
        if (sourceNode.getRoutingStrategy(jobClass) == RoutingStrategy.DISABLED) {
            return;
        }
        List<Double> sourceRouting = this.routingArray.get(this.getClassIndex(jobClass)).get(this.getNodeIndex(sourceNode));

        int destIndex = this.getNodeIndex(destNode);
        sourceRouting.set(destIndex,probability);
    }

    private void resolveUnappliedConnections() {
        for (List<List<Double>> jobClassRouting : this.routingArray) {
            for (List<Double> nodeRouting : jobClassRouting) {
                double residProb = 1;
                int nUnapplied = 0;
                for (Double routingAmount : nodeRouting) {
                    if (routingAmount.isNaN()) {
                        nUnapplied++;
                    } else {
                        residProb -= routingAmount;
                    }
                }
                if (nUnapplied == 0) {
                    continue;
                }
                double unitProb = residProb / nUnapplied;
                for (int i = 0; i < nodeRouting.size(); i++) {
                    if (nodeRouting.get(i).isNaN()) {
                        nodeRouting.set(i, unitProb);
                    }
                }
            }
        }
        this.hasUnappliedConnections = false;
    }

    public void addConnection(Node sourceNode, Node destNode, JobClass jobClass) {
        if (sourceNode.getRoutingStrategy(jobClass) == RoutingStrategy.DISABLED) {
            return;
        }

        this.hasUnappliedConnections = true;
        this.addConnection(sourceNode, destNode, jobClass, Double.NaN);
    }

    public void addConnection(Node sourceNode, Node destNode) {
        for (JobClass jobClass : this.jobClasses) {
            this.addConnection(sourceNode, destNode, jobClass);
        }
    }

    public void setRouting(Network model) {
        if (this.hasUnappliedConnections) {
            this.resolveUnappliedConnections();
        }
        for (int i = 0; i < this.jobClasses.size(); i++) {
            List<List<Double>> classArray = this.routingArray.get(i);
            JobClass jobClass = this.jobClasses.get(i);
            for (int j = 0; j < this.nodes.size(); j++) {
                for (int k = 0; k < this.nodes.size(); k++) {
                    double prob = classArray.get(j).get(k);
                    if (prob != 0) {
                        //System.out.format("%s->%s: Prob = %f\n", this.nodes.get(j).getName(), this.nodes.get(k).getName(), prob);
                        this.nodes.get(j).setRouting(jobClass, RoutingStrategy.PROB, this.nodes.get(k), prob);
                    }
                }
            }
        }
    }
}
