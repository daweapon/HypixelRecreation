package dev.daweapon.type.skyblockgeneric.command.groups.force;

import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

abstract class ForceToggleCommand extends HypixelCommand {
    protected abstract String eventName();

    protected abstract boolean set(boolean active);

    protected abstract boolean clear();

    protected abstract boolean active();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord state = ArgumentType.Word("state").from("on", "off", "auto");
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            player.sendMessage("<7>The " + eventName() + " is " + (active() ? "<a>on" : "<c>off") + "<7>.");
        });
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            String choice = context.get(state);
            boolean saved = switch (choice) {
                case "on" -> set(true);
                case "off" -> set(false);
                default -> clear();
            };
            sender.sendMessage(saved
                    ? "<a>The " + eventName() + " is now <f>" + (choice.equals("auto") ? "following its schedule" : choice) + " <a>on every server."
                    : "<c>Redis is unavailable, so the " + eventName() + " could not be changed.");
        }, state);
    }
}
