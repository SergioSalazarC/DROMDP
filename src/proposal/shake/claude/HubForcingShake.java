package proposal.shake.claude;

import proposal.Main;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Forces a few "hubs" (chosen with a degree-biased tournament) to the maximum
 * label, i.e. changes which vertices dominate the graph, and then lets the 2-hop
 * neighbourhood of every hub re-descend in random order: the neighbours (and the
 * labels that were protecting them) are released wherever the new hub covers them.
 * The local search will then have to compensate the extra cost elsewhere, which
 * is exactly the structural change the plain local search is not able to do.
 */
public class HubForcingShake extends AbstractShake {
    private final int tournament;

    public HubForcingShake() {
        this(0.1, 0.3, 3);
    }

    public HubForcingShake(double minFraction, double maxFraction, int tournament) {
        super(minFraction, maxFraction);
        this.tournament = tournament;
    }

    @Override
    protected void apply(DROMDSolution sol, int k) {
        DROMDInstance instance = sol.getInstance();
        if (randomFreeNode(instance) < 0) return;
        Set<Integer> affected = new LinkedHashSet<>();
        for (int i = 0; i < k; i++) {
            int hub = pickHub(sol);
            if (sol.getNodeValue(hub) != Main.MAX_VALUE) {
                sol.addLabel(hub, Main.MAX_VALUE);
            }
            affected.add(hub);
            for (int nb : instance.getAdjacent(hub)) {
                affected.add(nb);
                affected.addAll(instance.getAdjacent(nb));
            }
        }
        // hubs keep their label: only re-descend the others
        List<Integer> toLower = new ArrayList<>(affected);
        lowerRandomOrder(sol, toLower);
    }

    /** Tournament selection by degree among nodes that are not yet at the maximum label. */
    private int pickHub(DROMDSolution sol) {
        DROMDInstance instance = sol.getInstance();
        int best = -1;
        for (int t = 0; t < tournament; t++) {
            int c = randomFreeNode(instance);
            if (best < 0 || instance.getAdjacent(c).size() > instance.getAdjacent(best).size()) best = c;
        }
        return best;
    }
}
