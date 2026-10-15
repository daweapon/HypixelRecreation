package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerService;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTier;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTierDefinition;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "slayer", description = "Starts and manages Slayer quests",
    usage = "/slayer <start|status|cancel>",
    permission = Rank.DEFAULT,
    allowsConsole = false)
public class SlayerCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<SlayerType> typeArgument = ArgumentType.Enum("type", SlayerType.class);
        ArgumentEnum<SlayerTier> tierArgument = ArgumentType.Enum("tier", SlayerTier.class);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            SlayerService.StartResult result = SlayerService.startQuest(
                player,
                context.get(typeArgument),
                context.get(tierArgument)
            );
            if (!result.success()) {
                player.sendMessage("<c>{}", result.message());
            }
        }, ArgumentType.Literal("start"), typeArgument, tierArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            SlayerService.QuestStatus status = SlayerService.status(player);
            if (!status.active()) {
                player.sendMessage("<c>You do not have an active Slayer quest.");
                return;
            }

            SlayerTierDefinition tier = status.tier();
            if (tier == null) {
                player.sendMessage("<c>Your active Slayer quest is no longer configured.");
                return;
            }

            player.sendMessage("<5><l>SLAYER QUEST");
            player.sendMessage("<7>Boss: <c>{}", tier.displayName(status.quest().type()));
            player.sendMessage("<7>Progress: <e>{}<8>/<e>{} Combat XP", status.quest().combatXp(), tier.requiredCombatXp());
            player.sendMessage(status.quest().bossSpawned() ? "<c>Boss spawned!" : "<7>Kill matching mobs to summon your boss.");
        }, ArgumentType.Literal("status"));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SlayerService.cancelQuest((SkyBlockPlayer) sender);
        }, ArgumentType.Literal("cancel"));
    }
}
