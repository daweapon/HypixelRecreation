package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record KuudraMobSpec(String id, String name, int tokens, int firstWave, @Nullable String spawn,
                            int bestiaryCap, int bestiaryBracket, Map<KuudraTier, Stats> stats) {

    public record Stats(int level, long health, long damage, int combatXP, int xpOrbs, int coins) {
        static Stats parse(Object yaml) {
            List<Object> values = Yamls.list(yaml);
            if (values.size() != 6) throw new IllegalArgumentException("Kuudra mob stats need 6 values: " + values);
            return new Stats(number(values, 0).intValue(), number(values, 1).longValue(), number(values, 2).longValue(),
                    number(values, 3).intValue(), number(values, 4).intValue(), number(values, 5).intValue());
        }

        private static Number number(List<Object> values, int index) {
            return (Number) values.get(index);
        }
    }

    static KuudraMobSpec parse(Map<String, Object> yaml) {
        Map<String, Object> bestiary = Yamls.map(yaml.get("bestiary"));
        return new KuudraMobSpec(
                Yamls.string(yaml, "id"),
                Yamls.string(yaml, "name"),
                Yamls.integer(yaml, "tokens", 0),
                Yamls.integer(yaml, "first_wave", 0),
                Yamls.string(yaml, "spawn"),
                Yamls.integer(bestiary, "cap", 0),
                Yamls.integer(bestiary, "bracket", 0),
                Yamls.byTier(yaml.get("stats"), Stats::parse));
    }

    public @Nullable Stats stats(KuudraTier tier) {
        return stats.get(tier);
    }

    public boolean inBestiary() {
        return bestiaryCap > 0;
    }
}
