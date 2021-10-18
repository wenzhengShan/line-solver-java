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
        GettingStarted1.ex3();
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        System.out.format("Total time: %d", duration/1000000);
    }

    public static void ex3() {
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
}
