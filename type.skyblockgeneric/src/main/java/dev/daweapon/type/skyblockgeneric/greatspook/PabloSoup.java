package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class PabloSoup {
    private static final String SPEAKER = "<e>[NPC] <5>Pablo<f>: ";

    private PabloSoup() {
    }

    public static boolean trade(SkyBlockPlayer player) {
        if (!GreatSpookEvent.isActive()) return false;
        if (!SpookNpcIngredients.isSoup(WornArmor.id(player.getItemInMainHand()))) return false;
        TimedConsumables.consumeHeld(player, new SkyBlockItem(player.getItemInMainHand()));
        player.sendMessage(SPEAKER + "Thank you so much.");
        player.sendMessage(SPEAKER + "Here, I want you to have another painting.");
        player.addAndUpdateItem(new SkyBlockItem(ItemType.THE_SOUP_PAINTING));
        player.sendMessage("<a>You claimed a <o>The Soup<r><a>!");
        return true;
    }
}
