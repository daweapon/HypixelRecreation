package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorData;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class EphemeralGratitude {
    private EphemeralGratitude() {
    }

    public static void award(SkyBlockPlayer player, VisitorData.Present present) {
        if (!GreatSpookEvent.isActive()) return;
        if (!SpookNpcIngredients.acceptedQuickly(System.currentTimeMillis(), present.getArrivedAt())) return;
        player.addAndUpdateItem(new SkyBlockItem(ItemType.EPHEMERAL_GRATITUDE));
        player.sendMessage("<b><l>THANKFUL! <7>You received an <a>Ephemeral Gratitude <7>for accepting a commission so quickly!");
    }
}
