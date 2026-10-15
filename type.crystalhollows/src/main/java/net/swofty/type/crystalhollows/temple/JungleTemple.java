package net.swofty.type.crystalhollows.temple;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.text.Text;
import net.swofty.type.crystalhollows.config.ConfigReader;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JungleTemple {
    public static final String STRUCTURE = "jungle_temple";
    public static final Path FILE = Path.of("./configuration/skyblock/crystal_hollows/parkours.yml");

    private static final int EXIT_LEVER_X = 53;
    private static final int EXIT_LEVER_Y = 6;
    private static final int EXIT_LEVER_Z = 95;

    private record Cache(long seed, Pos origin, List<Parkour> parkours) {
    }

    private static final Map<UUID, ParkourState> STATES = new ConcurrentHashMap<>();
    private static volatile List<Parkour> templates;
    private static volatile Cache cache;

    private JungleTemple() {
    }

    public static void reload() {
        templates = null;
        cache = null;
    }

    public static List<Parkour> read(Path file) {
        List<Parkour> list = new ArrayList<>();
        for (Map<String, Object> entry : ConfigReader.list(ConfigReader.read(file), "parkours")) {
            List<Parkour.Checkpoint> checkpoints = new ArrayList<>();
            for (Map<String, Object> checkpoint : ConfigReader.list(entry, "checkpoints")) {
                checkpoints.add(new Parkour.Checkpoint(ConfigReader.box(ConfigReader.map(checkpoint, "box")),
                        ConfigReader.pos(ConfigReader.map(checkpoint, "respawn"))));
            }
            list.add(new Parkour(String.valueOf(entry.get("id")), ConfigReader.box(ConfigReader.map(entry, "start")),
                    ConfigReader.pos(ConfigReader.map(entry, "respawn")), List.copyOf(checkpoints),
                    ConfigReader.box(ConfigReader.map(entry, "finish"))));
        }
        return List.copyOf(list);
    }

    public static List<Parkour> parkours() {
        Pos origin = CrystalHollowsLayout.origin(STRUCTURE);
        if (origin == null) return List.of();
        long seed = CrystalHollowsLayout.seed();
        Cache cached = cache;
        if (cached != null && cached.seed() == seed && cached.origin().equals(origin)) return cached.parkours();
        List<Parkour> base = templates;
        if (base == null) templates = base = read(FILE);
        List<Parkour> shifted = base.stream().map(parkour -> parkour.shift(origin)).toList();
        cache = new Cache(seed, origin, shifted);
        return shifted;
    }

    public static boolean isExitLever(Point block) {
        Pos origin = CrystalHollowsLayout.origin(STRUCTURE);
        return origin != null && block.blockX() == origin.blockX() + EXIT_LEVER_X
                && block.blockY() == origin.blockY() + EXIT_LEVER_Y
                && block.blockZ() == origin.blockZ() + EXIT_LEVER_Z;
    }

    public static void useExitLever(SkyBlockPlayer player) {
        Pos target = CrystalHollowsLayout.anchor(STRUCTURE, "entrance");
        if (target == null) return;
        reset(player.getUuid());
        player.teleport(target);
        player.sendMessage(Text.of("<7>The lever pulls you out of the temple."));
    }

    public static ParkourState state(UUID player) {
        return STATES.computeIfAbsent(player, _ -> new ParkourState());
    }

    public static void reset(UUID player) {
        STATES.remove(player);
    }

    public static boolean allParkoursComplete(UUID player) {
        return state(player).allComplete(parkours());
    }

    public static void onMove(SkyBlockPlayer player, Pos at) {
        if (player.getActiveEffects().stream().anyMatch(timed -> timed.potion().effect() == PotionEffect.JUMP_BOOST)) {
            player.removeEffect(PotionEffect.JUMP_BOOST);
        }
        List<Parkour> parkours = parkours();
        if (parkours.isEmpty()) return;
        ParkourState state = state(player.getUuid());
        for (ParkourState.Event event : state.update(parkours, at)) {
            switch (event) {
                case ParkourState.Started started -> {
                    player.sendMessage(Text.of("<a>Parkour started! Don't touch the lava."));
                }
                case ParkourState.CheckpointReached reached -> player.sendMessage(Text.of("<7>Checkpoint reached."));
                case ParkourState.Completed completed -> player.sendMessage(Text.of("<a><l>PARKOUR COMPLETE! <7>({}/{})",
                        state.completedCount(), parkours.size()));
            }
        }
        if (isLava(player, at)) respawn(player, state);
    }

    private static boolean isLava(Player player, Pos at) {
        return player.getInstance() != null && player.getInstance().getBlock(at).compare(Block.LAVA);
    }

    private static void respawn(SkyBlockPlayer player, ParkourState state) {
        Pos target = state.respawn();
        if (target == null) target = CrystalHollowsLayout.anchor(STRUCTURE, "entrance");
        if (target == null) return;
        player.teleport(target);
        player.sendMessage(Text.of("<c>You touched the lava!"));
    }
}
