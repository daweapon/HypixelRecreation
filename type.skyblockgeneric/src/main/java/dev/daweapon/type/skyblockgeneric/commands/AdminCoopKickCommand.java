package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

// Staff override for CoopKickCommand - skips the member vote and the 2-person 35-day-inactivity
// bypass entirely, for moderation cases (rule violations, disputes) that can't wait on a vote.
@CommandParameters(labels = "admincoopkick",
        description = "Force-removes a player from their co-op, bypassing the member vote",
        usage = "/admincoopkick <player>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class AdminCoopKickCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord targetArg = ArgumentType.Word("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer admin = (SkyBlockPlayer) sender;
            String targetName = context.get(targetArg);

            UUID targetUuid;
            try {
                targetUuid = ProfilesDatabase.fetchUUID(targetName);
            } catch (Exception e) {
                admin.sendMessage("<c>That player could not be found!");
                return;
            }

            CoopDatabase.Coop coop = CoopDatabase.getFromMember(targetUuid);
            if (coop == null) {
                admin.sendMessage("<c>That player is not part of a co-op!");
                return;
            }

            UUID targetProfileId = CoopKickCommand.findMemberProfile(coop, targetUuid);
            if (targetProfileId == null) {
                admin.sendMessage("<c>Could not locate that member's co-op profile!");
                return;
            }

            CoopKickCommand.createFallbackProfile(targetUuid, targetProfileId);
            CoopKickCommand.performKick(admin, coop.coopUUID(), targetUuid, targetProfileId, targetName)
                    .thenAccept(kicked -> {
                        if (kicked) {
                            admin.sendMessage("<e>Force-kicked {} from their co-op.", targetName);
                        } else {
                            admin.sendMessage("<c>Could not force-kick {} from their co-op.", targetName);
                        }
                    });
        }, targetArg);
    }
}
