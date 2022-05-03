package jline.examples;

import java.util.Arrays;
import java.util.List;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.DConvertMatrixStruct;

import jline.lang.ClosedClass;
import jline.lang.Network;
import jline.lang.OpenClass;
import jline.lang.RoutingMatrix;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Exp;
import jline.lang.nodes.Queue;
import jline.lang.nodes.Sink;
import jline.lang.nodes.Source;

public class TestGetStruct {
	public static void main(String[] args) {
	    
		Network model = new Network("MM1LowU");
		
		Source source = new Source(model, "mySource");
		Queue queue1 = new Queue(model, "myQueue1");
		Queue queue2 = new Queue(model, "myQueue2");
		Sink sink = new Sink(model, "mySink");
		
		OpenClass openClass = new OpenClass(model, "openclass1");
		ClosedClass class2 = new ClosedClass(model, "closedclass1", 5, queue1);
		ClosedClass class3 = new ClosedClass(model, "closedclass2", 10, queue1);
		
		
		RoutingMatrix routingMatrix = new RoutingMatrix(model, Arrays.asList(openClass, class2, class3),
                Arrays.asList(source, queue1, queue2, sink));
        routingMatrix.addConnection(source, queue1, openClass, openClass, 1);
        routingMatrix.addConnection(queue1, queue1, openClass, openClass, 0.3);
        routingMatrix.addConnection(queue1, queue2, openClass, openClass, 0.7);
        routingMatrix.addConnection(queue2, sink, openClass, openClass);
        routingMatrix.addConnection(queue1, queue2, class2, class3);
        routingMatrix.addConnection(queue2, queue1, class3, class2);
        
        routingMatrix.resolveUnappliedConnections();
        
        List<List<DMatrixSparseCSC>> res = routingMatrix.getRoutings();
        for(List<DMatrixSparseCSC> lists : res) {
        	for (DMatrixSparseCSC matrix : lists)
        		matrix.print();
        }
        
//        model.link(routingMatrix);
//        
//		model.link(model.serialRouting(source,queue1,queue2,sink));
//		
//		model.getStruct();
	}
	
}
