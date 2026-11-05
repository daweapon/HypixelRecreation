package dev.daweapon.type.catacombs.crypt;

import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.EntityType;

import java.util.function.BiConsumer;

/** The Crypt Undead that climbs out of a blown-up crypt; a player kill counts the crypt. */
final class CryptUndead extends CrimsonIsleMob {
    private final BiConsumer<CryptUndead, SkyBlockPlayer> onKilled;

    CryptUndead(BiConsumer<CryptUndead, SkyBlockPlayer> onKilled) {
        super(profile());
        this.onKilled = onKilled;
    }

    private static CrimsonMobProfile profile() {
        DungeonMobDefinition undead = DungeonMobRegistry.defaults().mob("CRYPT_UNDEAD");
        return CrimsonMobProfile.builder(undead.id(), "<c>" + undead.displayName())
                .level(1)
                .entity(EntityType.ZOMBIE)
                .stats(undead.health(), undead.damage())
                .defense(undead.defense())
                .types(MobType.UNDEAD)
                .neverCorrupted()
                .build();
    }

    @Override
    protected float aggroRange() {
        return 24;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        onKilled.accept(this, killer);
    }
}
