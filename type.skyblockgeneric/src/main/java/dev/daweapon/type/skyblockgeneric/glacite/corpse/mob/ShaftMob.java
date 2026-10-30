package dev.daweapon.type.skyblockgeneric.glacite.corpse.mob;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.glacite.ColdService;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CutLoose;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public abstract class ShaftMob extends BestiaryMob {
    private static final double DAMAGE_PER_BREAKING_POWER = 0.25;
    private static final double DEFENSE = 1000;

    protected ShaftMob(EntityType entityType) {
        super(entityType);
    }

    protected abstract double baseHealth();

    protected abstract double attackDamage();

    protected abstract long powder();

    protected double baseSpeed() {
        return 100;
    }

    protected double coldPerHit() {
        return 1;
    }

    public static double breakingPowerMultiplier(int breakingPower) {
        return 1 + Math.max(0, breakingPower) * DAMAGE_PER_BREAKING_POWER;
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, baseHealth())
                .withBase(ItemStatistic.DAMAGE, attackDamage())
                .withBase(ItemStatistic.DEFENSE, DEFENSE)
                .withBase(ItemStatistic.SPEED, baseSpeed())
                .build();
    }

    @Override
    public float adjustHitDamage(SkyBlockPlayer attacker, float damage) {
        return (float) (damage * breakingPowerMultiplier(PlayerItemOrigin.getFromCache(attacker.getUuid()).get(PlayerItemOrigin.MAIN_HAND).getAttributeHandler().getBreakingPower()));
    }

    @Override
    public void onMeleeHit(SkyBlockPlayer target) {
        ColdService.addCold(target, -coldPerHit());
    }

    @Override
    public void kill() {
        super.kill();
        if (getLastDamageSource() == null || !(getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player)) return;
        HotmService.gainPowder(player, HotmPowder.GLACITE, powder());
        CutLoose.onGlacialKill(player);
    }

    protected static ItemStack head(String texture) {
        return ItemStacks.head(texture, dev.daweapon.commons.text.Text.empty(), List.of()).build();
    }

    protected void fire(SkyBlockPlayer target, Particle particle, int flightTicks) {
        if (getInstance() == null) return;
        net.minestom.server.coordinate.Pos from = getPosition().add(0, 1.5, 0);
        net.minestom.server.coordinate.Pos to = target.getPosition().add(0, 1, 0);
        for (int i = 0; i <= 10; i++) {
            double progress = i / 10D;
            ParticlePacket packet = new ParticlePacket(particle, true, true, from.x() + (to.x() - from.x()) * progress,
                    from.y() + (to.y() - from.y()) * progress, from.z() + (to.z() - from.z()) * progress, 0.02f, 0.02f, 0.02f, 0.02f, 1);
            getInstance().getPlayers().forEach(viewer -> viewer.sendPacket(packet));
        }
        scheduler().buildTask(() -> {
            if (isRemoved() || isDead() || target.isRemoved() || target.getInstance() != getInstance()) return;
            MobAttackService.strike(this, target);
        }).delay(TaskSchedule.tick(flightTicks)).schedule();
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.COMBAT;
    }

    @Override
    public long damageCooldown() {
        return 500;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.GLACIAL, MobType.HUMANOID);
    }

    @Override
    public int getMaxBestiaryTier() {
        return 15;
    }

    @Override
    public int getBestiaryBracket() {
        return 4;
    }
}
