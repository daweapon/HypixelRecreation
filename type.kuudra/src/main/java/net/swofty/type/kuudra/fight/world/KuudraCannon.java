package net.swofty.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.entity.ArmorStandModels;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A stationary cannon at the platform edge (research K1 3.8). Right-click to sit in it, left-click to fire where
 * you look, sneak to get out.
 */
public final class KuudraCannon {
    private static final String LABEL = "<f><l>CANNON\n<e><l>RIGHT-CLICK TO MOUNT";

    private static final List<ArmorStandModels.Stand> RIGHT = ArmorStandModels.load("/kuudra_models.json", "cannon_right");
    private static final List<ArmorStandModels.Stand> LEFT = ArmorStandModels.load("/kuudra_models.json", "cannon_left");

    private static List<ArmorStandModels.Stand> nearestModel(Pos at) {
        return distance(RIGHT, at) <= distance(LEFT, at) ? RIGHT : LEFT;
    }

    private static double distance(List<ArmorStandModels.Stand> model, Pos at) {
        return model.getFirst().position().distance(at);
    }

    private final Pos at;
    private final List<Entity> model = new ArrayList<>();
    private final Entity label;
    private final Entity hitbox;
    private final Entity seat;
    private @Nullable SkyBlockPlayer rider;

    public KuudraCannon(Instance instance, Pos at, Consumer<SkyBlockPlayer> onUse) {
        this.at = at;
        for (ArmorStandModels.Stand stand : nearestModel(at)) {
            LivingEntity entity = stand.create();
            entity.setInstance(instance, stand.position());
            model.add(entity);
        }
        label = Displays.text(instance, at.add(0, 2.2, 0), LABEL);
        hitbox = Displays.hitbox(instance, at, 1.5f, 1.6f, onUse);
        seat = new Entity(EntityType.ITEM_DISPLAY);
        seat.setNoGravity(true);
        seat.setInstance(instance, at.add(0, 0.6, 0));
    }

    public void tick() {
        if (rider != null && !seat.getPassengers().contains(rider)) dismount();
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
        return entity == hitbox || model.contains(entity);
    }

    public Pos position() {
        return at;
    }

    public void remove() {
        dismount();
        model.forEach(Displays::remove);
        Displays.remove(label);
        Displays.remove(hitbox);
        Displays.remove(seat);
    }
}
