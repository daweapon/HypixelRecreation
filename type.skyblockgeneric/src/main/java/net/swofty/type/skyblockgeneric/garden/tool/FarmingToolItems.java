package net.swofty.type.skyblockgeneric.garden.tool;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.GardenPlotCleaner;

public final class FarmingToolItems {
    public static final String PLOT_ERASER = "PLOT_ERASER";
    public static final String OVERCLOCKER = "OVERCLOCKER_3000";
    public static final String TOOL_EXP_CAPSULE = "TOOL_EXP_CAPSULE";

    private FarmingToolItems() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(PLOT_ERASER, InteractableItemConfig.builder()
                .rightClickHandler((player, _) -> useEraser(player))
                .build());
    }

    public static void registerAnvilCombines() {
        AnvilCombineRegistry.register(OVERCLOCKER, new AnvilCombineHandler(
                (upgradeItem, _) -> {
                    var handler = upgradeItem.getAttributeHandler();
                    handler.setFarmingToolOverclockers(handler.getFarmingToolOverclockers() + 1);
                    FarmingTools.refresh(upgradeItem);
                },
                (_, upgradeItem, _) -> canOverclock(upgradeItem),
                (_, _, _) -> 0));
        AnvilCombineRegistry.register(TOOL_EXP_CAPSULE, new AnvilCombineHandler(
                (upgradeItem, _) -> {
                    FarmingTools.Tool tool = FarmingTools.of(upgradeItem.getAttributeHandler().getTypeAsString());
                    if (tool != null) FarmingTools.grant(upgradeItem, tool, FarmingTools.CAPSULE_XP, false);
                },
                (_, upgradeItem, _) -> canAbsorbCapsule(upgradeItem),
                (_, _, _) -> 0));
    }

    public static boolean canOverclock(SkyBlockItem item) {
        return FarmingTools.of(item.getAttributeHandler().getTypeAsString()) != null
                && item.getAttributeHandler().getFarmingToolOverclockers() < FarmingTools.MAX_OVERCLOCKERS;
    }

    public static boolean canAbsorbCapsule(SkyBlockItem item) {
        return FarmingTools.of(item.getAttributeHandler().getTypeAsString()) != null
                && item.getAttributeHandler().getFarmingToolXp() < FarmingTools.cumulativeXp(FarmingTools.MAX_LEVEL);
    }

    private static void useEraser(SkyBlockPlayer player) {
        if (!HypixelConst.isGardenServer()) {
            player.sendMessage("<c>You can only use this in The Garden!");
            return;
        }
        int plot = GardenLayout.plotAt(player.getPosition().x(), player.getPosition().z());
        GardenProfile profile = Gardens.profile(player);
        if (plot == GardenLayout.NONE || plot == GardenLayout.BARN || !profile.isPlotUnlocked(plot)) {
            player.sendMessage("<c>You must be standing on one of your unlocked plots to use this!");
            return;
        }
        if (profile.isPlotCleaned(plot)) {
            player.sendMessage("<c>This plot is already clean!");
            return;
        }
        if (!player.removeItemFromPlayer(ItemType.PLOT_ERASER, 1)) return;
        GardenPlotCleaner.eraseWithoutClearing(player, plot);
        player.sendMessage("<a>Your plot has been cleaned, and the <9>Plot Eraser <a>has been consumed.");
        player.sendMessage("<a>Time saved, mess gone!");
    }
}
