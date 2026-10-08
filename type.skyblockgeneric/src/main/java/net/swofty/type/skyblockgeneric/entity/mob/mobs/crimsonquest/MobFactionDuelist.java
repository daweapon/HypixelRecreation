package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonquest;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import net.swofty.type.skyblockgeneric.entity.mob.impl.MobPlayerSkin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Rollim or Kaus in a training duel (wiki: Rollim, Kaus): only fights, and can only be hit by, the
 * player who asked for the lesson. Health and damage rise each of the 7 days; the first time it drops
 * to a quarter of its health it eats a Golden Apple and heals back to three quarters.
 */
public class MobFactionDuelist extends CrimsonIsleMob implements MobPlayerSkin {
    public static final double[] HEALTH = {50_000, 100_000, 250_000, 1_000_000, 2_500_000, 10_000_000, 25_000_000};
    public static final double[] DAMAGE = {200, 300, 400, 500, 750, 1_000, 2_500};

    private final @Nullable UUID opponent;
    private final @Nullable CrimsonFaction faction;
    private final String skin;
    private boolean ateGoldenApple;

    public MobFactionDuelist() {
        this(null, CrimsonFaction.MAGE, "Rollim", null, 1);
    }

    public MobFactionDuelist(@Nullable UUID opponent, CrimsonFaction faction, String name, @Nullable String skin, int day) {
        super(profile(faction, name, day));
        this.opponent = opponent;
        this.faction = faction;
        this.skin = skin;
        setItemInMainHand(ItemStack.of(Material.WOODEN_SWORD));
    }

    private static CrimsonMobProfile profile(CrimsonFaction faction, String name, int day) {
        int index = Math.max(0, Math.min(day, HEALTH.length) - 1);
        return CrimsonMobProfile.builder("DUELIST_" + faction.name(), faction.getColour() + name)
                .level(10 * (index + 1)).entity(EntityType.PLAYER)
                .stats(HEALTH[index], DAMAGE[index]).speed(110).rewards(0, 0, 0).neverCorrupted()
                .build();
    }

    public @Nullable UUID getOpponent() {
        return opponent;
    }

    public @Nullable CrimsonFaction getFaction() {
        return faction;
    }

    @Override
    protected boolean canTarget(SkyBlockPlayer player) {
        return player.getUuid().equals(opponent);
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return player.getUuid().equals(opponent);
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        if (!ateGoldenApple && getHealth() > 0 && getHealth() <= getMaxHealth() / 4) {
            ateGoldenApple = true;
            setHealth(getMaxHealth() * 3 / 4);
            player.sendMessage(getDisplayName() + " <e>ate a Golden Apple!");
        }
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public String getSkinTexture() {
        return skin;
    }

    @Override
    public String getSkinSignature() {
        return null;
    }
}
