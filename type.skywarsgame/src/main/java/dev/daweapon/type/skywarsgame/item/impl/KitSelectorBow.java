package dev.daweapon.type.skywarsgame.item.impl;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.gui.GUICageKitSelector;
import dev.daweapon.type.skywarsgame.item.SimpleInteractableItem;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;

public class KitSelectorBow extends SimpleInteractableItem {

    public KitSelectorBow() {
        super("kit_selector");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.BOW, 1, """
                <a>Kit Selector <7>(Right Click)
                <7>Right-click to select your kit!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(event.getPlayer());
        if (game != null && game.getGameStatus() == SkywarsGameStatus.WAITING) {
            SkywarsPlayer player = (SkywarsPlayer) event.getPlayer();
            String mode = game.getGameType().getModeString();
            new GUICageKitSelector(mode, game).open(player);
        }
    }
}
