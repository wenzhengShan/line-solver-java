import SimUtil.Erlang;
import SimUtil.Exp;
import TauSSA.*;
import StochLib.*;

public class GettingStarted {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Network model = GettingStarted.ex1();
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(100000).seed(50);
        /*solverSSA.setOptions().configureTauLeap(new TauLeapingType(
                TauLeapingVarType.Poisson,
                TauLeapingOrderStrategy.DirectedCycle,
                TauLeapingStateStrategy.Cutoff,
                0.1
        ));*/
        solverSSA.solve().printSummary(model);
        System.out.format("Total time: %d ms", duration/1000000);
    }

    public static Network ex1() {
        long startTime = System.nanoTime();
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

        return model;
    }

    public static Network ex5() {
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
        Network model = new Network("2CDSDC");
        OpenClass openClass1 = new OpenClass(model, "Open Class 1");
        OpenClass openClass2 = new OpenClass(model, "Open Class 2");
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
}
