package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureSpawner;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "spawnseacreature spawnsc",
        description = "Spawns a sea creature at your feet as if you had fished it up",
        usage = "/spawnseacreature <creature>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SpawnSeaCreatureCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord creature = ArgumentType.Word("creature");
        creature.setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toUpperCase();
            SeaCreatureSpawner.ids().stream().filter(id -> id.startsWith(typed))
                    .forEach(id -> suggestion.addEntry(new SuggestionEntry(id)));
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String id = context.get(creature).toUpperCase();
            if (!SeaCreatureSpawner.canSpawn(id)) {
                player.sendMessage("<c>Unknown sea creature <e>{}<c>.", id);
                return;
            }
            SeaCreatureSpawner.spawn(player, id, player.getPosition());
            SeaCreatureSpawner.announceCatch(player, id, 1);
        }, creature);
    }
}
