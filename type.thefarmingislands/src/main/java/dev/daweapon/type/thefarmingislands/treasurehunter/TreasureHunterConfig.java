package dev.daweapon.type.thefarmingislands.treasurehunter;

import dev.daweapon.commons.skyblock.item.ItemType;
import net.minestom.server.coordinate.Pos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record TreasureHunterConfig(List<Tier> tiers, List<Hint> hints) {
    public static final Path FILE = Path.of("./configuration/skyblock/treasure_hunter.yml");
    private static TreasureHunterConfig loaded;

    public record Tier(int fee, double weight, List<Reward> rewards) {
    }

    public record Reward(ItemType item, int amount, double weight) {
    }

    public record Hint(String text, Pos position) {
    }

    public static synchronized TreasureHunterConfig get() {
        if (loaded == null) loaded = load(FILE);
        return loaded;
    }

    public static TreasureHunterConfig load(Path file) {
        try (InputStream stream = Files.newInputStream(file)) {
            Map<?, ?> root = dev.daweapon.commons.YamlFileUtils.loadYaml(file.toFile());
            List<Tier> tiers = new ArrayList<>();
            for (Object tierEntry : (List<?>) root.get("tiers")) {
                Map<?, ?> tier = (Map<?, ?>) tierEntry;
                List<Reward> rewards = new ArrayList<>();
                for (Object rewardEntry : (List<?>) tier.get("rewards")) {
                    Map<?, ?> reward = (Map<?, ?>) rewardEntry;
                    rewards.add(new Reward(ItemType.get((String) reward.get("item")),
                            ((Number) reward.get("amount")).intValue(), ((Number) reward.get("weight")).doubleValue()));
                }
                tiers.add(new Tier(((Number) tier.get("fee")).intValue(), ((Number) tier.get("weight")).doubleValue(), rewards));
            }
            List<Hint> hints = new ArrayList<>();
            for (Object hintEntry : (List<?>) root.get("hints")) {
                Map<?, ?> hint = (Map<?, ?>) hintEntry;
                List<?> xyz = (List<?>) hint.get("pos");
                hints.add(new Hint((String) hint.get("text"), new Pos(((Number) xyz.get(0)).doubleValue(),
                        ((Number) xyz.get(1)).doubleValue(), ((Number) xyz.get(2)).doubleValue())));
            }
            return new TreasureHunterConfig(tiers, hints);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + file, exception);
        }
    }
}
