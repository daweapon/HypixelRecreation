package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;

// Blocks specific players from /visit-ing the player's island, regardless of its privacy tier -
// referenced (as unimplemented lore) by GUIGuests' "Access Permissions" button.
@CommandParameters(labels = "ignore",
        description = "Blocks a player from visiting your island",
        usage = "/ignore <add|remove|list> [player]",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class IgnoreCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord subcommandArg = ArgumentType.Word("subcommand");
        ArgumentWord playerArg = ArgumentType.Word("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!context.get(subcommandArg).equalsIgnoreCase("list")) {
                player.sendMessage("<c>Usage: {}", "/ignore <add|remove|list> [player]");
                return;
            }
            list(player);
        }, subcommandArg);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String sub = context.get(subcommandArg);
            String targetName = context.get(playerArg);

            UUID targetUuid;
            try {
                targetUuid = ProfilesDatabase.fetchUUID(targetName);
            } catch (Exception e) {
                player.sendMessage("<c>That player could not be found!");
                return;
            }

            switch (sub.toLowerCase()) {
                case "add" -> add(player, targetUuid, targetName);
                case "remove" -> remove(player, targetUuid, targetName);
                default -> player.sendMessage("<c>Usage: {}", "/ignore <add|remove|list> [player]");
            }
        }, subcommandArg, playerArg);
    }

    private void add(SkyBlockPlayer player, UUID targetUuid, String targetName) {
        if (targetUuid.equals(player.getUuid())) {
            player.sendMessage("<c>You can't ignore yourself!");
            return;
        }

        SkyBlockDataHandler handler = player.getSkyblockDataHandler();
        DatapointStringList ignored = handler.get(SkyBlockDataHandler.Data.IGNORED_VISITORS, DatapointStringList.class);
        List<String> value = ignored.getValue();
        if (value.contains(targetUuid.toString())) {
            player.sendMessage("<c>{} is already ignored!", targetName);
            return;
        }

        value.add(targetUuid.toString());
        ignored.setValue(value);
        handler.saveToApi();
        player.sendMessage("<e>{} can no longer visit your island.", targetName);
    }

    private void remove(SkyBlockPlayer player, UUID targetUuid, String targetName) {
        SkyBlockDataHandler handler = player.getSkyblockDataHandler();
        DatapointStringList ignored = handler.get(SkyBlockDataHandler.Data.IGNORED_VISITORS, DatapointStringList.class);
        List<String> value = ignored.getValue();
        if (!value.remove(targetUuid.toString())) {
            player.sendMessage("<c>{} isn't ignored!", targetName);
            return;
        }

        ignored.setValue(value);
        handler.saveToApi();
        player.sendMessage("<e>{} can now visit your island again.", targetName);
    }

    private void list(SkyBlockPlayer player) {
        List<String> value = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.IGNORED_VISITORS, DatapointStringList.class).getValue();

        if (value.isEmpty()) {
            player.sendMessage("<e>You haven't ignored anyone.");
            return;
        }
        player.sendMessage("<e>Ignored players ({}): <f>{}", value.size(), String.join(", ", value));
    }
}
