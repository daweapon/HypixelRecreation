package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.AshfangMinionMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Ashfang Follower (wiki: Ashfang_Follower): follows the player and can only be killed by Ashfang's Gravity
 * Orbs. Fires a fireball every 2 s and sometimes a Cryogenic Blast that slows for 5 s.
 */
public class MobAshfangFollower extends AshfangMinionMob {
    private static final double BLAST_DAMAGE = 2_000;

    public MobAshfangFollower() {
        super(CrimsonMobs.ASHFANG_FOLLOWER);
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        player.sendMessage("<8>Ashfang Follower <c>can only be damaged by Gravity.");
        return false;
    }

    @Override
    protected void onAiTick(int ticks) {
        rangedTarget(20).ifPresent(player -> {
            if (player.getPosition().distance(getPosition()) > 4) {
                setVelocity(player.getPosition().sub(getPosition()).asVec().normalize().mul(3));
            }
            if (ticks % 40 == 0) fireball(player, getProfile().damage());
            if (ticks % 200 == 100) {
                strike(player, BLAST_DAMAGE);
                player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 2, 100));
                player.sendMessage("<c>Ashfang Follower's Cryogenic Blast hit you for 2,000 damage!");
            }
        });
    }
}
