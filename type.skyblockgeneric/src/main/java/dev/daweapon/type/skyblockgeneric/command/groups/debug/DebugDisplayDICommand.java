package dev.daweapon.type.skyblockgeneric.command.groups.debug;

import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;

@CommandParameters(labels = "damageindicator",
        description = "Display damage indic",
        usage = "/debug damageindicator <dmg> <crit>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class DebugDisplayDICommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentBoolean crit = ArgumentType.Boolean("critical");
        ArgumentInteger damage = ArgumentType.Integer("damage");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            new DamageIndicator()
                    .damage(context.get(damage))
                    .pos(player.getPosition())
                    .critical(context.get(crit))
                    .display(player.getInstance());

        }, damage, crit);
    }
}
