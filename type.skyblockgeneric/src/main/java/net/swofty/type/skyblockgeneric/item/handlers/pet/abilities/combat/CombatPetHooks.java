package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetData;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Where game systems honour the combat pets' {@code kind: TEXT} abilities, and where they start the
 * timed buffs some {@code kind: HANDLER} abilities show.
 */
public final class CombatPetHooks {
    private static final double CLOSE_COMBAT_RANGE = 1.5;
    private static final long CAMOUFLAGE_MS = 20_000;
    private static final long TELEPORT_SAVVY_MS = 5_000;
    private static final double WEAKENED_DAMAGE = 0.9;
    private static final Set<ItemType> TRANSMISSION_ITEMS = Set.of(ItemType.ASPECT_OF_THE_END, ItemType.ASPECT_OF_THE_VOID);
    private static final Map<UUID, Long> WEAKENED_UNTIL = new ConcurrentHashMap<>();

    private CombatPetHooks() {
    }

    public static double outgoingMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!loaded(player)) return 1;
        double percent = 0;
        if (mob.getMobTypes().contains(MobType.ENDER)) {
            percent += PetAbilities.value(player, ItemType.ENDER_DRAGON_PET, "End Strike", 0);
        }
        if (mob.getMobTypes().contains(MobType.UNDEAD)) {
            percent += PetAbilities.value(player, ItemType.ZOMBIE_PET, "Rotten Blade", 0);
        }
        if (PetAbilities.has(player, ItemType.PIGMAN_PET, "Giant Slayer")) percent += giantSlayer(mob.getLevel());
        if (player.getPosition().distance(mob.getPosition()) <= CLOSE_COMBAT_RANGE) {
            percent += PetAbilities.value(player, ItemType.TYRANNOSAURUS_PET, "Close Combat", 0);
        }
        return 1 + percent / 100D;
    }

    public static double arrowMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!loaded(player)) return 1;
        double boneArrows = PetAbilities.value(player, ItemType.SKELETON_PET, "Bone Arrows", 0);
        return outgoingMultiplier(player, mob) * (1 + boneArrows / 100D);
    }

    public static void bowHit(SkyBlockPlayer player) {
        DatapointPetData.ActiveAbility combo = active(player, ItemType.SKELETON_PET, "Combo");
        if (combo == null) return;
        AbilityRuntime runtime = player.getPetData().getAbilityRuntime(combo.ability());
        int maxStacks = (int) Math.floor(values(combo)[0]);
        long now = System.currentTimeMillis();
        int stacks = now - runtime.getLastProc() > SkeletonComboHandler.STACK_MS ? 0 : runtime.getStacks();
        runtime.setStacks(Math.min(maxStacks, stacks + 1));
        runtime.setLastProc(now);
    }

    public static double mobDamageMultiplier(SkyBlockMob mob) {
        Long until = WEAKENED_UNTIL.get(mob.getUuid());
        if (until == null) return 1;
        if (System.currentTimeMillis() < until) return WEAKENED_DAMAGE;
        WEAKENED_UNTIL.remove(mob.getUuid());
        return 1;
    }

    static void weaken(SkyBlockMob mob, long millis) {
        WEAKENED_UNTIL.put(mob.getUuid(), System.currentTimeMillis() + millis);
    }

    public static void abilityCast(SkyBlockPlayer player, SkyBlockItem item) {
        if (!loaded(player)) return;
        startBuff(player, ItemType.CROW_PET, "Camouflage", CAMOUFLAGE_MS);
        if (TRANSMISSION_ITEMS.contains(item.getAttributeHandler().getPotentialType())) {
            startBuff(player, ItemType.ENDERMAN_PET, "Teleport Savvy", TELEPORT_SAVVY_MS);
        }
    }

    public static boolean savesMana(SkyBlockPlayer player) {
        if (!loaded(player)) return false;
        double chance = PetAbilities.value(player, ItemType.CROW_PET, "Insightful", 0);
        return chance > 0 && ThreadLocalRandom.current().nextDouble(100) < chance;
    }

    public static boolean keepsCoinsOnDeath(SkyBlockPlayer player) {
        return loaded(player) && PetAbilities.has(player, ItemType.PHOENIX_PET, "Eternal Coins");
    }

    public static boolean isKnockbackImmune(SkyBlockPlayer player) {
        return loaded(player) && PetAbilities.has(player, ItemType.TURTLE_PET, "Unflippable");
    }

    public static double vanquisherBonusPercent(SkyBlockPlayer player) {
        return loaded(player) ? PetAbilities.value(player, ItemType.KUUDRA_PET, "Wither Bait", 0) : 0;
    }

    private static double giantSlayer(@Nullable Integer level) {
        if (level == null) return 0;
        if (level >= 100) return 75;
        return level >= 50 ? 50 : 0;
    }

    private static void startBuff(SkyBlockPlayer player, ItemType pet, String ability, long millis) {
        DatapointPetData.ActiveAbility active = active(player, pet, ability);
        if (active == null) return;
        player.getPetData().getAbilityRuntime(active.ability()).setBuffUntil(System.currentTimeMillis() + millis);
    }

    private static double[] values(DatapointPetData.ActiveAbility active) {
        return ((ConfiguredPetAbility) active.ability()).valuesFor(active.pet());
    }

    private static @Nullable DatapointPetData.ActiveAbility active(SkyBlockPlayer player, ItemType pet, String ability) {
        if (!loaded(player)) return null;
        for (DatapointPetData.ActiveAbility active : player.getPetData().getActiveAbilities()) {
            if (active.pet().getAttributeHandler().getPotentialType() != pet) continue;
            if (active.ability() instanceof ConfiguredPetAbility configured && configured.getName().equals(ability)
                    && configured.isActive()) {
                return active;
            }
        }
        return null;
    }

    private static boolean loaded(SkyBlockPlayer player) {
        return player != null && player.getSkyblockDataHandler() != null;
    }
}
