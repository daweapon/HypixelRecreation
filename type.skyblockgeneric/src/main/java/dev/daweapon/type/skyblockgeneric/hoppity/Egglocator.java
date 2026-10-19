package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Egglocator {
    private static final long COOLDOWN_MS = 5_000L;
    private static final double STEP = 0.5;
    private static final double MAX_TRAIL = 30;
    private static final Map<UUID, Long> LAST_USED = new ConcurrentHashMap<>();

    private Egglocator() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register("EGGLOCATOR", InteractableItemConfig.builder()
                .rightClickHandler(Egglocator::use).build());
    }

    private static void use(SkyBlockPlayer player, SkyBlockItem item) {
        long now = System.currentTimeMillis();
        Long last = LAST_USED.get(player.getUuid());
        if (last != null && now - last < COOLDOWN_MS) {
            player.sendMessage("<c>This ability is on cooldown for {}s.", (COOLDOWN_MS - (now - last) + 999) / 1000);
            return;
        }
        if (!HuntEggManager.isRunning()) {
            player.sendMessage("<c>This island doesn't spawn Chocolate Rabbit Eggs!");
            return;
        }
        if (!HuntEggSchedule.isHuntActive(SkyBlockCalendar.getElapsed())) {
            player.sendMessage("<c>The Egglocator only works during Hoppity's Hunt!");
            return;
        }
        LAST_USED.put(player.getUuid(), now);
        ActiveEgg egg = HuntEggManager.nearestUnclaimed(player);
        if (egg == null) {
            player.sendMessage("<c>There are no Chocolate Rabbit Eggs nearby! Try again later!");
            return;
        }
        trail(player, HuntEggManager.position(egg));
        player.playSound(Sound.sound(Key.key("block.note_block.harp"), Sound.Source.PLAYER, 1f, 1.5f));
    }

    public static void trail(SkyBlockPlayer player, Pos target) {
        Vec origin = player.getPosition().add(0, player.getEyeHeight(), 0).asVec();
        Vec toTarget = new Vec(target.x(), target.y() + 0.5, target.z()).sub(origin);
        double distance = toTarget.length();
        if (distance < 0.001) return;
        Vec step = toTarget.normalize().mul(STEP);
        Vec cursor = origin;
        for (double travelled = 0; travelled < Math.min(distance, MAX_TRAIL); travelled += STEP) {
            cursor = cursor.add(step);
            player.sendPacket(new ParticlePacket(Particle.HAPPY_VILLAGER, false, true,
                    (float) cursor.x(), (float) cursor.y(), (float) cursor.z(), 0f, 0f, 0f, 0f, 1));
        }
    }
}
