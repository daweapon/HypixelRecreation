package net.swofty.type.skyblockgeneric.fishing.festival;

import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Shark fished up during the Fishing Festival. The shark travels through {@link #begin} because
 * SkyBlockMob's constructor reads stats before this class can assign fields.
 */
public class SharkMob extends BestiaryMob {
    private static final ThreadLocal<FestivalShark> CONSTRUCTING = new ThreadLocal<>();

    private final FestivalShark shark;

    protected SharkMob(FestivalShark shark) {
        super(begin(shark));
        this.shark = CONSTRUCTING.get();
        CONSTRUCTING.remove();
    }

    public static SharkMob of(FestivalShark shark) {
        return new SharkMob(shark);
    }

    private static EntityType begin(FestivalShark shark) {
        CONSTRUCTING.set(shark);
        return shark.getProfile().entityType();
    }

    public FestivalShark getShark() {
        return shark != null ? shark : CONSTRUCTING.get();
    }

    @Override
    public void kill() {
        super.kill();
        if (getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player) {
            SharkBestiary.updateKillsXP(player);
        }
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        SkyBlockPlayer killer = getLastDamageSource() != null
                && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player ? player : null;
        List<SkyBlockLootTable.LootRecord> records = new ArrayList<>();
        records.add(new SkyBlockLootTable.LootRecord(ItemType.SHARK_FIN, finAmount(killer), 100));
        records.add(new SkyBlockLootTable.LootRecord(getShark().getTooth(), 1, toothChance(killer)));
        getShark().getExtraDrops().stream()
                .filter(SharkDrop::isAvailable)
                .forEach(drop -> records.add(new SkyBlockLootTable.LootRecord(drop.item(), drop.amount(), drop.chancePercent())));
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return records;
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    private int finAmount(@Nullable SkyBlockPlayer killer) {
        int amount = getShark().getFinAmount();
        if (killer == null) return amount;
        double extraFinChance = FishingFestivalPerk.ELASMOBRANCHII_AFFINITY.bonus(killer);
        return ThreadLocalRandom.current().nextDouble(100D) < extraFinChance ? amount + 1 : amount;
    }

    private double toothChance(@Nullable SkyBlockPlayer killer) {
        if (killer == null) return FestivalShark.BASE_TOOTH_CHANCE;
        double bonus = SharkToothNecklace.toothChanceBonus(killer) + FishingFestivalPerk.DENTURE_COLLECTOR.bonus(killer);
        return FestivalShark.BASE_TOOTH_CHANCE * (1 + bonus / 100D);
    }

    @Override
    public String getDisplayName() {
        return getShark().getDisplayName();
    }

    @Override
    public Integer getLevel() {
        return getShark().getLevel();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return getShark().getProfile().behaviour().goals(this);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return getShark().getProfile().behaviour().targets(this);
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return getShark().getProfile().asBaseStatistics();
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.FISHING;
    }

    @Override
    public long damageCooldown() {
        return getShark().getProfile().damageCooldownMs();
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(getShark().getFishingXp(), getShark().getCoins(), getShark().getXpOrbs());
    }

    @Override
    public List<MobType> getMobTypes() {
        return getShark().getMobTypes();
    }

    @Override
    public int getMaxBestiaryTier() {
        return getShark().getBestiary().maxTier();
    }

    @Override
    public int getBestiaryBracket() {
        return getShark().getBestiary().bracket();
    }

    @Override
    public String getMobID() {
        return getShark().name();
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return new GUIMaterial(getShark().getHeadTexture());
    }
}
