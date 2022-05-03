package jline.lang;

import java.io.Serializable;
import java.util.*;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.DConvertMatrixStruct;

import jline.lang.constant.RoutingStrategy;
import jline.lang.nodes.ClassSwitch;
import jline.lang.nodes.Node;

public class RoutingMatrix implements Serializable {
    private List<List<List<Double>>> routingArray;	//To be removed
    private List<JobClass> jobClasses;
    private List<Node> nodes;
    private Map<JobClass, Integer> classIndexMap;
    private Map<Node, Integer> nodeIndexMap;
    private boolean hasUnappliedConnections;
    
    //Newly added
    private boolean[][] connections; //connections[i][j] = true means node i is connected with node j and direction is from i to j. 这里最后也得改（得包含class switch）
    private List<List<DMatrixSparseCSC>> routings; //Use DMatrixSparseTriplet for storage (O(1)). When computing, transfer to DMatrixSparseCSC.
    private int[][] csMatrix;
    private boolean hasClassSwitches;
    private Network model;
    
    //To be removed
    private List<List<Double>> generateEmptyRouting() {
        int nClasses = jobClasses.size();
        List<List<Double>> outArray = new ArrayList<List<Double>>(); 
        for (int i = 0; i < nClasses; i++) {//I think this should be the number of node instead of number of classes
            List<Double> rowArray = new ArrayList<Double>();
            outArray.add(rowArray);
            for (int j = 0; j < nClasses; j++) {//I think this should be the number of node instead of number of classes
                rowArray.add(0.0);
            }
        }

        return outArray;
    }
    
    //Newly Added
    private DMatrixSparseCSC generateEmptyNodeRouting() {
    	int nNodes = nodes.size();
    	DMatrixSparseTriplet nodeFrame = new DMatrixSparseTriplet(nNodes, nNodes, nNodes);
    	for(int m = 0; m < nNodes; m++) {
			for(int n = 0; n < nNodes; n++)
				nodeFrame.addItem(m, n, 0.0);
		}
    	return DConvertMatrixStruct.convert(nodeFrame, (DMatrixSparseCSC)null);
    }
    
    //Newly Added
    private DMatrixSparseCSC expandNodeRouting(DMatrixSparseCSC oldnodeRouting, int rows, int cols, int nz_length) {
		DMatrixSparseTriplet nodeRouting = new DMatrixSparseTriplet(rows, cols, nz_length);
		for(int i = 0; i < rows-1; i++) {
			for (int j = 0; j < cols-1; j++)
				nodeRouting.addItem(i, j, oldnodeRouting.get(i, j));
		}
		
		for(int i = 0; i < rows; i++)
			nodeRouting.addItem(i, cols - 1, 0.0);
		
		for(int i = 0; i < cols; i++)
			nodeRouting.addItem(rows - 1, i, 0.0);
		
    	return DConvertMatrixStruct.convert(nodeRouting, (DMatrixSparseCSC)null);
    }

    public RoutingMatrix() {
        this.routingArray = new ArrayList<List<List<Double>>>(); // To be removed
        this.jobClasses = new ArrayList<JobClass>();
        this.nodes = new ArrayList<Node>();
        this.hasUnappliedConnections = false;
        
        //Newly added
        int I = this.nodes.size();
        int K = this.jobClasses.size();
        this.connections = new boolean[I][I];
        this.csMatrix = new int[K][K];
        this.routings = new ArrayList<List<DMatrixSparseCSC>>();
        this.model = new Network("");
        this.hasClassSwitches = false;
    }

    public RoutingMatrix(Network model, List<JobClass> jobClasses, List<Node> nodes) {
        int nJobClasses = jobClasses.size();
        int nNodes = nodes.size();
        this.jobClasses = new ArrayList<JobClass>(jobClasses);	//To be removed
        this.nodes = new ArrayList<Node>(nodes);
        this.routingArray = new ArrayList<List<List<Double>>>(nJobClasses);
        this.classIndexMap = new HashMap<JobClass, Integer>();
        this.nodeIndexMap = new HashMap<Node, Integer>();
        this.hasUnappliedConnections = false;

        //To be removed
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
        
        //Newly added
        this.model = model;
        this.connections = new boolean[nNodes][nNodes];
        this.csMatrix = new int[nJobClasses][nJobClasses];
        this.hasClassSwitches = false;
        
        routings = new ArrayList<List<DMatrixSparseCSC>>(nJobClasses);
        for(int i = 0; i < nJobClasses; i++) {
        	List<DMatrixSparseCSC> frame = new ArrayList<DMatrixSparseCSC>(nJobClasses);
        	for(int j = 0; j < nJobClasses; j++)
        		frame.add(generateEmptyNodeRouting());
        	
        	this.routings.add(frame);
        	this.classIndexMap.put(this.jobClasses.get(i), i);
        	this.csMatrix[i][i] = 1;
        }
    }

