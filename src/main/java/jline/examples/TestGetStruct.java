package jline.examples;

import jline.lang.ClosedClass;
import jline.lang.Network;
import jline.lang.OpenClass;
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
		ClosedClass class3 = new ClosedClass(model, "closedclass2", 5, queue1);
		
		model.link(model.serialRouting(source,queue1,queue2,sink));
		
		model.getStruct();
	}
	
}
