package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.network.NetworkBuffer;
import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** A single-word {@link ItemType} id, suggested as the player types. */
public final class ArgumentItemType extends Argument<ItemType> {
    public static final int NOT_AN_ITEM_ERROR = 1;

    public ArgumentItemType(String id) {
        super(id);
        setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toUpperCase(Locale.ROOT);
            for (ItemType type : ItemType.values()) {
                if (type.name().startsWith(typed)) suggestion.addEntry(new SuggestionEntry(type.name()));
            }
        });
    }

    @Override
    public ItemType parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        ItemType type = ItemType.get(input);
        if (type == null) throw new ArgumentSyntaxException("Not an item", input, NOT_AN_ITEM_ERROR);
        return type;
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.STRING;
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, 0);
    }

    @Override
    public String toString() {
        return String.format("ItemType<%s>", getId());
    }
}
