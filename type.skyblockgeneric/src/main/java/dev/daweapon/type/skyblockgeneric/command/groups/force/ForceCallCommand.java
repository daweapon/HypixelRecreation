package dev.daweapon.type.skyblockgeneric.command.groups.force;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactCatalogue;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneIncomingCalls;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

@CommandParameters(labels = "call", description = "Makes a contact ring you now",
        usage = "/force call <contact>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceCallCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord contactArg = ArgumentType.Word("contact").from(AbiphoneContactCatalogue.all().stream()
                .map(contact -> contact.id()).toArray(String[]::new));
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            AbiphoneNPC npc = AbiphoneRegistry.getFromId(context.get(contactArg));
            if (npc == null) {
                player.sendMessage("<c>That contact is not in the game yet.");
                return;
            }
            AbiphoneIncomingCalls.Result result = AbiphoneIncomingCalls.ring(player, npc.getId(), npc::onIncomingPickUp);
            if (result != AbiphoneIncomingCalls.Result.RINGING) player.sendMessage("<c>The call was not placed: {}.", result.name());
        }, contactArg);
    }
}
