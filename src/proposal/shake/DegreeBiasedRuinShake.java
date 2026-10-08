package proposal.shake;

import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Redundancy / Degree-biased Ruin:
 * Prioritizes removing positive labels from nodes with lower degrees to favor higher-degree dominators.
 */
public class DegreeBiasedRuinShake extends AbstractRuinRecreateShake {

    public DegreeBiasedRuinShake() {
        super();
    }

    public DegreeBiasedRuinShake(double minPerturbationRatio, double maxPerturbationRatio) {
        super(minPerturbationRatio, maxPerturbationRatio);
    }

    @Override
    protected Set<Integer> ruin(DROMDSolution solution, DROMDInstance instance,
                                List<Integer> positiveNodes, Set<Integer> fixedNodes,
                                int targetRuin, Random rnd) {
        Set<Integer> ruined = new HashSet<>();
        List<Integer> sortedNodes = new ArrayList<>(positiveNodes);
        Collections.shuffle(sortedNodes, rnd);
        sortedNodes.sort(Comparator.comparingInt(n -> instance.getAdjacent(n).size()));

        for (int i = 0; i < Math.min(targetRuin, sortedNodes.size()); i++) {
            int node = sortedNodes.get(i);
            ruined.add(node);
            solution.addLabel(node, 0);
        }
        return ruined;
    }
}
