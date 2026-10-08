package proposal.algorithm;

import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;
import proposal.Main;
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
    public IteratedLocalSearch_multipleExecutions(Constructive<DROMDInstance, DROMDSolution> constructive, int maxIWI, double perturbationPercentage, int p) {
        this.constructive=constructive;
        this.maxIWI=maxIWI;
        this.perturbationPercentage=perturbationPercentage;
        this.p=p;
    }
    public IteratedLocalSearch_multipleExecutions(Constructive<DROMDInstance, DROMDSolution> constructive, Improvement<DROMDSolution> improvement, int maxIWI, double perturbationPercentage, int p, int numExecutions) {
        this.constructive=constructive;
        this.improvement=improvement;
        this.maxIWI=maxIWI;
        this.perturbationPercentage=perturbationPercentage;
        this.p=p;
        this.numExecutions=numExecutions;
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
                perturb(localSol);
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
    private void perturb(DROMDSolution solution){

        List<Integer> candidates=solution.getInstance().getCandidates();
        int limit= (int) Math.ceil(solution.getInstance().getNumNodes()*perturbationPercentage);
        if(p==1){ //pongo valor aleatorio a un nodo y arreglo sus vecinos (entre 0 y su valor original??)
            for(int i=0; i<limit; i++){
                int node=candidates.get(i);
                int rnd=RandomManager.getRandom().nextInt(Main.MIN_VALUE,Main.MAX_VALUE+1);
                solution.addLabel(node,rnd);
                if(!solution.isFeasibleTheMovement(node)){
                    solution.fixNeighboursValue(node);
                }
            }
        }else{ //pongo un numero aleatorio entre el asignado previamente y 3 de momento esto funciona mejor que la de arriba
            for(int i=0; i<limit; i++){
                int node=candidates.get(i);
                int originalValue=solution.getNodeValue(node);
                if(originalValue!=Main.MAX_VALUE){
                    int rnd=RandomManager.getRandom().nextInt(originalValue+1,Main.MAX_VALUE+1);
                    solution.addLabel(node,rnd);
                }
            }
        }
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
        return this.getClass().getSimpleName()+"("+constructive.toString()+"_"+imp+"ItersWI"+maxIWI+"Perturbation"+p+"percentage"+perturbationPercentage+")";
    }
}
