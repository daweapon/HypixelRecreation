package net.swofty.type.kuudra.fight.world;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Elle's Lava Rod, a run-only item: it casts a bobber that floats on lava and, when reeled in, pulls whatever
 * crate or Fuel Cell it landed next to. It catches nothing else.
 */
public final class ElleLavaRod {
    private static final double CAST_SPEED = 1.3;
    private static final double GRAVITY = 0.04;
    private static final double MAX_RANGE = 40;

    private final class Bobber {
        private final SkyBlockPlayer owner;
        private final Entity hook;
        private Pos position;
        private Vec velocity;
        private boolean floating;

        private Bobber(SkyBlockPlayer owner, Instance instance) {
            this.owner = owner;
            this.position = owner.getPosition().add(0, owner.getEyeHeight(), 0);
            this.velocity = owner.getPosition().direction().mul(CAST_SPEED);
            this.hook = new Entity(EntityType.FISHING_BOBBER);
            hook.setNoGravity(true);
            hook.editEntityMeta(FishingHookMeta.class, meta -> meta.setOwnerEntity(owner));
            hook.setInstance(instance, position);
        }
    }

    private final Instance instance;
    private final double lavaY;
    private final Map<UUID, Bobber> bobbers = new ConcurrentHashMap<>();
    private final BiConsumer<SkyBlockPlayer, Pos> onReel;

    public ElleLavaRod(Instance instance, double lavaY, BiConsumer<SkyBlockPlayer, Pos> onReel) {
        this.instance = instance;
        this.lavaY = lavaY;
        this.onReel = onReel;
    }

    public void use(SkyBlockPlayer player) {
        Bobber bobber = bobbers.remove(player.getUuid());
        if (bobber != null) {
            Displays.remove(bobber.hook);
            player.playSound(Sound.sound(Key.key("entity.fishing_bobber.retrieve"), Sound.Source.NEUTRAL, 1f, 1f));
            if (bobber.floating) onReel.accept(player, bobber.position);
            return;
        }
        bobbers.put(player.getUuid(), new Bobber(player, instance));
        player.playSound(Sound.sound(Key.key("entity.fishing_bobber.throw"), Sound.Source.NEUTRAL, 0.5f, 0.4f));
    }

    public void tick(Predicate<SkyBlockPlayer> holdingRod) {
        bobbers.values().removeIf(bobber -> {
            if (!bobber.owner.isOnline() || bobber.owner.getInstance() != instance || !holdingRod.test(bobber.owner)
                    || bobber.position.distance(bobber.owner.getPosition()) > MAX_RANGE) {
                Displays.remove(bobber.hook);
                return true;
            }
            if (!bobber.floating) {
                Pos next = bobber.position.add(bobber.velocity);
                Block block = instance.getBlock(next);
                if (block.compare(Block.LAVA) || next.y() <= lavaY) {
                    bobber.floating = true;
                    bobber.position = next.withY(lavaY);
                } else if (block.isSolid()) {
                    bobber.velocity = Vec.ZERO;
                } else {
                    bobber.position = next;
                    bobber.velocity = bobber.velocity.mul(0.92).sub(0, GRAVITY, 0);
                }
            }
            bobber.hook.teleport(bobber.position);
            return false;
        });
    }

    public void clear() {
        bobbers.values().forEach(bobber -> Displays.remove(bobber.hook));
        bobbers.clear();
    }
}
