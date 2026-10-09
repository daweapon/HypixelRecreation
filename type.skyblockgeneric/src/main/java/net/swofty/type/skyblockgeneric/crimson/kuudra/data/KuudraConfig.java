package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Every Kuudra definition under {@code configuration/skyblock/kuudra/}, loaded once on first use. */
public record KuudraConfig(int areaCombatLevel, int maxWave, int routeUnlockTokens, int tentacleTokens,
                           Map<KuudraTier, KuudraTierSpec> tiers, List<KuudraWave> waves, List<KuudraMobSpec> mobs,
                           KuudraPerkShop perkShop, KuudraDialogue dialogue, KuudraLootTable loot) {

    public static final Path DIRECTORY = Path.of("./configuration/skyblock/kuudra");
    private static volatile KuudraConfig loaded;

    public static KuudraConfig get() {
        KuudraConfig current = loaded;
        if (current != null) return current;
        synchronized (KuudraConfig.class) {
            if (loaded == null) loaded = load(DIRECTORY);
            return loaded;
        }
    }

    public static KuudraConfig load(Path directory) {
        Map<String, Object> tiersYaml = Yamls.load(directory.resolve("tiers.yml"));
        List<KuudraWave> waves = new ArrayList<>();
        List<Object> waveYaml = Yamls.list(tiersYaml.get("waves"));
        for (int i = 0; i < waveYaml.size(); i++) waves.add(KuudraWave.parse(i + 1, Yamls.strings(waveYaml.get(i))));

        Map<KuudraTier, KuudraTierSpec> tiers = new EnumMap<>(KuudraTier.class);
        Yamls.map(tiersYaml.get("tiers")).forEach((name, spec) -> {
            KuudraTier tier = KuudraTier.valueOf(name);
            tiers.put(tier, KuudraTierSpec.parse(tier, Yamls.map(spec)));
        });

        return new KuudraConfig(
                Yamls.integer(tiersYaml, "area_combat_level", 0),
                Yamls.integer(tiersYaml, "max_wave", 0),
                Yamls.integer(tiersYaml, "route_unlock_tokens", 0),
                Yamls.integer(tiersYaml, "tentacle_tokens", 0),
                Map.copyOf(tiers),
                List.copyOf(waves),
                Yamls.maps(Yamls.load(directory.resolve("mobs.yml")).get("mobs")).stream().map(KuudraMobSpec::parse).toList(),
                KuudraPerkShop.parse(Yamls.load(directory.resolve("perks.yml"))),
                KuudraDialogue.parse(Yamls.load(directory.resolve("dialogue.yml"))),
                KuudraLootTable.parse(Yamls.load(directory.resolve("loot.yml"))));
    }

    public KuudraTierSpec tier(KuudraTier tier) {
        KuudraTierSpec spec = tiers.get(tier);
        if (spec == null) throw new IllegalStateException("tiers.yml has no " + tier);
        return spec;
    }

    public @Nullable KuudraMobSpec mob(String id) {
        return mobs.stream().filter(mob -> mob.id().equals(id)).findFirst().orElse(null);
    }

    /** The wave's spawn list; waves past the documented 20 repeat them in order. */
    public KuudraWave wave(int number) {
        return waves.get((number - 1) % waves.size());
    }
}
