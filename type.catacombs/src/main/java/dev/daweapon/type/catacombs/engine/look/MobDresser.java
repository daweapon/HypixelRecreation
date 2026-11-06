package dev.daweapon.type.catacombs.engine.look;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.network.player.ResolvableProfile;
import org.tinylog.Logger;

/** Puts a captured outfit on a spawned dungeon mob: the skin on a player model and every equipment piece. */
public final class MobDresser {
    private static final byte ALL_SKIN_PARTS = 127;

    private MobDresser() {
    }

    public static void dress(LivingEntity entity, MobLook.Outfit outfit) {
        if (outfit.skin() != null && entity.getEntityMeta() instanceof MannequinMeta) {
            entity.editEntityMeta(MannequinMeta.class, meta -> {
                meta.setProfile(new ResolvableProfile(outfit.skin().playerSkin()));
                meta.setDescription(null);
                meta.setDisplayedSkinParts(ALL_SKIN_PARTS);
            });
        }
        outfit.equipment().forEach((slot, spec) -> {
            try {
                entity.setEquipment(slot, spec.toStack());
            } catch (RuntimeException exception) {
                Logger.warn(exception, "Dungeon mob piece {} could not be shown", spec);
            }
        });
    }
}
