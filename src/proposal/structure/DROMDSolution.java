package proposal.structure;

import grafo.optilib.structure.Solution;
import proposal.Main;

import java.util.List;

public class DROMDSolution implements Solution {
    private DROMDInstance instance;
    private int solution[];
    private int valueOF;
    private boolean hasChangedOF;
    private boolean hasChangedFeasibility;
    private boolean isFeasible;
    private final int FIXEDNODESVALUE=2;

    public DROMDSolution(DROMDInstance instance){
        this.instance=instance;
        this.isFeasible=false;
        this.hasChangedOF=true;
        this.hasChangedFeasibility=true;
        this.valueOF=0;
        this.solution=new int[instance.getNumNodes()];
    }
    public DROMDSolution(DROMDSolution solution){
        copy(solution);
    }
    public void copy(DROMDSolution solution){
        this.instance=solution.instance;
        this.hasChangedOF= solution.hasChangedOF;
        this.hasChangedFeasibility= solution.hasChangedFeasibility;
        this.isFeasible=solution.isFeasible;
        this.valueOF=solution.valueOF;
        this.solution = solution.solution.clone();
    }
    public void initialize(int value){
        for (int i = 0; i < instance.getNumNodes(); i++) {
            if(instance.fixedNodes().contains(i)){
                solution[i]=FIXEDNODESVALUE;
            }else{
                solution[i]=value;
            }
        }
    }
    public void clean(){
        for(int degree: instance.degrees()){
            List<Integer> nodesDegree = instance.nodesWithDegree(degree);
            for(int node: nodesDegree) {
                selectBestValue(node);
            }
        }
    }
    public void addLabel(int node, int value){
        if(instance.fixedNodes().contains(node)){return;}
        solution[node]=value;
        hasChangedFeasibility=true;
        hasChangedOF=true;
    }
    public void fixNeighboursValue(int node){
        for(int neighbour:instance.getAdjacent(node)){
            selectNewValueFromOriginal(neighbour);
        }
    }
    public void reduceNeighboursValue(int node){
        for(int neighbour:instance.getAdjacent(node)){
            selectBestValue(neighbour);
        }
    }
    public boolean isFeasible(){
        if(hasChangedFeasibility){
            for(int i=0; i<instance.getNumNodes();i++){
                if(!isFeasiblePartial(i)){
                    return isFeasible=false;
                }
            }
            isFeasible=true;
            hasChangedFeasibility=false;
        }
        return isFeasible;
    }
    public boolean isFeasibleAll(){
        for(int i=0; i<instance.getNumNodes();i++){
            if(!isFeasiblePartial(i)){
                return isFeasible=false;
            }
        }
        isFeasible=true;
        hasChangedFeasibility=false;
        return isFeasible;
    }
    private boolean isFeasiblePartial(int node){
        if(solution[node]>=(Main.MAX_VALUE-1)){//si vale 2 nos da igual
            return true;
        }
        int sum=solution[node];
        for(int neighbour:instance.getAdjacent(node)){
            if (solution[neighbour] == Main.MAX_VALUE){
                return true;
            }else if (solution[neighbour] == (Main.MAX_VALUE-1)){
                sum+=solution[neighbour];
                if(sum>=Main.MAX_VALUE) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean isFeasibleTheMovement(int node){
        if(!isFeasiblePartial(node)){
            return false;
        }
        for(int neighbor:instance.getAdjacent(node)){
            if(!isFeasiblePartial(neighbor)){
                return false;
            }
        }
        return true;
    }
    private void calculateOF(){
        valueOF=0;
        for (int i = 0; i < instance.getNumNodes(); i++) {
            valueOF+=solution[i];
        }
    }
    public void selectNewValueFromOriginal(int node){
        int originalValue=solution[node];
        for(int i=originalValue;i<=Main.MAX_VALUE;i++){
            addLabel(node,i);
            if(isFeasibleTheMovement(node)){
                return;
            }
        }
        addLabel(node,originalValue);
    }
    public void selectBestValue(int node){
        int originalValue=solution[node];
        for(int i=Main.MIN_VALUE;i<=Main.MAX_VALUE;i++){
            addLabel(node,i);
            if(isFeasibleTheMovement(node)){
                return;
            }
        }
        addLabel(node, originalValue);
    }
    public DROMDInstance getInstance() {
        return instance;
    }
    public int getNodeValue(int node) {
        return solution[node];
    }
    public int getOF() {
        if(hasChangedOF){
            calculateOF();
            hasChangedOF=false;
        }
        return valueOF;
    }
}