import SimUtil.Exp;
import StochLib.ClassSwitch;
import SolverSSA.*;
import StochLib.*;
import StochLib.Queue;
import SimUtil.MAPProcess;

import java.util.*;

public class GettingStarted {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        GettingStarted.ex3();
        GettingStarted.ex3();
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        System.out.format("Total time: %d", duration/1000000);
    }

    public static void ex3() {
        Network model = new Network("MRP");
        //StochLib.Delay delay = new StochLib.Delay(model, "WorkingSt");
        StochLib.Source source = new StochLib.Source(model, "Src");
        StochLib.Queue delay = new StochLib.Queue(model, "WorkingQ", SchedStrategy.FCFS);
        StochLib.Queue queue = new StochLib.Queue(model, "RepairQ", SchedStrategy.FCFS);
        StochLib.Sink sink = new StochLib.Sink(model, "Sink");
        queue.setNumberOfServers(2);

        JobClass oclass = new OpenClass(model, "Machines");
        source.setArrivalDistribution(oclass, new Exp(0.2));
        delay.setService(oclass, new Exp(1.0));
        queue.setService(oclass, new Exp(4.0));
        model.link(Network.serialRouting(source,delay, queue, sink));

        //System.out.println("HERE!!");

        model.printSummary();

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(1000000);
        solverSSA.setOptions().configureTauLeap(new TauLeapingType(TauLeapingVarType.Poisson,
                                                                    TauLeapingOrderStrategy.DirectedCycle,
                                                                    TauLeapingStateStrategy.Cutoff,0.5));
        long startTime = System.nanoTime();
        solverSSA.solve();
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        System.out.format("Total solving time: %d\n", duration/1000000);

    }
}