    public void addClass(JobClass jobClass) {
        if (this.jobClasses.contains(jobClass)) {
            // idempotent
            return;
        }

        int classIdx = this.jobClasses.size();
        this.jobClasses.add(jobClass);
        this.routingArray.add(this.generateEmptyRouting());	//To be removed
        this.classIndexMap.put(jobClass, classIdx);
        
        //Newly Added
        int nJobClasses = jobClasses.size();
        int[][] newCsMatrix = new int[nJobClasses][nJobClasses];
        for(int i = 0; i < nJobClasses-1; i++) {
        	for(int j = 0; j < nJobClasses-1; j++) {
        		newCsMatrix[i][j] = csMatrix[i][j];
        	}
        	newCsMatrix[i][i] = 1;
        }
        newCsMatrix[nJobClasses - 1][nJobClasses - 1] = 1;
        this.csMatrix = newCsMatrix;
        
        List<DMatrixSparseCSC> frame = new ArrayList<DMatrixSparseCSC>();
        for(int i = 0; i < nJobClasses - 1; i++) {
        	this.routings.get(i).add(this.generateEmptyNodeRouting()); // Old class to the new class
        	frame.add(this.generateEmptyNodeRouting()); // New class to old class
        }
        
        frame.add(this.generateEmptyNodeRouting());  // New class to New class
        routings.add(frame);
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
        //To be removed
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
        
        //Newly Added
        int I = this.nodes.size();
        boolean[][] newConnections = new boolean[I][I];
        for(int i = 0; i < I-1; i++) {
        	for(int j = 0; j < I-1; j++) {
        		newConnections[i][j] = connections[i][j];
        	}
        }
        this.connections = newConnections;
        
        for (List<DMatrixSparseCSC> classArray : this.routings) {
        	for(int i = 0; i < classArray.size(); i++)
        		classArray.set(i, expandNodeRouting(classArray.get(i), I,I,I));
        }
    }

    public void addConnection(Node sourceNode, Node destNode) {
        for (JobClass jobClass : this.jobClasses) {
            this.addConnection(sourceNode, destNode, jobClass, jobClass);
        }
    }
    
    public void addConnection(Node sourceNode, Node destNode, JobClass jobClass) {
        if (sourceNode.getRoutingStrategy(jobClass) == RoutingStrategy.DISABLED) {
            return;
        }

        this.hasUnappliedConnections = true;
        this.addConnection(sourceNode, destNode, jobClass, jobClass, Double.NaN);
    }
    
    public void addConnection(Node sourceNode, Node destNode, double probability) {
    	for (JobClass jobClass : this.jobClasses) {
    		this.addConnection(sourceNode, destNode, jobClass, probability);
    	}
    }
    
    public void addConnection(Node sourceNode, Node destNode, JobClass originClass, JobClass targetClass) {
    	if (sourceNode.getRoutingStrategy(originClass) == RoutingStrategy.DISABLED) {
    		return;
    	}
    	
    	this.hasUnappliedConnections = true;
    	this.addConnection(sourceNode, destNode, originClass, targetClass, Double.NaN);
    }

    public void addConnection(Node sourceNode, Node destNode, JobClass jobClass, double probability) {
        if (sourceNode.getRoutingStrategy(jobClass) == RoutingStrategy.DISABLED) {
            return;
        }
        
        if (Double.isNaN(probability)) {
        	this.hasUnappliedConnections = true;
        }
        
        this.addConnection(sourceNode, destNode, jobClass, jobClass, probability);
    }
    
    public void addConnection(Node sourceNode, Node destNode, JobClass originClass, JobClass targetClass, double probability) {
    	
    	//To be removed
        List<Double> sourceRouting = this.routingArray.get(this.getClassIndex(originClass)).get(this.getNodeIndex(sourceNode));

        int destIndex = this.getNodeIndex(destNode);
        sourceRouting.set(destIndex,probability);
        
        // Newly added
        if (!originClass.equals(targetClass))
        	this.hasClassSwitches = true;
        
    	int originClassIdx = getClassIndex(originClass);
    	int targetClassIdx = getClassIndex(targetClass);
    	int sourceNodeIdx = getNodeIndex(sourceNode);
    	int destNodeIdx = getNodeIndex(destNode);
    	
    	routings.get(originClassIdx).get(targetClassIdx).unsafe_set(sourceNodeIdx, destNodeIdx, probability);
    	        
        this.connections[sourceNodeIdx][destNodeIdx] = true;
    }

    public void resolveUnappliedConnections() {
    	// To be removed
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
        
        //Newly Added
        int I = nodes.size();
        for (List<DMatrixSparseCSC> jobClassRouting : this.routings) {
        	for (DMatrixSparseCSC nodeRouting : jobClassRouting) {
        		for(int row = 0; row < I; row++) {
        			double residProb = 1;
        			int nUnapplied = 0;
        			for(int col = 0; col < I; col++) {
        				Double routingAmount = nodeRouting.get(row, col);
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
                    for(int col = 0; col < I; col++) {
                    	if (Double.isNaN(nodeRouting.get(row, col)))
                    		nodeRouting.set(row, col, unitProb);
                    }
        		}
        	}
        }
        this.hasUnappliedConnections = false;
    }

    public void resolveClassSwitches() {

    }
    
    public void setRouting(Network model) {
        if (this.hasUnappliedConnections) {
            this.resolveUnappliedConnections();
        }
        if (this.hasClassSwitches) {
        	this.resolveClassSwitches();
        }
        //Change the following code
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
        //在这里创建一个新的struct，然后model.setstruct(sn)
        //在这里更新rtorig, csmatrix
    }

    public List<List<DMatrixSparseCSC>> getRoutings() {
    	return this.routings;
    }
}
