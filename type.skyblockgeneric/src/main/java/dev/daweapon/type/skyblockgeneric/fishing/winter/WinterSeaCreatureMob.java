package dev.daweapon.type.skyblockgeneric.fishing.winter;

import lombok.Getter;
import lombok.NonNull;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.gifts.ReindeerPet;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Winter Sea Creature fished up on Jerry's Workshop. The creature travels through {@link #begin}
 * because SkyBlockMob's constructor reads stats before this class can assign fields. Ice Essence
 * is a currency, so it is paid to the killer on death rather than dropped.
 */
public class WinterSeaCreatureMob extends BestiaryMob {
    private static final ThreadLocal<WinterSeaCreature> CONSTRUCTING = new ThreadLocal<>();
    private static final byte ALL_SKIN_PARTS = 127;

    private final WinterSeaCreature creature;
    @Getter
    private final @Nullable SkyBlockPlayer angler;
    private @Nullable Task ticker;
    @Getter
    private int ticksAlive;

    protected WinterSeaCreatureMob(WinterSeaCreature creature, @Nullable SkyBlockPlayer angler) {
        super(begin(creature));
        this.creature = CONSTRUCTING.get();
        CONSTRUCTING.remove();
        this.angler = angler;
        String skin = this.creature.getSkinTexture();
        if (skin != null) {
            editEntityMeta(MannequinMeta.class, meta -> {
                meta.setProfile(new ResolvableProfile(new PlayerSkin(encodeSkin(skin), null)));
                meta.setDescription(null);
                meta.setDisplayedSkinParts(ALL_SKIN_PARTS);
            });
        }
    }

    public static WinterSeaCreatureMob of(WinterSeaCreature creature, @Nullable SkyBlockPlayer angler) {
        return switch (creature) {
            case FROSTY_THE_SNOWMAN -> new FrostyMob(angler);
            case GRINCH -> new GrinchMob(angler);
            case NUTCRACKER -> new NutcrackerMob(angler);
            case YETI -> new YetiMob(angler);
            case REINDRAKE -> new ReindrakeMob(angler);
            default -> new WinterSeaCreatureMob(creature, angler);
        };
    }

    private static net.minestom.server.entity.EntityType begin(WinterSeaCreature creature) {
        CONSTRUCTING.set(creature);
        return creature.getEntityType();
    }

    static String encodeSkin(String textureHash) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + textureHash + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public WinterSeaCreature getCreature() {
        return creature != null ? creature : CONSTRUCTING.get();
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        ticker = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (isRemoved() || isDead()) {
                stopTicking();
                return;
            }
            onTick(++ticksAlive);
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    protected void onTick(int ticks) {
    }

    private void stopTicking() {
        if (ticker != null) ticker.cancel();
        ticker = null;
    }

    @Override
    public void remove() {
        stopTicking();
        super.remove();
    }

    protected @Nullable SkyBlockPlayer killer() {
        return getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player
                ? player : null;
    }

    @Override
    public void kill() {
        SkyBlockPlayer killer = killer();
        stopTicking();
        super.kill();
        if (killer == null) return;
        awardIceEssence(killer);
        onKilledBy(killer);
    }

    protected void onKilledBy(SkyBlockPlayer killer) {
    }

    private void awardIceEssence(SkyBlockPlayer killer) {
        WinterSeaCreature spec = getCreature();
        if (spec.getMaxIceEssence() <= 0) return;
        int amount = ThreadLocalRandom.current().nextInt(spec.getMinIceEssence(), spec.getMaxIceEssence() + 1);
        if (doublesIceEssence(killer)) amount *= 2;
        Essences.add(killer, EssenceType.ICE, amount);
        killer.sendMessage("<d><l>ESSENCE! </l><b>Ice Essence <8>x{}", amount);
    }

    static boolean doublesIceEssence(SkyBlockPlayer player) {
        return LootRoll.roll(null, null, ReindeerPet.icyWindPercent(player));
    }

    protected List<SkyBlockLootTable.LootRecord> rollDrops() {
        WinterSeaCreature spec = getCreature();
        List<SkyBlockLootTable.LootRecord> records = new ArrayList<>();
        for (WinterDrop drop : spec.getDrops()) {
            int amount = drop.rollAmount();
            if (amount > 0) records.add(new SkyBlockLootTable.LootRecord(drop.item(), amount, drop.chancePercent()));
        }
        double partyGiftChance = WinterGifts.partyGiftChance(spec.getRarity());
        if (partyGiftChance > 0 && WinterGifts.timeForGiving()) {
            records.add(new SkyBlockLootTable.LootRecord(ItemType.PARTY_GIFT, 1, partyGiftChance));
        }
        return records;
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        List<SkyBlockLootTable.LootRecord> records = rollDrops();
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
        if (type == ItemType.ENCHANTED_BOOK) {
            drop.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.PROSPERITY, 1));
        }
        return drop;
    }

    @Override
    public String getDisplayName() {
        return getCreature().getDisplayName();
    }

    @Override
    public Integer getLevel() {
        return getCreature().getLevel();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return getCreature().getProfile().behaviour().goals(this);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return getCreature().getProfile().behaviour().targets(this);
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        WinterSeaCreature spec = getCreature();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, spec.getHealth())
                .withBase(ItemStatistic.DAMAGE, spec.getDamage())
                .withBase(ItemStatistic.DEFENSE, spec.getDefense())
                .withBase(ItemStatistic.SPEED, spec.getSpeed())
                .build();
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.FISHING;
    }

    @Override
    public long damageCooldown() {
        return 400;
    }

    @Override
    public OtherLoot getOtherLoot() {
        WinterSeaCreature spec = getCreature();
        return new OtherLoot(spec.getFishingXp(), spec.getCoins(), spec.getXpOrbs());
    }

    @Override
    public List<MobType> getMobTypes() {
        return getCreature().getMobTypes();
    }

    @Override
    public int getMaxBestiaryTier() {
        return getCreature().getBestiary().maxTier();
    }

    @Override
    public int getBestiaryBracket() {
        return getCreature().getBestiary().bracket();
    }

    @Override
    public String getMobID() {
        return getCreature().name();
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return getCreature().getGuiMaterial();
    }
}
