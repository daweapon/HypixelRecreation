package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobGoblin;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mining pet abilities that game systems honour by asking for their value ({@code kind: TEXT}).
 */
public final class MiningPetBonuses {
    private static final Set<ItemType> RARE_GOBLIN_EGGS = Set.of(ItemType.GREEN_GOBLIN_EGG, ItemType.YELLOW_GOBLIN_EGG,
            ItemType.RED_GOBLIN_EGG, ItemType.BLUE_GOBLIN_EGG);

    private MiningPetBonuses() {
    }

    public static long powder(SkyBlockPlayer player, HotmPowder powder, long amount) {
        double bonus = switch (powder) {
            case GLACITE -> PetAbilities.value(player, ItemType.GLACITE_GOLEM_PET, "Powder-powered", 0);
            case GEMSTONE -> PetAbilities.value(player, ItemType.SCATHA_PET, "Bejeweled Eyes", 0);
            default -> 0;
        };
        return PetPerkMath.scaledAmount(amount, bonus, ThreadLocalRandom.current());
    }

    public static double miningSkillExperience(SkyBlockPlayer player, double experience) {
        return PetPerkMath.boosted(experience, PetAbilities.value(player, ItemType.SILVERFISH_PET, "Magnetic", 0));
    }

    public static boolean rollingMinerReady(SkyBlockPlayer player, long now) {
        AbilityRuntime runtime = PetAbilities.runtime(player, ItemType.ARMADILLO_PET, "Rolling Miner");
        if (runtime == null) return false;
        long cooldownMs = (long) (PetAbilities.value(player, ItemType.ARMADILLO_PET, "Rolling Miner", 0) * 1000);
        if (!PetPerkMath.offCooldown(runtime.getLastProc(), now, cooldownMs)) return false;
        runtime.setLastProc(now);
        return true;
    }

    public static double goblinEggChance(SkyBlockPlayer player, SkyBlockMob mob, ItemType item, double chancePercent) {
        if (!(mob instanceof MobGoblin) || !RARE_GOBLIN_EGGS.contains(item)) return chancePercent;
        return PetPerkMath.boostChance(chancePercent, PetAbilities.value(player, ItemType.GOBLIN_PET, "Free-range Eggs", 0));
    }
}
