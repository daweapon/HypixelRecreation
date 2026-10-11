package net.swofty.type.skyblockgeneric.command.groups.set;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.Player;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Sets a profile's balance of one essence type",
        labels = "essence",
        usage = "/set essence <type> <amount> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetEssenceCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<EssenceType> typeArgument = ArgumentType.Enum("type", EssenceType.class);
        ArgumentNumber<Long> amountArgument = ArgumentType.Long("amount").min(0L);
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            set((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(typeArgument), context.get(amountArgument));
        }, typeArgument, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            set((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(typeArgument), context.get(amountArgument));
        }, typeArgument, amountArgument, targetArgument);
    }

    private void set(SkyBlockPlayer sender, SkyBlockPlayer target, EssenceType type, long amount) {
        Essences.set(target, type, amount);
        sender.sendMessage("<a>Set <e>{}<a>'s <d>{} <a>to <d>{}<a>.", target.getUsername(), type.getFullName(),
                StringUtility.commaify(Essences.get(target, type)));
    }
}
