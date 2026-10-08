package proposal.shake;

import proposal.structure.DROMDSolution;

public class NonShake implements Shake{
    @Override
    public DROMDSolution perturb(DROMDSolution solution) {
        return new DROMDSolution(solution);
    }
}
