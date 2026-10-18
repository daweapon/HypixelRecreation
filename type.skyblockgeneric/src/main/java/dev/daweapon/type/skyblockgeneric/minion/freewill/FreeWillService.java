package dev.daweapon.type.skyblockgeneric.minion.freewill;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.achievement.SkyBlockItemAchievements;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.MinionFuelComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorage;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class FreeWillService {
    public static final String LOYAL_ATTRIBUTE = "loyal";
    private static final int LINE_DELAY_TICKS = 40;
    private static final Set<UUID> DECIDING_ISLANDS = ConcurrentHashMap.newKeySet();

    private FreeWillService() {
    }

    public static boolean isFreeWillItem(ItemType type) {
        return type == ItemType.FREE_WILL || type == ItemType.FREE_WILL_REFUND;
    }

    public static boolean isLoyal(IslandMinionData.IslandMinion minion) {
        return minion.getExtensionData().getAttribute(LOYAL_ATTRIBUTE) != null;
    }

    public static void begin(SkyBlockPlayer player, IslandMinionData.IslandMinion minion, ItemType used) {
        IslandMinionData minions = player.getSkyBlockIsland().getMinionData();
        if (minions == null || !minions.getMinions().contains(minion)) {
            player.sendMessage("<c>You can only give Free Will to a minion on your own island!");
            return;
        }
        if (isLoyal(minion)) {
            player.sendMessage("<c>This minion is already loyal to you!");
            return;
        }
        UUID island = player.getSkyBlockIsland().getIslandID();
        if (!DECIDING_ISLANDS.add(island)) {
            player.sendMessage("<c>Another minion is already making its choice!");
            return;
        }

        ItemStack held = player.getItemInMainHand();
        player.setItemInMainHand(held.withAmount(held.amount() - 1));

        double chance = used == ItemType.FREE_WILL_REFUND ? 100 : FreeWillOdds.stayChancePercent(minion.getTier());
        boolean stays = LootRoll.roll(null, null, chance);

        String name = minion.getMinion().getDisplay();
        String tool = (minion.getMinion().asSkyBlockMinion().getTiers()
                .get(minion.getTier() - 1).heldItem().key().value().replace('_', ' '));
        List<String> lines = new ArrayList<>(FreeWillDialogue.opening(name, tool));
        lines.addAll(FreeWillDialogue.outcome(stays, name, tool));
        speak(player, minion, island, name, lines, 0, stays);
    }

    private static void speak(SkyBlockPlayer player, IslandMinionData.IslandMinion minion, UUID island,
                              String name, List<String> lines, int index, boolean stays) {
        boolean abandoned = !player.isOnline() || !player.getSkyBlockIsland().getMinionData().getMinions().contains(minion);
        if (abandoned || index >= lines.size()) {
            resolve(player, minion, island, name, stays);
            return;
        }
        player.sendMessage("<7>[<e>{}<7>] <f>{}", name, lines.get(index));
        ScheduleUtility.delay(() -> speak(player, minion, island, name, lines, index + 1, stays), LINE_DELAY_TICKS);
    }

    private static void resolve(SkyBlockPlayer player, IslandMinionData.IslandMinion minion, UUID island,
                                String name, boolean stays) {
        DECIDING_ISLANDS.remove(island);
        IslandMinionData minions = player.getSkyBlockIsland().getMinionData();
        if (minions == null || !minions.getMinions().contains(minion)) return;

        if (stays) {
            minion.getExtensionData().setAttribute(LOYAL_ATTRIBUTE, "1");
            player.sendMessage("<a>Your {} is now loyal to you! <7>(<a>+{}% Minion Speed<7>)",
                    name, (int) FreeWillOdds.LOYALTY_SPEED_PERCENT);
            return;
        }

        returnContents(minion, player::addAndUpdateItem);
        minion.removeMinion();
        minions.getMinions().remove(minion);
        player.addAndUpdateItem(new SkyBlockItem(ItemType.POSTCARD));
        SkyBlockItemAchievements.onObtained(player, ItemType.POSTCARD);
        player.sendMessage("<e>Your {} left for good and sent you a <d>Postcard<e>.", name);
    }

    public static void returnContents(IslandMinionData.IslandMinion minion, Consumer<SkyBlockItem> receiver) {
        minion.getItemsInMinion().forEach(entry -> receiver.accept(entry.toSkyBlockItem()));
        MinionStorage.release(minion, receiver);
        for (SkyBlockItem upgrade : minion.getExtensionData().getMinionUpgrades()) receiver.accept(upgrade);
        SkyBlockItem skin = minion.getExtensionData().getMinionSkin();
        if (skin != null) receiver.accept(skin);
        SkyBlockItem shipping = minion.getExtensionData().getAutomatedShipping();
        if (shipping != null) receiver.accept(shipping);
        SkyBlockItem fuel = minion.getExtensionData().getFuel();
        if (fuel != null && fuel.getComponent(MinionFuelComponent.class).getFuelLastTimeInMS() == 0) receiver.accept(fuel);
        minion.getItemsInMinion().clear();
    }
}
