package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** A dungeon mob built from a {@link DungeonMobDefinition}: the definition's health, damage and defense, nothing else. */
public final class CatacombsMob extends CrimsonIsleMob {
    private static final String STAR = "<6>✯ <c>";
    private static final int PLACEHOLDER_LEVEL = 1;

    private final DungeonMobDefinition definition;
    private final int room;
    private final boolean starred;
    private final Listener listener;
    private boolean death;

    public interface Listener {
        void mobDied(CatacombsMob mob);
    }

    public CatacombsMob(DungeonMobDefinition definition, int room, boolean starred, Listener listener) {
        super(profile(definition, starred));
        this.definition = definition;
        this.room = room;
        this.starred = starred;
        this.listener = listener;
    }

    private static CrimsonMobProfile profile(DungeonMobDefinition definition, boolean starred) {
        return CrimsonMobProfile.builder(definition.id(), (starred ? STAR : "<c>") + definition.displayName())
                .level(PLACEHOLDER_LEVEL)
                .entity(MobEntityTypes.of(definition.id()))
                .stats(definition.health(), definition.damage())
                .defense(definition.defense())
                .types(MobType.UNDEAD)
                .neverCorrupted()
                .build();
    }

    public DungeonMobDefinition definition() {
        return definition;
    }

    public int room() {
        return room;
    }

    public boolean starred() {
        return starred;
    }

    @Override
    protected float aggroRange() {
        return 24;
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
    }

    @Override
    public void kill() {
        super.kill();
        if (death) return;
        death = true;
        listener.mobDied(this);
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }
}
