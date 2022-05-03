package jline.lang;

import java.io.*;
import java.lang.System.Logger;
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
    private List<Station> stations;
    
    private boolean hasStruct;
    private NetworkStruct sn;

    // caches
    private Map<Node, Map<JobClass, List<Node>>> classLinks;

    public Network(String modelName) {
        super(modelName);

        this.hasState = false;
        this.doChecks = true;

        this.nodes = new ArrayList<Node>();
        this.jobClasses = new ArrayList<JobClass>();
        this.stations = new ArrayList<Station>();

        this.classLinks = new HashMap<Node, Map<JobClass, List<Node>>>();
        
        this.hasStruct = false;
        this.sn = new NetworkStruct();
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
        if (node instanceof Station)
        	stations.add((Station) node);
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

    public RoutingMatrix serialRouting(List<JobClass> jobClasses, Node... nodes) {
        if (nodes.length == 0) {
            return new RoutingMatrix();
        }

        Network network = nodes[0].model;
        RoutingMatrix outMatrix = new RoutingMatrix(this, jobClasses, network.nodes);

        for (int i = 1; i < nodes.length; i++) {
            //System.out.format("Loading connection %s->%s\n", nodes[i-1].getName(), nodes[i].getName());
            outMatrix.addConnection(nodes[i-1], nodes[i]);
        }

        if(!(nodes[nodes.length-1] instanceof Sink)) {
            outMatrix.addConnection(nodes[nodes.length-1], nodes[0]);
        }

        return outMatrix;
    }

    public RoutingMatrix serialRouting(JobClass jobClass, Node... nodes) {
        List<JobClass> jobClasses = new ArrayList<JobClass>();
        jobClasses.add(jobClass);

        return this.serialRouting(jobClasses, nodes);
    }

    public RoutingMatrix serialRouting(Node... nodes) {
        if (nodes.length == 0) {
            return new RoutingMatrix();
        }
        Network network = nodes[0].model;
        return this.serialRouting(network.jobClasses, nodes);
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

    public boolean getHasStruct() {
    	return this.hasStruct;
    }
    
    public void setHasStruct(boolean hasStruct) {
    	this.hasStruct = hasStruct;
    }
    
    public void setStruct(NetworkStruct sn) {
    	this.sn = sn;
    }
    
    public NetworkStruct getStruct() {
    	if (!this.hasStruct)
    		refreshStruct(true);
    	
    	return this.sn;
    }
    
    public void refreshStruct(boolean hardRefresh) {
    	//Line 7-15 Ignored
    	
        NodeType[] nodetypes;
        String[] classnames;
        String[] nodenames;
        int[] refstat;
    	
    	if (this.hasStruct && !hardRefresh) {
    		nodetypes = sn.nodetypes;
    		classnames = sn.classnames;
    		nodenames = sn.nodenames;
    		refstat = sn.refstat;
    	} else {
    		nodetypes = getNodeTypes();
    		classnames = getClassNames();
    		nodenames = getNodeNames();
    		refstat = getReferenceStations();
    	}
    	
    	boolean[][] connections = sn.connections;
    	double[] njobs = getNumberOfJobs();
    	int[] numservers = getStationServers();
//    	lldscaling = getLimitedLoadDependence(self);
//    	cdscaling = getLimitedClassDependence(self);
    	
    	NetworkStruct struct = new NetworkStruct();
    	//rtorig
    }  
    
    public NodeType[] getNodeTypes() {
    	int M = getNumberOfNodes();
    	NodeType[] nodetypes = new NodeType[M];
    	
    	try {
    		for (int i = 0; i < M; i++) {
        		Node nodeIter = this.nodes.get(i);
        		if (nodeIter instanceof Logger) 
        			nodetypes[i] = NodeType.Logger;
        		else if (nodeIter instanceof ClassSwitch)
        			nodetypes[i] = NodeType.ClassSwitch;
        		else if (nodeIter instanceof Queue)
        			nodetypes[i] = NodeType.Queue;
        		else if (nodeIter instanceof Sink)
        			nodetypes[i] = NodeType.Sink;
        		else if (nodeIter instanceof Router)
        			nodetypes[i] = NodeType.Router;
        		else if (nodeIter instanceof Delay)
        			nodetypes[i] = NodeType.Delay;
        		else if (nodeIter instanceof Fork)
        			nodetypes[i] = NodeType.Fork;
        		else if (nodeIter instanceof Join)
        			nodetypes[i] = NodeType.Join;
        		else if (nodeIter instanceof Source)
        			nodetypes[i] = NodeType.Source;
//				Below node types are not supported in JLine
//        		else if (nodeIter instanceof Place)
//        			nodetypes[i] = NodeType.Place;
//        		else if (nodeIter instanceof Transition)
//        			nodetypes[i] = NodeType.Transition;
//        		else if (nodeIter instanceof Cache)
//        			nodetypes[i] = NodeType.Cache;
        		else
        			throw new Exception("Unknown node type.");
        	}
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    		
    	return nodetypes;
    }

    public String[] getClassNames() {
    	if (hasStruct && sn.classnames != null)
    		return sn.classnames;
    	
    	int K = getNumberOfClasses();
    	String[] classnames = new String[K];
    	for (int i = 0; i < K; i++) 
    		classnames[i] = jobClasses.get(i).getName();
    	
    	return classnames;
    }
    
    public String[] getNodeNames() {
    	if (hasStruct && sn.classnames != null)
    		return sn.nodenames;
    	
    	int M = getNumberOfNodes();
    	String[] nodenames = new String[M];
    	for (int i = 0; i < M; i++)
    		nodenames[i] = nodes.get(i).getName();
    		
    	return nodenames;
    }

    public int[] getReferenceStations() {
    	int K = getNumberOfClasses();
    	int[] refstat = new int[K];
    	
    	for (int i = 0; i < K; i++) {
    		if (jobClasses.get(i).type == JobClassType.Open) {
    			refstat[i] = getIndexSourceNode();
    		} else {
    			ClosedClass cc = (ClosedClass) jobClasses.get(i);
    			refstat[i] = getNodeIndex(cc.getRefstat());
    		}
    	}

    	
    	return refstat;
    }

    public int getIndexSourceNode() {
        int res = 0;
        for (Node nodeIter : this.nodes) {
        	if (nodeIter instanceof Source)
        		return res;
        	res++;
        }

        return -1;
    }
    
    public double[] getNumberOfJobs() {
    	int K = getNumberOfClasses();
    	double[] njobs = new double[K];
    	for(int i = 0; i < K; i++) {
    		if (jobClasses.get(i).type == JobClassType.Open)
    			njobs[i] = Double.POSITIVE_INFINITY;
    		else if (jobClasses.get(i).type == JobClassType.Closed)
    			njobs[i] = jobClasses.get(i).getNumberOfJobs();
    	}
    	return njobs;
    }

    public int[] getStationServers() {
    	int I = stations.size();
    	int[] numservers = new int[I];
    	for(int i = 0; i < I; i++)
    		numservers[i] = stations.get(i).getNumberOfServers();
    	
    	return numservers;
    }

}
