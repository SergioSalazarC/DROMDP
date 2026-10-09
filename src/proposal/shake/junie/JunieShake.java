package proposal.shake.junie;

import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Adaptive Multi-Neighborhood Shake (LLMShake):
 * Dynamically selects among 5 specialized ruin strategies on each perturbation call:
 * 1. Spatial / Cluster BFS Ruin (SpatialClusterShake)
 * 2. Hub Relocation / Shift Ruin (HubRelocationShake)
 * 3. Degree-Biased Ruin (DegreeBiasedRuinShake)
 * 4. Label Downgrade Ruin (LabelDowngradeShake)
 * 5. Uniform Random Ruin (UniformRandomRuinShake)
 */
public class JunieShake extends AbstractRuinRecreateShake {

    private final List<AbstractRuinRecreateShake> subShakes = new ArrayList<>();

    public JunieShake() {
        super();
        initSubShakes();
    }

    public JunieShake(double minPerturbationRatio, double maxPerturbationRatio) {
        super(minPerturbationRatio, maxPerturbationRatio);
        initSubShakes();
    }

    private void initSubShakes() {
        subShakes.add(new SpatialClusterShake(minPerturbationRatio, maxPerturbationRatio));
        subShakes.add(new HubRelocationShake(minPerturbationRatio, maxPerturbationRatio));
        subShakes.add(new DegreeBiasedRuinShake(minPerturbationRatio, maxPerturbationRatio));
        subShakes.add(new LabelDowngradeShake(minPerturbationRatio, maxPerturbationRatio));
        subShakes.add(new UniformRandomRuinShake(minPerturbationRatio, maxPerturbationRatio));
    }

    @Override
    protected Set<Integer> ruin(DROMDSolution solution, DROMDInstance instance,
                                List<Integer> positiveNodes, Set<Integer> fixedNodes,
                                int targetRuin, Random rnd) {
        int selected = rnd.nextInt(subShakes.size());
        return subShakes.get(selected).ruin(solution, instance, positiveNodes, fixedNodes, targetRuin, rnd);
    }
}
