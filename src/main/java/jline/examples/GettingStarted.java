package jline.examples;

import jline.lang.nodes.Delay;
import jline.solvers.ssa.*;
import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Erlang;
import jline.lang.distributions.Exp;
import jline.lang.nodes.Queue;
import jline.lang.nodes.Sink;
import jline.lang.nodes.Source;

import java.util.Arrays;

public class GettingStarted {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Network model = GettingStarted.ex8();
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(5000).seed(50);
        solverSSA.setOptions().R5(17);
        // Uncomment below to test Tau Leaping
        /*solverSSA.setOptions().configureTauLeap(new TauLeapingType(
                TauLeapingVarType.Poisson,
                TauLeapingOrderStrategy.DirectedCycle,
                TauLeapingStateStrategy.Cutoff,
                0.1
        ));*/
        Timeline solve_soln = solverSSA.solve();
        System.out.println("Your simulation has finished.");
        solve_soln.printSummary(model);
        //System.out.format("%d samples collected in %d ms", 100000, duration/1000000);
    }

    public static Network ex1() {
        /*  M/M/1 queue
         */
        Network model = new Network("MM1LowU");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(2));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(10));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        return model;
    }

    public static Network ex2() {
        /*  M/M/2 queue
         */
        Network model = new Network("MM2HighU");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(5));
        queue.setNumberOfServers(2);
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        return model;
    }

    public static Network ex3() {
        /*  3 markovian queues in series
         */
        Network model = new Network("3 Series");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Exp(12));
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        queue2.setService(openClass, new Exp(11));
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);
        queue3.setService(openClass, new Exp(10));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue1,queue2,queue3,sink));

        return model;
    }

    public static Network ex4() {
        /*  3 queues in parallel with Erlang-distributed service times
         */
        Network model = new Network("Parallel Erlang");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(10));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Erlang(8, 2));
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        queue2.setService(openClass, new Erlang(11,3));
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);
        queue3.setService(openClass, new Erlang(16,4));
        Sink sink = new Sink(model, "Sink");
        
        
        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(openClass),
                Arrays.asList(source, queue1, queue2, queue3, sink));
        routingMatrix.addConnection(source, queue1);
        routingMatrix.addConnection(queue1, sink);
        routingMatrix.addConnection(source, queue2);
        routingMatrix.addConnection(queue2, sink);
        routingMatrix.addConnection(source, queue3);
        routingMatrix.addConnection(queue3, sink);
        model.link(routingMatrix);


        return model;
    }

    public static Network ex5() {
        /* A closed network of 3 queues
         */
        Network model = new Network("3 Closed");
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);

        ClosedClass closedClass = new ClosedClass(model, "Closed Class", 15, queue1);

        queue1.setService(closedClass, new Exp(10));
        queue2.setService(closedClass, new Exp(11));
        queue3.setService(closedClass, new Exp(12));

        model.link(model.serialRouting(queue1, queue2, queue3));

        return model;
    }

    public static Network ex6() {
        /*An M/M/1/10 queue
         */
        Network model = new Network("MM1 10");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(10));
        queue.setCap(10);
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        return model;
    }

    public static Network ex7() {
        /* A queue with two different open classes
         */
        Network model = new Network("2CDSDC");
        OpenClass openClass1 = new OpenClass(model, "Open 1");
        OpenClass openClass2 = new OpenClass(model, "Open 2");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass1, new Exp(8));
        source.setArrivalDistribution(openClass2, new Exp(5));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass1, new Exp(12));
        queue.setService(openClass2, new Exp(16));
        queue.setClassCap(openClass1, 5);
        queue.setClassCap(openClass2, 3);
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        return model;
    }

    public static Network ex8() {
        Network model = new Network("2CDSDC");
        Delay Node1 = new Delay(model, "Delay");
        Queue Node2 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        ClosedClass closedClass1 = new ClosedClass(model, "Closed 1", 10, Node1,0);

        Node1.setService(closedClass1, new Exp(1));
        Node2.setService(closedClass1, new Exp(0.6666667));

        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(closedClass1),
                Arrays.asList(Node1, Node2));
        routingMatrix.addConnection(Node1, Node1, closedClass1,0.7);
        routingMatrix.addConnection(Node1, Node2, closedClass1,0.3);
        routingMatrix.addConnection(Node2, Node1, closedClass1,1.0);
        routingMatrix.addConnection(Node2, Node2, closedClass1,0.0);
        model.link(routingMatrix);
        return model;
    }
}
