package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.Player;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Sets a profile's Crimson Isle reputation with one faction",
        labels = "setreputation setrep",
        usage = "/setreputation <faction> <amount> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetReputationCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<CrimsonFaction> factionArgument = ArgumentType.Enum("faction", CrimsonFaction.class);
        ArgumentNumber<Integer> amountArgument = ArgumentType.Integer("amount").min(0);
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            set((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(factionArgument), context.get(amountArgument));
        }, factionArgument, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            set((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(factionArgument), context.get(amountArgument));
        }, factionArgument, amountArgument, targetArgument);
    }

    private void set(SkyBlockPlayer sender, SkyBlockPlayer target, CrimsonFaction faction, int amount) {
        CrimsonReputationService.set(target, faction, amount);
        sender.sendMessage("<a>Set <e>" + target.getUsername() + "<a>'s " + faction.coloured() + " Reputation <a>to <e>"
                + StringUtility.commaify(amount) + " <7>(" + CrimsonReputationService.tier(target, faction).coloured() + "<7>)<a>.");
    }
}
