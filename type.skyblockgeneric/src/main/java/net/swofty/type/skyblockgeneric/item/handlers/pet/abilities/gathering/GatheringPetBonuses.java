package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;
import org.jetbrains.annotations.Nullable;

/**
 * Gathering pet abilities that game systems honour by asking for their value ({@code kind: TEXT}).
 */
public final class GatheringPetBonuses {
    private GatheringPetBonuses() {
    }

    public static double miningSpeed(SkyBlockPlayer player, @Nullable MineableBlock block) {
        if (block == null) return 0;
        double bonus = 0;
        if (block.getBlockType().specificBlockFortune() == ItemStatistic.ORE_FORTUNE) {
            bonus += PetAbilities.value(player, ItemType.GOBLIN_PET, "Grunt Work", 0);
        }
        if (block.name().startsWith("MITHRIL")) {
            bonus += PetAbilities.value(player, ItemType.MITHRIL_GOLEM_PET, "Mithril Affinity", 0);
        }
        return bonus;
    }

    public static void seaCreatureSpawned(SkyBlockPlayer angler, SkyBlockMob mob) {
        double missing = PetAbilities.value(angler, ItemType.SPINOSAURUS_PET, "Sharp Attitude", 0);
        if (missing <= 0) return;
        mob.setHealth((float) (mob.getMaxHealth() * Math.max(0.01, 1 - missing / 100D)));
    }

    public static long mithrilPowder(SkyBlockPlayer player, long amount) {
        double bonus = PetAbilities.value(player, ItemType.MITHRIL_GOLEM_PET, "The Smell Of Powder", 0);
        return Math.round(amount * (1 + bonus / 100D));
    }
}
