package proposal.algorithm;

import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;
import proposal.Main;
import proposal.constructive.GreedyDestructive;
import proposal.improvement.LocalSearch2;
import proposal.shake.NonShake;
import proposal.shake.Shake;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.List;

public class IteratedLocalSearch_multipleExecutions implements Algorithm<DROMDInstance, DROMDSolution> {
    private int numSolutions;
    private int maxIWI;
    private int p;
    private int numExecutions;
    private double perturbationPercentage;
    private DROMDSolution bestSol;
    private Constructive<DROMDInstance, DROMDSolution> constructive;
    private Improvement<DROMDSolution> improvement;
    private Shake shake;

    public IteratedLocalSearch_multipleExecutions(Shake shake){
        this.shake=shake;
        this.improvement=new LocalSearch2();
        this.constructive=new GreedyDestructive(3);
        this.maxIWI=80;
        this.perturbationPercentage=0.125;
        this.p=2;
        this.numExecutions=1;
    }

    public IteratedLocalSearch_multipleExecutions(Shake shake, int numExec){
        this.shake=shake;
        this.improvement=new LocalSearch2();
        this.constructive=new GreedyDestructive(3);
        this.maxIWI=80;
        this.perturbationPercentage=0.125;
        this.p=2;
        this.numExecutions=numExec;
    }


    public IteratedLocalSearch_multipleExecutions(Constructive<DROMDInstance, DROMDSolution> constructive, int maxIWI, double perturbationPercentage, int p) {
        this.constructive=constructive;
        this.maxIWI=maxIWI;
        this.perturbationPercentage=perturbationPercentage;
        this.p=p;
        this.shake=new NonShake();
    }
    public IteratedLocalSearch_multipleExecutions(Constructive<DROMDInstance, DROMDSolution> constructive, Improvement<DROMDSolution> improvement, int maxIWI, double perturbationPercentage, int p, int numExecutions, Shake shake) {
        this.constructive=constructive;
        this.improvement=improvement;
        this.maxIWI=maxIWI;
        this.perturbationPercentage=perturbationPercentage;
        this.p=p;
        this.numExecutions=numExecutions;
        this.shake=shake;
    }
    @Override
    public Result execute(DROMDInstance instance) {
        RandomManager.setSeed(2304);
        Result result=new Result(instance.getName());
        System.out.println(instance.getName());
        for (int i = 1; i < numExecutions+1; i++) {
            System.out.print("Execution "+i+" ");
            Timer.initTimer();
            //Algorithm
            DROMDSolution sol=constructive.constructSolution(instance);
            if(improvement!=null) improvement.improve(sol);
            bestSol=new DROMDSolution(sol);
            int itersWithoutImprove=0;
            while(itersWithoutImprove<maxIWI){
                DROMDSolution localSol=new DROMDSolution(bestSol);
                localSol = shake.perturb(localSol);
                if(improvement!=null) improvement.improve(localSol);
                localSol.clean();
                if(localSol.getOF()<bestSol.getOF()){
                    bestSol.copy(localSol);
                    itersWithoutImprove = 0;
                }else{
                    itersWithoutImprove++;
                }
            }
            //end
            result.add("OF"+i,bestSol.getOF());
            result.add("Time"+i,Timer.getTime()/1000f);
            System.out.print("OF "+bestSol.getOF()+" Time "+Timer.getTime()/1000f+" ");
        }
        System.out.println();
        //System.out.println(bestSol.getOF()+" "+Timer.getTime()/1000f);
        return result;
    }

    @Override
    public DROMDSolution getBestSolution() {
        return bestSol;
    }
    public String toString(){
        String imp="";
        if(improvement!=null){
            imp=improvement.toString()+"_";
        }
        return this.getClass().getSimpleName()+"("+shake.getClass().getSimpleName()+")";
    }
}
