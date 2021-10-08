import SimUtil.Erlang;
import SimUtil.Experiment;
import SimUtil.MAPProcess;
import SolverSSA.SolverSSA;
import StochLib.*;
import StochLib.Queue;
import StochLib.Source;
import SimUtil.Exp;
import SolverSSA.*;
import sun.nio.ch.Net;

import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Benchmarks {
    public static void main(String[] args) {
        //tauLeapTest();
        //tuning();
        //altMain(args);
        //serversBinomial(args);
        //utilizationTimeWarp(args);
        //altMain(args);
        //standardResults();
        //tauLeap();
        //timings();
        //setupTime();
        //setupTimeClasses();
        //setupTimeNodes();
        runExperiment();
    }

    public static void runExperiment() {
        Experiment e = new Experiment();
        for (int i = 3000; i < 3500; i++) {
            System.out.format("Run %d\n", i);
            e.runExperiment(Network.loadCopy(String.format("model%d",i+1)));
        }
        e.printSummary();
        e.saveCopy("State3To500");
    }


    public static void serversBinomial(String[] args) {
        Random random = new Random();
        List<String> utilizationValues = new ArrayList<String>();

        for (int nServers = 1; nServers < 10; nServers++) {
            Experiment experiment = new Experiment();
            System.out.format("n: %d\n", nServers);
            for (int runCt = 0; runCt < 1; runCt++) {
                double serviceRate = random.nextDouble()*50.0;
                double utilization = (random.nextDouble()*0.8)+0.1;
                Network model = new Network("Unstable");

                Source source = new Source(model, "Source");
                OpenClass oClass = new OpenClass(model, "OpenClass");
                source.setArrivalDistribution(oClass, new Exp(serviceRate*utilization*nServers));

                Queue queue = new Queue(model, "Q");
                queue.setNumberOfServers(nServers);
                queue.setService(oClass, new Exp(serviceRate));

                Sink sink = new Sink(model, "Sink");

                model.link(model.serialRouting(source, queue, sink));
                experiment.runExperiment(model);
            }
            String uString = String.format("%d", nServers);
            utilizationValues = Stream.concat(utilizationValues.stream(), experiment.summaryString(uString).stream()).collect(Collectors.toList());
        }

        System.out.println("Utliization | Order Strat     | State Strat     | Total MAPE    | Total MAPE std   | Total Execution   | Execution Difference");

        for (String uValue : utilizationValues) {
            System.out.println(uValue);
        }

    }

    public static void utilizationTimeWarp(String[] args) {
        Random random = new Random();
        List<String> utilizationValues = new ArrayList<String>();

        for (double utilization = 0.85; utilization < 1; utilization += 0.01) {
            Experiment experiment = new Experiment();
            System.out.format("U: %f\n", utilization);
            for (int runCt = 0; runCt < 100; runCt++) {
                double serviceRate = random.nextDouble()*50.0;
                Network model = new Network("Unstable");

                Source source = new Source(model, "Source");
                OpenClass oClass = new OpenClass(model, "OpenClass");
                source.setArrivalDistribution(oClass, new Exp(serviceRate*utilization));

                Queue queue = new Queue(model, "Q");
                //queue.setNumberOfServers(nServers);
                queue.setService(oClass, new Exp(serviceRate));

                Sink sink = new Sink(model, "Sink");

                model.link(model.serialRouting(source, queue, sink));
                experiment.runExperiment(model);
            }
            String uString = String.format("%f", utilization);
            utilizationValues = Stream.concat(utilizationValues.stream(), experiment.summaryString(uString).stream()).collect(Collectors.toList());
        }

        System.out.println("Utliization | Order Strat     | State Strat     | Total MAPE    | Total MAPE std   | Total Execution   | Execution Difference");

        for (String uValue : utilizationValues) {
            System.out.println(uValue);
        }

    }

    public static void setupTimeNodes() {
        Random random = new Random();
        List<String> outString = new ArrayList<String>(50);
        for (int nQueues = 1; nQueues < 500; nQueues++) {
            double avgTime = 0;
            for (int nRuns = 0; nRuns < 205; nRuns++) {
                if (nRuns == 5) {
                    avgTime = 0;
                }
                Network model = new Network(String.format("nQueues%d", nQueues));
                Source source = new Source(model, "Source");
                OpenClass oClass = new OpenClass(model, "OpenClass");
                source.setArrivalDistribution(oClass, new Exp(50));

                Node[] allNodes = new Node[nQueues+2];
                allNodes[0] = source;

                for (int i = 0; i < nQueues; i++) {
                    double serviceRate = random.nextDouble()*45.0;
                    Queue queue = new Queue(model, String.format("Queue%d",nQueues), SchedStrategy.FCFS);
                    queue.setService(oClass, new Exp(serviceRate));
                    allNodes[i+1] = queue;
                }
                Sink sink = new Sink(model, "Sink");
                allNodes[nQueues+1] = sink;

                model.link(model.serialRouting(allNodes));

                long startTime = System.nanoTime();
                SolverSSA solverSSA = new SolverSSA();
                solverSSA.compile(model);
                long endTime = System.nanoTime();
                double duration = (endTime - startTime);
                avgTime += duration/1000000;
            }
            outString.add(String.format("%d: %f\n", nQueues+2, avgTime/200));
        }

        for (String cString : outString) {
            System.out.print(cString);
        }
    }

    public static void setupTimeClasses() {
        Random random = new Random();
        List<String> outString = new ArrayList<String>(50);
        for (int nClasses = 1; nClasses < 500; nClasses++) {
            double avgTime = 0;
            for (int nRuns = 0; nRuns < 205; nRuns++) {
                if (nRuns == 5) {
                    avgTime = 0;
                }
                Network model = new Network(String.format("nClasses%d", nClasses));
                Source source = new Source(model, "Source");
                Queue queue1 = new Queue(model, "Queue", SchedStrategy.FCFS);
                Queue queue2 = new Queue(model, "Queue", SchedStrategy.FCFS);
                Sink sink = new Sink(model, "Sink");

                List<JobClass> allClasses = new ArrayList<JobClass>(nClasses);

                for (int i = 0; i < nClasses; i++) {
                    OpenClass oClass = new OpenClass(model, String.format("Class%d", i));
                    double serviceRate1 = (random.nextDouble()*50)+1;
                    double serviceRate2 = random.nextDouble()*(serviceRate1-1);
                    double arrivalRate = random.nextDouble()*(serviceRate2-1);
                    source.setArrivalDistribution(oClass, new Exp(arrivalRate));
                    queue1.setService(oClass, new Exp(serviceRate1));
                    queue2.setService(oClass, new Exp(serviceRate2));
                }

                RoutingMatrix routingMatrix = new RoutingMatrix(allClasses,
                        Arrays.asList(source, queue1, queue2, sink));
                routingMatrix.addConnection(source, queue1);
                routingMatrix.addConnection(queue2, sink);
                for(JobClass jobClass : allClasses) {
                    double sinkProb = random.nextDouble();
                    routingMatrix.addConnection(queue1, sink, jobClass, sinkProb);
                    routingMatrix.addConnection(queue1, queue2, jobClass, 1.0-sinkProb);
                }
                model.link(routingMatrix);

                long startTime = System.nanoTime();
                SolverSSA solverSSA = new SolverSSA();
                solverSSA.compile(model);
                long endTime = System.nanoTime();
                double duration = (endTime - startTime);
                avgTime += duration/1000000;
            }
            outString.add(String.format("%d: %f\n", nClasses, avgTime/200));
        }

        for (String cString : outString) {
            System.out.print(cString);
        }
    }

    public static void setupTime() {
        Random random = new Random();
        List<String> timeSummaries = new ArrayList<String>();
        Map<String, Double> networkSetTimes = new HashMap<String, Double>();
        for (int i = 0; i < 35; i++) {
            List<Pair<Network, Double>> networks = Arrays.asList(
                    mm1Lowu(),
                    mm1Hiu(),
                    mm2Hiu(),
                    succSeries(),
                    parallelQueues(),
                    parallelErlang(),
                    closedNet(),
                    reworkNetwork(),
                    twoClassesSameService(),
                    twoClassesDifferentService(),
                    reworkNetworkClass(),
                    reworkNetworkHardEasy(),
                    parallelServers(),
                    layers(),
                    mm1_10(),
                    mm1ClassCapacity(),
                    parallelService()//,
                    //mapExample()
            );
            for (Pair<Network, Double> networkList : networks) {
                String networkName = networkList.getLeft().getName();
                if (i == 0) {
                    networkSetTimes.put(networkName, 0.0);
                } else if (i == 5) {
                    networkSetTimes.put(networkName, 0.0);
                }
                double timeBefore = networkSetTimes.get(networkName);
                networkSetTimes.put(networkName, timeBefore + networkList.getRight());
                if (i == 34) {
                    timeSummaries.add(String.format("%s: %f\n", networkName,(timeBefore + networkList.getRight())/30.0));
                }
            }
        }
        for (String timeSummary : timeSummaries) {
            System.out.print(timeSummary);
        }
    }

    public static void timings() {
        List<Pair<Network, Double>> networks = Arrays.asList(
                mm1Lowu(),
                mm1Hiu(),
                mm2Hiu(),
                succSeries(),
                parallelQueues(),
                parallelErlang(),
                closedNet(),
                reworkNetwork(),
                twoClassesSameService(),
                twoClassesDifferentService(),
                reworkNetworkClass(),
                reworkNetworkHardEasy(),
                parallelServers(),
                layers(),
                mm1_10(),
                mm1ClassCapacity(),
                parallelService()
        );

        List<TauLeapingOrderStrategy> orderStrategyList = new ArrayList<TauLeapingOrderStrategy>();
        orderStrategyList.add(TauLeapingOrderStrategy.RandomEvent);
        orderStrategyList.add(TauLeapingOrderStrategy.RandomEventFixed);
        orderStrategyList.add(TauLeapingOrderStrategy.DirectedGraph);
        orderStrategyList.add(TauLeapingOrderStrategy.DirectedCycle);
        List<TauLeapingStateStrategy> stateStrategyList = new ArrayList<TauLeapingStateStrategy>();
        stateStrategyList.add(TauLeapingStateStrategy.Cutoff);
        stateStrategyList.add(TauLeapingStateStrategy.TwoTimes);
        stateStrategyList.add(TauLeapingStateStrategy.TimeWarp);
        stateStrategyList.add(TauLeapingStateStrategy.TauTimeWarp);

        List<TauLeapingType> tauLeapingTypes = new ArrayList<TauLeapingType>();
        for (TauLeapingOrderStrategy orderStrategy : orderStrategyList) {
            for (TauLeapingStateStrategy stateStrategy : stateStrategyList) {
                tauLeapingTypes.add(new TauLeapingType(TauLeapingVarType.Poisson, orderStrategy, stateStrategy, 0.0625));
                break;
            }
        }

        Random random = new Random();
        List<String> timeSummaries = new ArrayList<String>();
        for (TauLeapingType tType : tauLeapingTypes) {
            double totalSolvingTime = 0;
            for (Pair<Network, Double> modelPair : networks) {
                double solvingTime = 0;
                Network model = modelPair.getLeft();
                String lineHeader = "";
                for (int i = 0; i < 35; i++) {
                    double modelTimeElapsed = modelPair.getRight();
                    //model.printSummary();

                    long startTime = System.nanoTime();
                    SolverSSA solverSSA = new SolverSSA();
                    solverSSA.compile(model);
                    solverSSA.setOptions().samples(Integer.MAX_VALUE).seed(random.nextInt());
                    solverSSA.setOptions().setEndTime(10000);
                    //solverSSA.setOptions().configureTauLeap(tType);
                    long endTime = System.nanoTime();
                    double duration = endTime - startTime;
                    double solverConfigTimeElapsed = duration / 1000000;
                    startTime = System.nanoTime();

                    Timeline timeline = solverSSA.solve();
                    endTime = System.nanoTime();
                    duration = endTime - startTime;
                    double solverTimeElapsed = duration / 1000000;
                    if (i == 5) {
                        modelTimeElapsed = 0;
                        solvingTime = 0;
                    }
                    solvingTime += solverTimeElapsed;
                    totalSolvingTime += (solvingTime/30.0);
                }
            }
            timeSummaries.add(String.format("%s: %f solving\n", tType.stringRep(), totalSolvingTime/17.0));
        }
        for (String timeSummary : timeSummaries) {
            System.out.print(timeSummary);
        }
    }

    public static void tauLeap() {
        // Repeat binomial
        List<TauLeapingType> tauLeapingTypes = new ArrayList<TauLeapingType>();
        List<Double> taus = Arrays.asList(new Double[] {0.01, 0.0625,0.125,0.25,0.5,1.0});
        List<TauLeapingOrderStrategy> orderStrategies = Arrays.asList(TauLeapingOrderStrategy.RandomEventFixed,
                                                                        TauLeapingOrderStrategy.RandomEvent,
                                                                        TauLeapingOrderStrategy.DirectedGraph,
                                                                        TauLeapingOrderStrategy.DirectedCycle);
        List<TauLeapingStateStrategy> stateStrategies = Arrays.asList(TauLeapingStateStrategy.Cutoff,
                TauLeapingStateStrategy.CycleCutoff,
                TauLeapingStateStrategy.TwoTimes,
                TauLeapingStateStrategy.TimeWarp,
                TauLeapingStateStrategy.TauTimeWarp);
        List<TauLeapingVarType> varTypes = Arrays.asList(TauLeapingVarType.Poisson, TauLeapingVarType.Binomial);
        for (TauLeapingVarType varType : varTypes) {
            for (TauLeapingOrderStrategy orderStrategy : orderStrategies) {
                for (TauLeapingStateStrategy stateStrategy : stateStrategies) {
                    for (double tau : taus) {
                        tauLeapingTypes.add(new TauLeapingType(varType, orderStrategy, stateStrategy, tau));
                    }
                }
            }
        }
        List<Pair<Network, Double>> networks = Arrays.asList(
                mm1Lowu(),
                /*mm1Hiu(),
                mm2Hiu(),
                succSeries(),
                parallelQueues(),
                parallelErlang(),
                closedNet(),
                reworkNetwork(),
                twoClassesSameService(),
                twoClassesDifferentService(),
                reworkNetworkClass(),
                reworkNetworkHardEasy(),
                parallelServers(),
                layers(),
                mm1_10(),
                mm1ClassCapacity(),*/
                parallelService()//,
                //mapExample()
        );
        Random random = new Random();
        for (Pair<Network, Double> modelPair : networks) {
            List<String> avgSummaries = new ArrayList<String>();
            List<String> smpSummaries = new ArrayList<String>();

            // this is wasteful but necessary due to coupling
            SolverSSA solverSSA2 = new SolverSSA();
            solverSSA2.setOptions().samples(10);
            solverSSA2.compile(modelPair.getLeft());
            Timeline dummyTimeline = solverSSA2.solve();

            String lineHeader = "model_name,variable,order,state,tau," + dummyTimeline.getLineHeader(modelPair.getLeft()) + ",model_setup_time, solver_config_time, solver_time\n";
            avgSummaries.add(lineHeader);
            smpSummaries.add(lineHeader);

            for (TauLeapingType tauLeapingType : tauLeapingTypes) {
                System.out.format("Current TL: %s\n", tauLeapingType.stringRep());
                try {
                    List<List<Double>> allValues = new ArrayList<List<Double>>();
                    Network model = modelPair.getLeft();
                    //String lineHeader = "";
                    for (int i = -4; i < 101; i++) {
                        double modelTimeElapsed = modelPair.getRight();

                        long startTime = System.nanoTime();
                        SolverSSA solverSSA = new SolverSSA();
                        solverSSA.compile(model);
                        solverSSA.setOptions().samples(10000000).seed(random.nextInt());
                        solverSSA.setOptions().configureTauLeap(tauLeapingType);
                        solverSSA.setOptions().setEndTime(10000);
                        solverSSA.setOptions().setTimeout(1);
                        long endTime = System.nanoTime();
                        double duration = endTime - startTime;
                        double solverConfigTimeElapsed = duration / 1000000;
                        startTime = System.nanoTime();

                        Timeline timeline = solverSSA.solve();
                        endTime = System.nanoTime();
                        duration = endTime - startTime;
                        double solverTimeElapsed = duration / 1000000;
                        if (i < 0) {
                            continue;
                        }

                        List<Double> lineValues = timeline.getLineValues(model);
                        lineValues.add(modelTimeElapsed);
                        lineValues.add(solverConfigTimeElapsed);
                        lineValues.add(solverTimeElapsed);
                        allValues.add(lineValues);
                        //timelineSummaries.add(timeline.getLineSummary(model) + String.format(",%f,%f,%f\n", modelTimeElapsed, solverConfigTimeElapsed, solverTimeElapsed));
                    }
                    String avgSummary = String.format("%s, %s, %s, %s, %f", model.getName(), tauLeapingType.getVarType().name(),
                            tauLeapingType.getOrderStrategy().name(), tauLeapingType.getStateStrategy().name(),
                            tauLeapingType.getTau());
                    String smpDevSummary = new String(avgSummary);

                    int nRows = allValues.size();
                    int nCols = allValues.get(0).size();
                    if (nRows*nCols == 0) {
                        continue;
                    }

                    Iterator<List<Double>> allValsIterator = allValues.listIterator();

                    while(allValsIterator.hasNext()) {
                        List<Double> row = allValsIterator.next();
                        for (int i = 0; i < nCols; i++) {
                            if (Double.isNaN(row.get(i)) || !Double.isFinite(row.get(i))) {
                                allValsIterator.remove();
                                break;
                            }
                        }
                    }

                    for (int i = 0; i < nCols; i++) {
                        double total = 0;
                        int validCt = 0;
                        for (int j = 0; j < nRows; j++) {
                            double cVal = allValues.get(j).get(i);
                            if ((!Double.isNaN(cVal)) && (Double.isFinite(cVal))) {
                                total += cVal;
                                validCt++;
                            }
                        }
                        if (validCt == 0) {
                            avgSummary += ",";
                            continue;
                        }
                        double colMean = total/(double)validCt;
                        avgSummary += String.format(",%f", colMean);
                        double momentSum = 0;
                        for (int j = 0; j < nRows; j++) {
                            double cVal = allValues.get(j).get(i);
                            if ((!Double.isNaN(cVal)) && (Double.isFinite(cVal))) {
                                momentSum += Math.pow(cVal - colMean, 2);
                            }
                        }
                        double colSmpDev = Math.sqrt(momentSum/(validCt-1));
                        smpDevSummary += String.format(",%f", colSmpDev);
                    }
                    avgSummary += "\n";
                    smpDevSummary += "\n";
                    avgSummaries.add(avgSummary);
                    smpSummaries.add(smpDevSummary);

                    /*timelineSummaries.add("\n");
                    timelineSummaries.add(lineHeader);
                    timelineSummaries.add("\n\n");*/
                    FileWriter writer;
                } catch (Exception e) {
                    continue;
                }
            }

            try {
                String filename = String.format("%s_avg.csv", modelPair.getLeft().getName().replace(" ", ""));
                FileWriter writer = new FileWriter("/home/matts/Masters/IndivProj/Result/TauLeapCombined/" + filename);
                for (String line : avgSummaries) {
                    writer.write(line);
                }
                writer.flush();
                writer.close();

                filename = String.format("%s_dev.csv", modelPair.getLeft().getName().replace(" ", ""));
                writer = new FileWriter("/home/matts/Masters/IndivProj/Result/TauLeapCombined/" + filename);
                for (String line : smpSummaries) {
                    writer.write(line);
                }
                writer.flush();
                writer.close();
            } catch (java.io.IOException e) {
                return;
            }
        }
    }

    public static void standardResults() {
        List<Pair<Network,Double>> networks = Arrays.asList(
                mm1Lowu(),
                mm1Hiu(),
                mm2Hiu(),
                succSeries(),
                parallelQueues(),
                parallelErlang(),
                closedNet(),
                reworkNetwork(),
                twoClassesSameService(),
                twoClassesDifferentService(),
                reworkNetworkClass(),
                reworkNetworkHardEasy(),
                parallelServers(),
                layers(),
                mm1_10(),
                mm1ClassCapacity(),
                parallelService()//,
                //mapExample()
        );
        Random random = new Random();
        List<String> timelineSummaries = new ArrayList<String>();
            for (Pair<Network, Double> modelPair : networks) {
                Network model = modelPair.getLeft();
                String lineHeader = "";
                for (int i = -4; i < 101; i++) {
                    double modelTimeElapsed = modelPair.getRight();
                    //model.printSummary();

                    long startTime = System.nanoTime();
                    SolverSSA solverSSA = new SolverSSA();
                    solverSSA.compile(model);
                    solverSSA.setOptions().samples(1000000).seed(random.nextInt());
                    long endTime = System.nanoTime();
                    double duration = endTime-startTime;
                    double solverConfigTimeElapsed = duration/1000000;
                    startTime = System.nanoTime();

                    Timeline timeline = solverSSA.solve();
                    endTime = System.nanoTime();
                    duration = endTime-startTime;
                    double solverTimeElapsed = duration/1000000;
                    if (i < 0) {
                        continue;
                    } else if (i == 0) {
                        lineHeader = "model_name," + timeline.getLineHeader(model) + ",model_setup_time, solver_config_time, solver_time\n";
                        timelineSummaries.add(lineHeader);
                    } else if (i==20) {
                        break;
                    }
                    timelineSummaries.add(timeline.getLineSummary(model)+String.format(",%f,%f,%f\n", modelTimeElapsed,solverConfigTimeElapsed,solverTimeElapsed));
                }
                timelineSummaries.add("\n");
                timelineSummaries.add(lineHeader);
                timelineSummaries.add("\n\n");
            }


            /*FileWriter writer;
            try {
                writer = new FileWriter("/home/matts/Masters/IndivProj/Result/standard.csv");
                for (String line : timelineSummaries) {
                    writer.write(line);
                }
                writer.flush();
                writer.close();
            } catch(java.io.IOException e) {
                return;
            }*/
    }

    public static void tauLeapTest() {
        double modelSetup  = 0.0;
        double solverSetup = 0.0;
        double solvingTime = 0.0;
        List<TauLeapingType> tauLeapingTypes = Arrays.asList(
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 10),
                new TauLeapingType(TauLeapingVarType.Binomial, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.001),
                new TauLeapingType(TauLeapingVarType.Binomial, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.01),
                new TauLeapingType(TauLeapingVarType.Binomial, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.1),
                new TauLeapingType(TauLeapingVarType.Binomial, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 1),
                new TauLeapingType(TauLeapingVarType.Binomial, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 10),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEvent, TauLeapingStateStrategy.Cutoff, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEvent, TauLeapingStateStrategy.Cutoff, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEvent, TauLeapingStateStrategy.Cutoff, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEvent, TauLeapingStateStrategy.Cutoff, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEvent, TauLeapingStateStrategy.Cutoff, 10),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedGraph, TauLeapingStateStrategy.Cutoff, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedGraph, TauLeapingStateStrategy.Cutoff, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedGraph, TauLeapingStateStrategy.Cutoff, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedGraph, TauLeapingStateStrategy.Cutoff, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedGraph, TauLeapingStateStrategy.Cutoff, 10),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedCycle, TauLeapingStateStrategy.Cutoff, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedCycle, TauLeapingStateStrategy.Cutoff, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedCycle, TauLeapingStateStrategy.Cutoff, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedCycle, TauLeapingStateStrategy.Cutoff, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.DirectedCycle, TauLeapingStateStrategy.Cutoff, 10),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.Cutoff, 10),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TimeWarp, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TimeWarp, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TimeWarp, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TimeWarp, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TimeWarp, 10),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TauTimeWarp, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TauTimeWarp, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TauTimeWarp, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TauTimeWarp, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TauTimeWarp, 10),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TwoTimes, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TwoTimes, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TwoTimes, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TwoTimes, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.TwoTimes, 10),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.CycleCutoff, 0.001),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.CycleCutoff, 0.01),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.CycleCutoff, 0.1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.CycleCutoff, 1),
                new TauLeapingType(TauLeapingVarType.Poisson, TauLeapingOrderStrategy.RandomEventFixed, TauLeapingStateStrategy.CycleCutoff, 10)
        );
        List<Pair<Network,Double>> networks = Arrays.asList(
                /*mm1Lowu(),
                mm1Hiu(),
                mm2Hiu(),
                succSeries(),
                parallelQueues(),
                parallelErlang(),
                closedNet(),
                reworkNetwork(),
                twoClassesSameService(),
                twoClassesDifferentService(),
                reworkNetworkClass(),
                reworkNetworkHardEasy(),*/
                parallelServers(),
                layers(),
                mm1_10(),
                mm1ClassCapacity(),
                parallelService()//,
                //mapExample()
        );
        int tTypeCt = 0;
        for (TauLeapingType tType : tauLeapingTypes) {
            tTypeCt++;
            for (int i = -4; i < 101; i++) {
                for (Pair<Network, Double> modelPair : networks) {
                    Network model = modelPair.getLeft();
                    double modelTimeElapsed = modelPair.getRight();
                    model.printSummary();

                    long startTime = System.nanoTime();
                    SolverSSA solverSSA = new SolverSSA();
                    solverSSA.compile(model);
                    solverSSA.setOptions().samples(100000);
                    //solverSSA.setOptions().configureTauLeap(tType);
                    //solverSSA.setOptions().setEndTime(10000).seed((new Random()).nextInt());
                    long endTime = System.nanoTime();
                    double duration = endTime-startTime;
                    double solverConfigTimeElapsed = duration/1000000;
                    startTime = System.nanoTime();

                    Timeline timeline = solverSSA.solve();
                    endTime = System.nanoTime();
                    duration = endTime-startTime;
                    double solverTimeElapsed = duration/1000000;
                    List<String> additionalLines = new ArrayList<String>();
                    additionalLines.add(String.format("Model setup time, %f", modelTimeElapsed));
                    additionalLines.add(String.format("Solver config time, %f", solverConfigTimeElapsed));
                    additionalLines.add(String.format("Solving time, %f", solverTimeElapsed));
                    additionalLines.add(String.format("Tau Leap config: %s", tType.stringRep()));
                    if (i <= 0) {
                        continue;
                    }
                    String modelFolder = "/home/matts/Masters/IndivProj/Result/GrandTauLeap/";
                    timeline.saveSummary(String.format("%s%s_%d_%d.csv", modelFolder, model.getName(), i, tTypeCt), model, additionalLines);

                    System.out.format("Model setup time: %f\nSolver config time: %f\nSolving time: %f\n", modelTimeElapsed, solverConfigTimeElapsed, solverTimeElapsed);
                    System.out.format("Total time: %f\n", modelTimeElapsed+solverConfigTimeElapsed+solverTimeElapsed);
                }
            }
        }
    }

    public static void tuning() {
        double modelSetup  = 0.0;
        double solverSetup = 0.0;
        double solvingTime = 0.0;
        for (int i = 0; i < 100; i++) {
            if (i == 5) {
                modelSetup = 0.0;
                solverSetup = 0.0;
                solvingTime = 0.0;
            }
            long startTime = System.nanoTime();
            List<Pair<Network,Double>> networks = Arrays.asList(
                mm1Lowu(),
                mm1Hiu(),
                succSeries(),
                parallelQueues(),
                parallelErlang()
                /*closedNet(),
                reworkNetwork(),
                twoClassesSameService(),
                twoClassesDifferentService(),
                reworkNetworkClass(),
                reworkNetworkHardEasy(),
                parallelServers(),
                layers(),
                mm1_10(),
                mm1ClassCapacity(),
                parallelService()*/
            );
            long endTime = System.nanoTime();
            double duration = endTime-startTime;
            modelSetup += (duration / 1000000);

            for (Pair<Network,Double> modelPair : networks) {
                Network model = modelPair.getLeft();
                startTime = System.nanoTime();
                SolverSSA solverSSA = new SolverSSA();
                solverSSA.compile(model);
                //solverSSA.setOptions().configureTauLeap(new TauLeapingType(TauLeapingVarType.Poisson,
                        //TauLeapingOrderStrategy.RandomEventFixed,TauLeapingStateStrategy.Cutoff,1));
                solverSSA.setOptions().samples(100000).seed(105);
                /*solverSSA.setOptions().samples(10000000).seed(105);
                solverSSA.setOptions().setEndTime(1000).seed(100);*/
                endTime = System.nanoTime();
                duration = endTime - startTime;
                solverSetup += (duration / 1000000);
                startTime = System.nanoTime();

                Timeline timeline = solverSSA.solve();
                endTime = System.nanoTime();
                duration = endTime - startTime;
                solvingTime += (duration/1000000);
            }
        }
        System.out.format("Total model setup: %f\n", modelSetup);
        System.out.format("Total solver setup: %f\n", solverSetup);
        System.out.format("Total solving: %f\n", solvingTime);
    }

    public static Pair<Network,Double> mm1Lowu() {
        /*
            Benchmark 1, a low utilization m/m/ queue.
         */
        long startTime = System.nanoTime();
        Network model = new Network("MM1LowU");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(2));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(10));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> mm1Hiu() {
        /*
            Benchmark 2, a high utilization m/m/1 queue.
         */
        long startTime = System.nanoTime();
        Network model = new Network("MM1HighU");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(10));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> mm2Hiu() {
        /*
            Benchmark 3, a high utilization m/m/2 queue.
         */
        long startTime = System.nanoTime();
        Network model = new Network("MM2HighU");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(5));
        queue.setNumberOfServers(2);
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> succSeries() {
        /*
            Benchmark 3, 3 queues in series
         */
        long startTime = System.nanoTime();
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

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network, Double> parallelQueues() {
        /*
            Benchmark 4, 3 queues in parallel
         */
        long startTime = System.nanoTime();
        Network model = new Network("Parallel Queues");
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

        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(openClass),
                Arrays.asList(source, queue1, queue2, queue3, sink));
        routingMatrix.addConnection(source, queue1);
        routingMatrix.addConnection(queue1, sink);
        routingMatrix.addConnection(source, queue2);
        routingMatrix.addConnection(queue2, sink);
        routingMatrix.addConnection(source, queue3);
        routingMatrix.addConnection(queue3, sink);
        model.link(routingMatrix);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> parallelErlang() {
        /*
            Benchmark 5, 3 queues in parallel with erlang distributed times
         */
        long startTime = System.nanoTime();
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

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> closedNet() {
        /*
            Benchmark 6, 3 queues in a loop with a closed class
         */

        long startTime = System.nanoTime();
        Network model = new Network("3 Closed");
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);

        ClosedClass closedClass = new ClosedClass(model, "Closed Class", 15, queue1);

        queue1.setService(closedClass, new Exp(10));
        queue2.setService(closedClass, new Exp(11));
        queue3.setService(closedClass, new Exp(12));

        model.link(model.serialRouting(queue1, queue2, queue3));

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> reworkNetwork() {
        /*
            Benchmark 7, 3 queues in series, with a router to rework 10%
         */
        long startTime = System.nanoTime();
        Network model = new Network("3 Series with rework");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Exp(12));
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        queue2.setService(openClass, new Exp(11));
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);
        queue3.setService(openClass, new Exp(10));
        Router router = new Router(model, "Router");
        Sink sink = new Sink(model, "Sink");

        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(openClass),
                Arrays.asList(source, queue1, queue2, queue3, router, sink));
        routingMatrix.addConnection(source, queue1);
        routingMatrix.addConnection(queue1, queue2);
        routingMatrix.addConnection(queue2, queue3);
        routingMatrix.addConnection(queue3, router);
        routingMatrix.addConnection(router, sink, openClass, 0.9);
        routingMatrix.addConnection(router, queue1, openClass, 0.1);

        model.link(routingMatrix);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> twoClassesSameService() {
        /*
            Benchmark 8, 2 classes served identically
         */
        long startTime = System.nanoTime();
        Network model = new Network("2C S S");
        OpenClass openClass1 = new OpenClass(model, "Open Class 1");
        OpenClass openClass2 = new OpenClass(model, "Open Class 2");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass1, new Exp(8));
        source.setArrivalDistribution(openClass2, new Exp(2));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass1, new Exp(12));
        queue.setService(openClass2, new Exp(12));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> twoClassesDifferentService() {
        /*
            Benchmark 9, 2 classes served differently
         */
        long startTime = System.nanoTime();
        Network model = new Network("2C D S");
        OpenClass openClass1 = new OpenClass(model, "Open Class 1");
        OpenClass openClass2 = new OpenClass(model, "Open Class 2");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass1, new Exp(8));
        source.setArrivalDistribution(openClass2, new Exp(5));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass1, new Exp(12));
        queue.setService(openClass2, new Exp(16));
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> reworkNetworkClass() {
        /*
            Benchmark 10, rework with a different class
         */
        long startTime = System.nanoTime();
        Network model = new Network("3 Series with rework class");
        OpenClass openClass = new OpenClass(model, "Open Class");
        OpenClass reworkClass = new OpenClass(model, "Rework Class");

        Map<JobClass, Map<JobClass, Double>> csMatrix = new HashMap<JobClass, Map<JobClass, Double>>();
        csMatrix.put(openClass, new HashMap<JobClass, Double>());
        csMatrix.get(openClass).put(reworkClass, 1.0);

        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Exp(12));
        queue1.setService(reworkClass, new Exp(24));
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        queue2.setService(openClass, new Exp(11));
        queue2.setService(reworkClass, new Exp(22));
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);
        queue3.setService(openClass, new Exp(10));
        queue3.setService(reworkClass, new Exp(20));
        Router router = new Router(model, "Router");
        ClassSwitch classSwitch = new ClassSwitch(model, "Class Switch", csMatrix);
        Sink sink = new Sink(model, "Sink");

        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(openClass, reworkClass),
                Arrays.asList(source, queue1, queue2, queue3, router, classSwitch, sink));
        routingMatrix.addConnection(source, queue1);
        routingMatrix.addConnection(queue1, queue2);
        routingMatrix.addConnection(queue2, queue3);
        routingMatrix.addConnection(queue3, router);
        routingMatrix.addConnection(router, sink, openClass, 0.9);
        routingMatrix.addConnection(router, classSwitch, openClass, 0.1);
        routingMatrix.addConnection(router, sink, reworkClass, 1);
        routingMatrix.addConnection(classSwitch, queue1);
        model.link(routingMatrix);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }


    public static Pair<Network,Double> reworkNetworkHardEasy() {
        /*
            Benchmark 11, rework with hard and easy reworks
         */
        long startTime = System.nanoTime();
        Network model = new Network("3 Series with 2 rework classes");
        OpenClass openClass = new OpenClass(model, "Open Class");
        OpenClass easyRework = new OpenClass(model, "Easy Rework Class");
        OpenClass hardRework = new OpenClass(model, "Hard Rework Class");

        Map<JobClass, Map<JobClass, Double>> csMatrix = new HashMap<JobClass, Map<JobClass, Double>>();
        csMatrix.put(openClass, new HashMap<JobClass, Double>());
        csMatrix.get(openClass).put(easyRework, 0.9);
        csMatrix.get(openClass).put(hardRework, 0.1);

        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Exp(12));
        queue1.setService(easyRework, new Exp(24));
        queue1.setService(hardRework, new Exp(6));
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        queue2.setService(openClass, new Exp(11));
        queue2.setService(easyRework, new Exp(22));
        queue2.setService(hardRework, new Exp(5.5));
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);
        queue3.setService(openClass, new Exp(10));
        queue3.setService(easyRework, new Exp(20));
        queue3.setService(hardRework, new Exp(5));
        Router router = new Router(model, "Router");
        ClassSwitch classSwitch = new ClassSwitch(model, "Class Switch", csMatrix);
        Sink sink = new Sink(model, "Sink");

        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(openClass, easyRework, hardRework),
                Arrays.asList(source, queue1, queue2, queue3, router, classSwitch, sink));
        routingMatrix.addConnection(source, queue1);
        routingMatrix.addConnection(queue1, queue2);
        routingMatrix.addConnection(queue2, queue3);
        routingMatrix.addConnection(queue3, router);
        routingMatrix.addConnection(router, sink, openClass, 0.9);
        routingMatrix.addConnection(router, classSwitch, openClass, 0.1);
        routingMatrix.addConnection(router, sink, easyRework, 1);
        routingMatrix.addConnection(router, sink, hardRework, 1);
        routingMatrix.addConnection(classSwitch, queue1);
        model.link(routingMatrix);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> parallelServers() {
        /*
            Benchmark 12, 3 queues in parallel with different numbers of servers
         */
        long startTime = System.nanoTime();
        Network model = new Network("Parallel Servers");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(30));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Exp(6));
        queue1.setNumberOfServers(2);
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.FCFS);
        queue2.setService(openClass, new Exp(4));
        queue2.setNumberOfServers(3);
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.FCFS);
        queue3.setService(openClass, new Exp(4));
        queue3.setNumberOfServers(4);
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

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> layers() {
        /*
            Benchmark 13,  5 layers of queues with different service rates and distributions
         */
        long startTime = System.nanoTime();
        Network model = new Network("Burst");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(30));
        List<List<Queue>> layers = new ArrayList<List<Queue>>();

        List<Queue> layer1 = new ArrayList<Queue>();
        layer1.add(new Queue(model, "Queue 1"));
        layer1.get(0).setService(openClass, new Exp(40));
        List<Queue> layer2 = new ArrayList<Queue>();
        layer2.add(new Queue(model, "Queue 2"));
        layer2.get(0).setService(openClass, new Exp(20));
        layer2.add(new Queue(model, "Queue 3"));
        layer2.get(1).setService(openClass, new Exp(10));
        layer2.get(1).setNumberOfServers(2);
        List<Queue> layer3 = new ArrayList<Queue>();
        layer3.add(new Queue(model, "Queue 4"));
        layer3.get(0).setService(openClass, new Exp(10));
        layer3.get(0).setNumberOfServers(1);
        layer3.add(new Queue(model, "Queue 5"));
        layer3.get(1).setService(openClass, new Erlang(40, 4));
        layer3.get(1).setNumberOfServers(1);
        layer3.add(new Queue(model, "Queue 6"));
        layer3.get(2).setService(openClass, new Exp(5));
        layer3.get(2).setNumberOfServers(4);
        layer3.add(new Queue(model, "Queue 7"));
        layer3.get(3).setService(openClass, new Erlang(10, 2));
        layer3.get(3).setNumberOfServers(2);
        List<Queue> layer4 = new ArrayList<Queue>();
        layer4.add(new Queue(model, "Queue 8"));
        layer4.get(0).setService(openClass, new Exp(40));
        layer4.get(0).setNumberOfServers(1);
        layer4.add(new Queue(model, "Queue 9"));
        layer4.get(1).setService(openClass, new Exp(10));
        layer4.get(1).setNumberOfServers(4);
        List<Queue> layer5 = new ArrayList<Queue>();
        layer5.add(new Queue(model, "Queue 10"));
        layer5.get(0).setService(openClass, new Exp(50));
        layers.add(layer1);
        layers.add(layer2);
        layers.add(layer3);
        layers.add(layer4);
        layers.add(layer5);
        Sink sink = new Sink(model, "Sink");

        List<Node> allNodes = new ArrayList<Node>();
        allNodes.add(source);
        allNodes.add(sink);
        for (List<Queue> layer : layers) {
            for (Queue q : layer) {
                allNodes.add(q);
            }
        }


        RoutingMatrix routingMatrix = new RoutingMatrix(Arrays.asList(openClass),
                allNodes);

        routingMatrix.addConnection(source, layers.get(0).get(0));
        routingMatrix.addConnection(layers.get(0).get(0), layers.get(1).get(0));
        routingMatrix.addConnection(layers.get(0).get(0), layers.get(1).get(1));
        routingMatrix.addConnection(layers.get(1).get(0), layers.get(2).get(0));
        routingMatrix.addConnection(layers.get(1).get(0), layers.get(2).get(1));
        routingMatrix.addConnection(layers.get(1).get(1), layers.get(2).get(2));
        routingMatrix.addConnection(layers.get(1).get(1), layers.get(2).get(3));
        routingMatrix.addConnection(layers.get(2).get(0), layers.get(3).get(0));
        routingMatrix.addConnection(layers.get(2).get(1), layers.get(3).get(0));
        routingMatrix.addConnection(layers.get(2).get(2), layers.get(3).get(1));
        routingMatrix.addConnection(layers.get(2).get(3), layers.get(3).get(1));;
        routingMatrix.addConnection(layers.get(3).get(0), layers.get(4).get(0));
        routingMatrix.addConnection(layers.get(3).get(1), layers.get(4).get(0));
        routingMatrix.addConnection(layers.get(4).get(0), sink);

        model.link(routingMatrix);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network, Double> mm1_10() {
        long startTime = System.nanoTime();
        Network model = new Network("MM1 10");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(8));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
        queue.setService(openClass, new Exp(10));
        queue.setCap(10);
        Sink sink = new Sink(model, "Sink");

        model.link(model.serialRouting(source,queue,sink));

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static Pair<Network,Double> mm1ClassCapacity() {
        /*
            Benchmark 15, 2 classes served differently with different service rates
         */
        long startTime = System.nanoTime();
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

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }


    public static Pair<Network, Double> parallelService() {
        /*
            Benchmark 16, 3 queues in parallel with different service strategies
         */
        long startTime = System.nanoTime();
        Network model = new Network("Parallel Service");
        OpenClass openClass = new OpenClass(model, "Open Class");
        Source source = new Source(model,"Source");
        source.setArrivalDistribution(openClass, new Exp(10));
        Queue queue1 = new Queue(model, "Queue1", SchedStrategy.FCFS);
        queue1.setService(openClass, new Erlang(10,2));
        Queue queue2 = new Queue(model, "Queue2", SchedStrategy.LCFS);
        queue2.setService(openClass, new Erlang(10,2));
        Queue queue3 = new Queue(model, "Queue3", SchedStrategy.PS);
        queue3.setService(openClass, new Erlang(10,2));
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

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
        return new Pair<Network, Double>(model,duration/1000000);
    }

    public static  Pair<Network,Double> mapExample() {
        /*
            Sample 17, a G/M/1 queue with an MAP input
         */
        long startTime = System.nanoTime();
        Network model = new Network("MAP/M/1");
        OpenClass openClass = new OpenClass(model, "Open Class");
        List<List<Double>> arrivalRates = new ArrayList<List<Double>>(3);
        List<Double> arrivalRates1 = new ArrayList<Double>(3);
        List<Double> arrivalRates2 = new ArrayList<Double>(3);
        List<Double> arrivalRates3 = new ArrayList<Double>(3);
        arrivalRates1.add(40.0);
        arrivalRates1.add(3.0);
        arrivalRates1.add(2.0);
        arrivalRates2.add(2.0);
        arrivalRates2.add(20.0);
        arrivalRates2.add(3.0);
        arrivalRates3.add(10.0);
        arrivalRates3.add(10.0);
        arrivalRates3.add(80.0);
        arrivalRates.add(arrivalRates1);
        arrivalRates.add(arrivalRates2);
        arrivalRates.add(arrivalRates3);

        List<List<Double>> rateTransitions = new ArrayList<List<Double>>(3);
        List<Double> rateTransitions1 = new ArrayList<Double>(3);
        rateTransitions1.add(-51.0);
        rateTransitions1.add(5.0);
        rateTransitions1.add(1.0);
        List<Double> rateTransitions2 = new ArrayList<Double>(3);
        rateTransitions2.add(12.0);
        rateTransitions2.add(-38.0);
        rateTransitions2.add(1.0);
        List<Double> rateTransitions3 = new ArrayList<Double>(3);
        rateTransitions3.add(10.0);
        rateTransitions3.add(3.0);
        rateTransitions3.add(-113.0);
        rateTransitions.add(rateTransitions1);
        rateTransitions.add(rateTransitions2);
        rateTransitions.add(rateTransitions3);
        Source source = new Source(model,"Source");
            source.setArrivalDistribution(openClass, new MAPProcess(3, rateTransitions, arrivalRates));
        Queue queue = new Queue(model, "Queue", SchedStrategy.FCFS);
            queue.setService(openClass, new Exp(75));
            queue.setCap(10);
        Sink sink = new Sink(model, "Sink");

            model.link(model.serialRouting(source,queue,sink));

        long endTime = System.nanoTime();
        double duration = (endTime - startTime);
            return new Pair<Network, Double>(model,duration/1000000);
    }
}
