package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "updatepetlevel setpetlevel",
        description = "Sets the level of the held pet, or of the active pet when not holding one",
        usage = "/setpetlevel <level>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetPetLevel extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentInteger level = new ArgumentInteger("level");
        level.between(1, ItemAttributePetData.MAX_LEVEL);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            int target = context.get(level);

            SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
            if (held.hasComponent(PetComponent.class)) {
                player.updateItem(PlayerItemOrigin.MAIN_HAND, item -> applyLevel(item, target));
                player.sendMessage("<a>Updated the level of the pet to <e>{}<a>.", target);
                return;
            }

            SkyBlockItem active = player.getPetData().getEnabledPet();
            if (active == null) {
                player.sendMessage("<c>Hold a pet or summon one first.");
                return;
            }

            applyLevel(active, target);
            player.getPetData().updatePetEntityImpl(player);
            player.sendMessage("<a>Updated the level of your active pet to <e>{}<a>.", target);
        }, level);
    }

    private static void applyLevel(SkyBlockItem pet, int level) {
        ItemAttributePetData.PetData data = pet.getAttributeHandler().getPetData();
        data.setLevel(level, pet.getAttributeHandler().getRarity());
        pet.getAttributeHandler().setPetData(data);
    }
}
