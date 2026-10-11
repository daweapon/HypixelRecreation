package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Where game systems honour the {@code kind: TEXT} abilities of the Farming, Alchemy, Enchanting,
 * Taming and Gabagool pets.
 */
public final class MiscPetHooks {
    private static final int WATER_RADIUS = 3;
    private static final int HEAL_SECONDS = 3;

    private MiscPetHooks() {
    }

    public static double manaRegenPercent(SkyBlockPlayer player) {
        if (!loaded(player)) return 0;
        double manaPool = PetAbilities.value(player, ItemType.GUARDIAN_PET, "Mana Pool", 0);
        if (manaPool > 0 && nearWater(player)) manaPool *= 2;
        return manaPool + PetAbilities.value(player, ItemType.SUBZERO_WISP_PET, "Ephemeral Stability", 0);
    }

    public static double manaCostMultiplier(SkyBlockPlayer player) {
        if (!loaded(player)) return 1;
        double saved = PetAbilities.value(player, ItemType.SHEEP_PET, "Mana Saver", 0);
        return Math.max(0, 1 - saved / 100D);
    }

    public static double potionDurationMultiplier(SkyBlockPlayer player) {
        if (!loaded(player)) return 1;
        double repeat = PetAbilities.value(player, ItemType.PARROT_PET, "Repeat", 0);
        return 1 + (repeat + PetAbilities.value(player, ItemType.BINGO_PET, "Consumer", 0)) / 100D;
    }

    public static double godPotionDurationMultiplier(SkyBlockPlayer player) {
        if (!loaded(player)) return 1;
        double best = 0;
        for (SkyBlockItem pet : player.getPetData().getPetsMap().keySet()) {
            if (pet.getAttributeHandler().getPotentialType() != ItemType.PARROT_PET) continue;
            best = Math.max(best, ownValue(pet, "Parrot Feather Infusion"));
        }
        return 1 + (best + PetAbilities.value(player, ItemType.BINGO_PET, "Consumer", 0)) / 100D;
    }

    public static double transmissionManaMultiplier(SkyBlockPlayer player) {
        if (!loaded(player)) return 1;
        return PetPerkMath.manaCostFactor(PetAbilities.value(player, ItemType.ENDERMITE_PET, "Daily Commuter", 0));
    }

    public static int collectionAmount(SkyBlockPlayer player, ItemType item, int amount) {
        if (!loaded(player) || CollectionCategories.getCategory(item) == null) return amount;
        double extra = PetAbilities.value(player, ItemType.BINGO_PET, "Lucky Looting", 0);
        return PetPerkMath.scaledAmount(amount, extra, ThreadLocalRandom.current());
    }

    public static double learningMultiplier(SkyBlockPlayer player) {
        if (!loaded(player)) return 1;
        return 1 + PetAbilities.value(player, ItemType.BINGO_PET, "Fast Learner", 0) / 100D;
    }

    public static double trainingRefundPercent(SkyBlockPlayer player, double coinCost) {
        if (!loaded(player) || !PetAbilities.has(player, ItemType.OWL_PET, "Training Refunds")) return 0;
        return PetPerkMath.trainingRefundPercent(coinCost);
    }

    public static double slugfishBobberPercent(SkyBlockPlayer player) {
        if (!loaded(player)) return 0;
        return PetAbilities.value(player, ItemType.SLUG_PET, "Slow and Steady", 0);
    }

    public static double chocolateMultiplierBonus(SkyBlockPlayer player) {
        if (!loaded(player)) return 0;
        return PetAbilities.value(player, ItemType.RABBIT_PET, "Chocolate Injections", 0);
    }

    public static void petTick(SkyBlockPlayer player) {
        if (!loaded(player) || player.getInstance() == null || player.isDead()) return;
        long now = System.currentTimeMillis();
        OverhealShield.tick(player, now);
        ExtinguishPools.tick(player, now);
    }

    public static void release(UUID player) {
        OverhealShield.release(player);
        ExtinguishPools.release(player);
        CombatState.release(player);
    }

    public static int intimidationBonusLevels(SkyBlockPlayer player) {
        if (!loaded(player)) return 0;
        return (int) PetAbilities.value(player, ItemType.PARROT_PET, "Flamboyant", 0);
    }

    public static double trainingXpMultiplier(SkyBlockItem trainedPet) {
        if (trainedPet.getAttributeHandler().getPotentialType() != ItemType.OWL_PET) return 1;
        return 1 + ownValue(trainedPet, "Efficient Trainer") / 100D;
    }

    public static boolean breaksFirePillarsFaster(SkyBlockPlayer player) {
        return firePillarHealPercent(player) > 0;
    }

    public static void destroyedFirePillar(SkyBlockPlayer player) {
        double percent = firePillarHealPercent(player);
        if (percent <= 0) return;
        float perSecond = (float) (player.getMaxHealth() * percent / 100D / HEAL_SECONDS);
        for (int second = 1; second <= HEAL_SECONDS; second++) {
            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                if (player.isOnline()) player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + perSecond));
            }, TaskSchedule.seconds(second), TaskSchedule.stop());
        }
    }

    private static double firePillarHealPercent(SkyBlockPlayer player) {
        if (!loaded(player)) return 0;
        double drophammer = PetAbilities.value(player, ItemType.DROPLET_WISP_PET, "Drophammer", 0);
        double icehammer = Math.max(PetAbilities.value(player, ItemType.FROST_WISP_PET, "Icehammer", 0),
                Math.max(PetAbilities.value(player, ItemType.GLACIAL_WISP_PET, "Icehammer", 0),
                        PetAbilities.value(player, ItemType.SUBZERO_WISP_PET, "Icehammer", 0)));
        return Math.max(drophammer, icehammer);
    }

    private static double ownValue(SkyBlockItem pet, String ability) {
        if (!pet.hasComponent(PetComponent.class)) return 0;
        for (PetAbility unlocked : pet.getComponent(PetComponent.class).abilitiesFor(pet)) {
            if (unlocked instanceof ConfiguredPetAbility configured && configured.getName().equals(ability)) {
                double[] values = configured.valuesFor(pet);
                return values.length == 0 ? 0 : values[0];
            }
        }
        return 0;
    }

    private static boolean nearWater(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return false;
        Pos position = player.getPosition();
        for (int x = -WATER_RADIUS; x <= WATER_RADIUS; x++) {
            for (int y = -WATER_RADIUS; y <= WATER_RADIUS; y++) {
                for (int z = -WATER_RADIUS; z <= WATER_RADIUS; z++) {
                    Pos at = position.add(x, y, z);
                    if (instance.isChunkLoaded(at) && instance.getBlock(at).compare(Block.WATER)) return true;
                }
            }
        }
        return false;
    }

    private static boolean loaded(SkyBlockPlayer player) {
        return player != null && player.getSkyblockDataHandler() != null;
    }
}
