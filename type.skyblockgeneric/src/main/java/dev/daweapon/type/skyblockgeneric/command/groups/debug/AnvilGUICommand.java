package dev.daweapon.type.skyblockgeneric.command.groups.debug;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.gui.HypixelAnvilGUI;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "anvilgui",
        description = "Opens a graphical user interface",
        usage = "/debug anvilgui <text>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class AnvilGUICommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString text = ArgumentType.String("text");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String anvilText = context.get(text);
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            new HypixelAnvilGUI(player).open(anvilText).thenAccept(line -> {
                if (line == null) {
                    return;
                }

                player.sendMessage("<7>You wrote: <a>{}", line);
            });
        }, text);
    }
}
