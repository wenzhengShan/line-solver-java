import SimUtil.Exp;
import StochLib.ClassSwitch;
import SolverSSA.*;
import StochLib.*;
import StochLib.Queue;
import SimUtil.MAPProcess;

import java.util.*;

public class GettingStarted1 {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        GettingStarted1.mm1Test();
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        System.out.format("Total time: %d", duration/1000000);
    }

    public static void erlangTest() {
        long startTime = System.nanoTime();
        Network model = new Network("E/M/1");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new SimUtil.Erlang(3.0,3));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(75));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        model.printSummary();

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(Integer.MAX_VALUE);
        solverSSA.setOptions().setEndTime(10000).seed(100);
        solverSSA.solve();
    }

    public static void mTest() {
        long startTime = System.nanoTime();
        Network model = new Network("MAP/M/1");
        OpenClass openClass = new OpenClass(model, "Open Class");
        List<List<Double>> arrivalRates = new ArrayList<List<Double>>(3);
        List<Double> arrivalRates1 = new ArrayList<Double>(3);
        List<Double> arrivalRates2 = new ArrayList<Double>(3);
        List<Double> arrivalRates3 = new ArrayList<Double>(3);
        arrivalRates1.add(0.0);
        arrivalRates1.add(0.0);
        arrivalRates1.add(0.0);
        arrivalRates2.add(0.0);
        arrivalRates2.add(0.0);
        arrivalRates2.add(0.0);
        arrivalRates3.add(3.0);
        arrivalRates3.add(0.0);
        arrivalRates3.add(0.0);
        arrivalRates.add(arrivalRates1);
        arrivalRates.add(arrivalRates2);
        arrivalRates.add(arrivalRates3);

        List<List<Double>> rateTransitions = new ArrayList<List<Double>>(3);
        List<Double> rateTransitions1 = new ArrayList<Double>(3);
        rateTransitions1.add(-3.0);
        rateTransitions1.add(3.0);
        rateTransitions1.add(0.0);
        List<Double> rateTransitions2 = new ArrayList<Double>(3);
        rateTransitions2.add(0.0);
        rateTransitions2.add(-3.0);
        rateTransitions2.add(3.0);
        List<Double> rateTransitions3 = new ArrayList<Double>(3);
        rateTransitions3.add(0.0);
        rateTransitions3.add(0.0);
        rateTransitions3.add(-3.0);
        rateTransitions.add(rateTransitions1);
        rateTransitions.add(rateTransitions2);
        rateTransitions.add(rateTransitions3);
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new MAPProcess(3, rateTransitions, arrivalRates));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(75));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        model.printSummary();

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(Integer.MAX_VALUE);
        solverSSA.setOptions().setEndTime(10000).seed(100);
        solverSSA.solve();
    }

    public static void gs3() {
        Network model = new Network("MRP");
        //StochLib.Delay delay = new StochLib.Delay(model, "WorkingSt");
        StochLib.Queue delay = new StochLib.Queue(model, "WorkingQ", SchedStrategy.FCFS);
        StochLib.Queue queue = new StochLib.Queue(model, "RepairQ", SchedStrategy.FCFS);
        queue.setNumberOfServers(2);

        JobClass cclass = new ClosedClass(model, "Machines", 3, delay);
        delay.setService(cclass, new Exp(0.5));
        queue.setService(cclass, new Exp(4.0));
        model.link(Network.serialRouting(delay, queue));

        model.printSummary();

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(10000).seed(8000);
        solverSSA.solve();
    }

    public static void gs4() {
        Network model = new Network("Complex");

        JobClass jobClass1 = new OpenClass(model, "LowerClass");

        Source source1 = new Source(model, "mySource");
        source1.setArrivalDistribution(jobClass1, new Exp(0.5));
        Router router = new Router(model, "myRouter");
        StochLib.Queue queue1 = new StochLib.Queue(model, "UpperQueue");
        queue1.setService(jobClass1, new Exp(1));
        StochLib.Queue queue2 = new StochLib.Queue(model, "LowerQueue");
        queue2.setService(jobClass1, new Exp(2));
        Sink sink = new Sink(model, "StochLib.Sink");
        //ClassSwitch classSwitch = new ClassSwitch(model, "StochLib.ClassSwitcher");

        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(jobClass1), Arrays.asList(source1, router, queue1, queue2, sink));
        routingMatrix.addConnection(source1, router);
        routingMatrix.addConnection(router, queue1, jobClass1, 0.5);
        //routingMatrix.addConnection(router, classSwitch);
        //routingMatrix.addConnection(classSwitch, queue2);
        routingMatrix.addConnection(router, queue2, jobClass1, 0.5);
        routingMatrix.addConnection(queue1, sink);
        routingMatrix.addConnection(queue2, sink);
        model.link(routingMatrix);

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(Integer.MAX_VALUE);
        solverSSA.setOptions().setEndTime(10000).seed(100);
        solverSSA.solve();
    }

    public static void gs5() {
        Network model = new Network("Complex");

        JobClass jobClass1 = new OpenClass(model, "LowerClass");
        JobClass jobClass2 = new OpenClass(model, "UpperClass");

        Map<JobClass, Map<JobClass, Double>> csMatrix = new HashMap<JobClass, Map<JobClass, Double>>();
        csMatrix.put(jobClass1, new HashMap<JobClass, Double>());
        csMatrix.get(jobClass1).put(jobClass2, 1.0);

        Source source = new Source(model, "mySource");
        source.setArrivalDistribution(jobClass1, new Exp(0.5));
        Router split = new Router(model, "split");
        ClassSwitch classSwitch = new ClassSwitch(model, "ClassSwitch", csMatrix);
        Router combine = new Router(model, "combine");
        StochLib.Queue queue = new StochLib.Queue(model, "myQueue");
        queue.setService(jobClass1, new Exp(1));
        queue.setService(jobClass2, new Exp(2));
        Sink sink = new Sink(model, "StochLib.Sink");

        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(jobClass1, jobClass2), Arrays.asList(source, split, classSwitch, combine, queue, sink));
        routingMatrix.addConnection(source, split);
        routingMatrix.addConnection(split, combine);
        routingMatrix.addConnection(split, classSwitch);
        routingMatrix.addConnection(classSwitch, combine);
        routingMatrix.addConnection(combine, queue);
        routingMatrix.addConnection(queue, sink);
        model.link(routingMatrix);

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(10000).seed(8000);
        solverSSA.solve();
    }

    public static void gs6() {
        Network model = new Network("Complex");

        JobClass jobClass1 = new OpenClass(model, "Class1");
        JobClass jobClass2 = new OpenClass(model, "Class2");

        Map<JobClass, Map<JobClass, Double>> csMatrix = new HashMap<JobClass, Map<JobClass, Double>>();

        Source source = new Source(model, "mySource");
        source.setArrivalDistribution(jobClass1, new Exp(0.25));
        source.setArrivalDistribution(jobClass2, new Exp(0.25));
        Queue queue = new StochLib.Queue(model, "myQueue");
        queue.setService(jobClass1, new Exp(1));
        queue.setService(jobClass2, new Exp(2));
        Sink sink = new Sink(model, "StochLib.Sink");

        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(jobClass1, jobClass2), Arrays.asList(source, queue, sink));
        routingMatrix.addConnection(source, queue);
        routingMatrix.addConnection(queue, sink);
        model.link(routingMatrix);

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(10000).seed(8000);
        solverSSA.solve();
    }

    public static void gs7() {
        Network model = new Network("M/M/1");
        Source source = new Source(model, "mySource");
        StochLib.Queue queue = new StochLib.Queue(model, "myQueue", SchedStrategy.FCFS);
        Sink sink = new Sink(model, "mySink");
        OpenClass oclass = new OpenClass(model, "MyClass");
        OpenClass oclass2 = new OpenClass(model, "MyClass2");
        source.setArrivalDistribution(oclass, new Exp(10));
        source.setArrivalDistribution(oclass2, new Exp(10));
        queue.setService(oclass, new Exp(14));
        queue.setService(oclass2, new Exp(10));
        model.link(model.serialRouting(source,queue,sink));

        model.printSummary();

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(Integer.MAX_VALUE);
        solverSSA.setOptions().setEndTime(10000).seed(100);
        solverSSA.solve();

        solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(Integer.MAX_VALUE);
        solverSSA.setOptions().configureTauLeap(new TauLeapingType(TauLeapingVarType.Poisson,
                                                                             TauLeapingOrderStrategy.RandomEvent,
                                                                             TauLeapingStateStrategy.Cutoff, 1));
        solverSSA.setOptions().setEndTime(10000).seed(100);
        solverSSA.solve();

        solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(Integer.MAX_VALUE);
        solverSSA.setOptions().configureTauLeap(new TauLeapingType(TauLeapingVarType.Poisson,
                TauLeapingOrderStrategy.RandomEvent,
                TauLeapingStateStrategy.Cutoff, 0.5));
        solverSSA.setOptions().setEndTime(10000).seed(100);
        solverSSA.solve();
    }

    public static void mm1Test() {
        Random random = new Random();
        Network model = new Network("M/M/1");

        OpenClass openClass = new OpenClass(model, "MyClass");

        Source source = new Source(model, "mySource");
        double serviceRate = 10;
        double arrivalRate = 8;
        source.setArrivalDistribution(openClass, new Exp(arrivalRate));
        Queue queue = new Queue(model, "MM1Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(serviceRate));
        Sink sink = new Sink (model, "mySink");

        model.link(model.serialRouting(source,queue,sink));

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(10000).seed(3);
        //solverSSA.setOptions().recordMetricTimeline(false);
        //solverSSA.setOptions().MSER5();
        Timeline timeline = solverSSA.solve();
    }
}
