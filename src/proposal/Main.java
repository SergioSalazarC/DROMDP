package proposal;

import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.results.Experiment;
import proposal.algorithm.*;
import proposal.constructive.GreedyDestructive;
import proposal.improvement.LocalSearch2;
import proposal.shake.*;
import proposal.structure.DROMDFactory;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.Calendar;

public class Main {
    public static final int MIN_VALUE=0;
    public static final int MAX_VALUE=3;
    public static void main(String[] args) {
        Algorithm<DROMDInstance, DROMDSolution>[] algorithms = new Algorithm[]{
                new IteratedLocalSearch_multipleExecutions(new GreedyDestructive(MAX_VALUE),new LocalSearch2(),80,0.125,2,1, new SOTAShake()),
                new IteratedLocalSearch_multipleExecutions(new GreedyDestructive(MAX_VALUE),new LocalSearch2(),80,0.125,2,1, new SpatialClusterShake()),
                new IteratedLocalSearch_multipleExecutions(new GreedyDestructive(MAX_VALUE),new LocalSearch2(),80,0.125,2,1, new HubRelocationShake()),
                new IteratedLocalSearch_multipleExecutions(new GreedyDestructive(MAX_VALUE),new LocalSearch2(),80,0.125,2,1, new DegreeBiasedRuinShake()),
                new IteratedLocalSearch_multipleExecutions(new GreedyDestructive(MAX_VALUE),new LocalSearch2(),80,0.125,2,1, new LabelDowngradeShake()),
                new IteratedLocalSearch_multipleExecutions(new GreedyDestructive(MAX_VALUE),new LocalSearch2(),80,0.125,2,1, new UniformRandomRuinShake()),
                new IteratedLocalSearch_multipleExecutions(new GreedyDestructive(MAX_VALUE),new LocalSearch2(),80,0.125,2,1, new LLMShake()),
        };
        String instanceFolder = ((args.length == 0) ? "instances/less10": args[0]);
        DROMDFactory factory = new DROMDFactory();
        Experiment<DROMDInstance, DROMDFactory, DROMDSolution> experiment = new Experiment<>(algorithms, factory);

        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        String date = String.format("%04d-%02d-%02d", year, month, day);
        String outDir = "experiments/" + date + "/";

        experiment.launch(outDir, instanceFolder, new String[]{".mtx"});
    }
}
