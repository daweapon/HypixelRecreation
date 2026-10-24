package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneCalls;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContact;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactCatalogue;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

@CommandParameters(description = "Call a contact on one of your Abiphones", usage = "/call <contact>", permission = Rank.DEFAULT,
        labels = "call", allowsConsole = false)
public class CallCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord contactArg = ArgumentType.Word("contact").from(AbiphoneContactCatalogue.all().stream()
                .flatMap(contact -> contact.aliases().stream()).toArray(String[]::new));

        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            sender.sendMessage("Usage: /call <contact>");
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            AbiphoneContact contact = AbiphoneContactCatalogue.byAlias(context.get(contactArg)).orElse(null);
            AbiphoneNPC npc = contact == null ? null : AbiphoneRegistry.getFromId(contact.id());
            if (npc == null) {
                player.sendMessage("<c>You don't have a contact with that name on any of your Abiphones!");
                return;
            }
            AbiphoneCalls.phoneWith(player, npc).ifPresentOrElse(
                    phone -> AbiphoneCalls.call(player, phone, npc),
                    () -> player.sendMessage("<c>{} is not a contact on any of your Abiphones!", npc.getName()));
        }, contactArg);
    }
}
