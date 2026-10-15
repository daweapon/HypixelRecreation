package dev.daweapon.type.skywarsgame.item.impl;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.gui.GUISpectatorTeleporter;
import dev.daweapon.type.skywarsgame.item.SimpleInteractableItem;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;

public class SpectatorCompass extends SimpleInteractableItem {

    public SpectatorCompass() {
        super("spectator_compass");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.COMPASS, 1, """
                <a>Teleporter <7>(Right Click)
                <7>Right-click to teleport to players!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        SkywarsPlayer player = (SkywarsPlayer) event.getPlayer();
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game != null) {
            new GUISpectatorTeleporter(game).open(player);
        }
    }
}
