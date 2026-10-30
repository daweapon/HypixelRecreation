package dev.daweapon.type.skyblockgeneric.island.pad;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;

public final class TeleporterPill {
    public static final String ITEM_ID = "TELEPORTER_PILL";
    private static final String KEY = "teleporter_pill:unlocked";

    private TeleporterPill() {
    }

    public static boolean isUnlocked(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler() != null && DatapointCrimsonIsle.of(player).getNumber(KEY) > 0;
    }

    public static void unlock(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(KEY, 1L));
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ITEM_ID, InteractableItemConfig.builder()
                .rightClickHandler(TeleporterPill::consume).build());
    }

    public static void openMenu(SkyBlockPlayer player) {
        if (!isUnlocked(player)) {
            player.sendMessage("<c>You haven't unlocked the Teleport Pads menu yet! Consume a Teleporter Pill first.");
            return;
        }
        SkyBlockIsland island = ownIslandOf(player);
        if (island == null) {
            player.sendMessage("<c>You can only use the Teleport Pads menu on your own Private Island!");
            return;
        }
        player.openView(new TeleportPadsMenuView(island));
    }

    public static SkyBlockIsland ownIslandOf(SkyBlockPlayer player) {
        if (!HypixelConst.isIslandServer() || player.getInstance() == null) return null;
        SkyBlockIsland island = SkyBlockIsland.forInstance(player.getInstance());
        return island != null && TeleportPads.isMember(player, island) ? island : null;
    }

    private static void consume(SkyBlockPlayer player, SkyBlockItem item) {
        if (isUnlocked(player)) {
            player.sendMessage("<c>You have already unlocked the Teleport Pads menu!");
            return;
        }
        unlock(player);
        TimedConsumables.consumeHeld(player, item);
        player.sendMessage("<a>You unlocked the Teleport Pads menu! Use <e>/teleports <a>on your Private Island to travel between your pads.");
    }
}
