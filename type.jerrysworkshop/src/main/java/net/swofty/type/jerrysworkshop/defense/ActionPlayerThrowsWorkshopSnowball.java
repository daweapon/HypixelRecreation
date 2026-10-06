package net.swofty.type.jerrysworkshop.defense;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Throws a snowball during an active "Defend Jerry's Workshop" wave - the only way the Magma Cubes
// take damage. A held Snowball is used up; Frosty the Snow Cannon fires repeatedly instead. Its
// ammo count is not modelled, so the cannon never runs dry.
public class ActionPlayerThrowsWorkshopSnowball implements HypixelEventClass {
    private static final long CANNON_COOLDOWN_MS = 250;
    private static final Map<UUID, Long> lastCannonShot = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        if (JerryWorkshopDefenseManager.getPhase() != JerryWorkshopDefenseManager.Phase.WAVE_ACTIVE) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ItemStack held = event.getItemStack();

        if (held.material() == Material.SNOWBALL) {
            event.setCancelled(true);
            shoot(player);
            int heldSlot = player.getHeldSlot();
            ItemStack currentItem = player.getInventory().getItemStack(heldSlot);
            player.getInventory().setItemStack(heldSlot,
                    currentItem.amount() > 1 ? currentItem.withAmount(currentItem.amount() - 1) : ItemStack.AIR);
            return;
        }

        if (new SkyBlockItem(held).getAttributeHandler().getPotentialType() != ItemType.SNOW_CANNON) return;
        event.setCancelled(true);
        long now = System.currentTimeMillis();
        Long last = lastCannonShot.get(player.getUuid());
        if (last != null && now - last < CANNON_COOLDOWN_MS) return;
        lastCannonShot.put(player.getUuid(), now);
        shoot(player);
    }

    private static void shoot(SkyBlockPlayer player) {
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
    }
}
