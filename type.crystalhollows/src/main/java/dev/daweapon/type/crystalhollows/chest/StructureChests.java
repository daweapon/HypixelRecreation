package dev.daweapon.type.crystalhollows.chest;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.crystalhollows.quest.CrystalHollowsProgress;
import dev.daweapon.type.skyblockgeneric.crystalhollows.TreasureLoot;
import dev.daweapon.type.skyblockgeneric.crystalhollows.TreasurePool;
import dev.daweapon.type.skyblockgeneric.commission.CommissionProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class StructureChests {
    public static final Path FILE = Path.of("./configuration/skyblock/crystal_hollows/chests.yml");
    private static final Set<String> OPENED = ConcurrentHashMap.newKeySet();

    public record ChestSpec(String id, String structure, String anchor, TreasurePool pool, boolean requiresJungleKey) {
        public Pos position() {
            return CrystalHollowsLayout.anchor(structure, anchor);
        }
    }

    private static volatile List<ChestSpec> specs;

    private StructureChests() {
    }

    public static List<ChestSpec> specs() {
        List<ChestSpec> loaded = specs;
        if (loaded == null) specs = loaded = read();
        return loaded;
    }

    public static void reload() {
        specs = null;
    }

    @SuppressWarnings("unchecked")
    private static List<ChestSpec> read() {
        if (!Files.exists(FILE)) return List.of();
        try (InputStream in = Files.newInputStream(FILE)) {
            Map<String, Object> root = new Yaml().load(in);
            List<ChestSpec> list = new ArrayList<>();
            for (Map<String, Object> entry : (List<Map<String, Object>>) root.getOrDefault("chests", List.of())) {
                list.add(new ChestSpec((String) entry.get("id"), (String) entry.get("structure"),
                        (String) entry.get("anchor"), TreasurePool.valueOf((String) entry.get("pool")),
                        Boolean.TRUE.equals(entry.get("requires_jungle_key"))));
            }
            return List.copyOf(list);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
    }

    public static ChestSpec at(Point point) {
        for (ChestSpec spec : specs()) {
            Pos position = spec.position();
            if (position != null && position.blockX() == point.blockX() && position.blockY() == point.blockY()
                    && position.blockZ() == point.blockZ()) return spec;
        }
        return null;
    }

    public static void open(SkyBlockPlayer player, ChestSpec spec) {
        if (spec.requiresJungleKey() && !CrystalHollowsProgress.hasActiveJungleKey(player.getUuid())) {
            player.sendMessage(Text.of("<c>The chest is sealed. You need an active Jungle Key to open it!"));
            return;
        }
        if (!OPENED.add(player.getUuid() + ":" + spec.id())) {
            player.sendMessage(Text.of("<c>You have already looted this chest!"));
            return;
        }
        Random lootRandom = new Random(CrystalHollowsLayout.seed() ^ spec.id().hashCode());
        TreasureLoot.open(player, spec.pool(), lootRandom, ThreadLocalRandom.current());
        CommissionProgress.recordChestOpened(player);
    }
}
