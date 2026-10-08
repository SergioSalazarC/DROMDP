package proposal.constructive;
import grafo.optilib.metaheuristics.Constructive;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedyDestructive implements Constructive<DROMDInstance, DROMDSolution> {
    private int initialValue;
    public GreedyDestructive(int initialValue){
        this.initialValue=initialValue;
    }
    @Override
    public DROMDSolution constructSolution(DROMDInstance instance) {
        DROMDSolution sol=new DROMDSolution(instance);
        sol.initialize(initialValue);
        for (int degree: instance.degrees()) {
            List<Integer> nodesDegree = instance.nodesWithDegree(degree);
            for(int node: nodesDegree) {
                sol.selectBestValue(node);
            }
        }
        return sol;
    }
    public String toString(){
        return this.getClass().getSimpleName()+"InitialValue"+initialValue;
    }
}