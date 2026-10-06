package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.staff.MaxStickService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

@CommandParameters(labels = "skyblockxp sbxp",
        description = "Gives, takes or sets a player's SkyBlock XP",
        usage = "/skyblockxp <give|take|set|setlevel|prestige|max|reset|info|source> [amount] [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SkyBlockXPCommand extends HypixelCommand {
    /** Real Hypixel shifts the bracketed level prefix to a new colour every 40 levels. */
    public static final int PRESTIGE_INTERVAL = 40;

    private static final String[] AMOUNT_ACTIONS = {"give", "take", "set", "setlevel"};
    private static final String[] SIMPLE_ACTIONS = {"prestige", "max", "reset", "info"};

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord amountAction = ArgumentType.Word("action").from(AMOUNT_ACTIONS);
        ArgumentWord simpleAction = ArgumentType.Word("action").from(SIMPLE_ACTIONS);
        ArgumentDouble amountArgument = ArgumentType.Double("amount");
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        ArgumentWord sourceLiteral = ArgumentType.Word("source").from("source");
        ArgumentWord sourceArgument = ArgumentType.Word("xpSource");
        sourceArgument.setSuggestionCallback((_, _, suggestion) -> {
            for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
                suggest(suggestion, source.name(), "<6>{} <7>- max <b>{} XP <8>({})",
                        source.getDisplayName(), source.getMaxXP(), source.getAvailability().getDisplayName());
            }
        });

        // Numeric syntaxes are registered first so a numeric argument never gets parsed as a
        // player name by the shorter "<action> [player]" forms below.
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            runAmount((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(amountAction), context.get(amountArgument));
        }, amountAction, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer target = resolve((SkyBlockPlayer) sender, context.get(targetArgument).findFirstPlayer(sender));
            if (target == null) return;
            runAmount((SkyBlockPlayer) sender, target, context.get(amountAction), context.get(amountArgument));
        }, amountAction, amountArgument, targetArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            runSimple((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(simpleAction));
        }, simpleAction);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer target = resolve((SkyBlockPlayer) sender, context.get(targetArgument).findFirstPlayer(sender));
            if (target == null) return;
            runSimple((SkyBlockPlayer) sender, target, context.get(simpleAction));
        }, simpleAction, targetArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            runSource((SkyBlockPlayer) sender, (SkyBlockPlayer) sender, context.get(sourceArgument), context.get(amountArgument));
        }, sourceLiteral, sourceArgument, amountArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer target = resolve((SkyBlockPlayer) sender, context.get(targetArgument).findFirstPlayer(sender));
            if (target == null) return;
            runSource((SkyBlockPlayer) sender, target, context.get(sourceArgument), context.get(amountArgument));
        }, sourceLiteral, sourceArgument, amountArgument, targetArgument);
    }

    private SkyBlockPlayer resolve(SkyBlockPlayer sender, Player found) {
        if (found == null) {
            sender.sendMessage("<c>Could not find that player.");
            return null;
        }
        return (SkyBlockPlayer) found;
    }

    private void runAmount(SkyBlockPlayer sender, SkyBlockPlayer target, String action, double amount) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = target.getSkyBlockExperience();
        double before = experience.getTotalXP();

        switch (action.toLowerCase(Locale.ROOT)) {
            case "give" -> experience.addBonusXP(amount);
            case "take" -> experience.addBonusXP(-amount);
            case "set" -> experience.setTotalXP(amount);
            case "setlevel" -> {
                int level = (int) Math.round(amount);
                if (!SkyBlockLevelRequirement.levelExists(level)) {
                    sender.sendMessage("<c>Level <e>{}<c> does not exist. Valid range is <e>0<c>-<e>{}<c>.",
                            level, SkyBlockLevelRequirement.getMaxLevel());
                    return;
                }
                experience.setTotalXP(SkyBlockLevelRequirement.getLevel(level).getCumulativeExperience());
            }
            default -> {
                sender.sendMessage("<c>Unknown action <e>{}<c>.", action);
                return;
            }
        }

        report(sender, target, before, experience);
    }

    private void runSimple(SkyBlockPlayer sender, SkyBlockPlayer target, String action) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = target.getSkyBlockExperience();
        double before = experience.getTotalXP();

        switch (action.toLowerCase(Locale.ROOT)) {
            case "prestige" -> {
                int current = experience.getLevel().asInt();
                int next = ((current / PRESTIGE_INTERVAL) + 1) * PRESTIGE_INTERVAL;
                if (!SkyBlockLevelRequirement.levelExists(next)) {
                    sender.sendMessage("<c>Already at the highest prefix tier (level <e>{}<c>).", current);
                    return;
                }
                experience.setTotalXP(SkyBlockLevelRequirement.getLevel(next).getCumulativeExperience());
                sender.sendMessage("<a>Skipped <e>{}<a> to the next prefix tier: level <e>{}<a>.",
                        target.getUsername(), next);
            }
            case "max" -> MaxStickService.maxSkyBlockLevel(target);
            case "reset" -> {
                experience.getCompletedExperienceCauses().clear();
                experience.getSourceProgress().clear();
                experience.setBonusXP(0D);
                sender.sendMessage("<a>Reset <e>{}<a>'s SkyBlock XP to zero.", target.getUsername());
            }
            case "info" -> {
                sendInfo(sender, target, experience);
                return;
            }
            default -> {
                sender.sendMessage("<c>Unknown action <e>{}<c>.", action);
                return;
            }
        }

        report(sender, target, before, experience);
    }

    private void runSource(SkyBlockPlayer sender, SkyBlockPlayer target, String sourceName, double amount) {
        SkyBlockXPSource source;
        try {
            source = SkyBlockXPSource.valueOf(sourceName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            sender.sendMessage("<c>Unknown XP source <e>{}<c>.", sourceName);
            return;
        }

        if (!source.usesSourceProgress()) {
            sender.sendMessage("<c>{} <c>is earned through its own causes — use <e>/giveskyblockxp<c> instead.",
                    source.getDisplayName());
            return;
        }

        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = target.getSkyBlockExperience();
        double before = experience.getTotalXP();
        experience.setSourceXP(source, amount);

        sender.sendMessage("<a>Set <e>{}<a>'s <e>{}<a> progress to <b>{}<a>/<b>{} XP<a>.",
                target.getUsername(), source.getDisplayName(),
                Math.round(experience.getSourceXP(source)), source.getMaxXP());
        report(sender, target, before, experience);
    }

    private void sendInfo(SkyBlockPlayer sender, SkyBlockPlayer target,
                          DatapointSkyBlockExperience.PlayerSkyBlockExperience experience) {
        sender.sendMessage("<3><m>---------------------------------");
        sender.sendMessage("  <b><l>SKYBLOCK XP <7>- <f>{}", target.getUsername());
        sender.sendMessage("  <7>Level: <3>{} <8>({})", experience.getLevel().asInt(),
                String.format(Locale.ROOT, "%.2f", experience.getPreciseLevel()));
        sender.sendMessage("  <7>Total XP: <b>{}<7>/<b>{}", Math.round(experience.getTotalXP()),
                SkyBlockXPSource.TOTAL_MAX_XP);
        sender.sendMessage("  <7>Earned: <b>{} <8>(causes <b>{}<8>, sources <b>{}<8>)",
                Math.round(experience.getEarnedXP()),
                Math.round(experience.getEarnedXP() - experience.getTotalSourceXP()),
                Math.round(experience.getTotalSourceXP()));
        sender.sendMessage("  <7>Admin bonus: <e>{}", Math.round(experience.getBonusXP()));
        sender.sendMessage("  <7>Progress: {}", experience.getNextLevelDisplay());
        sender.sendMessage("<3><m>---------------------------------");
    }

    private void report(SkyBlockPlayer sender, SkyBlockPlayer target, double before,
                        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience) {
        double after = experience.getTotalXP();
        sender.sendMessage("<a>{}<a>: <b>{} <8>-> <b>{} XP <7>(level <3>{}<7>)",
                target.getUsername(), Math.round(before), Math.round(after), experience.getLevel().asInt());
    }
}
