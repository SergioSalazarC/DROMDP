package proposal.shake;

import grafo.optilib.tools.RandomManager;
import proposal.Main;
import proposal.structure.DROMDSolution;

import java.util.List;

public class SOTAShake implements Shake{
    @Override
    public DROMDSolution perturb(DROMDSolution solutionOR) {
        DROMDSolution solution = new DROMDSolution(solutionOR);

        List<Integer> candidates=solution.getInstance().getCandidates();
        int limit= (int) Math.ceil(solution.getInstance().getNumNodes()*0.125);


        for(int i=0; i<limit; i++){
            int node=candidates.get(i);
            int originalValue=solution.getNodeValue(node);
            if(originalValue!=Main.MAX_VALUE){
                int rnd=RandomManager.getRandom().nextInt(originalValue+1,Main.MAX_VALUE+1);
                solution.addLabel(node,rnd);
            }
        }

        return solution;
    }


}
