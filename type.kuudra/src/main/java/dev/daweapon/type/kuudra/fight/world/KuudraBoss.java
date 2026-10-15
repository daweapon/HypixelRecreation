package dev.daweapon.type.kuudra.fight.world;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.kuudra.fight.KuudraBossState;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

/**
 * Kuudra himself: two stacked size-30 Magma Cubes 12 blocks apart (research K1 3.6 and the captures) that rise
 * out of and sink into the lava on one of the four sides, with his health on a boss bar. On the stun tiers he
 * leans in toward the platform and sags while stunned; in the Infernal lair only his head pokes out of the walls.
 */
public final class KuudraBoss {
    public static final double STACK_GAP = 12;

    private final KuudraBossState state;
    private final double cubeHeight;
    private final double lavaY;
    private final double surfacedDepth;
    private final double submergedDepth;
    private final Entity lower;
    private final Entity upper;
    private final BossBar bar;
    private final Set<Player> viewers = ConcurrentHashMap.newKeySet();
    private Map<String, Pos> sides;
    private String side;
    private @Nullable Pos leanToward;
    private double leanIn;
    private double sag;
    private @Nullable Pos lairCentre;
    private double retract;

    public KuudraBoss(Instance instance, KuudraBossState state, Map<String, Pos> sides, double lavaY,
                      KuudraFightSpec.Boss spec, RandomGenerator random) {
        if (sides.isEmpty()) throw new IllegalArgumentException("arena.yml needs kuudra_sides");
        this.state = state;
        this.cubeHeight = 0.52 * spec.size();
        this.sides = sides;
        this.lavaY = lavaY;
        this.surfacedDepth = spec.surfacedDepth();
        this.submergedDepth = spec.submergedDepth();
        List<String> names = new ArrayList<>(sides.keySet());
        this.side = names.get(random.nextInt(names.size()));
        this.lower = cube(instance, spec.size(), position());
        this.upper = cube(instance, spec.size(), position().add(0, STACK_GAP, 0));
        bar = BossBar.bossBar(title().asComponent(), 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    }

    private static Entity cube(Instance instance, int size, Pos at) {
        Entity cube = new Entity(EntityType.MAGMA_CUBE);
        cube.setNoGravity(true);
        cube.editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(size));
        cube.setInstance(instance, at);
        return cube;
    }

    public void tick(Collection<? extends Player> players) {
        Pos at = position();
        lower.teleport(at);
        upper.teleport(at.add(0, STACK_GAP, 0));
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

    /** From the stun tiers' fuel phase on he rises this many blocks nearer the platform centre (the captures). */
    public void leanIn(@Nullable Pos centre, double blocks) {
        this.leanToward = centre;
        this.leanIn = centre == null ? 0 : blocks;
    }

    public void setSag(double blocks) {
        this.sag = blocks;
    }

    /** Moves him into the lair: his head pokes out of one wall at a time and pulls back {@code retract} blocks. */
    public void enterLair(Map<String, Pos> lairSides, Pos centre, double retract, RandomGenerator random) {
        if (lairSides.isEmpty()) return;
        this.sides = lairSides;
        this.lairCentre = centre;
        this.retract = retract;
        this.leanIn = 0;
        this.leanToward = null;
        this.sag = 0;
        List<String> names = new ArrayList<>(lairSides.keySet());
        this.side = names.get(random.nextInt(names.size()));
    }

    public boolean inLair() {
        return lairCentre != null;
    }

    private Pos position() {
        Pos at = sides.get(side);
        if (lairCentre != null) {
            Vec out = new Vec(at.x() - lairCentre.x(), 0, at.z() - lairCentre.z());
            Vec back = out.lengthSquared() < 1e-6 ? Vec.ZERO : out.normalize().mul(retract * state.depth());
            return new Pos(at.x() + back.x(), at.y() - STACK_GAP, at.z() + back.z());
        }
        double depth = surfacedDepth + (submergedDepth - surfacedDepth) * state.depth() + sag;
        double x = at.x();
        double z = at.z();
        if (leanToward != null && leanIn > 0) {
            Vec in = new Vec(leanToward.x() - x, 0, leanToward.z() - z);
            if (in.length() > leanIn) {
                Vec step = in.normalize().mul(leanIn);
                x += step.x();
                z += step.z();
            }
        }
        return new Pos(x, lavaY - depth, z);
    }

    /** Where Ballista shots and Wandering Blazes aim: his mouth, on the upper cube. */
    public Pos mouth() {
        return position().add(0, STACK_GAP + cubeHeight * 0.5, 0);
    }

    public Pos base() {
        return position();
    }

    /** The two cubes together, bottom of the lower to top of the upper. */
    public double height() {
        return STACK_GAP + cubeHeight;
    }

    public double width() {
        return cubeHeight;
    }

    /** The upper cube's box, the only part that pokes into the lair. */
    public double headBottom() {
        return position().y() + STACK_GAP;
    }

    public String side() {
        return side;
    }

    public boolean is(Entity entity) {
        return entity == lower || entity == upper;
    }

    /** The captures' boss bar name, obfuscated while he is under the lava and throughout the lair. */
    private Text title() {
        boolean up = state.stance() == KuudraBossState.Stance.SURFACED || state.stance() == KuudraBossState.Stance.RISING;
        return Text.parse(up && lairCentre == null ? "<6>﴾ <c><l>Kuudra</l><6> ﴿" : "<6>﴾ <c><l><k>Kuudra</k></l><6> ﴿");
    }

    public void hideBar() {
        viewers.forEach(player -> player.hideBossBar(bar));
        viewers.clear();
    }

    public void remove() {
        hideBar();
        Displays.remove(lower);
        Displays.remove(upper);
    }
}
