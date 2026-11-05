package dev.daweapon.type.catacombs.secret;

import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.EntityType;

import java.util.function.Consumer;

/** A bat secret: it only counts when a player kills it. */
final class SecretBat extends CrimsonIsleMob {
    private final Consumer<SkyBlockPlayer> onKilled;

    SecretBat(Consumer<SkyBlockPlayer> onKilled) {
        super(profile());
        this.onKilled = onKilled;
        setNoGravity(true);
    }

    private static CrimsonMobProfile profile() {
        DungeonMobDefinition bat = DungeonMobRegistry.defaults().mob("BAT");
        return CrimsonMobProfile.builder(bat.id(), "<c>" + bat.displayName())
                .level(1)
                .entity(EntityType.BAT)
                .stats(bat.health(), bat.damage())
                .defense(bat.defense())
                .types(MobType.ANIMAL)
                .neverCorrupted()
                .build();
    }

    @Override
    protected Temper temper() {
        return Temper.PASSIVE;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        onKilled.accept(killer);
    }
}
