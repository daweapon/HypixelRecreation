package dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

public class TrainingDummyMob extends SkyBlockMob {
    public static final float HEALTH = 2_000_000_000f;
    private static final long DPS_WINDOW_MILLIS = 5000;
    private static final long REFRESH_MILLIS = 1000;
    private static final ThreadLocal<TrainingDummyForm> CONSTRUCTING = new ThreadLocal<>();

    private record Hit(long at, double damage) {
    }

    private final TrainingDummyForm form;
    private final Deque<Hit> hits = new ArrayDeque<>();
    private PlacedTrainingDummy placed;
    private long lastRefresh;

    public TrainingDummyMob(TrainingDummyForm form) {
        super(begin(form));
        this.form = form;
        CONSTRUCTING.remove();
    }

    private static EntityType begin(TrainingDummyForm form) {
        CONSTRUCTING.set(form);
        return form.entityType();
    }

    private TrainingDummyForm form() {
        return form != null ? form : CONSTRUCTING.get();
    }

    public PlacedTrainingDummy placed() {
        return placed;
    }

    public void placed(PlacedTrainingDummy placed) {
        this.placed = placed;
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        boolean applied = super.damage(damage);
        if (applied) {
            hits.addLast(new Hit(System.currentTimeMillis(), damage.getAmount()));
            heal();
            refreshName();
        }
        return applied;
    }

    @Override
    public void kill() {
        heal();
    }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        long now = System.currentTimeMillis();
        if (hits.isEmpty() || now - lastRefresh < REFRESH_MILLIS) return;
        refreshName();
    }

    @Override
    protected String nameHeader() {
        long now = System.currentTimeMillis();
        lastRefresh = now;
        while (!hits.isEmpty() && now - hits.peekFirst().at() > DPS_WINDOW_MILLIS) hits.removeFirst();
        if (hits.isEmpty()) return "";
        double total = hits.stream().mapToDouble(Hit::damage).sum();
        double elapsed = (now - hits.peekFirst().at()) / 1000.0;
        double dps = total / Math.max(1, elapsed);
        return "<e>DPS: <f>" + NumberFormat.getIntegerInstance(Locale.US).format(Math.round(dps));
    }

    @Override
    public String getDisplayName() {
        return form().label() + " Dummy";
    }

    @Override
    public Integer getLevel() {
        return 1;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of();
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, (double) HEALTH)
                .withBase(ItemStatistic.DAMAGE, 0D)
                .withBase(ItemStatistic.SPEED, 0D)
                .build();
    }

    @Override
    public SkyBlockLootTable getLootTable() {
        return null;
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.COMBAT;
    }

    @Override
    public long damageCooldown() {
        return 0;
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(0, 0, 0);
    }

    @Override
    public List<MobType> getMobTypes() {
        return form().mobTypes();
    }

    @Override
    public boolean hasCollision() {
        return false;
    }
}
