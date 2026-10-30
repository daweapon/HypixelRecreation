package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.particle.Particle;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureCapture;
import dev.daweapon.type.skyblockgeneric.hunting.creature.Lassoable;
import dev.daweapon.type.skyblockgeneric.hunting.creature.LassoService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Dive Ghast (wiki: Dive_Ghast): no fireballs; it hisses, dives at the player and explodes for 10,000. It leaves
 * anyone wearing a Netherrack-Looking Sunshade alone and can be lassoed like a forest creature (the wiki marks it
 * lasso and charm). The dive cooldown is an estimate.
 */
public class MobDiveGhast extends CrimsonIsleMob implements Lassoable {
    public MobDiveGhast() {
        super(CrimsonMobs.DIVE_GHAST);
    }

    private static final String SHARD = "Dive Ghast";
    private static final double EXPLOSION_RADIUS = 3;
    private static final double SHARD_CHANCE = 100;
    private static final int DIVE_COOLDOWN_TICKS = 140;
    private int cooldown = 40;
    private SkyBlockPlayer diving;
    private String header = "";
    private boolean captured;

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    protected boolean canTarget(SkyBlockPlayer player) {
        return !wearsSunshade(player);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
    }

    @Override
    protected String nameHeader() {
        return header;
    }

    @Override
    public void setHeader(String text) {
        header = text;
        refreshName();
    }

    @Override
    public String lassoName() {
        return getDisplayName();
    }

    @Override
    public int lassoBars() {
        return AttributeRegistry.findByShard(SHARD).map(shard -> shard.rarity().ordinal() + 1).orElse(1);
    }

    @Override
    public boolean lassoAllowed() {
        return !captured;
    }

    @Override
    public void capture(SkyBlockPlayer player) {
        if (captured || isDead() || isRemoved()) return;
        captured = true;
        AttributeRegistry.findByShard(SHARD).ifPresentOrElse(
                shard -> CreatureCapture.awardShard(player, shard, getDisplayName(), SHARD_CHANCE, base -> base, ShardCaughtEvent.Source.LASSO),
                () -> player.sendMessage("<a><l>CAPTURED!</l> <7>You caught <e>{}<7>!", getDisplayName()));
        remove();
    }

    @Override
    protected void onAiTick(int ticks) {
        if (LassoService.isLassoed(this)) {
            diving = null;
            return;
        }
        if (diving != null) {
            dive();
            return;
        }
        if (--cooldown > 0) return;
        rangedTarget(32).ifPresent(player -> {
            diving = player;
            MythologicalCombat.sound(this, "entity.ghast.warn", 2f, 1f);
            MythologicalCombat.particles(getInstance(), Particle.FLAME, getPosition().add(0, 2, 0), 1.5f, 20);
        });
    }

    private void dive() {
        if (diving.isDead() || diving.getInstance() != getInstance() || wearsSunshade(diving)) {
            diving = null;
            cooldown = DIVE_COOLDOWN_TICKS;
            return;
        }
        Vec toward = diving.getPosition().sub(getPosition()).asVec();
        if (toward.length() > EXPLOSION_RADIUS) {
            setVelocity(toward.normalize().mul(18));
            return;
        }
        MythologicalCombat.particles(getInstance(), Particle.EXPLOSION, getPosition(), 1f, 3);
        MythologicalCombat.sound(this, "entity.generic.explode", 2f, 1f);
        playersWithin(EXPLOSION_RADIUS + 1).forEach(player -> strike(player, getProfile().damage()));
        teleport(getPosition().add(0, 15, 0));
        diving = null;
        cooldown = DIVE_COOLDOWN_TICKS;
    }

    private static boolean wearsSunshade(SkyBlockPlayer player) {
        return new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType() == ItemType.NETHERRACK_LOOKING_SUNSHADE
                || AccessoryOwnership.owns(player, ItemType.NETHERRACK_LOOKING_SUNSHADE);
    }
}
