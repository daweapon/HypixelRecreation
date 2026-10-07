package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreature;
import net.swofty.type.skyblockgeneric.mythological.burrow.GriffinBurrowService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "forcedianamob dianamob",
        description = "Makes your next Griffin Mob burrow spawn the given Mythological creature",
        usage = "/forcedianamob <creature>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceDianaMobCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<MythologicalCreature> creature = ArgumentType.Enum("creature", MythologicalCreature.class);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            MythologicalCreature chosen = context.get(creature);
            GriffinBurrowService.INSTANCE.forceNextCreature(player.getUuid(), chosen);
            player.sendMessage("<a>Your next Mob burrow will spawn a <e>{}<a>.", chosen.getDisplayName());
        }, creature);
    }
}
