package net.swofty.type.skyblockgeneric.fishing.winter;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.Damage;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * The Grinch is Timid and Shielded: it sprints away from anyone nearby, often off the island, takes
 * exactly one damage per hit and hands the hitter a Gift every time it is hit (wiki: Grinch). The
 * wiki doesn't say which Gift; a White Gift is given.
 */
public class GrinchMob extends WinterSeaCreatureMob {
    private static final double FLEE_RANGE = 12;
    private static final double FLEE_SPEED = 9;
    private static final double HOP = 4;
    private static final int FLEE_INTERVAL_TICKS = 5;

    public GrinchMob(@Nullable SkyBlockPlayer angler) {
        super(WinterSeaCreature.GRINCH, angler);
    }

    @Override
    protected void onTick(int ticks) {
        if (ticks % FLEE_INTERVAL_TICKS != 0) return;
        MythologicalCombat.nearby(this, FLEE_RANGE).stream()
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())))
                .ifPresent(this::fleeFrom);
    }

    private void fleeFrom(SkyBlockPlayer player) {
        Vec away = getPosition().sub(player.getPosition()).asVec().withY(0);
        if (away.lengthSquared() < 0.01) away = new Vec(1, 0, 0);
        setVelocity(away.normalize().mul(FLEE_SPEED).withY(isOnGround() ? HOP : getVelocity().y()));
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (!(damage.getAttacker() instanceof SkyBlockPlayer player)) return false;
        boolean hit = super.damage(new Damage(damage.getType(), damage.getSource(), damage.getAttacker(),
                damage.getSourcePosition(), 1f));
        if (hit) player.addAndUpdateItem(ItemType.WHITE_GIFT);
        return hit;
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        killer.getAchievementHandler().completeAchievement("skyblock.caught_the_grinch");
    }
}
