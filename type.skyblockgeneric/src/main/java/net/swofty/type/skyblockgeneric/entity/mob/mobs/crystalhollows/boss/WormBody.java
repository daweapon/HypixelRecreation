package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.gui.inventory.ItemStacks;

import java.util.ArrayList;
import java.util.List;

final class WormBody {
    static final int SEGMENTS = 14;
    static final int SPACING = 2;
    private static final double LIFT = 0.85;
    private static final double WAVE_HEIGHT = 0.25;
    private static final double WAVE_SPEED = 0.35;
    private static final double WAVE_PHASE = 0.7;

    private final List<LivingEntity> segments = new ArrayList<>();

    void spawn(Instance instance, Pos at, String texture) {
        for (int i = 0; i < SEGMENTS; i++) {
            LivingEntity segment = new LivingEntity(EntityType.ARMOR_STAND);
            segment.setInvisible(true);
            segment.setNoGravity(true);
            segment.setInvulnerable(true);
            segment.editEntityMeta(ArmorStandMeta.class, meta -> {
                meta.setSmall(true);
                meta.setMarker(true);
                meta.setHasNoBasePlate(true);
            });
            segment.setHelmet(ItemStacks.head(texture, "").build());
            segment.setInstance(instance, at);
            segments.add(segment);
        }
    }

    void follow(List<Pos> trail, long ticks) {
        if (trail.isEmpty()) return;
        for (int i = 0; i < segments.size(); i++) {
            Pos anchor = trail.get(Math.min((i + 1) * SPACING, trail.size() - 1));
            double wave = Math.sin(ticks * WAVE_SPEED - i * WAVE_PHASE) * WAVE_HEIGHT;
            LivingEntity segment = segments.get(i);
            if (!segment.isRemoved()) segment.teleport(anchor.add(0, LIFT + wave, 0));
        }
    }

    void remove() {
        segments.forEach(segment -> {
            if (!segment.isRemoved()) segment.remove();
        });
        segments.clear();
    }
}
