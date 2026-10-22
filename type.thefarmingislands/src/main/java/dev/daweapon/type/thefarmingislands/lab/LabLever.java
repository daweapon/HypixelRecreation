package dev.daweapon.type.thefarmingislands.lab;

import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.instance.Instance;

public final class LabLever {
    private static InteractionEntity click;
    private static HologramEntity label;

    private LabLever() {
    }

    public static synchronized void ensure(Instance instance) {
        if (click != null && !click.isRemoved() && click.getInstance() == instance) return;
        click = new InteractionEntity(0.8f, 1f, (player, _) -> LabScenes.pullLever((SkyBlockPlayer) player));
        click.setInstance(instance, LabScenes.LEVER);
        label = new HologramEntity("<e>Stuck Lever");
        label.setInstance(instance, LabScenes.LEVER.add(0, 1.1, 0));
    }
}
