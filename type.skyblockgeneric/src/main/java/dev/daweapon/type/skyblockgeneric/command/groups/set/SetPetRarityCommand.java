package dev.daweapon.type.skyblockgeneric.command.groups.set;

import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.item.ItemAttributeHandler;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandParameters(labels = "petrarity",
        description = "Sets the rarity of the pet in your hand",
        usage = "/set petrarity <rarity>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetPetRarityCommand extends HypixelCommand {
    private static final List<Rarity> PET_RARITIES = Arrays.stream(Rarity.values())
            .filter(rarity -> rarity != Rarity.DIVINE && rarity != Rarity.ADMIN)
            .toList();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord rarityArgument = new ArgumentWord("rarity");
        rarityArgument.setSuggestionCallback((sender, context, suggestion) ->
                PET_RARITIES.forEach(rarity ->
                        suggestion.addEntry(new SuggestionEntry(rarity.name()))));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Rarity rarity = Rarity.getRarity(context.get(rarityArgument));
            if (rarity == null || !PET_RARITIES.contains(rarity)) {
                sender.sendMessage(Text.of("<c>Invalid rarity. Use {}.", PET_RARITIES.stream()
                        .map(Enum::name).collect(Collectors.joining("/"))));
                return;
            }
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            player.updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributeHandler handler = item.getAttributeHandler();
                if (!handler.isPet()) {
                    sender.sendMessage("<c>The item in your hand is not a pet.");
                    return;
                }
                List<Rarity> available = item.getComponent(PetComponent.class).getRarities();
                if (!available.contains(rarity)) {
                    sender.sendMessage(Text.of("<c>This pet only comes in {}.", available.stream()
                            .map(Enum::name).collect(Collectors.joining("/"))));
                    return;
                }
                handler.setRarity(rarity);
                sender.sendMessage(Text.of("<a>Set rarity to <color:{}>{}<a>.",
                        rarity.getColor(), rarity.name()));
            });
        }, rarityArgument);
    }
}
