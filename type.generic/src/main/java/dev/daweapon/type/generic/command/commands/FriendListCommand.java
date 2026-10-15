package dev.daweapon.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.friend.FriendManager;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(
        labels = "fl friendlist",
        description = "List your friends",
        usage = "/fl [page]",
        permission = Rank.DEFAULT,
        allowsConsole = false
)
// Command that lists a player's friends, paginated and optionally sorted by "best" status.
public class FriendListCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString pageArg = ArgumentType.String("page");

        // No args -> page 1
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            FriendManager.listFriends(player, 1, false);
        });

        // With page/best
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            String arg = context.get(pageArg);

            if ("best".equalsIgnoreCase(arg)) {
                FriendManager.listFriends(player, 1, true);
                return;
            }

            int page = parsePageNumber(arg);
            FriendManager.listFriends(player, page, false);
        }, pageArg);
    }

    private int parsePageNumber(String arg) {
        try {
            return Math.max(1, Integer.parseInt(arg));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}


