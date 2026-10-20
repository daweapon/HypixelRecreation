package dev.daweapon.type.skyblockgeneric.romero;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryBag;
import dev.daweapon.type.skyblockgeneric.crimson.town.RomeroCrimsonStep;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointRomero;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public final class RomeroJulietteQuest {
    public static final int COMBAT_REQUIREMENT = 22;
    public static final double RESTORE_COST = 100_000;
    private static final String ACHIEVEMENT = "skyblock.friar_lawrence";

    private RomeroJulietteQuest() {
    }

    public static void registerCrimsonStep() {
        RomeroCrimsonStep.register(player -> isActive(player, RomeroStep.STEW), player -> deliver(player, RomeroStep.STEW));
    }

    public static DatapointRomero.RomeroData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ROMERO_QUEST, DatapointRomero.class).getValue();
    }

    private static void save(SkyBlockPlayer player, DatapointRomero.RomeroData data) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ROMERO_QUEST, DatapointRomero.class).setValue(data);
    }

    public static int step(SkyBlockPlayer player) {
        return data(player).getStep();
    }

    public static boolean isStarted(SkyBlockPlayer player) {
        return step(player) >= RomeroStep.FIRST;
    }

    public static boolean isCompleted(SkyBlockPlayer player) {
        return step(player) >= RomeroStep.COMPLETED;
    }

    public static boolean isActive(SkyBlockPlayer player, RomeroStep step) {
        return step(player) == step.getNumber();
    }

    public static boolean isDelivered(SkyBlockPlayer player) {
        return data(player).isDelivered();
    }

    public static RomeroStep currentStep(SkyBlockPlayer player) {
        return RomeroStep.of(step(player));
    }

    public static boolean meetsCombatRequirement(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= COMBAT_REQUIREMENT;
    }

    public static void start(SkyBlockPlayer player) {
        DatapointRomero.RomeroData data = data(player);
        if (data.getStep() >= RomeroStep.FIRST) return;
        data.setStep(RomeroStep.FIRST);
        data.setDelivered(false);
        save(player, data);
    }

    public static void deliver(SkyBlockPlayer player, RomeroStep step) {
        DatapointRomero.RomeroData data = data(player);
        if (data.getStep() != step.getNumber() || data.isDelivered()) return;
        data.setDelivered(true);
        save(player, data);
        player.addAndUpdateItem(step.questItem());
    }

    public static boolean holdsQuestItem(SkyBlockPlayer player) {
        RomeroStep step = currentStep(player);
        return step != null && step.questItem().equals(RomeroInteraction.heldType(player));
    }

    public static boolean restoreItem(SkyBlockPlayer player) {
        RomeroStep step = currentStep(player);
        if (step == null || !isDelivered(player)) return false;
        if (player.countItem(step.questItem()) > 0) {
            player.sendMessage("<c>You already have the item Romero gave you!");
            return false;
        }
        if (player.getCoins() < RESTORE_COST) {
            player.sendMessage("<c>You need {} coins to cover Romero's expenses!", String.format("%,d", (long) RESTORE_COST));
            return false;
        }
        player.removeCoins(RESTORE_COST);
        player.addAndUpdateItem(step.questItem());
        return true;
    }

    public static void completeStep(SkyBlockPlayer player) {
        DatapointRomero.RomeroData data = data(player);
        RomeroStep step = RomeroStep.of(data.getStep());
        if (step == null || !data.isDelivered()) return;
        player.removeItemFromPlayer(step.questItem(), 1);
        data.setStep(step.getNumber() + 1);
        data.setDelivered(false);
        save(player, data);
        upgradeRing(player, step.getNumber() - 1);
        if (step == RomeroStep.WEDDING) player.getAchievementHandler().completeAchievement(ACHIEVEMENT);
    }

    public static void upgradeRing(SkyBlockPlayer player, int ringIndex) {
        RomeroRing next = RomeroRing.values()[ringIndex];
        if (ringIndex == 0) {
            player.addAndUpdateItem(next.itemType());
            return;
        }
        RomeroRing previous = RomeroRing.values()[ringIndex - 1];
        if (!replaceInBag(player, previous.itemType(), next.itemType())) {
            if (player.countItem(previous.itemType()) > 0) player.removeItemFromPlayer(previous.itemType(), 1);
            player.addAndUpdateItem(next.itemType());
        }
        player.sendMessage(previous.upgradeMessage(next));
    }

    private static boolean ownsInBag(SkyBlockPlayer player, ItemType type) {
        return player.getAccessoryBag().getAllAccessories().stream()
                .anyMatch(item -> type.equals(item.getAttributeHandler().getPotentialType()));
    }

    private static boolean replaceInBag(SkyBlockPlayer player, ItemType previous, ItemType next) {
        DatapointAccessoryBag.PlayerAccessoryBag bag = player.getAccessoryBag();
        for (Map.Entry<Integer, SkyBlockItem> entry : Map.copyOf(bag.getAccessoryMap()).entrySet()) {
            if (!previous.equals(entry.getValue().getAttributeHandler().getPotentialType())) continue;
            bag.setInSlot(entry.getKey(), new SkyBlockItem(next));
            AccessoryBag.save(player, bag);
            return true;
        }
        return false;
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointRomero.RomeroData data = data(player);
        data.setStep(RomeroStep.COMPLETED);
        data.setDelivered(false);
        save(player, data);
        ItemType legendary = RomeroRing.LEGENDARY.itemType();
        for (RomeroRing ring : RomeroRing.values()) {
            if (ring != RomeroRing.LEGENDARY && player.countItem(ring.itemType()) > 0) player.removeItemFromPlayer(ring.itemType(), player.countItem(ring.itemType()));
        }
        if (player.countItem(legendary) == 0 && !ownsInBag(player, legendary)) player.addAndUpdateItem(legendary);
        player.getAchievementHandler().completeAchievement(ACHIEVEMENT);
    }
}
