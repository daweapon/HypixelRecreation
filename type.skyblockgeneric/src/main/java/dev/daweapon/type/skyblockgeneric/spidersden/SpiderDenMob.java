package dev.daweapon.type.skyblockgeneric.spidersden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.CataloguedBestiaryAreas;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base for the Spider's Den mobs. They reuse the Crimson Isle mob engine (profile numbers, damage ranking,
 * LootRoll drops) but count towards the Spider's Den Bestiary and are never Corrupted.
 */
public class SpiderDenMob extends CrimsonIsleMob {
    public SpiderDenMob(CrimsonMobProfile profile) {
        super(profile);
    }

    protected double hitCap() {
        return 0;
    }

    protected boolean immuneToMagic() {
        return false;
    }

    @Override
    protected Damage filterIncomingDamage(Damage damage) {
        if (immuneToMagic() && damage.getType() == DamageType.MAGIC) return null;
        double cap = hitCap();
        if (cap <= 0 || damage.getAmount() <= cap) return damage;
        return new Damage(damage.getType(), damage.getSource(), damage.getAttacker(), damage.getSourcePosition(), (float) cap);
    }

    @Override
    public boolean countsForBestiary() {
        return BestiaryIds.IDS.contains(getMobID());
    }

    protected void afterDeath(Instance instance, Pos position) {
    }

    public Optional<SkyBlockPlayer> leapTarget(double range) {
        return rangedTarget(range);
    }

    @Override
    public void kill() {
        Instance instance = getInstance();
        Pos position = getPosition();
        boolean alive = !isDead();
        super.kill();
        if (alive && instance != null) afterDeath(instance, position);
    }

    private static final class BestiaryIds {
        private static final Set<String> IDS = Arrays.stream(CataloguedBestiaryAreas.SPIDERS_DEN)
                .flatMap(family -> family.getMobs().stream())
                .map(BestiaryMob::getMobID)
                .collect(Collectors.toUnmodifiableSet());
    }
}
