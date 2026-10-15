package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandGroup;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.command.groups.unlock.UnlockBestiaryCommand;
import dev.daweapon.type.skyblockgeneric.command.groups.unlock.UnlockWarpsCommand;

import java.util.List;
import java.util.Map;

@CommandParameters(labels = "unlock",
        description = "Unlocks Fast Travel warps or the Bestiary",
        usage = "/unlock <warps|bestiary> ...",
        permission = Rank.STAFF,
        allowsConsole = false)
public class UnlockCommand extends CommandGroup {
    @Override
    protected List<HypixelCommand> subcommands() {
        return List.of(new UnlockWarpsCommand(),
                new UnlockBestiaryCommand());
    }

    @Override
    protected Map<String, String> legacyLabels() {
        return Map.ofEntries(Map.entry("unlockwarps", "unlock warps"),
                Map.entry("unlockbestiary", "unlock bestiary"));
    }
}
