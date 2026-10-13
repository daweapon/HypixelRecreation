package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.entity.damage.Damage;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import net.swofty.type.skyblockgeneric.crimson.mob.MobDrop;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A Crystal Hollows boss or worm. Everyone who dealt enough damage gets the kill, the Combat XP and their own roll of
 * the loot table. A mob with a hit damage loses that much health per player hit, whatever the weapon.
 */
public abstract class CrystalHollowsBossMob extends CrimsonIsleMob {

    protected CrystalHollowsBossMob(CrimsonMobProfile profile) {
        super(profile);
    }

    @Override
    public boolean countsForBestiary() {
        return getProfile().inBestiary();
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    protected List<MobDrop> drops() {
        return List.of();
    }

    protected float hitDamage() {
        return 0;
    }

    protected double lootShareDamage() {
        return 1;
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        float fixed = hitDamage();
        if (fixed > 0 && damage.getAttacker() instanceof SkyBlockPlayer) {
            damage = new Damage(damage.getType(), damage.getSource(), damage.getAttacker(), damage.getSourcePosition(), fixed);
        }
        return super.damage(damage);
    }

    public List<UUID> lootSharers() {
        double threshold = lootShareDamage();
        return getDamageDealt().entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .map(Map.Entry::getKey)
                .toList();
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
            CrimsonMobLoot.roll(player, this, getProfile().drops());
            reward(player);
        }
        onDefeated();
    }

    protected void reward(SkyBlockPlayer player) {
    }

    protected void onDefeated() {
    }
}
