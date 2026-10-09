package net.swofty.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Predicate;

/**
 * A server-driven shot drawn with an item display: Ballista bolts fly straight, cannonballs arc. It lands on a
 * solid block, in the lava, on whatever {@code hits} accepts, or after its lifetime.
 */
public final class Projectile {
    private final Entity display;
    private final SkyBlockPlayer shooter;
    private final double gravity;
    private final int lifetime;
    private final Instance instance;
    private Pos position;
    private Vec velocity;
    private int ticks;

    public Projectile(Instance instance, SkyBlockPlayer shooter, Pos from, Vec velocity, ItemStack look, float scale,
                      double gravity, int lifetime) {
        this.instance = instance;
        this.shooter = shooter;
        this.position = from;
        this.velocity = velocity;
        this.gravity = gravity;
        this.lifetime = lifetime;
        this.display = Displays.item(instance, from, look, scale);
    }

    /** Moves one tick; returns true once it has landed (then {@link #position()} is where). */
    public boolean tick(Predicate<Pos> hits) {
        ticks++;
        int steps = Math.max(1, (int) Math.ceil(velocity.length() / 0.5));
        Vec step = velocity.div(steps);
        for (int i = 0; i < steps; i++) {
            position = position.add(step);
            Block block = instance.getBlock(position);
            if (hits.test(position) || block.isSolid() || block.compare(Block.LAVA)) return land();
        }
        velocity = velocity.sub(0, gravity, 0);
        display.teleport(position.withDirection(velocity));
        return ticks >= lifetime && land();
    }

    private boolean land() {
        Displays.remove(display);
        return true;
    }

    public void remove() {
        Displays.remove(display);
    }

    public Pos position() {
        return position;
    }

    public SkyBlockPlayer shooter() {
        return shooter;
    }
}
