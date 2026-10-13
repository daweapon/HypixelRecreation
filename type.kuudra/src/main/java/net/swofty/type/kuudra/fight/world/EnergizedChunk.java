package net.swofty.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * A fuse-lit chunk from the Infernal capture: any hit sends it sliding away from the attacker until it stops, falls
 * off the platform, reaches Kuudra or its timer runs out.
 */
public final class EnergizedChunk {
    public static final String HEAD_TEXTURE = "db6975af70724d6a44fd5946e60b2717737dfdb545b4dab1893351a9c9dd183c";
    public static final int FUSE_TICKS = 20 * 20;
    private static final double HIT_SPEED = 0.85;
    private static final double FRICTION = 0.94;
    private static final double STOP_SPEED = 0.03;
    private static final float HEAD_SCALE = 2.6f;
    private static final float HITBOX_SIZE = 2.2f;

    private final Entity head;
    private final Entity title;
    private final Entity timer;
    private final Displays.Hitbox hitbox;
    private Pos position;
    private Vec velocity = Vec.ZERO;
    private int fuse = FUSE_TICKS;
    private SkyBlockPlayer lastHitter;

    public EnergizedChunk(Instance instance, Pos at) {
        this.position = at;
        this.head = Displays.item(instance, at.add(0, HEAD_SCALE / 2, 0), ItemStacks.head(HEAD_TEXTURE, "").build(), HEAD_SCALE);
        this.title = Displays.text(instance, at.add(0, HEAD_SCALE + 0.7, 0), "<c><b>ENERGIZED CHUNK");
        this.timer = Displays.text(instance, at.add(0, HEAD_SCALE + 0.3, 0), label());
        this.hitbox = Displays.hitbox(instance, at.add(0, 0.2, 0), HITBOX_SIZE, HITBOX_SIZE, _ -> {
        });
    }

    public boolean owns(Entity entity) {
        return entity == hitbox;
    }

    public void hit(SkyBlockPlayer player) {
        Vec away = position.sub(player.getPosition()).asVec().withY(0);
        if (away.lengthSquared() < 1e-4) away = player.getPosition().direction().withY(0);
        if (away.lengthSquared() < 1e-4) return;
        velocity = away.normalize().mul(HIT_SPEED);
        lastHitter = player;
    }

    public void tick() {
        fuse--;
        if (!velocity.isZero()) {
            position = position.add(velocity);
            velocity = velocity.mul(FRICTION);
            if (velocity.length() < STOP_SPEED) velocity = Vec.ZERO;
            head.teleport(position.add(0, HEAD_SCALE / 2, 0));
            title.teleport(position.add(0, HEAD_SCALE + 0.7, 0));
            timer.teleport(position.add(0, HEAD_SCALE + 0.3, 0));
            hitbox.teleport(position.add(0, 0.2, 0));
        }
        if (fuse % 20 == 0) Displays.setText(timer, label());
    }

    public boolean expired() {
        return fuse <= 0;
    }

    public Pos position() {
        return position;
    }

    public SkyBlockPlayer lastHitter() {
        return lastHitter;
    }

    public void remove() {
        Displays.remove(head);
        Displays.remove(title);
        Displays.remove(timer);
        Displays.remove(hitbox);
    }

    private String label() {
        return "<f>Explodes In: <c>" + Math.max(0, fuse / 20) + "s";
    }
}
