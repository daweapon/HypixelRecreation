package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandGroup;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.command.groups.hand.ChangeNBTCommand;
import net.swofty.type.skyblockgeneric.command.groups.hand.EnchantCommand;
import net.swofty.type.skyblockgeneric.command.groups.hand.GetTrackedItemInformationCommand;
import net.swofty.type.skyblockgeneric.command.groups.hand.MithrilInfuseCommand;
import net.swofty.type.skyblockgeneric.command.groups.hand.NBTCommand;
import net.swofty.type.skyblockgeneric.command.groups.hand.RecombobulateCommand;
import net.swofty.type.skyblockgeneric.command.groups.hand.ReforgeCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "hand",
        description = "Edits the item in your hand: enchant, reforge, recombobulate, Mithril Infuse, NBT",
        usage = "/hand <enchant|reforge|recomb|mithrilinfuse|nbt|setnbt|tracked> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HandCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new EnchantCommand(),
                new ReforgeCommand(),
                new RecombobulateCommand(),
                new MithrilInfuseCommand(),
                new NBTCommand(),
                new ChangeNBTCommand(),
                new GetTrackedItemInformationCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("enchant", "hand enchant"),
                Map.entry("ench", "hand enchant"),
                Map.entry("reforge", "hand reforge"),
                Map.entry("reforgeitem", "hand reforge"),
                Map.entry("recomb", "hand recomb"),
                Map.entry("mithrilinfuse", "hand mithrilinfuse"),
                Map.entry("nbt", "hand nbt"),
                Map.entry("iteminfo", "hand nbt"),
                Map.entry("editnbt", "hand setnbt"),
                Map.entry("itemtrackedinformation", "hand tracked"));
    }
}
