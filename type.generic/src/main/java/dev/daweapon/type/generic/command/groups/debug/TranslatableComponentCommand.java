package dev.daweapon.type.generic.command.groups.debug;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@SuppressWarnings("unused")
@CommandParameters(labels = "component",
    description = "Preview a TranslatableComponent",
    usage = "/debug component <string>",
    permission = Rank.STAFF,
    allowsConsole = false)
// Debug command to preview how a Minecraft TranslatableComponent key renders for the sender.
public class TranslatableComponentCommand extends HypixelCommand {

    @Override
    public void registerUsage(HypixelCommand.MinestomCommand command) {
        ArgumentString arg = ArgumentType.String("translatable");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            String key = context.get(arg);

            HypixelPlayer player = (HypixelPlayer) sender;
            player.sendMessage(Component.translatable(key));
        }, arg);
    }

}
