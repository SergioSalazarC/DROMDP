package proposal.shake.junie;

import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Uniform Random Ruin:
 * Randomly selects positive-label nodes across the entire graph and sets their labels to 0.
 */
public class UniformRandomRuinShake extends AbstractRuinRecreateShake {

    public UniformRandomRuinShake() {
        super();
    }

    public UniformRandomRuinShake(double minPerturbationRatio, double maxPerturbationRatio) {
        super(minPerturbationRatio, maxPerturbationRatio);
    }

    @Override
    protected Set<Integer> ruin(DROMDSolution solution, DROMDInstance instance,
                                List<Integer> positiveNodes, Set<Integer> fixedNodes,
                                int targetRuin, Random rnd) {
        Set<Integer> ruined = new HashSet<>();
        List<Integer> shuffled = new ArrayList<>(positiveNodes);
        Collections.shuffle(shuffled, rnd);

        for (int i = 0; i < Math.min(targetRuin, shuffled.size()); i++) {
            int node = shuffled.get(i);
            ruined.add(node);
            solution.addLabel(node, 0);
        }
        return ruined;
    }
}
