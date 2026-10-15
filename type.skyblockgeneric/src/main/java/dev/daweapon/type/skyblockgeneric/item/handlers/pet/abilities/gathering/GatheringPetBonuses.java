package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import dev.daweapon.commons.skyblock.item.ItemType;
import net.minestom.server.entity.LivingEntity;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobStatusEffects;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import dev.daweapon.type.skyblockgeneric.fishing.winter.WinterSeaCreatureMob;
import dev.daweapon.type.skyblockgeneric.glacite.ColdService;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;
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

    public record SlowStart(double sweep, double foragingFortune) {
        public static final SlowStart NONE = new SlowStart(0, 0);
    }

    public static void seaCreatureSpawned(SkyBlockPlayer angler, SkyBlockMob mob) {
        double missing = PetAbilities.value(angler, ItemType.SPINOSAURUS_PET, "Sharp Attitude", 0);
        if (missing > 0) mob.setHealth((float) (mob.getMaxHealth() * Math.max(0.01, 1 - missing / 100D)));
        if (PetAbilities.has(angler, ItemType.DOLPHIN_PET, "Splash Surprise")) {
            MobStatusEffects.stun(mob, PetPerkMath.SPLASH_SURPRISE_STUN_MS);
        }
        double cold = PetAbilities.value(angler, ItemType.PENGUIN_PET, "Thick Blubber", 0);
        if (cold > 0) ColdService.addCold(angler, cold);
    }

    public static int squidDropAmount(SkyBlockPlayer player, SkyBlockMob mob, int amount) {
        if (!(mob instanceof SeaCreatureMob seaCreature) || !PetPerkMath.doublesSquidDrops(seaCreature.getSeaCreatureId())) {
            return amount;
        }
        double chance = PetAbilities.value(player, ItemType.SQUID_PET, "More Ink", 0);
        return chance > 0 && LootRoll.roll(player.getUuid(), null, chance) ? amount * 2 : amount;
    }

    public static ItemStatistics against(SkyBlockPlayer player, @Nullable LivingEntity enemy) {
        if (!(enemy instanceof WinterSeaCreatureMob)) return ItemStatistics.empty();
        double magicFind = PetAbilities.value(player, ItemType.BABY_YETI_PET, "Frosty Familiarity", 0);
        if (magicFind <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.MAGIC_FIND, magicFind).build();
    }

    public static boolean treeEssenceProcs(SkyBlockPlayer player) {
        double chance = PetAbilities.value(player, ItemType.OCELOT_PET, "Tree Essence", 0);
        return chance > 0 && LootRoll.roll(player.getUuid(), null, chance);
    }

    public static SlowStart consumeSlowStart(SkyBlockPlayer player) {
        AbilityRuntime runtime = PetAbilities.runtime(player, ItemType.SLOTH_PET, "Slow Start");
        long now = System.currentTimeMillis();
        if (runtime == null || !PetPerkMath.offCooldown(runtime.getLastProc(), now, PetPerkMath.SLOW_START_COOLDOWN_MS)) {
            return SlowStart.NONE;
        }
        runtime.setLastProc(now);
        return new SlowStart(PetAbilities.value(player, ItemType.SLOTH_PET, "Slow Start", 0),
                PetAbilities.value(player, ItemType.SLOTH_PET, "Slow Start", 1));
    }

    public static long mithrilPowder(SkyBlockPlayer player, long amount) {
        double bonus = PetAbilities.value(player, ItemType.MITHRIL_GOLEM_PET, "The Smell Of Powder", 0);
        return Math.round(amount * (1 + bonus / 100D));
    }
}
