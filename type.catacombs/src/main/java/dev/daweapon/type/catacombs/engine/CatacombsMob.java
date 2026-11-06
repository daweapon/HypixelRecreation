package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.type.catacombs.engine.look.DungeonNameTag;
import dev.daweapon.type.catacombs.engine.look.MobDresser;
import dev.daweapon.type.catacombs.engine.look.MobLook;
import dev.daweapon.type.catacombs.engine.look.MobLooks;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A dungeon mob built from a {@link DungeonMobDefinition}: the definition's health, damage and defense, dressed in its
 * captured Hypixel look ({@link MobLooks}) with Hypixel's dungeon name tag.
 */
public final class CatacombsMob extends CrimsonIsleMob {
    private static final int PLACEHOLDER_LEVEL = 1;

    private final DungeonMobDefinition definition;
    private final int room;
    private final boolean starred;
    private final Listener listener;
    private final MobLook look;
    private boolean death;

    public interface Listener {
        void mobDied(CatacombsMob mob);
    }

    public CatacombsMob(DungeonMobDefinition definition, int room, boolean starred, Listener listener) {
        this(definition, room, starred, listener, lookOf(definition.id()));
    }

    public CatacombsMob(DungeonMobDefinition definition, int room, boolean starred, Listener listener, MobLook look) {
        super(profile(definition, look));
        this.definition = definition;
        this.room = room;
        this.starred = starred;
        this.listener = listener;
        this.look = look;
        MobDresser.dress(this, look.pick(ThreadLocalRandom.current()));
        refreshName();
    }

    static MobLook lookOf(String mobId) {
        return MobLooks.shared().look(mobId).orElseGet(() -> new MobLook(MobEntityTypes.of(mobId), List.of(MobType.UNDEAD),
                "<c>", false, null, null, false, null, List.of()));
    }

    private static CrimsonMobProfile profile(DungeonMobDefinition definition, MobLook look) {
        return CrimsonMobProfile.builder(definition.id(), look.displayName() != null ? look.displayName() : definition.displayName())
                .level(PLACEHOLDER_LEVEL)
                .entity(look.entity())
                .stats(definition.health(), definition.damage())
                .defense(definition.defense())
                .types(look.types().toArray(MobType[]::new))
                .neverCorrupted()
                .build();
    }

    @Override
    protected String nameLine() {
        if (look == null) return super.nameLine();
        return DungeonNameTag.format(look.types(), starred, look.nameColour(), look.bold(), getProfile().displayName(),
                getHealth(), getMaxHealth(), look.level(), look.showMaxHealth());
    }

    @Override
    protected String nameHeader() {
        return look == null || look.header() == null ? "" : look.header();
    }

    public DungeonMobDefinition definition() {
        return definition;
    }

    public MobLook look() {
        return look;
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
