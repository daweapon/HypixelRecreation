package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class ShaftCorpses {
    private static final Map<Instance, ShaftCorpses> SHAFTS = Collections.synchronizedMap(new WeakHashMap<>());

    public record Corpse(int index, CorpseType type, Pos position, Entity stand, InteractionEntity click) {
    }

    private final String layoutCode;
    private final UUID owner;
    private final List<Corpse> corpses = new ArrayList<>();
    private final Map<UUID, Set<Integer>> looted = new ConcurrentHashMap<>();

    ShaftCorpses(String layoutCode, UUID owner) {
        this.layoutCode = layoutCode;
        this.owner = owner;
    }

    static void register(Instance instance, ShaftCorpses shaft) {
        SHAFTS.put(instance, shaft);
    }

    static List<Map.Entry<Instance, ShaftCorpses>> all() {
        synchronized (SHAFTS) {
            return new ArrayList<>(SHAFTS.entrySet());
        }
    }

    public static Optional<ShaftCorpses> of(@Nullable Instance instance) {
        return instance == null ? Optional.empty() : Optional.ofNullable(SHAFTS.get(instance));
    }

    public String layoutCode() {
        return layoutCode;
    }

    public UUID owner() {
        return owner;
    }

    public List<Corpse> corpses() {
        return List.copyOf(corpses);
    }

    void add(Corpse corpse) {
        corpses.add(corpse);
    }

    public boolean isLooted(UUID player, Corpse corpse) {
        return looted.getOrDefault(player, Set.of()).contains(corpse.index());
    }

    void markLooted(UUID player, Corpse corpse) {
        looted.computeIfAbsent(player, ignored -> new HashSet<>()).add(corpse.index());
    }

    public List<Text> widgetLines(SkyBlockPlayer player) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<b><l>Frozen Corpses:"));
        for (Corpse corpse : corpses) {
            lines.add(Text.of(" " + corpse.type().coloured() + "<f>: " + (isLooted(player.getUuid(), corpse) ? "<a>LOOTED" : "<c>NOT LOOTED")));
        }
        return lines;
    }
}
