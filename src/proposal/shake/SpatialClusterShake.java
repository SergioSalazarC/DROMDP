package proposal.shake;

import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Spatial / Cluster BFS Ruin:
 * Selects a seed dominator node and expands a BFS traversal to ruin labels in a localized cluster.
 */
public class SpatialClusterShake extends AbstractRuinRecreateShake {

    public SpatialClusterShake() {
        super();
    }

    public SpatialClusterShake(double minPerturbationRatio, double maxPerturbationRatio) {
        super(minPerturbationRatio, maxPerturbationRatio);
    }

    @Override
    protected Set<Integer> ruin(DROMDSolution solution, DROMDInstance instance,
                                List<Integer> positiveNodes, Set<Integer> fixedNodes,
                                int targetRuin, Random rnd) {
        Set<Integer> ruined = new HashSet<>();
        int seed = positiveNodes.get(rnd.nextInt(positiveNodes.size()));
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(seed);
        Set<Integer> visited = new HashSet<>();
        visited.add(seed);

        while (!queue.isEmpty() && ruined.size() < targetRuin) {
            int curr = queue.poll();
            if (!fixedNodes.contains(curr) && solution.getNodeValue(curr) > 0) {
                ruined.add(curr);
                solution.addLabel(curr, 0);
            }
            List<Integer> adj = instance.getAdjacent(curr);
            for (int neighbor : adj) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return ruined;
    }
}
