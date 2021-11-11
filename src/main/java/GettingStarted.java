import SimUtil.Exp;
import TauSSA.*;
import StochLib.*;

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
        StochLib.Queue delay = new StochLib.Delay(model, "WorkingQ");
        StochLib.Queue queue = new StochLib.Queue(model, "RepairQ", SchedStrategy.FCFS);

        JobClass cclass = new ClosedClass(model, "Machines", 3, delay);
        delay.setService(cclass, new Exp(0.5));
        queue.setService(cclass, new Exp(4.0));
        queue.setNumberOfServers(2);
        model.link(Network.serialRouting(delay, queue));

        model.printSummary();

        SolverSSA solverSSA = new SolverSSA();
        solverSSA.compile(model);
        solverSSA.setOptions().samples(10000);
        /*solverSSA.setOptions().configureTauLeap(new TauLeapingType(TauLeapingVarType.Poisson,
                                                                    TauLeapingOrderStrategy.DirectedCycle,
                                                                    TauLeapingStateStrategy.Cutoff,0.5));*/
        long startTime = System.nanoTime();
        solverSSA.solve();
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        System.out.format("Total solving time: %d\n", duration/1000000);

    }
}
