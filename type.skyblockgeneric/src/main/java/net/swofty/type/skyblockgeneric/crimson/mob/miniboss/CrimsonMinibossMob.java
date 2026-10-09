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
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
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
    private static final double NATURAL_SPAWN_TOLERANCE = 2;

    private @Nullable Pos home;

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
    public void onSpawn() {
        if (home == null) home = getPosition();
        super.onSpawn();
    }

    /** Where this boss fights: its arena when spawned naturally, wherever staff summoned it otherwise. */
    public Pos home() {
        return home != null ? home : miniboss().getSpawn();
    }

    public boolean atNaturalSpawn() {
        return isAtNaturalSpawn(home(), miniboss());
    }

    public static boolean isAtNaturalSpawn(Pos home, CrimsonMiniboss miniboss) {
        return home.distance(miniboss.getSpawn()) <= NATURAL_SPAWN_TOLERANCE;
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (isDead() || isRemoved() || getInstance() == null) return;
        if (getPosition().distance(home()) > LEASH) teleport(home());
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        for (UUID sharer : lootSharers()) {
            SkyBlockPlayer player = sharer.equals(killer.getUuid()) ? killer : SkyBlockGenericLoader.getFromUUID(sharer);
            if (player == null || player.getInstance() != getInstance()) continue;
            if (player != killer) {
                HypixelEventHandler.callCustomEvent(new PlayerKilledSkyBlockMobEvent(player, this));
                player.getSkills().increase(player, getSkillCategory(), (double) getOtherLoot().getSkillXPAmount());
            }
            reward(player);
        }
        onDefeated();
    }

    public List<UUID> lootSharers() {
        return getDamageDealt().entrySet().stream()
                .filter(entry -> entry.getValue() >= CrimsonMiniboss.LOOT_SHARE_DAMAGE)
                .map(Map.Entry::getKey)
                .toList();
    }

    private void reward(SkyBlockPlayer player) {
        CrimsonMobLoot.roll(player, this, miniboss().getDrops());
        awardDailyReputation(player, miniboss());
        HypixelEventHandler.callCustomEvent(new CrimsonMinibossKilledEvent(player, miniboss()));
    }

    public static boolean awardDailyReputation(SkyBlockPlayer player, CrimsonMiniboss miniboss) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) return false;
        String key = DAILY_KEY + miniboss.name();
        long marker = dailyMarker(Instant.now());
        if (!isFirstKillToday(DatapointCrimsonIsle.of(player).getNumber(key), marker)) return false;
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(key, marker));
        CrimsonReputationService.add(player, faction, CrimsonMiniboss.DAILY_REPUTATION, miniboss.getDisplayName());
        return true;
    }

    /** The stored value for a kill at {@code now}: the UTC day number plus one, so an unset 0 never matches. */
    public static long dailyMarker(Instant now) {
        return LocalDate.ofInstant(now, ZoneOffset.UTC).toEpochDay() + 1;
    }

    public static boolean isFirstKillToday(long stored, long marker) {
        return stored != marker;
    }

    protected void onDefeated() {
    }

    protected List<SkyBlockPlayer> fighters(double range) {
        return playersWithin(range);
    }
}
