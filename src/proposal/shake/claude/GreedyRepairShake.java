package proposal.shake.claude;

import proposal.Main;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Ruin & Recreate with a different recreate: a connected region is wiped to 0
 * (leaving it infeasible) and rebuilt with a randomized coverage-greedy: while
 * there are uncovered nodes, a random uncovered node is picked and the vertex of
 * its closed neighbourhood that fixes more uncovered nodes (plus random noise)
 * receives the maximum label. A final random re-descent removes redundancies.
 * Unlike raising everything to 3 and descending, this builds the region "from
 * below" and tends to produce few well-placed hubs.
 */
public class GreedyRepairShake extends AbstractShake {
    private final double noise;

    public GreedyRepairShake() {
        this(0.2, 0.5, 1.5);
    }

    public GreedyRepairShake(double minFraction, double maxFraction, double noise) {
        super(minFraction, maxFraction);
        this.noise = noise;
    }

    @Override
    protected void apply(DROMDSolution sol, int k) {
        DROMDInstance instance = sol.getInstance();
        int seed = randomFreeNode(instance);
        if (seed < 0) return;
        List<Integer> region = ball(instance, seed, k);
        for (int u : region) sol.addLabel(u, Main.MIN_VALUE);

        List<Integer> area = closedNeighbourhood(instance, region);
        coverGreedily(sol, area, noise);
        // release redundant labels in random order
        lowerRandomOrder(sol, area);
    }
}
