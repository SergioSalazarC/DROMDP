package proposal.shake.junie;

import proposal.Main;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Hub Relocation / Shift Ruin:
 * Identifies major dominating hubs (labels 2 or 3), sets them to 0 and shifts domination
 * to one of their adjacent neighbors.
 */
public class HubRelocationShake extends AbstractRuinRecreateShake {

    public HubRelocationShake() {
        super();
    }

    public HubRelocationShake(double minPerturbationRatio, double maxPerturbationRatio) {
        super(minPerturbationRatio, maxPerturbationRatio);
    }

    @Override
    protected Set<Integer> ruin(DROMDSolution solution, DROMDInstance instance,
                                List<Integer> positiveNodes, Set<Integer> fixedNodes,
                                int targetRuin, Random rnd) {
        Set<Integer> ruined = new HashSet<>();
        List<Integer> hubs = new ArrayList<>();
        for (int node : positiveNodes) {
            if (solution.getNodeValue(node) >= 2) {
                hubs.add(node);
            }
        }

        if (!hubs.isEmpty()) {
            Collections.shuffle(hubs, rnd);
            int count = 0;
            for (int hub : hubs) {
                if (count >= targetRuin) break;
                ruined.add(hub);
                solution.addLabel(hub, 0);
                List<Integer> neighbors = instance.getAdjacent(hub);
                if (!neighbors.isEmpty()) {
                    int targetNeighbor = neighbors.get(rnd.nextInt(neighbors.size()));
                    if (!fixedNodes.contains(targetNeighbor)) {
                        solution.addLabel(targetNeighbor, Main.MAX_VALUE);
                        ruined.add(targetNeighbor);
                    }
                }
                count++;
            }
        } else {
            // Fallback if no hub >= 2 exists
            Collections.shuffle(positiveNodes, rnd);
            for (int i = 0; i < Math.min(targetRuin, positiveNodes.size()); i++) {
                int node = positiveNodes.get(i);
                ruined.add(node);
                solution.addLabel(node, 0);
            }
        }

        return ruined;
    }
}
