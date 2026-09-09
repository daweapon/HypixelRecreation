package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.IslandPrivacy;

// Sets who can /visit the player's current island: PUBLIC (anyone), FRIENDS_ONLY, or PRIVATE.
@CommandParameters(labels = "islandprivacy",
        description = "Sets who can visit your island",
        usage = "/islandprivacy <public|friends|private>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class IslandPrivacyCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord tierArg = ArgumentType.Word("tier");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String requested = context.get(tierArg);

            IslandPrivacy tier = switch (requested.toLowerCase()) {
                case "public" -> IslandPrivacy.PUBLIC;
                case "friends", "friendsonly", "friends_only" -> IslandPrivacy.FRIENDS_ONLY;
                case "private" -> IslandPrivacy.PRIVATE;
                default -> null;
            };

            if (tier == null) {
                player.sendMessage("<c>Usage: {}", "/islandprivacy <public|friends|private>");
                return;
            }

            SkyBlockDataHandler handler = player.getSkyblockDataHandler();
            handler.get(SkyBlockDataHandler.Data.ISLAND_PRIVACY, DatapointString.class).setValue(tier.name());
            handler.saveToApi();

            player.sendMessage("<e>Your island is now set to <b>{}<e>!", tier.name());
        }, tierArg);
    }
}
