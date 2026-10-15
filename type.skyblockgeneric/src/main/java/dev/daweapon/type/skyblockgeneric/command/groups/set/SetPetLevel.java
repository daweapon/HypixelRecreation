package dev.daweapon.type.skyblockgeneric.command.groups.set;

import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import dev.daweapon.type.skyblockgeneric.user.pets.PetScore;

@CommandParameters(labels = "petlevel",
        description = "Sets the level of the held pet, or of the active pet when not holding one",
        usage = "/set petlevel <level>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetPetLevel extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentInteger level = new ArgumentInteger("level");
        level.between(1, ItemAttributePetData.HIGHEST_MAX_LEVEL);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            int target = context.get(level);

            SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
            if (held.hasComponent(PetComponent.class)) {
                int applied = Math.min(target, PetLevels.maxLevel(held));
                player.updateItem(PlayerItemOrigin.MAIN_HAND, item -> PetLevels.setLevel(item, applied));
                player.sendMessage("<a>Updated the level of the pet to <e>{}<a>.", applied);
                return;
            }

            SkyBlockItem active = player.getPetData().getEnabledPet();
            if (active == null) {
                player.sendMessage("<c>Hold a pet or summon one first.");
                return;
            }

            int applied = Math.min(target, PetLevels.maxLevel(active));
            PetLevels.setLevel(active, applied);
            player.getPetData().refreshCachedAbilities();
            player.getPetData().updatePetEntityImpl(player);
            PetScore.syncSkyBlockXP(player);
            player.sendMessage("<a>Updated the level of your active pet to <e>{}<a>.", applied);
        }, level);
    }
}
