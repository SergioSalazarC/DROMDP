package proposal.structure;

import grafo.optilib.structure.InstanceFactory;

public class DROMDFactory extends InstanceFactory<DROMDInstance> {
    @Override
    public DROMDInstance readInstance(String s) {
        return new DROMDInstance(s);
    }
}
