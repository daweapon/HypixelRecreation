package net.swofty.type.kuudra.fight.world;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.instance.Instance;
import net.swofty.commons.text.Text;
import net.swofty.type.kuudra.fight.KuudraBossState;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

/**
 * Kuudra himself: a size-30 Magma Cube (research K1 3.6) that rises out of and sinks into the lava on one of the
 * four sides, with his health on a boss bar.
 */
public final class KuudraBoss {
    private final KuudraBossState state;
    private final double height;
    private final Map<String, Pos> sides;
    private final double lavaY;
    private final double surfacedDepth;
    private final double submergedDepth;
    private final Entity cube;
    private final BossBar bar;
    private final Set<Player> viewers = ConcurrentHashMap.newKeySet();
    private String side;

    public KuudraBoss(Instance instance, KuudraBossState state, Map<String, Pos> sides, double lavaY,
                      KuudraFightSpec.Boss spec, RandomGenerator random) {
        if (sides.isEmpty()) throw new IllegalArgumentException("arena.yml needs kuudra_sides");
        this.state = state;
        this.height = 0.52 * spec.size();
        this.sides = sides;
        this.lavaY = lavaY;
        this.surfacedDepth = spec.surfacedDepth();
        this.submergedDepth = spec.submergedDepth();
        List<String> names = new ArrayList<>(sides.keySet());
        this.side = names.get(random.nextInt(names.size()));
        this.cube = new Entity(EntityType.MAGMA_CUBE);
        cube.setNoGravity(true);
        cube.editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(spec.size()));
        cube.setInstance(instance, position());
        bar = BossBar.bossBar(title().asComponent(), 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    }

    public void tick(Collection<? extends Player> players) {
        cube.teleport(position());
        bar.progress((float) Math.max(0, Math.min(1, state.fraction())));
        bar.name(title().asComponent());
        for (Player player : players) {
            if (viewers.add(player)) player.showBossBar(bar);
        }
        viewers.removeIf(player -> {
            if (players.contains(player)) return false;
            player.hideBossBar(bar);
            return true;
        });
    }

    /** Called while he is under the lava, so the next rise can come from another side. */
    public void moveSide(RandomGenerator random) {
        List<String> others = new ArrayList<>(sides.keySet());
        if (others.size() > 1) others.remove(side);
        side = others.get(random.nextInt(others.size()));
    }

    private Pos position() {
        Pos at = sides.get(side);
        double depth = surfacedDepth + (submergedDepth - surfacedDepth) * state.depth();
        return new Pos(at.x(), lavaY - depth, at.z());
    }

    /** Where Ballista shots and Wandering Blazes aim: his mouth, on top of the cube. */
    public Pos mouth() {
        return position().add(0, height * 0.75, 0);
    }

    public Pos base() {
        return position();
    }

    public double height() {
        return height;
    }

    public String side() {
        return side;
    }

    public boolean is(Entity entity) {
        return entity == cube;
    }

    /** The capture's boss bar name, obfuscated while he is under the lava. */
    private Text title() {
        boolean up = state.stance() == KuudraBossState.Stance.SURFACED || state.stance() == KuudraBossState.Stance.RISING;
        return Text.parse(up ? "<6>﴾ <c><l>Kuudra</l><6> ﴿" : "<6>﴾ <c><l><k>Kuudra</k></l><6> ﴿");
    }

    public void remove() {
        viewers.forEach(player -> player.hideBossBar(bar));
        viewers.clear();
        Displays.remove(cube);
    }
}
