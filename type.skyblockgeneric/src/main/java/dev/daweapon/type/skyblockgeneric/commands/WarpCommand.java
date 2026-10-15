package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.text.Text;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.warps.ScrollWarp;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollIslands;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollType;

import java.util.List;

@CommandParameters(description = "Warps to the given destination",
        usage = "/warp <warp>",
        permission = Rank.DEFAULT,
    labels = "teleportregion warp",
        allowsConsole = false)
public class WarpCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString warpArgument = ArgumentType.String("warp");
        warpArgument.setSuggestionCallback(((sender, context, suggestion) -> {
            for (TravelScrollIslands island : TravelScrollIslands.values()) {
                suggest(suggestion, island.getInternalName(), island.getDescriptiveName());
                for (TravelScrollType scroll : island.getAssociatedScrolls()) {
                    suggest(suggestion, scroll.getInternalName(), scroll.getDisplayName());
                }
            }
        }));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            final String warp = TravelScrollIslands.resolveAlias(context.get(warpArgument));
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            TravelScrollIslands island = TravelScrollIslands.getFromInternalName(warp);
            if (island != null) {
                if (!island.isAvailable()) {
                    player.sendMessage(TravelScrollIslands.NOT_AVAILABLE_MESSAGE);
                    return;
                }
                Text blocked = island.entryBlockReason(player);
                if (blocked != null) {
                    player.sendMessage(blocked);
                    return;
                }
                if (island.isUnlocked(player)) {
                    player.asProxyPlayer().transferToWithIndication(island.getServerType()).thenRun(() -> {
                        player.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", island.getDescriptiveName());
                    });
                } else {
                    player.sendMessage("<c>You have not unlocked this warp.");
                }
                return;
            }

            TravelScrollType scroll = TravelScrollType.getFromInternalName(warp);
            if (scroll != null) {
                List<String> unlockedWarps = player.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.USED_SCROLLS, DatapointStringList.class)
                        .getValue();
                TravelScrollIslands islandFromScroll = TravelScrollIslands.getFromTravelScroll(scroll);
                if (islandFromScroll == null || !islandFromScroll.isAvailable()) {
                    player.sendMessage(TravelScrollIslands.NOT_AVAILABLE_MESSAGE);
                    return;
                }
                if (unlockedWarps.contains(warp)) {
                    Text blocked = islandFromScroll.entryBlockReason(player);
                    if (blocked != null) {
                        player.sendMessage(blocked);
                        return;
                    }
                    ScrollWarp.travel(player, scroll, islandFromScroll.getServerType());
                } else {
                    player.sendMessage("<c>You have not unlocked this warp.");
                }
                return;
            }

            player.sendMessage("<c>Could not find a warp with the name '{}'.", warp);
        }, warpArgument);
    }
}
