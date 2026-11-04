package dev.daweapon.type.skyblockgeneric.potion.mixin;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.potion.PotionEffectType;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Every Mixin (wiki: Mixins) with the name it shows on a God Potion, its effect and who may consume it. A Mixin whose
 * effect needs a system this server lacks keeps the reason and cannot be consumed.
 */
@Getter
public enum MixinType {
    ZOMBIE_BRAIN_MIXIN("Zombie Brain", flat(ItemStatistic.FEROCITY, 10), slayer(SlayerType.REVENANT_HORROR), "Zombie Slayer 8", null),
    SPIDER_EGG_MIXIN("Spider Egg", flat(ItemStatistic.CRITICAL_DAMAGE, 15), slayer(SlayerType.TARANTULA_BROODFATHER), "Spider Slayer 8", null),
    WOLF_FUR_MIXIN("Wolf Fur", none(), slayer(SlayerType.SVEN_PACKMASTER), "Wolf Slayer 8",
            "Its Magic Find on one-hit kills isn't built yet"),
    END_PORTAL_FUMES("End Portal Fumes", none(), slayer(SlayerType.VOIDGLOOM_SERAPH), "Enderman Slayer 8",
            "Soulflow overflow isn't built yet"),
    GABAGOEY_MIXIN("Gabagoey", player -> ItemStatistics.builder()
            .withMultiplicativePercentage(ItemStatistic.TRUE_DEFENSE, 5D).build(),
            slayer(SlayerType.INFERNO_DEMONLORD), "Blaze Slayer 8", null),
    DEEPTERROR_MIXIN("Deepterror", MixinType::deepterror, anyone(), null, null),
    MUSHED_MUSHROOM_MIXIN("Glowing Mush", MixinType::glowingMush, anyone(), null, null),
    HOT_CHOCOLATE_MIXIN("Hot Chocolate", flat(ItemStatistic.PET_LUCK, 15),
            player -> ChocolateFactoryHelper.getData(player).getPrestigeLevel() >= 5, "Chocolate Factory VI", null),
    HOTSPOT_TONIC_MIXIN("Hotspot Tonic", none(), anyone(), null, null),
    ;

    public static final double SLAYER_LEVEL = 8;
    public static final double HOT_CHOCOLATE_MULTIPLIER = 0.05;
    public static final double HOTSPOT_TONIC_CHANCE = 10;

    private final String godPotionName;
    private final Function<SkyBlockPlayer, ItemStatistics> statistics;
    private final Predicate<SkyBlockPlayer> requirement;
    private final @Nullable String requirementName;
    private final @Nullable String unavailableReason;

    MixinType(String godPotionName, Function<SkyBlockPlayer, ItemStatistics> statistics,
              Predicate<SkyBlockPlayer> requirement, @Nullable String requirementName, @Nullable String unavailableReason) {
        this.godPotionName = godPotionName;
        this.statistics = statistics;
        this.requirement = requirement;
        this.requirementName = requirementName;
        this.unavailableReason = unavailableReason;
    }

    public boolean isAvailable() {
        return unavailableReason == null;
    }

    public boolean meetsRequirement(SkyBlockPlayer player) {
        return requirement.test(player);
    }

    public static @Nullable MixinType byItemId(String itemId) {
        for (MixinType type : values()) {
            if (type.name().equals(itemId)) return type;
        }
        return null;
    }

    private static Function<SkyBlockPlayer, ItemStatistics> flat(ItemStatistic statistic, double value) {
        return player -> ItemStatistics.builder().withBase(statistic, value).build();
    }

    private static Function<SkyBlockPlayer, ItemStatistics> none() {
        return player -> ItemStatistics.empty();
    }

    private static Predicate<SkyBlockPlayer> slayer(SlayerType type) {
        return player -> SlayerRequirement.level(player, type) >= SLAYER_LEVEL;
    }

    private static Predicate<SkyBlockPlayer> anyone() {
        return player -> true;
    }

    private static ItemStatistics deepterror(SkyBlockPlayer player) {
        ServerType server = HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
        if (server != ServerType.SKYBLOCK_THE_END && server != ServerType.SKYBLOCK_CRIMSON_ISLE) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 210D)
                .withBase(ItemStatistic.DEFENSE, 40D)
                .build();
    }

    private static ItemStatistics glowingMush(SkyBlockPlayer player) {
        String tonic = PotionEffectType.MUSHED_GLOWY_TONIC.getLevelDisplay(1);
        boolean fromTonic = player.getStatistics().getDisplayableActiveEffects().stream()
                .anyMatch(effect -> tonic.equals(effect.getDisplayName()));
        return fromTonic ? ItemStatistics.empty() : ItemStatistics.builder().withBase(ItemStatistic.FISHING_SPEED, 30D).build();
    }
}
