package dev.daweapon.type.kuudra.fight.world;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;

import java.util.List;

/**
 * The Infernal platform centre falling into Kuudra's lair, replayed from the block edits in the Infernal capture
 * ({@code lair_collapse.yml}): first the hidden layer under the floor, then the floor itself.
 */
public final class LairCollapse {
    private LairCollapse() {
    }

    /** Clears one captured stage; returns how many blocks fell. */
    public static int collapse(Instance instance, KuudraArena.Lair lair, int stage) {
        List<List<KuudraArena.Run>> stages = lair.collapse();
        if (stage < 0 || stage >= stages.size()) return 0;
        int cleared = 0;
        for (KuudraArena.Run run : stages.get(stage)) {
            for (int x = run.xFrom(); x <= run.xTo(); x++) {
                instance.setBlock(x, run.y(), run.z(), Block.AIR);
                cleared++;
            }
        }
        return cleared;
    }

    public static int stages(KuudraArena.Lair lair) {
        return lair.collapse().size();
    }
}
