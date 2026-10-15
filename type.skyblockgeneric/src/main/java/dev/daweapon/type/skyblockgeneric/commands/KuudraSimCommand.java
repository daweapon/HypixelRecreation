package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra.GUIKuudraRun;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

@CommandParameters(labels = "kuudrasim",
        description = "Simulates winning a Kuudra run of the given tier",
        usage = "/kuudrasim <tier> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class KuudraSimCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord tierArgument = ArgumentType.Word("tier");
        tierArgument.setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toUpperCase();
            for (KuudraTier tier : KuudraTier.values()) {
                if (tier.name().startsWith(typed)) suggestion.addEntry(new SuggestionEntry(tier.name()));
            }
        });
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            simulate(player, player, context.get(tierArgument));
        }, tierArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            simulate((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(tierArgument));
        }, tierArgument, targetArgument);
    }

    private static void simulate(SkyBlockPlayer staff, SkyBlockPlayer target, String input) {
        KuudraTier tier = parse(input);
        if (tier == null) {
            staff.sendMessage("<c>Unknown Kuudra tier <e>" + input + "<c>. Use BASIC, HOT, BURNING, FIERY, INFERNAL or 1-5.");
            return;
        }
        KuudraRun run = KuudraProgress.completeRun(target, tier);
        target.sendMessage("<c><l>KUUDRA DOWN! <7>(simulated " + tier.getDisplayName() + " Tier win)");
        target.sendMessage("<7>Your chests are stored at <e>Vesuvius <7>for <a>72 hours<7>.");
        target.openView(new GUIKuudraRun(run));
        if (target != staff) {
            staff.sendMessage("<a>Simulated a " + tier.getDisplayName() + " Tier Kuudra win for <e>" + target.getUsername() + "<a>.");
        }
    }

    private static @Nullable KuudraTier parse(String input) {
        String key = input.toUpperCase().replaceFirst("^T", "");
        if (key.matches("[1-5]")) return KuudraTier.values()[Integer.parseInt(key) - 1];
        for (KuudraTier tier : KuudraTier.values()) if (tier.name().equals(key)) return tier;
        return null;
    }
}
