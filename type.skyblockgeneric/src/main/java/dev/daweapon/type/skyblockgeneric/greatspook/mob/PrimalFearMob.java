package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PrimalFearMob extends PrimalMob {
    private static final int RED = 0xAA0000;

    public PrimalFearMob(UUID owner, String ownerName) {
        super(PrimalProfiles.of("PRIMAL_FEAR_100", "Primal Fear"), owner, ownerName);
    }

    @Override
    protected Map<EquipmentSlot, ItemStack> appearance() {
        return Map.of(EquipmentSlot.HELMET, leather(Material.LEATHER_HELMET, RED),
                EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, RED),
                EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, RED),
                EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, RED));
    }

    @Override
    protected void afterRewards(SkyBlockPlayer killer, Instance instance, Pos position) {
        PrimalFearProgress.recordKill(killer);
        Manifestation next = Manifestation.ALL.get(ThreadLocalRandom.current().nextInt(Manifestation.ALL.size()));
        killer.sendMessage(Text.parseLenient("<c>The Primal Fear has turned into a " + next.displayName() + "!"));
        PrimalManifestations.spawn(next, instance, position, killer);
    }
}
