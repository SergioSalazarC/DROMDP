package proposal.improvement;

import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.Timer;
import proposal.Main;
import proposal.structure.DROMDSolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LocalSearch2 implements Improvement<DROMDSolution> {
    //recorrer nodos de mayor a menor grado, pongo 3, pruebo a reducir los vecinos, luego pruebo 2 y lo mismo (de 3 a tu etiqueta)
    @Override
    public void improve(DROMDSolution solution) {
        boolean improve=true;
        while(improve){
            improve=checkImprove(solution);
            if (Timer.timeReached()) break;
        }
    }
    private boolean checkImprove(DROMDSolution solution){
        DROMDSolution original = new DROMDSolution(solution);
        List<Integer> degrees=solution.getInstance().reverseDegrees();
        for (Integer degree : degrees) {
            List<Integer> nodesDegree = solution.getInstance().nodesWithDegree(degree);
            for(int node: nodesDegree) {
                int previousOF= solution.getOF();
                int previousValue=solution.getNodeValue(node);
                int value=Main.MAX_VALUE;
                while(Main.MIN_VALUE<=value && value>previousValue){
                    solution.addLabel(node,value);
                    solution.reduceNeighboursValue(node);
                    int newOF=solution.getOF();
                    if(newOF<previousOF){
                        return true;
                    }
                    solution.copy(original);
                    value--;
                }
            }
        }
        return false;
    }
    public String toString(){
        return this.getClass().getSimpleName();
    }
}
