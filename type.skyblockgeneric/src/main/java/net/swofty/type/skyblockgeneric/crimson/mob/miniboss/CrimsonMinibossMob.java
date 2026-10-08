package net.swofty.type.skyblockgeneric.crimson.mob.miniboss;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import net.swofty.type.skyblockgeneric.crimson.mob.MobDrop;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.event.custom.CrimsonMinibossKilledEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A Crimson Isle miniboss or the Magma Boss. Everyone who dealt 250,000+ damage gets the kill, their own roll of
 * the loot table, the {@link CrimsonMinibossKilledEvent}, and, the first time each day, +20 reputation with their
 * faction.
 */
public abstract class CrimsonMinibossMob extends CrimsonIsleMob {
    public static final String DAILY_KEY = "miniboss:day:";
    private static final double LEASH = 30;

    protected CrimsonMinibossMob(CrimsonMobProfile profile) {
        super(profile);
    }

    public abstract CrimsonMiniboss miniboss();

    @Override
    protected List<MobDrop> drops() {
        return List.of();
    }

    @Override
    public boolean countsTowardVanquisher() {
        return true;
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (isDead() || isRemoved() || getInstance() == null || miniboss() == null) return;
        Pos home = miniboss().getSpawn();
        if (getPosition().distance(home) > LEASH) teleport(home);
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        for (Map.Entry<UUID, Double> entry : getDamageDealt().entrySet()) {
            if (entry.getValue() < CrimsonMiniboss.LOOT_SHARE_DAMAGE) continue;
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(entry.getKey());
            if (player == null || player.getInstance() != getInstance()) continue;
            if (player != killer) {
                HypixelEventHandler.callCustomEvent(new PlayerKilledSkyBlockMobEvent(player, this));
                player.getSkills().increase(player, getSkillCategory(), (double) getOtherLoot().getSkillXPAmount());
            }
            reward(player);
        }
        onDefeated();
    }

    private void reward(SkyBlockPlayer player) {
        CrimsonMobLoot.roll(player, this, miniboss().getDrops());
        awardDailyReputation(player, miniboss());
        HypixelEventHandler.callCustomEvent(new CrimsonMinibossKilledEvent(player, miniboss()));
    }

    public static boolean awardDailyReputation(SkyBlockPlayer player, CrimsonMiniboss miniboss) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) return false;
        long today = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        String key = DAILY_KEY + miniboss.name();
        if (DatapointCrimsonIsle.of(player).getNumber(key) == today + 1) return false;
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(key, today + 1));
        CrimsonReputationService.add(player, faction, CrimsonMiniboss.DAILY_REPUTATION, miniboss.getDisplayName());
        return true;
    }

    protected void onDefeated() {
    }

    protected List<SkyBlockPlayer> fighters(double range) {
        return playersWithin(range);
    }
}
