package dev.daweapon.type.skyblockgeneric.hunting.creature.trigger;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureCatalog;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpawner;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

final class TriggerSupport {
    private TriggerSupport() {
    }

    static List<CreatureSpec> specs(String triggerType) {
        if (HypixelConst.getTypeLoader() == null) return List.of();
        return CreatureCatalog.forIsland(CreatureSpawner.islandOf(HypixelConst.getTypeLoader().getType())).stream()
                .filter(spec -> spec.spawn().trigger() != null && spec.spawn().trigger().type().equals(triggerType))
                .toList();
    }

    static boolean hasRoom(CreatureSpec spec) {
        return CreatureSpawner.aliveCount(spec.id()) < spec.spawn().max();
    }

    static @Nullable ItemType shardItem(CreatureSpec spec) {
        if (spec.shard() == null) return null;
        return AttributeRegistry.findByShard(spec.shard())
                .map(AttributeShardComponent::itemType)
                .filter(type -> type != ItemType.ATTRIBUTE_SHARD)
                .orElse(null);
    }

    static boolean roll(@Nullable UUID player, CreatureSpec spec, double chancePercent) {
        return LootRoll.roll(player, shardItem(spec), chancePercent);
    }

    static @Nullable HuntingCreatureMob spawn(CreatureSpec spec, Instance instance, Pos position) {
        if (!hasRoom(spec)) return null;
        HuntingCreatureMob mob = CreatureSpawner.spawnAt(spec, instance, position);
        applyHealthVariant(spec, mob);
        return mob;
    }

    static Pos scatter(Instance instance, Pos center, CreatureSpec spec, double radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        boolean water = spec.medium() == CreatureSpec.Medium.WATER;
        for (int attempt = 0; attempt < 6; attempt++) {
            int x = (int) Math.floor(center.x() + random.nextDouble(-radius, radius));
            int z = (int) Math.floor(center.z() + random.nextDouble(-radius, radius));
            if (!instance.isChunkLoaded(x >> 4, z >> 4)) continue;
            Pos surface = CreatureSpawner.surface(instance, x, z, water, center.blockY() + 6);
            if (surface == null) continue;
            return spec.medium() == CreatureSpec.Medium.AIR ? surface.add(0, 3, 0) : surface;
        }
        return spec.medium() == CreatureSpec.Medium.AIR ? center.add(0, 3, 0) : center;
    }

    static boolean playerNear(Instance instance, Pos position) {
        return dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader.getLoadedPlayers().stream()
                .anyMatch(player -> player.getInstance() == instance
                        && player.getPosition().distance(position) < CreatureSpawner.DESPAWN_RANGE);
    }

    static @Nullable Text message(CreatureSpec spec, String when, String... replacements) {
        for (CreatureSpec.Message message : spec.messages()) {
            if (!message.when().equals(when)) continue;
            String text = message.text();
            for (int i = 0; i + 1 < replacements.length; i += 2) text = text.replace(replacements[i], replacements[i + 1]);
            return Text.parse(text);
        }
        return null;
    }

    static void tell(SkyBlockPlayer player, CreatureSpec spec, String when) {
        Text text = message(spec, when);
        if (text != null) player.sendMessage(text);
    }

    @SuppressWarnings("unchecked")
    private static void applyHealthVariant(CreatureSpec spec, HuntingCreatureMob mob) {
        if (!(spec.raw().get("healthVariants") instanceof List<?> variants) || variants.isEmpty()) return;
        Object pick = variants.get(ThreadLocalRandom.current().nextInt(variants.size()));
        if (pick instanceof java.util.Map<?, ?> map && map.get("health") instanceof Number health) {
            mob.setMaxHealth(health.floatValue());
        }
    }
}
