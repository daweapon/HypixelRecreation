package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.particle.Particle;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.LavaBeam;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Hellwisp (wiki: Hellwisp): hovers over the lava pools outside the Matriarch's Lair and lashes a tentacle that
 * reels the player into its mouth. Tentacle timing and bite damage are estimates. Hypixel draws it with a pack
 * model this server doesn't have, so the hitbox ghast is hidden behind a large head wearing Hypixel's own Hellwisp
 * head texture, trailing soul fire.
 */
public class MobHellwisp extends CrimsonIsleMob {
    static final String HEAD_TEXTURE = "199c3470ba4317913717ab36b626aec0b563266e28b3a517d71e9a0af0c90f96";
    private static final double MODEL_SCALE = 4;
    private static final int TENTACLE_INTERVAL_TICKS = 100;
    private static final int REEL_TICKS = 30;
    private static final double BITE_DAMAGE_PER_TICK = 250;
    private SkyBlockPlayer caught;
    private int reel;
    private Entity model;

    public MobHellwisp() {
        super(CrimsonMobs.HELLWISP);
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
        getAttribute(Attribute.SCALE).setBaseValue(0.5);
        setInvisible(true);
        model = new Entity(EntityType.ITEM_DISPLAY);
        model.setNoGravity(true);
        model.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(ItemStacks.head(HEAD_TEXTURE, "<c>Hellwisp").build());
            meta.setScale(new Vec(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE));
            meta.setTranslation(new Vec(0, -1.2, 0));
            meta.setPosRotInterpolationDuration(3);
        });
        model.setInstance(getInstance(), getPosition());
        addPassenger(model);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (model != null && !model.isRemoved()) {
            model.setView(getPosition().yaw(), 0);
            if (ticks % 6 == 0) {
                MythologicalCombat.particles(getInstance(), Particle.SOUL_FIRE_FLAME, getPosition().add(0, 0.6, 0), 0.6f, 2);
            }
        }
        if (caught != null) {
            reelIn();
            return;
        }
        if (ticks % TENTACLE_INTERVAL_TICKS != 0) return;
        rangedTarget(14).ifPresent(player -> {
            caught = player;
            reel = REEL_TICKS;
        });
    }

    private void reelIn() {
        if (caught.isDead() || caught.getInstance() != getInstance()) {
            caught = null;
            return;
        }
        LavaBeam.draw(getInstance(), getPosition().add(0, 1, 0), caught.getPosition().add(0, 1, 0));
        Vec pull = getPosition().sub(caught.getPosition()).asVec();
        if (pull.length() > 2) {
            caught.setVelocity(pull.normalize().mul(10));
        } else {
            trueStrike(caught, BITE_DAMAGE_PER_TICK * 5);
        }
        if (--reel <= 0) caught = null;
    }

    @Override
    public void kill() {
        removeModel();
        super.kill();
    }

    @Override
    public void remove() {
        removeModel();
        super.remove();
    }

    private void removeModel() {
        if (model != null && !model.isRemoved()) model.remove();
    }
}
