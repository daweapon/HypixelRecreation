package dev.daweapon.type.skyblockgeneric.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.entity.mob.SpawnableMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "spawn",
        description = "Spawns any mob, sea creature or Mythological creature at your feet",
        usage = "/spawn <mob> [amount]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SpawnCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord mob = ArgumentType.Word("mob");
        mob.setSuggestionCallback((_, context, suggestion) -> {
            String typed = SpawnableMobs.normalize(context.getInput().substring(context.getInput().lastIndexOf(' ') + 1));
            for (SpawnableMobs.Entry entry : SpawnableMobs.all()) {
                if (SpawnableMobs.normalize(entry.id()).startsWith(typed)) {
                    suggestion.addEntry(new SuggestionEntry(entry.id(), Component.text(entry.displayName())));
                }
            }
        });
        ArgumentNumber<Integer> amount = ArgumentType.Integer("amount").between(1, SpawnableMobs.MAX_AMOUNT);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            spawn((SkyBlockPlayer) sender, context.get(mob), 1);
        }, mob);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            spawn((SkyBlockPlayer) sender, context.get(mob), context.get(amount));
        }, mob, amount);
    }

    @Override
    public List<net.minestom.server.command.builder.Command> legacyCommands() {
        return CommandGroup.legacyCommands(getCommand(), Map.of(
                "spawnseacreature", "spawn", "spawnsc", "spawn",
                "spawncrimsonmob", "spawn", "spawncm", "spawn"));
    }

    private static void spawn(SkyBlockPlayer player, String input, int amount) {
        SpawnableMobs.Entry entry = SpawnableMobs.find(input);
        if (entry == null) {
            player.sendMessage("<c>Unknown mob <e>{}<c>. Press Tab to see them all.", input);
            return;
        }
        int spawned = entry.spawner().spawn(player, amount);
        if (spawned == 0) {
            player.sendMessage("<c>Could not spawn <e>{}<c>.", entry.displayName());
        } else if (spawned < amount) {
            player.sendMessage("<a>Spawned <e>{}<a> of <e>{} {}<a>; the rest hit a spawn limit.", spawned, amount, entry.displayName());
        } else {
            player.sendMessage("<a>Spawned <e>{} {}<a>.", spawned, entry.displayName());
        }
    }
}
