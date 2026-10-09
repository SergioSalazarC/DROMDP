package proposal.shake.claude;

import grafo.optilib.tools.RandomManager;
import proposal.shake.Shake;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.Random;

/**
 * Adaptive shake for the ILS. It combines several perturbation operators and
 * adapts both:
 *  - the strength: it grows with the number of consecutive iterations in which
 *    the incumbent did not improve (the shake is always called on the incumbent,
 *    so a change of its OF reveals an improvement) and goes back to the minimum
 *    after an improvement;
 *  - the operator: roulette wheel with weights that are rewarded when the
 *    operator was the one used right before an improvement.
 * No change of the ILS is needed; the state is reset automatically when a new
 * instance / execution starts.
 */
public class AdaptiveShake implements Shake {
    private final AbstractShake[] operators;
    private final double[] weights;
    private final double minFraction;
    private final double maxFraction;
    private final int growthPeriod;
    private final double decay;
    private final double reward;
    private final double minWeight;

    private DROMDInstance lastInstance;
    private int lastOF;
    private int lastOperator;
    private int stagnation;

    public AdaptiveShake() {
        this(new AbstractShake[]{new GreedyRepairShake(), new RandomLabelShake(), new BallRuinRecreateShake()},
                0.15, 0.5, 8, 0.9, 1.0, 0.1);
    }

    public AdaptiveShake(AbstractShake[] operators, double minFraction, double maxFraction,
                         int growthPeriod, double decay, double reward, double minWeight) {
        this.operators = operators;
        this.weights = new double[operators.length];
        this.minFraction = minFraction;
        this.maxFraction = maxFraction;
        this.growthPeriod = growthPeriod;
        this.decay = decay;
        this.reward = reward;
        this.minWeight = minWeight;
        reset(null);
    }

    private void reset(DROMDInstance instance) {
        lastInstance = instance;
        lastOF = Integer.MAX_VALUE;
        lastOperator = -1;
        stagnation = 0;
        for (int i = 0; i < weights.length; i++) weights[i] = 1.0;
    }

    @Override
    public DROMDSolution perturb(DROMDSolution solution) {
        DROMDInstance instance = solution.getInstance();
        int of = solution.getOF();
        if (instance != lastInstance || of > lastOF) {
            reset(instance); // new instance or new execution
        } else if (lastOperator >= 0) {
            for (int i = 0; i < weights.length; i++) weights[i] = Math.max(minWeight, weights[i] * decay);
            if (of < lastOF) { // the last shake led to a better incumbent
                weights[lastOperator] += reward;
                stagnation = 0;
            } else {
                stagnation++;
            }
        }
        lastOF = of;

        int op = roulette();
        lastOperator = op;
        // strength grows in steps with stagnation and wraps around so we alternate intensification/diversification
        double level = Math.min(1.0, (stagnation % (4 * growthPeriod)) / (double) (4 * growthPeriod));
        double fraction = minFraction + (maxFraction - minFraction) * level;
        int free = instance.getNumNodes() - instance.fixedNodes().size();
        int k = Math.max(1, Math.min(free, (int) Math.round(fraction * free)));
        return operators[op].perturb(solution, k);
    }

    private int roulette() {
        double total = 0;
        for (double w : weights) total += w;
        Random r = RandomManager.getRandom();
        double x = r.nextDouble() * total;
        for (int i = 0; i < weights.length; i++) {
            x -= weights[i];
            if (x <= 0) return i;
        }
        return weights.length - 1;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
