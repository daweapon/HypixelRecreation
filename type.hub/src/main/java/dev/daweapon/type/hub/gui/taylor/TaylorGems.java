package dev.daweapon.type.hub.gui.taylor;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class TaylorGems {
    private TaylorGems() {
    }

    static <S> void slot(ViewLayout<S> layout, int slot) {
        layout.slot(slot, (s, c) -> item((SkyBlockPlayer) c.player()),
                (click, c) -> c.player().sendMessage("<a>Gems are handed out by staff on this server."));
    }

    private static ItemStack.Builder item(SkyBlockPlayer player) {
        return ItemStacks.item(Material.EMERALD, """
                <a>SkyBlock Gems
                <7>Use SkyBlock Gems to purchase:
                <7> - <6>Booster Cookies
                <7> - <c>Fire Sales
                <7> - <b>Taylor's Cosmetics
                <7> - <a>SkyMart Barn & Greenhouse Skins
                <7> - <d>Account & Profile Upgrades

                <7>You have: <a>{} Gems
                <8>Gems are handed out by staff on
                <8>this server.""", StringUtility.commaify(player.getGems()));
    }
}
