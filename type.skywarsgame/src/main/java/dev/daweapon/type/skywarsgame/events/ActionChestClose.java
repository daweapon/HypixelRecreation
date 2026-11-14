package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

public class ActionChestClose implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;

        AbstractInventory closedInventory = event.getInventory();
        if (!(closedInventory instanceof Inventory)) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getGameStatus() != SkywarsGameStatus.IN_PROGRESS) return;

        Pos chestPos = game.getChestManager().getChestPositionForInventory((Inventory) closedInventory);
        if (chestPos != null) {
            game.getChestManager().onChestClosed(chestPos);
        }
    }
}
