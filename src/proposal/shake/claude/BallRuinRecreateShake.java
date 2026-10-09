package proposal.shake.claude;

import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.List;

/**
 * Ruin & Recreate over a connected region: a BFS ball around a random node is
 * reset to the maximum label (ruin) and then re-descended in a random order
 * (recreate). The random order makes the greedy re-descent land on a different
 * local structure each time, while the rest of the solution is untouched.
 * Neighbours of the region are also re-descended so labels that have become
 * redundant at the border are released.
 */
public class BallRuinRecreateShake extends AbstractShake {
    public BallRuinRecreateShake() {
        this(0.2, 0.5);
    }

    public BallRuinRecreateShake(double minFraction, double maxFraction) {
        super(minFraction, maxFraction);
    }

    @Override
    protected void apply(DROMDSolution sol, int k) {
        DROMDInstance instance = sol.getInstance();
        int seed = randomFreeNode(instance);
        if (seed < 0) return;
        List<Integer> region = ball(instance, seed, k);
        raiseToMax(sol, region);
        List<Integer> toLower = closedNeighbourhood(instance, region);
        lowerRandomOrder(sol, toLower);
    }
}
