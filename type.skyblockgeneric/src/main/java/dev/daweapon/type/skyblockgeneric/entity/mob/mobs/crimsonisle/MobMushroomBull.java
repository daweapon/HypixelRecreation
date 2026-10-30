package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.consumable.SporeBurst;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.particle.Particle;

/**
 * Mushroom Bull (wiki: Mushroom_Bull): a fast charging bull in the Mystic Marsh. It grazes Mushroom Spores it
 * walks over and, once it has eaten any, dies in a burst of Brown Mushroom explosions. The explosions are
 * cosmetic and their count and spread are estimates.
 */
public class MobMushroomBull extends CrimsonIsleMob {
    private static final double GRAZE_RADIUS = 1.8;
    private static final int GRAZE_INTERVAL_TICKS = 5;
    private static final int EXPLOSIONS_PER_SPORE = 2;
    private static final int MAX_EXPLOSIONS = 12;
    private static final double EXPLOSION_SPREAD = 4;

    private int sporesEaten;

    public MobMushroomBull() {
        super(CrimsonMobs.MUSHROOM_BULL);
    }

    @Override
    protected int strollRadius() {
        return 12;
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % GRAZE_INTERVAL_TICKS != 0) return;
        getInstance().getNearbyEntities(getPosition(), GRAZE_RADIUS).stream()
                .filter(entity -> entity instanceof DroppedItemEntityImpl dropped && !dropped.isRemoved() && isSpore(dropped))
                .map(entity -> (DroppedItemEntityImpl) entity)
                .forEach(this::graze);
    }

    private static boolean isSpore(DroppedItemEntityImpl dropped) {
        return dropped.getItem().getAttributeHandler().getPotentialType() == ItemType.MUSHROOM_SPORE;
    }

    private void graze(DroppedItemEntityImpl spore) {
        sporesEaten += spore.getItemStack().amount();
        spore.remove();
        MythologicalCombat.sound(this, "entity.generic.eat", 1f, 0.8f);
        MythologicalCombat.particles(getInstance(), Particle.HAPPY_VILLAGER, getPosition().add(0, 1, 0), 0.4f, 6);
    }

    @Override
    public void kill() {
        var instance = getInstance();
        Pos at = getPosition();
        int explosions = Math.min(MAX_EXPLOSIONS, sporesEaten * EXPLOSIONS_PER_SPORE);
        if (instance != null && explosions > 0) MythologicalCombat.sound(this, "entity.generic.explode", 1.5f, 1.2f);
        super.kill();
        if (instance == null || explosions == 0) return;
        SporeBurst.explode(instance, at, EXPLOSION_SPREAD, explosions);
        sporesEaten = 0;
    }
}
