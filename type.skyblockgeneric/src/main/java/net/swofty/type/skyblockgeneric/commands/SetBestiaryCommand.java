package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "updatebestiary",
        description = "Updates the bestiary of a player",
        usage = "/setbestiary <mob> <amount> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetBestiaryCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString mobArgument = new ArgumentString("mob");
        ArgumentInteger amountArgument = new ArgumentInteger("amount");
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            setBestiary((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(mobArgument), context.get(amountArgument));
        }, mobArgument, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            setBestiary((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(mobArgument), context.get(amountArgument));
        }, mobArgument, amountArgument, targetArgument);
    }

    private void setBestiary(SkyBlockPlayer sender, SkyBlockPlayer target, String mobId, int amount) {
        BestiaryMob mob = MobRegistry.getMobById(mobId);
        if (mob == null) {
            sender.sendMessage("<c>Unknown mob: {}", mobId);
            return;
        }

        target.getBestiaryData().set(mob, amount);

        if (target == sender) {
            sender.sendMessage("<a>Set bestiary kills for <e>{}<a> to <e>{}<a>.", mob.getDisplayName(), amount);
        } else {
            target.sendMessage("<a>Your bestiary kills for <e>{}<a> were set to <e>{}<a>.", mob.getDisplayName(), amount);
            sender.sendMessage("<a>Set <e>{}<a>'s bestiary kills for <e>{}<a> to <e>{}<a>.", target.getUsername(), mob.getDisplayName(), amount);
        }
    }
}
