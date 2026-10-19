package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.command.groups.set.BitsCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.CoinsCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.GemsCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.SetBestiaryCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.SetCollectionCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.SetEssenceCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.SetPetLevel;
import dev.daweapon.type.skyblockgeneric.command.groups.set.SetPetRarityCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.SetReputationCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.SetStatCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.set.XpCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "set",
        description = "Sets a profile value: coins, bits, Gems, experience levels, essence, reputation, pet level or rarity, bestiary, collection or a statistic",
        usage = "/set <coins|bits|gems|xp|essence|reputation|petlevel|petrarity|bestiary|collection|stat> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new CoinsCommand(),
                new BitsCommand(),
                new GemsCommand(),
                new XpCommand(),
                new SetEssenceCommand(),
                new SetReputationCommand(),
                new SetPetLevel(),
                new SetPetRarityCommand(),
                new SetBestiaryCommand(),
                new SetCollectionCommand(),
                new SetStatCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("coins", "set coins"),
                Map.entry("bits", "set bits"),
                Map.entry("bit", "set bits"),
                Map.entry("gems", "set gems"),
                Map.entry("xp", "set xp"),
                Map.entry("experience", "set xp"),
                Map.entry("setessence", "set essence"),
                Map.entry("setreputation", "set reputation"),
                Map.entry("setrep", "set reputation"),
                Map.entry("setpetlevel", "set petlevel"),
                Map.entry("updatepetlevel", "set petlevel"),
                Map.entry("setpetrarity", "set petrarity"),
                Map.entry("updatebestiary", "set bestiary"),
                Map.entry("updatecollection", "set collection"),
                Map.entry("setstat", "set stat"),
                Map.entry("setstatistic", "set stat"));
    }
}
