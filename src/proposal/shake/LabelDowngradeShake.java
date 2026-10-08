package proposal.shake;

import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Label Downgrade Ruin:
 * Decreases label values instead of removing them completely (3 -> 2 or 1, and 2 -> 0),
 * exploring fine-grained cost reductions.
 */
public class LabelDowngradeShake extends AbstractRuinRecreateShake {

    public LabelDowngradeShake() {
        super();
    }

    public LabelDowngradeShake(double minPerturbationRatio, double maxPerturbationRatio) {
        super(minPerturbationRatio, maxPerturbationRatio);
    }

    @Override
    protected Set<Integer> ruin(DROMDSolution solution, DROMDInstance instance,
                                List<Integer> positiveNodes, Set<Integer> fixedNodes,
                                int targetRuin, Random rnd) {
        Set<Integer> ruined = new HashSet<>();
        List<Integer> shuffled = new ArrayList<>(positiveNodes);
        Collections.shuffle(shuffled, rnd);

        int count = 0;
        for (int node : shuffled) {
            if (count >= targetRuin) break;
            int curVal = solution.getNodeValue(node);
            if (curVal == 3) {
                int nextVal = rnd.nextBoolean() ? 2 : 1;
                solution.addLabel(node, nextVal);
                ruined.add(node);
                count++;
            } else if (curVal == 2) {
                solution.addLabel(node, 0);
                ruined.add(node);
                count++;
            }
        }
        return ruined;
    }
}
