package dev.daweapon.type.hub.gui.carnival;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class CarnivalTokensDisplay {
    private CarnivalTokensDisplay() {}

    static ItemStack.Builder of(SkyBlockPlayer player) {
        return ItemStacks.item(Material.SUNFLOWER, "<e>Carnival Tokens\n"
                + "<7>Your Tokens: <a>" + StringUtility.commaify(Carnivals.profile(player).getTokens()) + "\n\n"
                + "<7>Earn these by playing <e>Carnival\n"
                + "<7>games and scoring <a>points<7>!");
    }
}
