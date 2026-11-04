package dev.daweapon.type.skyblockgeneric.command.groups.force;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.theend.ProtectorProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "protector", description = "Shows or sets the server Zealot counter that summons the End Stone Protector",
        usage = "/force protector <status|kills <0-5000>>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceProtectorCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord actionArgument = ArgumentType.Word("action").from("status", "kills");
        ArgumentWord valueArgument = ArgumentType.Word("value");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run(sender, context.get(actionArgument), null);
        }, actionArgument);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run(sender, context.get(actionArgument), context.get(valueArgument));
        }, actionArgument, valueArgument);
    }

    private static void run(CommandSender sender, String action, String value) {
        SkyBlockPlayer player = (SkyBlockPlayer) sender;
        switch (action) {
            case "status" -> status(player);
            case "kills" -> kills(player, value);
            default -> player.sendMessage("<c>Usage: /force protector status or kills 0-{}", ProtectorProgress.KILLS_TO_SUMMON);
        }
    }

    private static void status(SkyBlockPlayer player) {
        player.sendMessage("<7>End Stone Protector: {} <8>({}/{} Zealots, your share {})",
                ProtectorProgress.stage().display(), ProtectorProgress.kills(), ProtectorProgress.KILLS_TO_SUMMON,
                ProtectorProgress.contributed(player.getUuid()));
    }

    private static void kills(SkyBlockPlayer player, String value) {
        try {
            ProtectorProgress.forceKills(Integer.parseInt(value));
            status(player);
        } catch (NumberFormatException | NullPointerException exception) {
            player.sendMessage("<c>Kills must be a number from 0 to {}.", ProtectorProgress.KILLS_TO_SUMMON);
        }
    }
}
