package dev.daweapon.type.catacombs.crypt;

import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.type.catacombs.engine.look.DungeonNameTag;
import dev.daweapon.type.catacombs.engine.look.MobDresser;
import dev.daweapon.type.catacombs.engine.look.MobLook;
import dev.daweapon.type.catacombs.engine.look.MobLooks;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.EntityType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

/** The Crypt Undead that climbs out of a blown-up crypt, a skinned player model holding a bone; a player kill counts the crypt. */
final class CryptUndead extends CrimsonIsleMob {
    private static final String ID = "CRYPT_UNDEAD";

    private final BiConsumer<CryptUndead, SkyBlockPlayer> onKilled;
    private final MobLook look;

    CryptUndead(BiConsumer<CryptUndead, SkyBlockPlayer> onKilled) {
        this(onKilled, MobLooks.shared().look(ID).orElseGet(() -> new MobLook(EntityType.ZOMBIE, List.of(MobType.UNDEAD),
                "<c>", false, null, null, false, null, List.of())));
    }

    private CryptUndead(BiConsumer<CryptUndead, SkyBlockPlayer> onKilled, MobLook look) {
        super(profile(look));
        this.onKilled = onKilled;
        this.look = look;
        MobDresser.dress(this, look.pick(ThreadLocalRandom.current()));
        refreshName();
    }

    private static CrimsonMobProfile profile(MobLook look) {
        DungeonMobDefinition undead = DungeonMobRegistry.defaults().mob(ID);
        return CrimsonMobProfile.builder(undead.id(), undead.displayName())
                .level(1)
                .entity(look.entity())
                .stats(undead.health(), undead.damage())
                .defense(undead.defense())
                .types(look.types().toArray(MobType[]::new))
                .neverCorrupted()
                .build();
    }

    @Override
    protected String nameLine() {
        if (look == null) return super.nameLine();
        return DungeonNameTag.format(look.types(), false, look.nameColour(), false, getProfile().displayName(),
                getHealth(), getMaxHealth(), null, false);
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
