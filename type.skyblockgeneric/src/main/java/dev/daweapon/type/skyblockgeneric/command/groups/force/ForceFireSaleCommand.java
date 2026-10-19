package dev.daweapon.type.skyblockgeneric.command.groups.force;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.firesale.FireSale;
import dev.daweapon.type.skyblockgeneric.firesale.FireSaleCatalogue;
import dev.daweapon.type.skyblockgeneric.firesale.FireSaleService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "firesale", description = "Starts a Fire Sale, replays a past one, or ends the active one",
        usage = "/force firesale <next|end|sale id> [minutes]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceFireSaleCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord saleArgument = ArgumentType.Word("sale");
        saleArgument.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("next"));
            suggestion.addEntry(new SuggestionEntry("end"));
            FireSaleCatalogue.all().keySet().forEach(id -> suggestion.addEntry(new SuggestionEntry(id)));
        });
        var minutesArgument = ArgumentType.Integer("minutes").min(1);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run((SkyBlockPlayer) sender, context.get(saleArgument), FireSaleService.DEFAULT_DURATION_MINUTES);
        }, saleArgument);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            run((SkyBlockPlayer) sender, context.get(saleArgument), context.get(minutesArgument));
        }, saleArgument, minutesArgument);
    }

    private void run(SkyBlockPlayer player, String argument, long minutes) {
        if (argument.equalsIgnoreCase("end")) {
            player.sendMessage(FireSaleService.end() ? "<a>Ended the active Fire Sale." : "<c>Redis is unavailable.");
            return;
        }
        FireSale sale = argument.equalsIgnoreCase("next")
                ? FireSaleCatalogue.next(FireSaleService.lastSaleId())
                : FireSaleCatalogue.get(argument);
        if (sale == null) {
            player.sendMessage("<c>Unknown Fire Sale. Use tab completion for the sale ids.");
            return;
        }
        if (!FireSaleService.start(sale, minutes)) {
            player.sendMessage("<c>Redis is unavailable.");
            return;
        }
        player.sendMessage("<a>Started the Fire Sale <6>{} <a>({}) for <e>{} <a>minutes.", sale.name(), sale.id(), minutes);
    }
}
