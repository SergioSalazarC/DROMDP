package proposal.shake.claude;

import proposal.Main;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.List;
import java.util.Random;

/**
 * Random-label perturbation: the labels of a connected region are replaced by
 * random ones (distribution biased to 0), the uncovered nodes are repaired with
 * a randomized coverage-greedy and finally everything is re-descended in a random
 * order. It is the most disruptive operator: it forgets the structure of the incumbent
 * inside the region, so it is the one that can leave deep basins of attraction.
 */
public class RandomLabelShake extends AbstractShake {
    private final double[] cumulative;
    private final double noise;

    public RandomLabelShake() {
        this(0.2, 0.5, 1.5, new double[]{0.5, 0.1, 0.2, 0.2});
    }

    public RandomLabelShake(double minFraction, double maxFraction, double noise, double[] probabilities) {
        super(minFraction, maxFraction);
        this.noise = noise;
        this.cumulative = new double[probabilities.length];
        double acc = 0;
        for (int i = 0; i < probabilities.length; i++) {
            acc += probabilities[i];
            cumulative[i] = acc;
        }
    }

    @Override
    protected void apply(DROMDSolution sol, int k) {
        DROMDInstance instance = sol.getInstance();
        int seed = randomFreeNode(instance);
        if (seed < 0) return;
        Random rnd = rnd();
        List<Integer> region = ball(instance, seed, k);
        for (int u : region) {
            double x = rnd.nextDouble() * cumulative[cumulative.length - 1];
            int label = Main.MIN_VALUE;
            while (label < cumulative.length - 1 && x > cumulative[label]) label++;
            sol.addLabel(u, label);
        }
        List<Integer> area = closedNeighbourhood(instance, region);
        coverGreedily(sol, area, noise);
        lowerRandomOrder(sol, area);
    }
}
