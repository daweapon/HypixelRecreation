package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrapperMob extends SkyBlockMob {
    private record Spec(TrapperRarity rarity, TrapperAnimal animal) {
    }

    private static final ThreadLocal<Spec> IN_CONSTRUCTION = new ThreadLocal<>();
    private static final int NAVIGATE_EVERY_TICKS = 10;
    private static final int ABILITY_EVERY_TICKS = 20;

    private final Spec spec;
    @Getter
    private final SkyBlockPlayer owner;
    private boolean fleeing;
    private boolean vanished;

    private TrapperMob(Spec spec, SkyBlockPlayer owner) {
        super(spec.animal().getEntityType());
        this.spec = spec;
        this.owner = owner;
    }

    public static TrapperMob create(TrapperRarity rarity, TrapperAnimal animal, SkyBlockPlayer owner) {
        IN_CONSTRUCTION.set(new Spec(rarity, animal));
        try {
            TrapperMob mob = new TrapperMob(new Spec(rarity, animal), owner);
            mob.restrictViewers(viewer -> viewer == owner);
            return mob;
        } finally {
            IN_CONSTRUCTION.remove();
        }
    }

    private Spec live() {
        return spec != null ? spec : IN_CONSTRUCTION.get();
    }

    public TrapperRarity rarity() {
        return live().rarity();
    }

    public TrapperAnimal animal() {
        return live().animal();
    }

    @Override
    public String getDisplayName() {
        return rarity().coloured() + " " + animal().getDisplayName();
    }

    @Override
    public Integer getLevel() {
        return 1 + rarity().ordinal() * 10;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new VanillaRandomStrollGoal(this, 10));
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, (double) rarity().getHealth())
                .withBase(ItemStatistic.SPEED, (double) animal().getSpeedBonus())
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return null;
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.HUNTING;
    }

    @Override
    public long damageCooldown() {
        return 200;
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(0, 0, 0);
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ANIMAL, MobType.TIMID);
    }

    public boolean vanished() {
        return vanished;
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (isDead() || isRemoved() || getInstance() == null || vanished) return;
        if (owner.isRemoved() || owner.getInstance() != getInstance()) return;
        double distance = owner.getPosition().distance(getPosition());
        if (aware(distance)) {
            vanished = true;
            TrapperService.vanished(this);
            return;
        }
        long age = getAliveTicks();
        if (age % NAVIGATE_EVERY_TICKS == 0) flee(distance);
        if (age % ABILITY_EVERY_TICKS == 0) ability(distance);
    }

    private boolean aware(double distance) {
        if (!rarity().isAware() || getAliveTicks() < 40) return false;
        return distance < (owner.isSneaking() ? 5 : 12);
    }

    private void flee(double distance) {
        int radius = rarity().getFleeRadius();
        double trigger = owner.isSneaking() ? radius / 2D : radius;
        boolean shouldFlee = radius > 0 && distance < trigger;
        if (shouldFlee != fleeing) {
            fleeing = shouldFlee;
            int speed = shouldFlee ? rarity().getFleeSpeed() + (animal() == TrapperAnimal.HORSE ? 120 : 0) : animal().getSpeedBonus();
            getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue((float) (speed / 1000D * 2.5));
        }
        if (!shouldFlee) return;
        Vec away = getPosition().sub(owner.getPosition()).asVec().withY(0);
        if (away.lengthSquared() < 0.01) away = new Vec(1, 0, 0);
        Pos target = getPosition().add(away.normalize().mul(12));
        getVanillaNavigator().pathTo(target, 200);
    }

    private void ability(double distance) {
        if (rarity().isBlinds() && distance < 6) owner.addEffect(new Potion(PotionEffect.BLINDNESS, (byte) 0, 60));
        switch (animal()) {
            case COW -> setInvisible(distance < 8);
            case PIG -> {
                if (distance < 6) owner.addEffect(new Potion(PotionEffect.NAUSEA, (byte) 0, 100));
            }
            case SHEEP -> {
                if (distance < 2.5) ram();
            }
            case RABBIT -> setVelocity(getVelocity().withY(9));
            default -> {
            }
        }
    }

    private void ram() {
        Vec direction = owner.getPosition().sub(getPosition()).asVec().withY(0);
        if (direction.lengthSquared() < 0.01) return;
        owner.setVelocity(direction.normalize().mul(28).withY(16));
    }

    @Override
    public void kill() {
        var source = getLastDamageSource();
        boolean byPlayer = source != null && source.getAttacker() instanceof SkyBlockPlayer;
        if (byPlayer) super.kill();
        else remove();
        TrapperService.killed(this);
    }
}
