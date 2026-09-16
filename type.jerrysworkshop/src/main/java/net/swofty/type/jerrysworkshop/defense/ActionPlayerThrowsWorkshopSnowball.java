package net.swofty.type.jerrysworkshop.defense;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Throws a snowball at a WorkshopDefenseMob when a player right-clicks with one in hand during an
// active "Defend Jerry's Workshop" wave (Roadmap #20) - the only way these mobs take damage.
public class ActionPlayerThrowsWorkshopSnowball implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        if (event.getItemStack().material() != Material.SNOWBALL) return;
        if (JerryWorkshopDefenseManager.getPhase() != JerryWorkshopDefenseManager.Phase.WAVE_ACTIVE) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setCancelled(true);

        Pos playerPos = player.getPosition();
        double yaw = Math.toRadians(playerPos.yaw());
        double pitch = Math.toRadians(playerPos.pitch());

        double power = 1.4;
        double vx = -Math.sin(yaw) * Math.cos(pitch) * power;
        double vy = -Math.sin(pitch) * power;
        double vz = Math.cos(yaw) * Math.cos(pitch) * power;

        SnowballEntityImpl snowball = new SnowballEntityImpl(player);
        snowball.setVelocity(new Vec(vx, vy, vz).mul(20));
        snowball.setInstance(player.getInstance(), playerPos.add(0, 1.5, 0));

        int heldSlot = player.getHeldSlot();
        ItemStack currentItem = player.getInventory().getItemStack(heldSlot);
        if (currentItem.amount() > 1) {
            player.getInventory().setItemStack(heldSlot, currentItem.withAmount(currentItem.amount() - 1));
        } else {
            player.getInventory().setItemStack(heldSlot, ItemStack.AIR);
        }
    }
}
