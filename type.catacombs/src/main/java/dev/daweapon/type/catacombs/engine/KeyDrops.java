package dev.daweapon.type.catacombs.engine;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Keys on the floor: any player who walks up takes it for the whole party, and after 10 seconds it goes to the party anyway. */
final class KeyDrops {
    private static final long AUTO_PICKUP_MILLIS = 10_000;
    private static final double PICKUP_RANGE = 2.5;
    private static final double GUARANTEED_CHANCE_PERCENT = 100;
    private static final double LABEL_HEIGHT = 1.53125;
    private static final double HEAD_DROP = 0.25;

    private record Pending(KeyType key, Entity entity, List<Entity> parts, long droppedAt) {
    }

    private final DungeonHost host;
    private final List<Pending> pending = new CopyOnWriteArrayList<>();

    KeyDrops(DungeonHost host) {
        this.host = host;
    }

    void drop(KeyType key, Pos at) {
        if (!LootRoll.roll(null, null, GUARANTEED_CHANCE_PERCENT)) return;
        Pos label = at.add(0, LABEL_HEIGHT, 0);
        LivingEntity head = new LivingEntity(EntityType.ARMOR_STAND);
        head.editEntityMeta(ArmorStandMeta.class, meta -> meta.setInvisible(true));
        head.setNoGravity(true);
        head.setHelmet(ItemStack.builder(Material.PLAYER_HEAD)
                .set(DataComponents.PROFILE, new ResolvableProfile(key.head().playerSkin())).build());
        head.setInstance(host.instance(), label.sub(0, HEAD_DROP, 0));
        Entity name = new Entity(EntityType.ARMOR_STAND);
        name.editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setInvisible(true);
            meta.setMarker(true);
            meta.setCustomName(Text.parse(key.coloured()).asComponent());
            meta.setCustomNameVisible(true);
        });
        name.setNoGravity(true);
        name.setInstance(host.instance(), label);
        pending.add(new Pending(key, name, List.of(head, name), System.currentTimeMillis()));
        host.broadcast("<7>A " + key.coloured() + " <7>dropped!");
    }

    void poll() {
        long now = System.currentTimeMillis();
        for (Pending drop : pending) {
            SkyBlockPlayer nearest = host.alivePlayers().stream()
                    .filter(player -> player.getPosition().distance(drop.entity().getPosition()) <= PICKUP_RANGE)
                    .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(drop.entity().getPosition())))
                    .orElse(null);
            if (nearest == null && now - drop.droppedAt() < AUTO_PICKUP_MILLIS) continue;
            pickUp(drop, nearest);
        }
    }

    private void pickUp(Pending drop, SkyBlockPlayer picker) {
        if (!pending.remove(drop)) return;
        drop.parts().forEach(Entity::remove);
        host.keys().grant(drop.key());
        host.broadcast(picker == null
                ? Text.of("<a>A {}<a> was picked up!", Text.parse(drop.key().coloured()))
                : Text.of("{}<a> picked up the {}<a>!", picker.getColouredName(), Text.parse(drop.key().coloured())));
        host.instance().playSound(Sound.sound(Key.key("entity.item.pickup"), Sound.Source.PLAYER, 1f, 1f),
                drop.entity().getPosition().x(), drop.entity().getPosition().y(), drop.entity().getPosition().z());
    }

    void clear() {
        pending.forEach(drop -> drop.parts().forEach(Entity::remove));
        pending.clear();
    }
}
