package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;

@CommandParameters(labels = "spawncrimsonmob spawncm",
        description = "Spawns a Crimson Isle mob or miniboss at your feet",
        usage = "/spawncrimsonmob <mob>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SpawnCrimsonMobCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord mob = ArgumentType.Word("mob");
        mob.setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toUpperCase();
            MobRegistry.REGISTERED_MOBS.stream()
                    .filter(registry -> registry.getMobCache() instanceof CrimsonIsleMob)
                    .map(registry -> ((CrimsonIsleMob) registry.getMobCache()).getMobID())
                    .filter(id -> id.startsWith(typed))
                    .sorted()
                    .forEach(id -> suggestion.addEntry(new SuggestionEntry(id)));
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String id = context.get(mob).toUpperCase();
            Optional<MobRegistry> registry = MobRegistry.REGISTERED_MOBS.stream()
                    .filter(entry -> entry.getMobCache() instanceof CrimsonIsleMob crimson && crimson.getMobID().equals(id))
                    .findFirst();
            if (registry.isEmpty()) {
                player.sendMessage("<c>Unknown Crimson Isle mob <e>{}<c>.", id);
                return;
            }
            registry.get().asMob().setInstance(player.getInstance(), player.getPosition());
            player.sendMessage("<a>Spawned <e>{}<a>.", id);
        }, mob);
    }
}
