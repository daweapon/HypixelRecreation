package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;

import java.util.Map;

public record KuudraTierSpec(KuudraTier tier, int number, int secondsPerWave, int requiredReputation,
                             int firstClearSkyBlockXP, int collectionPoints, int dailyReputation,
                             int armorCombatLevel, double krakenShards, int wanderingBlazeDps,
                             int tentacleDyeOdds, boolean stunRequired, boolean lairPhase) {

    static KuudraTierSpec parse(KuudraTier tier, Map<String, Object> yaml) {
        return new KuudraTierSpec(tier,
                Yamls.integer(yaml, "number", tier.ordinal() + 1),
                Yamls.integer(yaml, "seconds_per_wave", 0),
                Yamls.integer(yaml, "required_reputation", 0),
                Yamls.integer(yaml, "first_clear_skyblock_xp", 0),
                Yamls.integer(yaml, "collection_points", 0),
                Yamls.integer(yaml, "daily_reputation", 0),
                Yamls.integer(yaml, "armor_combat_level", 0),
                Yamls.decimal(yaml, "kraken_shards", 1),
                Yamls.integer(yaml, "wandering_blaze_dps", 0),
                Yamls.integer(yaml, "tentacle_dye_odds", 0),
                Yamls.bool(yaml, "stun_required"),
                Yamls.bool(yaml, "lair_phase"));
    }

    public double tentacleDyeChancePercent() {
        return tentacleDyeOdds <= 0 ? 0 : 100D / tentacleDyeOdds;
    }
}
