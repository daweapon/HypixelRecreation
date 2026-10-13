package net.swofty.type.skyblockgeneric.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorData;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorService;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorSlots;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

@CommandParameters(labels = "visitor",
        description = "Summons any Garden visitor to your Garden",
        usage = "/visitor spawn <visitor>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class VisitorCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord visitor = ArgumentType.Word("visitor");
        visitor.setSuggestionCallback((_, context, suggestion) -> {
            String typed = normalize(context.getInput().substring(context.getInput().lastIndexOf(' ') + 1));
            for (VisitorCatalogue.Visitor entry : VisitorCatalogue.get().visitors()) {
                if (normalize(entry.id()).startsWith(typed) || normalize(entry.name()).startsWith(typed)) {
                    suggestion.addEntry(new SuggestionEntry(entry.id(), Component.text(entry.name() + " (" + entry.rarity().getLabel() + ")")));
                }
            }
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            spawn((SkyBlockPlayer) sender, context.get(visitor));
        }, ArgumentType.Literal("spawn"), visitor);
    }

    private static void spawn(SkyBlockPlayer player, String input) {
        if (!HypixelConst.isGardenServer()) {
            player.sendMessage("<c>You can only summon visitors on your Garden.");
            return;
        }
        String wanted = normalize(input);
        VisitorCatalogue.Visitor found = VisitorCatalogue.get().visitors().stream()
                .filter(entry -> normalize(entry.id()).equals(wanted) || normalize(entry.name()).equals(wanted))
                .findFirst().orElse(null);
        if (found == null) {
            player.sendMessage("<c>Unknown visitor <e>{}<c>. Press Tab to see them all.", input);
            return;
        }
        GardenProfile profile = Gardens.profile(player);
        VisitorData data = profile.visitors();
        if (data.find(found.id()) != null) {
            player.sendMessage("<c>{} <c>is already on your Garden.", found.name());
            return;
        }
        if (!VisitorService.add(profile, player, found, System.currentTimeMillis())) {
            player.sendMessage("<c>Your Garden has no room for another visitor.");
            return;
        }
        Gardens.save(player, profile);
        VisitorSlots.refresh(player.getUuid(), data);
        player.sendMessage("<a>Summoned <e>{}<a>.", found.name());
    }

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
