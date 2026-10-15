package dev.daweapon.type.skyblockgeneric.command.groups.force;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.loottable.ForcedDrops;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@CommandParameters(labels = "drop",
        description = "Guarantees an item from the next mob killed that can drop it",
        usage = "/force drop <item> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceDropCommand extends HypixelCommand {
    private static final Map<String, ItemType> ALIASES = Map.of("CHIMERA", ItemType.ENCHANTED_BOOK);

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord itemArgument = ArgumentType.Word("item");
        itemArgument.setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toUpperCase();
            ALIASES.keySet().stream().filter(alias -> alias.startsWith(typed))
                    .forEach(alias -> suggestion.addEntry(new SuggestionEntry(alias)));
            for (ItemType type : ItemType.values()) {
                if (type.name().startsWith(typed)) suggestion.addEntry(new SuggestionEntry(type.name()));
            }
        });
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            force(player, player, context.get(itemArgument));
        }, itemArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            force((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(itemArgument));
        }, itemArgument, targetArgument);
    }

    private static void force(SkyBlockPlayer staff, SkyBlockPlayer target, String input) {
        ItemType item = resolve(input);
        if (item == null) {
            staff.sendMessage("<c>Unknown item <e>" + input + "<c>.");
            return;
        }
        ForcedDrops.force(target.getUuid(), item);
        String name = ALIASES.containsKey(input.toUpperCase()) ? "Chimera I book" : item.getDisplayName();
        target.sendMessage("<a>The next mob you kill that can drop <e>" + name + " <a>will drop it.");
        if (target != staff) {
            staff.sendMessage("<a>The next mob <e>" + target.getUsername() + " <a>kills that can drop <e>" + name + " <a>will drop it.");
        }
    }

    private static @Nullable ItemType resolve(String input) {
        String key = input.toUpperCase();
        if (ALIASES.containsKey(key)) return ALIASES.get(key);
        return ItemType.get(key);
    }
}
