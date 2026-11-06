package dev.daweapon.type.catacombs.engine.watcher;

import dev.daweapon.commons.text.Text;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * The Watcher as the captures show it: an invisible zombie wearing the Watcher's head, never fighting, with its name on
 * an armor stand two blocks up and each line it speaks on a bold hologram for two seconds. It floats to the display
 * cases on the walls before a summon and back to the middle of the room for it.
 */
public final class WatcherEntity {
    private static final double NAME_HEIGHT = 1.97;
    private static final double LINE_HEIGHT = 2.3;
    private static final int LINE_TICKS = 40;
    private static final double FLY_SPEED = 0.5;
    private static final int AMBIENCE_FROM_TICK = 20;
    private static final int AMBIENCE_UNTIL_TICK = 60;
    private static final Sound.Source AMBIENCE_SOURCE = Sound.Source.HOSTILE;

    private final Instance instance;
    private final Pos home;
    private final List<Pos> stops;
    private final RandomGenerator random;
    private final LivingEntity body = new LivingEntity(EntityType.ZOMBIE);
    private final Entity nameTag;
    private final List<Line> lines = new ArrayList<>();
    private Pos target;
    private long ticks;
    private long nextAmbience = AMBIENCE_FROM_TICK;

    private record Line(Entity stand, long until) {
    }

    public WatcherEntity(Instance instance, Pos home, List<Pos> stops, WatcherConfig config, RandomGenerator random) {
        this.instance = instance;
        this.home = home;
        this.stops = stops.isEmpty() ? List.of(home) : List.copyOf(stops);
        this.random = random;
        this.target = home;
        body.setInvisible(true);
        body.setNoGravity(true);
        body.setHelmet(ItemStack.builder(Material.PLAYER_HEAD)
                .set(DataComponents.PROFILE, new ResolvableProfile(config.head().playerSkin())).build());
        body.setInstance(instance, home);
        nameTag = stand(config.name());
        nameTag.setInstance(instance, home.add(0, NAME_HEIGHT, 0));
    }

    private static Entity stand(String markup) {
        Entity stand = new Entity(EntityType.ARMOR_STAND);
        stand.editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setMarker(true);
            meta.setInvisible(true);
            meta.setCustomName(Text.parse(markup).asComponent());
            meta.setCustomNameVisible(true);
        });
        stand.setNoGravity(true);
        return stand;
    }

    public Pos home() {
        return home;
    }

    public void say(String line) {
        Entity stand = stand("<l>" + line);
        stand.setInstance(instance, body.getPosition().add(0, LINE_HEIGHT, 0));
        lines.add(new Line(stand, ticks + LINE_TICKS));
    }

    public void flyOut() {
        target = stops.get(random.nextInt(stops.size()));
    }

    public void flyHome() {
        target = home;
    }

    public void tick() {
        ticks++;
        Pos at = body.getPosition();
        double dx = target.x() - at.x(), dy = target.y() - at.y(), dz = target.z() - at.z();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 0.01) {
            double step = Math.min(FLY_SPEED, distance) / distance;
            Pos next = at.add(dx * step, dy * step, dz * step).withLookAt(target);
            body.teleport(next);
            nameTag.teleport(next.add(0, NAME_HEIGHT, 0));
            for (Line line : lines) line.stand().teleport(next.add(0, LINE_HEIGHT, 0));
        }
        lines.removeIf(line -> {
            if (line.until() > ticks) return false;
            line.stand().remove();
            return true;
        });
        if (ticks >= nextAmbience && ticks <= AMBIENCE_UNTIL_TICK) {
            float pitch = 0.49f + random.nextFloat() * 0.49f;
            instance.playSound(Sound.sound(Key.key("entity.zombie_villager.converted"), AMBIENCE_SOURCE, 1f, pitch),
                    home.x(), home.y() + 1, home.z());
            nextAmbience = ticks + 2 + random.nextInt(2);
        }
    }

    public void remove() {
        body.remove();
        nameTag.remove();
        lines.forEach(line -> line.stand().remove());
        lines.clear();
    }
}
