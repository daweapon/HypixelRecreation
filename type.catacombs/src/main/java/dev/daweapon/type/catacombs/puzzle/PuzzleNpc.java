package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.commons.text.Text;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.ResolvableProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * A standing puzzle NPC as Hypixel shows them in dungeon rooms: a player-skinned body that never moves and its name
 * lines on invisible armor stands at its feet, the first line on top (Tomioka and the Three Weirdos captures).
 */
public final class PuzzleNpc {
    private static final double LINE_GAP = 0.25;

    private final Entity body = new Entity(EntityType.MANNEQUIN);
    private final List<Entity> lines = new ArrayList<>();

    public PuzzleNpc(PlayerSkin skin, List<String> nameLines) {
        body.editEntityMeta(MannequinMeta.class, meta -> {
            meta.setImmovable(true);
            meta.setProfile(new ResolvableProfile(skin));
            meta.setDescription(Component.empty());
        });
        body.setNoGravity(true);
        for (String line : nameLines) lines.add(stand(line));
    }

    private static Entity stand(String markup) {
        Entity stand = new Entity(EntityType.ARMOR_STAND);
        stand.editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setInvisible(true);
            meta.setHasNoBasePlate(true);
            meta.setCustomName(Text.parse(markup).asComponent());
            meta.setCustomNameVisible(true);
        });
        stand.setNoGravity(true);
        return stand;
    }

    public void spawn(Instance instance, Pos at) {
        body.setInstance(instance, at);
        for (int index = 0; index < lines.size(); index++) lines.get(index).setInstance(instance, at.sub(0, LINE_GAP * index, 0));
    }

    public boolean is(Entity entity) {
        return entity == body || lines.contains(entity);
    }

    public Pos position() {
        return body.getPosition();
    }

    public void remove() {
        body.remove();
        lines.forEach(Entity::remove);
    }
}
