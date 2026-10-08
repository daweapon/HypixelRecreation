package net.swofty.type.skyblockgeneric.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureSpawner;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "spawnseacreature spawnsc",
        description = "Spawns a sea creature at your feet as if you had fished it up",
        usage = "/spawnseacreature <creature> [amount]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SpawnSeaCreatureCommand extends HypixelCommand {
    private static final int MAX_AMOUNT = 10;

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord creature = ArgumentType.Word("creature");
        creature.setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toUpperCase();
            for (String id : SeaCreatureSpawner.ids()) {
                if (!id.startsWith(typed)) continue;
                SeaCreatureProfile profile = SeaCreatureSpawner.getProfile(id);
                suggestion.addEntry(new SuggestionEntry(id, Component.text(profile.displayName())));
            }
        });
        ArgumentNumber<Integer> amount = ArgumentType.Integer("amount").between(1, MAX_AMOUNT);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            spawn((SkyBlockPlayer) sender, context.get(creature), 1);
        }, creature);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            spawn((SkyBlockPlayer) sender, context.get(creature), context.get(amount));
        }, creature, amount);
    }

    private static void spawn(SkyBlockPlayer player, String input, int amount) {
        String id = SeaCreatureSpawner.resolveId(input);
        if (id == null) {
            player.sendMessage("<c>Unknown sea creature <e>{}<c>.", input);
            return;
        }
        int spawned = 0;
        for (int i = 0; i < amount; i++) {
            if (SeaCreatureSpawner.spawn(player, id, player.getPosition()) != null) spawned++;
        }
        if (spawned > 0) SeaCreatureSpawner.announceCatch(player, id, spawned);
    }
}
