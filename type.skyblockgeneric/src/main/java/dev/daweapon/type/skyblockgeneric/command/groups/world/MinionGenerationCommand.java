package dev.daweapon.type.skyblockgeneric.command.groups.world;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "minionspeed",
        description = "Handle minion speed",
        usage = "/world minionspeed",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MinionGenerationCommand extends HypixelCommand {
    public static int divisionFactor = 1;

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<Speed> argument = new ArgumentEnum<>("speed", Speed.class);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            Speed speed = context.get(argument);

            switch (speed) {
                case MULTIPLY_2:
                    divisionFactor = 2;
                    break;
                case MULTIPLY_5:
                    divisionFactor = 5;
                    break;
                case MULTIPLY_10:
                    divisionFactor = 10;
                    break;
                case NORMAL:
                    divisionFactor = 1;
                    break;
            }

            sender.sendMessage(Text.of("<a>Successfully set minion speed to <c>{}", speed.name().toLowerCase()));
        }, argument);
    }

    enum Speed {
        MULTIPLY_2,
        MULTIPLY_5,
        MULTIPLY_10,
        NORMAL
    }
}
