package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Bal (wiki: Bal): the Magma Fields boss in Khazad-dum. Every hit on him shakes loose Bald Blazes and a steady
 * stream of Fire Bats keeps coming. Whoever hurt him when he sinks into the ground earns the Topaz Crystal.
 * The wiki gives no real health, so he takes 20 hits (10 of his 200 health each); the blaze and bat counts per
 * wave, their caps, the swipe range and the swipe interval are estimates.
 */
public class MobBal extends CrystalHollowsBossMob {
    private static final float HIT_DAMAGE = 10;
    private static final int BLAZES_PER_HIT = 6;
    private static final int MAX_BLAZES = 18;
    private static final int BATS_PER_WAVE = 3;
    private static final int MAX_BATS = 12;
    private static final int BAT_INTERVAL = 100;
    private static final int SWIPE_INTERVAL = 40;
    private static final double SWIPE_RANGE = 6;
    private static final double SUMMON_SPREAD = 5;
    private static final int MAGMA_CUBE_SIZE = 12;

    private final List<CrimsonIsleMob> blazes = new ArrayList<>();
    private final List<CrimsonIsleMob> bats = new ArrayList<>();

    public MobBal() {
        super(CrystalHollowsBossProfiles.BAL);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(MAGMA_CUBE_SIZE));
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected float hitDamage() {
        return HIT_DAMAGE;
    }

    @Override
    protected double hitMultiplier(SkyBlockPlayer attacker) {
        return MiningPetBonuses.cannibalMultiplier(attacker);
    }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        blazes.removeIf(mob -> mob.isDead() || mob.isRemoved());
        for (int i = 0; i < BLAZES_PER_HIT && blazes.size() < MAX_BLAZES; i++) {
            blazes.add(summon(MobBaldBlaze::new));
        }
    }

    @Override
    protected void onAiTick(int ticks) {
        List<SkyBlockPlayer> fighters = playersWithin(24);
        if (fighters.isEmpty()) return;
        if (ticks % BAT_INTERVAL == 0) {
            bats.removeIf(mob -> mob.isDead() || mob.isRemoved());
            for (int i = 0; i < BATS_PER_WAVE && bats.size() < MAX_BATS; i++) bats.add(summon(MobFireBat::new));
        }
        if (ticks % SWIPE_INTERVAL == 0) {
            fighters.stream()
                    .filter(player -> player.getPosition().distance(getPosition()) <= SWIPE_RANGE)
                    .forEach(player -> strike(player, getProfile().damage()));
        }
    }

    private CrimsonIsleMob summon(Supplier<CrimsonIsleMob> factory) {
        CrimsonIsleMob mob = factory.get();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Pos at = getPosition().add((random.nextDouble() - 0.5) * 2 * SUMMON_SPREAD, 1, (random.nextDouble() - 0.5) * 2 * SUMMON_SPREAD);
        mob.setInstance(getInstance(), at);
        return mob;
    }

    @Override
    protected void reward(SkyBlockPlayer player) {
        PetDropTable.of("BAL_PET")
                .rarity(Rarity.EPIC, 0.0126)
                .rarity(Rarity.LEGENDARY, 0.0054)
                .roll(player, this);
        claimTopazCrystal(player);
    }

    private void claimTopazCrystal(SkyBlockPlayer player) {
        if (CrystalNucleus.state(player, NucleusCrystal.TOPAZ) != CrystalNucleus.State.NOT_FOUND) return;
        CrystalNucleus.makeAvailable(player, NucleusCrystal.TOPAZ);
        player.sendMessage(Text.of("<d>A {}<d> has appeared in Khazad-dûm!", NucleusCrystal.TOPAZ.coloured()));
    }

    @Override
    protected void onDefeated() {
        despawn(blazes);
        despawn(bats);
    }

    @Override
    public void remove() {
        despawn(blazes);
        despawn(bats);
        super.remove();
    }

    private static void despawn(List<CrimsonIsleMob> mobs) {
        mobs.forEach(mob -> {
            if (!mob.isRemoved()) mob.remove();
        });
        mobs.clear();
    }
}
