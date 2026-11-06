package dev.daweapon.type.catacombs.engine;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.catacombs.arena.RoomFeatures;
import dev.daweapon.type.catacombs.blessing.PartyBlessings;
import dev.daweapon.type.catacombs.engine.watcher.WatcherConfig;
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

/**
 * The Blessings the Watcher leaves in the Blood Room with the portal: on Floor I a Blessing of Life and one of Power,
 * each a blessing head on an invisible armor stand under a pink name. The party picks them up automatically when it
 * reaches the boss.
 */
final class BloodBlessings {
    private static final double HEAD_DROP = 0.151;

    private final List<Entity> shown = new ArrayList<>();
    private List<WatcherConfig.Blessing> waiting = List.of();

    void drop(Instance instance, List<RoomFeatures.Spot> spots, Pos fallback, WatcherConfig config) {
        waiting = config.blessings();
        for (int index = 0; index < waiting.size(); index++) {
            WatcherConfig.Blessing blessing = waiting.get(index);
            Pos at = index < spots.size()
                    ? new Pos(spots.get(index).x(), spots.get(index).y(), spots.get(index).z())
                    : fallback.add(index, 0, 0);
            LivingEntity head = new LivingEntity(EntityType.ARMOR_STAND);
            head.editEntityMeta(ArmorStandMeta.class, meta -> meta.setInvisible(true));
            head.setNoGravity(true);
            head.setHelmet(ItemStack.builder(Material.PLAYER_HEAD)
                    .set(DataComponents.PROFILE, new ResolvableProfile(config.blessingHead().playerSkin())).build());
            head.setInstance(instance, at.sub(0, HEAD_DROP, 0));
            Entity name = new Entity(EntityType.ARMOR_STAND);
            name.editEntityMeta(ArmorStandMeta.class, meta -> {
                meta.setInvisible(true);
                meta.setMarker(true);
                meta.setCustomName(Text.parse("<d>" + blessing.type().displayName()).asComponent());
                meta.setCustomNameVisible(true);
            });
            name.setNoGravity(true);
            name.setInstance(instance, at);
            shown.add(head);
            shown.add(name);
        }
    }

    void pickUp(DungeonHost host, PartyBlessings blessings) {
        for (WatcherConfig.Blessing blessing : waiting) {
            host.broadcast("<e>A <d>" + blessing.type().displayName() + "<e> was picked up!");
            blessings.grant(blessing.type(), blessing.level(), null);
        }
        waiting = List.of();
        clear();
    }

    void clear() {
        shown.forEach(Entity::remove);
        shown.clear();
    }
}
