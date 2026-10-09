package net.swofty.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.kuudra.fight.CargoPickup;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/** A supply crate or Ballista Fuel Cell bobbing in the lava until someone reels it in and lifts it out. */
public final class LavaCargo {
    public enum Kind {
        SUPPLIES("<f><l>SUPPLIES", "Elle's Supplies", Block.CHEST),
        FUEL("<c><l>FUEL CELL", "Ballista Fuel Cell", Block.REDSTONE_LAMP.withProperty("lit", "true"));

        private final String label;
        private final String itemName;
        private final Block block;

        Kind(String label, String itemName, Block block) {
            this.label = label;
            this.itemName = itemName;
            this.block = block;
        }

        public String label() {
            return label;
        }

        public String itemName() {
            return itemName;
        }

        public Block block() {
            return block;
        }
    }

    private static final float SIZE = 1.1f;

    private final Kind kind;
    private final String spot;
    private final Pos home;
    private Pos position;
    private final Entity display;
    private final Entity label;
    private final Entity hitbox;
    private @Nullable CargoPickup pickup;
    private int bobTicks;
    private boolean reachable;

    public LavaCargo(Instance instance, Kind kind, String spot, Pos home, BiConsumer<LavaCargo, SkyBlockPlayer> onUse) {
        this.kind = kind;
        this.spot = spot;
        this.home = home;
        this.position = home;
        this.display = Displays.block(instance, home, kind.block(), new Vec(SIZE, SIZE, SIZE));
        this.label = Displays.text(instance, home.add(0, 2, 0), kind.label());
        this.hitbox = Displays.hitbox(instance, home, 1.6f, 1.6f, player -> onUse.accept(this, player));
    }

    public void tick() {
        bobTicks++;
        double bob = Math.sin(bobTicks * 0.15) * 0.12;
        Pos at = position.add(0, -0.3 + bob, 0).withYaw((bobTicks * 2) % 360);
        display.teleport(at);
        hitbox.teleport(position.add(0, -0.3, 0));
        label.teleport(position.add(0, 1.6, 0));
        if (bobTicks % 4 == 0) {
            Displays.setText(label, kind.label() + "\n" + (reachable ? "<a><l>CLICK TO PICK UP" : "<e><l>HOOK CLOSER"));
        }
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public void moveTo(Pos position) {
        this.position = position;
    }

    public void remove() {
        Displays.remove(display);
        Displays.remove(label);
        Displays.remove(hitbox);
    }

    public Kind kind() {
        return kind;
    }

    public String spot() {
        return spot;
    }

    public Pos home() {
        return home;
    }

    public Pos position() {
        return position;
    }

    public @Nullable CargoPickup pickup() {
        return pickup;
    }

    public void setPickup(@Nullable CargoPickup pickup) {
        this.pickup = pickup;
    }

    public boolean owns(Entity entity) {
        return entity == hitbox || entity == display;
    }
}
