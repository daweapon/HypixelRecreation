package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class ShardsByMaxHpSpecial implements CreatureSpecial {
    record Variant(double health, int shards) {
    }

    static int shardsFor(List<Variant> variants, double maxHealth, int fallback) {
        int shards = fallback;
        double best = -1;
        for (Variant variant : variants) {
            if (variant.health() <= maxHealth && variant.health() > best) {
                best = variant.health();
                shards = variant.shards();
            }
        }
        return shards;
    }

    private static List<Variant> variants(HuntingCreatureMob mob) {
        List<Variant> result = new ArrayList<>();
        if (!(mob.spec().raw().get("healthVariants") instanceof List<?> rows)) return result;
        for (Object row : rows) {
            if (row instanceof Map<?, ?> map && map.get("health") instanceof Number health && map.get("shards") instanceof Number shards) {
                result.add(new Variant(health.doubleValue(), shards.intValue()));
            }
        }
        return result;
    }

    @Override
    public void onSpawn(HuntingCreatureMob mob) {
        List<Variant> variants = variants(mob);
        if (variants.isEmpty()) return;
        mob.setMaxHealth((float) variants.get(ThreadLocalRandom.current().nextInt(variants.size())).health());
    }

    @Override
    public int shardAmount(HuntingCreatureMob mob, int base) {
        return base * shardsFor(variants(mob), mob.getMaxHealth(), 1);
    }
}
