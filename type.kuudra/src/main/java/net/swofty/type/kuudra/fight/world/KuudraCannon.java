package net.swofty.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A stationary cannon at the platform edge (research K1 3.8). Right-click to sit in it, left-click to fire where
 * you look, sneak to get out.
 */
public final class KuudraCannon {
    private static final String LABEL = "<f><l>CANNON\n<e><l>RIGHT-CLICK TO MOUNT";

    private final Pos at;
    private final Entity base;
    private final Entity barrel;
    private final Entity label;
    private final Entity hitbox;
    private final Entity seat;
    private @Nullable SkyBlockPlayer rider;

    public KuudraCannon(Instance instance, Pos at, Consumer<SkyBlockPlayer> onUse) {
        this.at = at;
        base = Displays.block(instance, at, Block.POLISHED_BLACKSTONE_BRICKS, new Vec(1.4, 0.8, 1.4));
        barrel = Displays.block(instance, at.add(0, 0.8, 0), Block.BLACK_CONCRETE, new Vec(0.7, 0.7, 2.2));
        label = Displays.text(instance, at.add(0, 2.2, 0), LABEL);
        hitbox = Displays.hitbox(instance, at, 1.5f, 1.6f, onUse);
        seat = new Entity(EntityType.ITEM_DISPLAY);
        seat.setNoGravity(true);
        seat.setInstance(instance, at.add(0, 0.6, 0));
    }

    public void tick() {
        if (rider != null && !seat.getPassengers().contains(rider)) dismount();
        float yaw = rider == null ? at.yaw() : rider.getPosition().yaw();
        barrel.teleport(at.add(0, 0.8, 0).withView(yaw, 0));
    }

    public boolean mount(SkyBlockPlayer player) {
        if (rider != null && seat.getPassengers().contains(rider)) return false;
        rider = player;
        seat.addPassenger(player);
        Displays.setText(label, "");
        return true;
    }

    public void dismount() {
        if (rider != null && seat.getPassengers().contains(rider)) seat.removePassenger(rider);
        rider = null;
        Displays.setText(label, LABEL);
    }

    public boolean isRiding(SkyBlockPlayer player) {
        return rider == player && seat.getPassengers().contains(player);
    }

    public Pos muzzle(SkyBlockPlayer player) {
        return at.add(0, 1.6, 0).add(player.getPosition().direction().mul(1.5));
    }

    public boolean owns(Entity entity) {
        return entity == hitbox || entity == base || entity == barrel;
    }

    public Pos position() {
        return at;
    }

    public void remove() {
        dismount();
        Displays.remove(base);
        Displays.remove(barrel);
        Displays.remove(label);
        Displays.remove(hitbox);
        Displays.remove(seat);
    }
}
