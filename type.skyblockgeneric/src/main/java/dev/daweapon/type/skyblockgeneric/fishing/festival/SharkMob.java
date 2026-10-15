package dev.daweapon.type.skyblockgeneric.fishing.festival;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalPerk;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
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

    @Override
    public SkyBlockItem createDrop(ItemType type, int amount) {
        SkyBlockItem drop = super.createDrop(type, amount);
        getShark().getExtraDrops().stream()
                .filter(extra -> extra.item() == type && extra.rarity() != null)
                .findFirst()
                .ifPresent(extra -> drop.getAttributeHandler().setRarity(extra.rarity()));
        return drop;
    }

    private int finAmount(@Nullable SkyBlockPlayer killer) {
        int amount = getShark().getFinAmount();
        if (killer == null) return amount;
        double extraFinChance = Carnivals.perkValue(killer, CarnivalPerk.ELASMOBRANCHII_AFFINITY);
        return ThreadLocalRandom.current().nextDouble(100D) < extraFinChance ? amount + 1 : amount;
    }

    private double toothChance(@Nullable SkyBlockPlayer killer) {
        if (killer == null) return FestivalShark.BASE_TOOTH_CHANCE;
        double bonus = SharkToothNecklace.toothChanceBonus(killer) + Carnivals.perkValue(killer, CarnivalPerk.DENTURE_COLLECTOR);
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
