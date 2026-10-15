package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.attribute.Attribute;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Krondor Necromancer (wiki: Krondor_Necromancer): a rare Fire Mage that, once hostile, throws a fireball every
 * 3 s (much faster up close) and raises Krondor Necromancer Summons. Killing one costs 25 Mage Reputation. The
 * summon cadence (2 at a time, every 20 s) is an estimate.
 */
public class MobKrondorNecromancer extends CrimsonIsleMob {
    public MobKrondorNecromancer() {
        super(CrimsonMobs.KRONDOR_NECROMANCER);
    }

    private static final int FIREBALL_INTERVAL_TICKS = 60;
    private static final int CLOSE_FIREBALL_INTERVAL_TICKS = 20;
    private static final int SUMMON_INTERVAL_TICKS = 400;
    private final List<MobKrondorNecromancerSummon> summons = new ArrayList<>();

    @Override
    protected Temper temper() {
        return Temper.NEUTRAL;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        getAttribute(Attribute.SCALE).setBaseValue(1.3);
    }

    @Override
    protected void onAiTick(int ticks) {
        Optional<SkyBlockPlayer> target = rangedTarget(24);
        if (target.isEmpty()) return;
        SkyBlockPlayer player = target.get();
        int interval = player.getPosition().distance(getPosition()) < 6 ? CLOSE_FIREBALL_INTERVAL_TICKS : FIREBALL_INTERVAL_TICKS;
        if (ticks % interval == 0) fireball(player, getProfile().damage());
        summons.removeIf(summon -> summon.isDead() || summon.isRemoved());
        if (ticks % SUMMON_INTERVAL_TICKS == 0 && summons.size() < 2) {
            MobKrondorNecromancerSummon summon = new MobKrondorNecromancerSummon();
            summon.setInstance(getInstance(), getPosition().add(summons.isEmpty() ? 1.5 : -1.5, 0, 0));
            summons.add(summon);
        }
    }

    @Override
    public void remove() {
        summons.forEach(MobKrondorNecromancerSummon::remove);
        super.remove();
    }
}
