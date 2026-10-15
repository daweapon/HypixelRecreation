package dev.daweapon.type.skyblockgeneric.garden.tool;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class OverclockerPity {
    public enum Source {
        VISITORS(0, 0),
        CONTESTS(0, 0),
        CROPS(0, 0),
        GREENHOUSE(0.0005, 4_000),
        PESTS(0, 0);

        private final double baseChance;
        private final int guaranteeAfter;

        Source(double baseChance, int guaranteeAfter) {
            this.baseChance = baseChance;
            this.guaranteeAfter = guaranteeAfter;
        }

        public boolean active() {
            return guaranteeAfter > 0;
        }
    }

    private final Map<Source, Integer> counters = new EnumMap<>(Source.class);

    public int counter(Source source) {
        return counters.getOrDefault(source, 0);
    }

    public double progress(Source source) {
        return source.active() ? chance(source, counter(source)) : 0;
    }

    public static double chance(Source source, int counter) {
        if (!source.active()) return 0;
        return Math.min(1, Math.max(source.baseChance, (double) counter / source.guaranteeAfter));
    }

    public boolean activity(Source source, @Nullable java.util.UUID player, RandomGenerator random) {
        if (!source.active()) return false;
        int counter = counter(source) + 1;
        double chance = chance(source, counter);
        if (LootRoll.roll(player, ItemType.OVERCLOCKER_3000, chance * 100, random)) {
            counters.put(source, 0);
            return true;
        }
        counters.put(source, counter);
        return false;
    }

    public static void record(SkyBlockPlayer player, GardenProfile profile, Source source) {
        if (!profile.overclockerPity().activity(source, player.getUuid(), ThreadLocalRandom.current())) return;
        player.addAndUpdateItem(new SkyBlockItem(ItemType.OVERCLOCKER_3000));
        player.sendMessage("<6><l>ABOUT TIME!</l> <e>You find an <6>Overclocker 3000<e>!");
    }

    public static void onActivity(SkyBlockPlayer player, Source source) {
        GardenProfile profile = Gardens.profile(player);
        record(player, profile, source);
        Gardens.save(player, profile);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        counters.forEach((source, value) -> {
            if (value > 0) json.put(source.name(), value);
        });
        return json;
    }

    public static OverclockerPity fromJson(@Nullable JSONObject json) {
        OverclockerPity pity = new OverclockerPity();
        if (json == null) return pity;
        for (String key : json.keySet()) {
            try {
                pity.counters.put(Source.valueOf(key), json.getInt(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return pity;
    }
}
