package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonSkins;
import dev.daweapon.type.skyblockgeneric.crimson.mob.LavaBeam;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMinibossMob;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.mythological.ability.TemporaryBlocks;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mage Outlaw (wiki: Mage_Outlaw): fights in the Courtyard and can only be hurt from inside it. He cycles through
 * an End Crystal that zaps the Courtyard and pulls back anyone who leaves (11 s, or until 10M damage), a
 * reflective shield with either lightning under every player or a growing ring of redstone, and two Summoned
 * Skulls that become stronger orbiting skulls if they reach him. Damage numbers for the zaps, lightning and
 * ring, and the shield's 2,000 reflect, are estimates.
 */
public class MobMageOutlaw extends CrimsonMinibossMob {
    private static final double CRYSTAL_ZAP = 1_500;
    private static final double LIGHTNING_DAMAGE = 10_000;
    private static final double RING_DAMAGE = 4_000;
    private static final double REFLECT_DAMAGE = 2_000;
    private static final double CRYSTAL_DAMAGE_CAP = 10_000_000;
    private static final int PATTERN_TICKS = 220;
    private static final int REST_TICKS = 60;
    private static final double SUMMONED_ARENA = 25;

    private enum Pattern { CRYSTAL, SHIELD_LIGHTNING, SHIELD_RING, SKULLS }

    private final List<MobSummonedSkull> skulls = new ArrayList<>();
    private final TemporaryBlocks markers = new TemporaryBlocks();
    private final Map<Pos, Integer> strikes = new HashMap<>();
    private @Nullable Pattern pattern;
    private @Nullable Entity crystal;
    private int patternTicks;
    private int rest = REST_TICKS;
    private double crystalDamage;

    public MobMageOutlaw() {
        super(CrimsonMobs.MAGE_OUTLAW);
    }

    @Override
    public CrimsonMiniboss miniboss() {
        return CrimsonMiniboss.MAGE_OUTLAW;
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        CrimsonSkins.dress(this, CrimsonSkins.MAGE_OUTLAW);
    }

    private static boolean inCourtyard(Pos position) {
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(position);
        return region != null && region.getType() == RegionType.COURTYARD;
    }

    private boolean inArena(Pos position) {
        return atNaturalSpawn() ? inCourtyard(position) : position.distance(home()) <= SUMMONED_ARENA;
    }

    private List<SkyBlockPlayer> courtyardPlayers() {
        return playersWithin(40).stream().filter(player -> inArena(player.getPosition())).toList();
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        if (!inArena(player.getPosition())) return false;
        if (shielded()) {
            trueStrike(player, REFLECT_DAMAGE);
            return false;
        }
        return pattern != Pattern.SKULLS || patternTicks < PATTERN_TICKS - 20;
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        if (pattern == Pattern.CRYSTAL) crystalDamage += damage.getAmount();
    }

    private boolean shielded() {
        return pattern == Pattern.SHIELD_LIGHTNING || pattern == Pattern.SHIELD_RING;
    }

    @Override
    protected void onAiTick(int ticks) {
        skulls.removeIf(skull -> skull.isDead() || skull.isRemoved());
        List<SkyBlockPlayer> players = courtyardPlayers();
        if (pattern == null) {
            if (players.isEmpty() || --rest > 0) return;
            begin(Pattern.values()[(int) (Math.random() * Pattern.values().length)], players);
            return;
        }
        patternTicks--;
        switch (pattern) {
            case CRYSTAL -> tickCrystal(players);
            case SHIELD_LIGHTNING -> tickLightning();
            case SHIELD_RING -> tickRing(players);
            case SKULLS -> {
            }
        }
        if (shielded()) MythologicalCombat.particles(getInstance(), Particle.SOUL_FIRE_FLAME, getPosition().add(0, 1, 0), 1.2f, 10);
        if (patternTicks <= 0 || (pattern == Pattern.CRYSTAL && crystalDamage >= CRYSTAL_DAMAGE_CAP)) end();
    }

    private void begin(Pattern next, List<SkyBlockPlayer> players) {
        pattern = next;
        patternTicks = PATTERN_TICKS;
        crystalDamage = 0;
        switch (next) {
            case CRYSTAL -> {
                crystal = new Entity(EntityType.END_CRYSTAL);
                crystal.setNoGravity(true);
                crystal.setInstance(getInstance(), home().add(0, 4, 0));
            }
            case SHIELD_LIGHTNING -> {
                teleport(home().add(0, 3, 0));
                setNoGravity(true);
                for (SkyBlockPlayer player : players) {
                    Pos under = player.getPosition().sub(0, 1, 0);
                    Pos key = new Pos(under.blockX(), under.blockY(), under.blockZ());
                    if (markers.place(getInstance(), key, LootRoll.roll(null, null, 50) ? Block.NETHER_BRICKS : Block.SEA_LANTERN, true)) {
                        strikes.put(key, 20);
                    }
                }
            }
            case SHIELD_RING -> {
                teleport(home().add(0, 3, 0));
                setNoGravity(true);
            }
            case SKULLS -> {
                for (int side : new int[]{-1, 1}) {
                    MobSummonedSkull skull = new MobSummonedSkull(this, false);
                    skull.setInstance(getInstance(), home().add(side * 8, 2, 0));
                    skulls.add(skull);
                }
            }
        }
    }

    private void tickCrystal(List<SkyBlockPlayer> players) {
        if (crystal == null || patternTicks % 20 != 0) return;
        for (SkyBlockPlayer player : players) {
            LavaBeam.draw(getInstance(), crystal.getPosition(), player.getPosition().add(0, 1, 0), Particle.END_ROD);
            strike(player, CRYSTAL_ZAP);
        }
        for (SkyBlockPlayer player : playersWithin(40)) {
            if (!inArena(player.getPosition()) && isProvokedBy(player)) player.teleport(home().add(4, 0, 0));
        }
    }

    private void tickLightning() {
        strikes.replaceAll((pos, delay) -> delay - 1);
        strikes.entrySet().removeIf(entry -> {
            if (entry.getValue() > 0) return false;
            Pos top = entry.getKey().add(0.5, 1, 0.5);
            MythologicalCombat.lightning(getInstance(), top);
            playersWithin(40).stream().filter(player -> player.getPosition().distance(top) <= 1.5)
                    .forEach(player -> strike(player, LIGHTNING_DAMAGE));
            return true;
        });
    }

    private void tickRing(List<SkyBlockPlayer> players) {
        double radius = 1 + 11.0 * (PATTERN_TICKS - patternTicks) / PATTERN_TICKS;
        Pos centre = home();
        LavaBeam.ring(getInstance(), centre, radius, Particle.DUST);
        if (patternTicks % 20 != 0) return;
        players.stream().filter(player -> player.getPosition().distance(centre) <= radius)
                .forEach(player -> strike(player, RING_DAMAGE));
    }

    private void end() {
        if (crystal != null) crystal.remove();
        crystal = null;
        markers.restoreAll();
        strikes.clear();
        if (shielded()) {
            setNoGravity(false);
            teleport(home());
        }
        pattern = null;
        rest = REST_TICKS;
    }

    void empower(MobSummonedSkull skull) {
        Pos at = skull.getPosition();
        skull.remove();
        MobSummonedSkull strong = new MobSummonedSkull(this, true);
        strong.setInstance(getInstance(), at);
        skulls.add(strong);
    }

    Vec towardMe(Pos from) {
        return getPosition().add(0, 1.5, 0).sub(from).asVec();
    }

    @Override
    public void remove() {
        end();
        skulls.forEach(MobSummonedSkull::remove);
        super.remove();
    }
}
