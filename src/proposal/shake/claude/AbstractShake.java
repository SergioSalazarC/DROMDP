package proposal.shake.claude;

import grafo.optilib.tools.RandomManager;
import proposal.Main;
import proposal.shake.Shake;
import proposal.structure.DROMDInstance;
import proposal.structure.DROMDSolution;

import java.util.*;

/**
 * Base class for the shakes. It provides the common tools: region selection
 * (BFS balls), local feasibility checks, randomized re-construction and a safety
 * net that guarantees that the returned solution is always feasible
 * (the local search assumes a feasible starting point).
 *
 * Subclasses only implement {@link #apply(DROMDSolution, int)}, which perturbs a
 * region of (about) k nodes. The strength k is drawn in [minFraction, maxFraction]*n.
 */
public abstract class AbstractShake implements Shake {
    protected final double minFraction;
    protected final double maxFraction;

    protected AbstractShake(double minFraction, double maxFraction) {
        this.minFraction = minFraction;
        this.maxFraction = maxFraction;
    }

    /** Perturbs the solution in place affecting around k nodes. Must keep feasibility locally. */
    protected abstract void apply(DROMDSolution sol, int k);

    @Override
    public DROMDSolution perturb(DROMDSolution solution) {
        DROMDInstance instance = solution.getInstance();
        double f = minFraction + (maxFraction - minFraction) * RandomManager.getRandom().nextDouble();
        return perturb(solution, strength(instance, f));
    }

    /** Perturbation with explicit strength (used by adaptive wrappers). */
    public DROMDSolution perturb(DROMDSolution solution, int k) {
        DROMDSolution backup = new DROMDSolution(solution);
        apply(solution, k);
        return ensureFeasible(solution, backup);
    }

    // ------------------------------------------------------------------ helpers

    protected static int strength(DROMDInstance instance, double fraction) {
        int n = instance.getNumNodes() - instance.fixedNodes().size();
        return Math.max(1, Math.min(n, (int) Math.round(fraction * n)));
    }

    protected static Random rnd() {
        return RandomManager.getRandom();
    }

    /** Random node that is not fixed (isolated). Returns -1 if none exists. */
    protected static int randomFreeNode(DROMDInstance instance) {
        int n = instance.getNumNodes();
        if (instance.fixedNodes().size() >= n) return -1;
        int node;
        do {
            node = rnd().nextInt(n);
        } while (instance.fixedNodes().contains(node));
        return node;
    }

    /**
     * Connected region of (up to) k free nodes grown by a randomized BFS from the seed.
     * If the component is exhausted, the growth continues from another random node.
     */
    protected static List<Integer> ball(DROMDInstance instance, int seed, int k) {
        int n = instance.getNumNodes();
        boolean[] visited = new boolean[n];
        List<Integer> region = new ArrayList<>(k);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int free = n - instance.fixedNodes().size();
        k = Math.min(k, free);
        int start = seed;
        while (region.size() < k) {
            if (!visited[start]) {
                visited[start] = true;
                queue.add(start);
            }
            while (!queue.isEmpty() && region.size() < k) {
                int u = queue.poll();
                if (!instance.fixedNodes().contains(u)) region.add(u);
                List<Integer> adj = new ArrayList<>(instance.getAdjacent(u));
                Collections.shuffle(adj, rnd());
                for (int v : adj) {
                    if (!visited[v]) {
                        visited[v] = true;
                        queue.add(v);
                    }
                }
            }
            if (region.size() < k) {
                start = randomFreeNode(instance);
                int tries = 0;
                while (visited[start] && tries++ < 10 * n) start = randomFreeNode(instance);
                if (visited[start]) break;
            }
        }
        return region;
    }

    /** Region plus its direct neighbours (without duplicates). */
    protected static List<Integer> closedNeighbourhood(DROMDInstance instance, Collection<Integer> region) {
        LinkedHashSet<Integer> set = new LinkedHashSet<>(region);
        for (int u : region) set.addAll(instance.getAdjacent(u));
        return new ArrayList<>(set);
    }

    /** Node-level feasibility (same rule used by DROMDSolution). */
    protected static boolean nodeOk(DROMDSolution sol, int node) {
        int own = sol.getNodeValue(node);
        if (own >= Main.MAX_VALUE - 1) return true;
        int sum = own;
        for (int nb : sol.getInstance().getAdjacent(node)) {
            int v = sol.getNodeValue(nb);
            if (v == Main.MAX_VALUE) return true;
            if (v == Main.MAX_VALUE - 1) {
                sum += v;
                if (sum >= Main.MAX_VALUE) return true;
            }
        }
        return false;
    }

    /** Set the nodes to the maximum label (always locally feasible). */
    protected static void raiseToMax(DROMDSolution sol, Collection<Integer> nodes) {
        for (int u : nodes) sol.addLabel(u, Main.MAX_VALUE);
    }

    /** Randomized greedy re-descent: each node takes the smallest locally-feasible label. */
    protected static void lowerRandomOrder(DROMDSolution sol, List<Integer> nodes) {
        List<Integer> order = new ArrayList<>(nodes);
        Collections.shuffle(order, rnd());
        for (int u : order) sol.selectBestValue(u);
    }

    /**
     * Randomized coverage-greedy: while there are uncovered nodes in the area, a random uncovered
     * node is picked and the vertex of its closed neighbourhood that fixes more uncovered nodes
     * (plus random noise) receives the maximum label.
     */
    protected static void coverGreedily(DROMDSolution sol, List<Integer> area, double noise) {
        DROMDInstance instance = sol.getInstance();
        Random rnd = rnd();
        Set<Integer> uncovered = new LinkedHashSet<>();
        for (int u : area) if (!nodeOk(sol, u)) uncovered.add(u);
        while (!uncovered.isEmpty()) {
            List<Integer> list = new ArrayList<>(uncovered);
            int target = list.get(rnd.nextInt(list.size()));
            int bestNode = target;
            double bestScore = -1;
            List<Integer> cands = new ArrayList<>(instance.getAdjacent(target));
            cands.add(target);
            for (int c : cands) {
                int gain = uncovered.contains(c) ? 1 : 0;
                for (int nb : instance.getAdjacent(c)) if (uncovered.contains(nb)) gain++;
                double score = gain + noise * rnd.nextDouble();
                if (score > bestScore) {
                    bestScore = score;
                    bestNode = c;
                }
            }
            sol.addLabel(bestNode, Main.MAX_VALUE);
            uncovered.remove(bestNode);
            for (int nb : instance.getAdjacent(bestNode)) uncovered.remove(nb);
        }
    }

    /** Safety net: if something went wrong the solution is repaired setting
     * to the maximum label every node that is not covered; if it is still not feasible
     * the backup is returned.
     */
    protected DROMDSolution ensureFeasible(DROMDSolution sol, DROMDSolution backup) {
        if (sol.isFeasibleAll()) return sol;
        int n = sol.getInstance().getNumNodes();
        for (int i = 0; i < n; i++) {
            if (!nodeOk(sol, i)) sol.addLabel(i, Main.MAX_VALUE);
        }
        return sol.isFeasibleAll() ? sol : backup;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
