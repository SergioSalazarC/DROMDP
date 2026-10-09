package proposal.shake.junie;

import grafo.optilib.tools.RandomManager;
import proposal.Main;
import proposal.shake.Shake;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Base class containing shared repair and cleanup routines for Ruin-and-Recreate Shake operators.
 */
public abstract class AbstractRuinRecreateShake implements Shake {

    protected final double minPerturbationRatio;
    protected final double maxPerturbationRatio;

    public AbstractRuinRecreateShake() {
        this(0.08, 0.22);
    }

    public AbstractRuinRecreateShake(double minPerturbationRatio, double maxPerturbationRatio) {
        this.minPerturbationRatio = minPerturbationRatio;
        this.maxPerturbationRatio = maxPerturbationRatio;
    }

    @Override
    public DROMDSolution perturb(DROMDSolution solution) {
        DROMDInstance instance = solution.getInstance();
        int numNodes = instance.getNumNodes();
        if (numNodes <= 2) {
            return solution;
        }

        Random rnd = RandomManager.getRandom();
        Set<Integer> fixedNodes = instance.fixedNodes();

        List<Integer> positiveNodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            if (!fixedNodes.contains(i) && solution.getNodeValue(i) > 0) {
                positiveNodes.add(i);
            }
        }

        if (positiveNodes.isEmpty()) {
            return solution;
        }

        double ratio = minPerturbationRatio + rnd.nextDouble() * (maxPerturbationRatio - minPerturbationRatio);
        int targetRuin = Math.max(1, (int) Math.round(positiveNodes.size() * ratio));
        if (targetRuin < 2 && positiveNodes.size() >= 4) {
            targetRuin = 2;
        }

        Set<Integer> ruined = ruin(solution, instance, positiveNodes, fixedNodes, targetRuin, rnd);

        repair(solution, instance, fixedNodes, rnd);

        for (int node : ruined) {
            solution.selectBestValue(node);
            for (int neighbor : instance.getAdjacent(node)) {
                solution.selectBestValue(neighbor);
            }
        }

        return solution;
    }

    /**
     * Executes the specific ruin strategy on the solution.
     * @return set of nodes that were modified/ruined during this phase.
     */
    protected abstract Set<Integer> ruin(DROMDSolution solution, DROMDInstance instance,
                                         List<Integer> positiveNodes, Set<Integer> fixedNodes,
                                         int targetRuin, Random rnd);

    protected void repair(DROMDSolution solution, DROMDInstance instance, Set<Integer> fixedNodes, Random rnd) {
        int numNodes = instance.getNumNodes();

        List<Integer> infeasible = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            if (!isFeasible(solution, instance, i)) {
                infeasible.add(i);
            }
        }

        while (!infeasible.isEmpty()) {
            int target;
            if (rnd.nextDouble() < 0.7) {
                int minDeg = Integer.MAX_VALUE;
                int bestTarget = infeasible.get(0);
                int sampleSize = Math.min(5, infeasible.size());
                for (int k = 0; k < sampleSize; k++) {
                    int candNode = infeasible.get(rnd.nextInt(infeasible.size()));
                    int deg = instance.getAdjacent(candNode).size();
                    if (deg < minDeg) {
                        minDeg = deg;
                        bestTarget = candNode;
                    }
                }
                target = bestTarget;
            } else {
                target = infeasible.get(rnd.nextInt(infeasible.size()));
            }

            List<Integer> candidates = new ArrayList<>();
            if (!fixedNodes.contains(target)) {
                candidates.add(target);
            }
            for (int neighbor : instance.getAdjacent(target)) {
                if (!fixedNodes.contains(neighbor)) {
                    candidates.add(neighbor);
                }
            }

            if (candidates.isEmpty()) {
                solution.addLabel(target, 2);
                infeasible.remove((Integer) target);
                continue;
            }

            double bestScore = -1.0;
            List<CandidateAction> bestActions = new ArrayList<>();

            for (int cand : candidates) {
                int currentVal = solution.getNodeValue(cand);
                int[] testValues = (currentVal < 2) ? new int[]{3, 2} : new int[]{3};

                for (int val : testValues) {
                    if (val <= currentVal) continue;
                    int cost = val - currentVal;

                    solution.addLabel(cand, val);

                    int newlyFeasible = 0;
                    if (isFeasible(solution, instance, cand)) {
                        newlyFeasible++;
                    }
                    for (int adj : instance.getAdjacent(cand)) {
                        if (isFeasible(solution, instance, adj)) {
                            newlyFeasible++;
                        }
                    }

                    solution.addLabel(cand, currentVal);

                    double score = (double) newlyFeasible / (double) cost;
                    if (score > bestScore + 1e-6) {
                        bestScore = score;
                        bestActions.clear();
                        bestActions.add(new CandidateAction(cand, val));
                    } else if (Math.abs(score - bestScore) <= 1e-6) {
                        bestActions.add(new CandidateAction(cand, val));
                    }
                }
            }

            if (bestActions.isEmpty()) {
                solution.addLabel(target, Main.MAX_VALUE);
            } else {
                CandidateAction chosen = bestActions.get(rnd.nextInt(bestActions.size()));
                solution.addLabel(chosen.node, chosen.value);
            }

            infeasible.removeIf(node -> isFeasible(solution, instance, node));
        }
    }

    protected boolean isFeasible(DROMDSolution solution, DROMDInstance instance, int node) {
        int val = solution.getNodeValue(node);
        if (val >= (Main.MAX_VALUE - 1)) {
            return true;
        }
        int sum = val;
        for (int neighbor : instance.getAdjacent(node)) {
            int nVal = solution.getNodeValue(neighbor);
            if (nVal == Main.MAX_VALUE) {
                return true;
            } else if (nVal == (Main.MAX_VALUE - 1)) {
                sum += nVal;
                if (sum >= Main.MAX_VALUE) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    protected static class CandidateAction {
        final int node;
        final int value;

        CandidateAction(int node, int value) {
            this.node = node;
            this.value = value;
        }
    }
}
