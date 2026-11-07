package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.type.catacombs.engine.look.DungeonNameTag;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.EntityType;

import java.util.List;
import java.util.function.Consumer;

/** A Higher Or Lower blaze (M7 run 2 capture): it hangs still in the chamber, never fights back, and shows its health. */
final class PuzzleBlaze extends CrimsonIsleMob {
    private static final String ID = "PUZZLE_BLAZE";
    private static final String NAME = "Blaze";

    private final int level;
    private final Consumer<SkyBlockPlayer> onKilled;

    PuzzleBlaze(int health, int level, Consumer<SkyBlockPlayer> onKilled) {
        super(CrimsonMobProfile.builder(ID, NAME)
                .entity(EntityType.BLAZE)
                .level(level)
                .stats(health, 0)
                .speed(0)
                .types(MobType.INFERNAL)
                .neverCorrupted()
                .build());
        this.level = level;
        this.onKilled = onKilled;
        setNoGravity(true);
        refreshName();
    }

    @Override
    protected String nameLine() {
        return DungeonNameTag.format(List.of(MobType.INFERNAL), false, "<c>", false, NAME, getHealth(), getMaxHealth(), level, true);
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
