package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spider's Web-Weaver: a mob the player hits is slowed by {0}% for a few seconds.
 */
public final class WebWeaverHandler implements PetAbilityHandler {
    private static final Key MODIFIER = Key.key("skyblock", "spider_web_weaver");
    private static final long SLOW_MS = 3_000;
    private static final Map<UUID, Long> SLOWED_UNTIL = new ConcurrentHashMap<>();

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.MeleeHit hit)) return;
        double slow = Math.max(0, Math.min(100, context.value(0))) / 100D;
        if (slow <= 0) return;
        SkyBlockMob mob = hit.mob();
        AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);
        speed.removeModifier(MODIFIER);
        speed.addModifier(new AttributeModifier(MODIFIER, -slow, AttributeOperation.ADD_MULTIPLIED_TOTAL));
        SLOWED_UNTIL.put(mob.getUuid(), System.currentTimeMillis() + SLOW_MS);
        MinecraftServer.getSchedulerManager().scheduleTask(() -> release(mob), TaskSchedule.millis(SLOW_MS),
                TaskSchedule.stop());
    }

    private static void release(SkyBlockMob mob) {
        Long until = SLOWED_UNTIL.get(mob.getUuid());
        if (until != null && System.currentTimeMillis() < until) return;
        SLOWED_UNTIL.remove(mob.getUuid());
        if (!mob.isRemoved()) mob.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(MODIFIER);
    }
}
