package dev.daweapon.type.skyblockgeneric.enchantment;

import lombok.Getter;
import lombok.SneakyThrows;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.EnchantmentCatalogue;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.StatEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.*;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

// Registry of every enchantment, each backed by its implementing Ench class instance, with
// helpers for apply cost, description, and table-source lookup.
@Getter
public enum EnchantmentType {
    SHARPNESS(EnchantmentSharpness.class),
    EFFICIENCY(EnchantmentEfficiency.class),
    SCAVENGER(EnchantmentScavenger.class),
    PROTECTION(EnchantmentProtection.class),
    CRITICAL(EnchantmentCritical.class),
    FIRST_STRIKE(EnchantmentFirstStrike.class),
    GROWTH(EnchantmentGrowth.class),
    LUCK(EnchantmentLuck.class),
    LOOTING(EnchantmentLooting.class),
    ENDER_SLAYER(EnchantmentEnderSlayer.class),
    SMITE(EnchantmentSmite.class),
    SILK_TOUCH(EnchantmentSilkTouch.class),
    SMELTING_TOUCH(EnchantmentSmeltingTouch.class),
    GIANT_KILLER(EnchantmentGiantKiller.class),
    EXECUTE(EnchantmentExecute.class),
    IMPALING(EnchantmentImpaling.class),
    CASTER(EnchantmentCaster.class),
    CHARM(EnchantmentCharm.class),
    ANGLER(EnchantmentAngler.class),
    BLESSING(EnchantmentBlessing.class),
    CORRUPTION(EnchantmentCorruption.class),
    EXPERTISE(EnchantmentExpertise.class),
    FLASH(EnchantmentFlash.class),
    FRAIL(EnchantmentFrail.class),
    LUCK_OF_THE_SEA(EnchantmentLuckOfTheSea.class),
    LURE(EnchantmentLure.class),
    MAGNET(EnchantmentMagnet.class),
    PISCARY(EnchantmentPiscary.class),
    QUICK_BITE(EnchantmentQuickBite.class),
    SPIKED_HOOK(EnchantmentSpikedHook.class),
    TABASCO(EnchantmentTabasco.class),
    BANE_OF_ARTHROPODS(EnchantmentBaneOfArthropods.class),
    CUBISM(EnchantmentCubism.class),
    GRAVITY(EnchantmentGravity.class),
    SMOLDERING(EnchantmentSmoldering.class),
    SUGAR_RUSH(EnchantmentSugarRush.class),
    WOODSPLITTER(EnchantmentWoodsplitter.class),
    FORTUNE(EnchantmentFortune.class),
    CLEAVE(EnchantmentCleave.class),
    EXPERIENCE(EnchantmentExperience.class),
    PALEONTOLOGIST(EnchantmentPaleontologist.class),
    FIRE_ASPECT(EnchantmentFireAspect.class),
    KNOCKBACK(EnchantmentKnockback.class),
    LETHALITY(EnchantmentLethality.class),
    LIFE_STEAL(EnchantmentLifeSteal.class),
    DRAIN(EnchantmentDrain.class),
    MANA_STEAL(EnchantmentManaSteal.class),
    PROSECUTE(EnchantmentProsecute.class),
    THUNDERBOLT(EnchantmentThunderbolt.class),
    THUNDERLORD(EnchantmentThunderlord.class),
    VAMPIRISM(EnchantmentVampirism.class),
    VENOMOUS(EnchantmentVenomous.class),
    CHIMERA(EnchantmentChimera.class),
    VICIOUS(EnchantmentVicious.class),
    PROSPERITY(EnchantmentProsperity.class),
    REFRIGERATE(EnchantmentRefrigerate.class),
    MAGMARIZER(EnchantmentPyroclasm.class),
    CAYENNE(EnchantmentCayenne.class),
    HABANERO_TACTICS(EnchantmentHabaneroTactics.class),
    STRONG_MANA(EnchantmentStrongVitality.class),
    HARDENED_MANA(EnchantmentHardenedVitality.class),
    FEROCIOUS_MANA(EnchantmentVivaciousVitality.class),
    MANA_VAMPIRE(EnchantmentVampiricVitality.class),
    INFERNO(EnchantmentInferno.class),
    FATAL_TEMPO(EnchantmentFatalTempo.class),
    FLAME(EnchantmentFlame.class),
    FIRE_PROTECTION(EnchantmentFireProtection.class),
    ABSORB(CatalogueEnchantment.of("ABSORB")),
    AQUA_AFFINITY(CatalogueEnchantment.of("AQUA_AFFINITY")),
    BIG_BRAIN(StatEnchantment.of("BIG_BRAIN").linear(ItemStatistic.INTELLIGENCE, 5)),
    BLAST_PROTECTION(CatalogueEnchantment.of("BLAST_PROTECTION")),
    BUG_BLENDER(CatalogueEnchantment.of("BUG_BLENDER")),
    CHAMPION(CatalogueEnchantment.of("CHAMPION")),
    CHANCE(CatalogueEnchantment.of("CHANCE")),
    COMPACT(CatalogueEnchantment.of("COMPACT")),
    COUNTER_STRIKE(CatalogueEnchantment.of("COUNTER_STRIKE")),
    CULTIVATING(CatalogueEnchantment.of("CULTIVATING")),
    DEDICATION(CatalogueEnchantment.of("DEDICATION")),
    DELICATE(CatalogueEnchantment.of("DELICATE")),
    DEPTH_STRIDER(CatalogueEnchantment.of("DEPTH_STRIDER")),
    DIVINE_GIFT(StatEnchantment.of("DIVINE_GIFT").linear(ItemStatistic.MAGIC_FIND, 2)),
    DRAGON_TRACER(CatalogueEnchantment.of("DRAGON_TRACER")),
    FEAST(StatEnchantment.of("FEAST").linear(ItemStatistic.OVERBLOOM, 2)),
    FEATHER_FALLING(CatalogueEnchantment.active("FEATHER_FALLING")),
    FOREST_PLEDGE(StatEnchantment.of("FOREST_PLEDGE").linear(ItemStatistic.FORAGING_FORTUNE, 2)),
    GREAT_SPOOK(CatalogueEnchantment.of("GREAT_SPOOK")),
    GREEN_THUMB(CatalogueEnchantment.of("GREEN_THUMB")),
    HARVESTING(StatEnchantment.of("HARVESTING").linear(ItemStatistic.FARMING_FORTUNE, 12.5)),
    HECATOMB(CatalogueEnchantment.of("HECATOMB")),
    ICE_COLD(StatEnchantment.of("ICE_COLD").linear(ItemStatistic.COLD_RESISTANCE, 1)),
    INFINITE_QUIVER(EnchantmentInfiniteQuiver.class),
    KARMA(CatalogueEnchantment.of("KARMA")),
    LAPIDARY(CatalogueEnchantment.of("LAPIDARY")),
    OVERLOAD(StatEnchantment.of("OVERLOAD").linear(ItemStatistic.CRITICAL_DAMAGE, 1).linear(ItemStatistic.CRITICAL_CHANCE, 1).limited("Mega Critical Hits are not active yet")),
    PESTERMINATOR(StatEnchantment.of("PESTERMINATOR").linear(ItemStatistic.FARMING_FORTUNE, 2).linear(ItemStatistic.BONUS_PEST_CHANCE, 1)),
    PETALFALL(CatalogueEnchantment.of("PETALFALL")),
    PIERCING(EnchantmentPiercing.class),
    POWER(StatEnchantment.of("POWER").values(ItemStatistic.DAMAGE, 8, 16, 24, 32, 40, 50, 65)),
    PRISMATIC(StatEnchantment.of("PRISMATIC").linear(ItemStatistic.PRISTINE, 0.5)),
    PROJECTILE_PROTECTION(CatalogueEnchantment.of("PROJECTILE_PROTECTION")),
    PUNCH(EnchantmentPunch.class),
    QUANTUM(CatalogueEnchantment.of("QUANTUM")),
    RAINBOW(CatalogueEnchantment.of("RAINBOW")),
    REFLECTION(StatEnchantment.of("REFLECTION").linear(ItemStatistic.INTELLIGENCE, 2).linear(ItemStatistic.TRUE_DEFENSE, 1).limited("Arrow reflection is not active yet")),
    REJUVENATE(StatEnchantment.of("REJUVENATE").linear(ItemStatistic.HEALTH_REGENERATION, 1)),
    REPLENISH(CatalogueEnchantment.of("REPLENISH")),
    RESPIRATION(StatEnchantment.of("RESPIRATION").linear(ItemStatistic.RESPIRATION, 15)),
    RESPITE(CatalogueEnchantment.of("RESPITE")),
    SCUBA(StatEnchantment.of("SCUBA").linear(ItemStatistic.PRESSURE_RESISTANCE, 1)),
    SMALL_BRAIN(StatEnchantment.of("SMALL_BRAIN").offsetLinear(ItemStatistic.INTELLIGENCE, -5, 3).offsetLinear(ItemStatistic.TRUE_DEFENSE, 1, 3)),
    SMARTY_PANTS(StatEnchantment.of("SMARTY_PANTS").linear(ItemStatistic.INTELLIGENCE, 5)),
    SNIPE(EnchantmentSnipe.class),
    STEALTH(CatalogueEnchantment.of("STEALTH")),
    THORNS(CatalogueEnchantment.active("THORNS")),
    TIDAL(CatalogueEnchantment.active("TIDAL")),
    TITAN_KILLER(EnchantmentTitanKiller.class),
    TOXOPHILITE(CatalogueEnchantment.of("TOXOPHILITE")),
    TRANSYLVANIAN(CatalogueEnchantment.of("TRANSYLVANIAN")),
    TRIPLE_STRIKE(EnchantmentTripleStrike.class),
    TRUE_PROTECTION(StatEnchantment.of("TRUE_PROTECTION").linear(ItemStatistic.TRUE_DEFENSE, 5)),
    TURBO_CACTUS(StatEnchantment.of("TURBO_CACTUS").linear(ItemStatistic.CACTUS_FORTUNE, 5)),
    TURBO_CANE(StatEnchantment.of("TURBO_CANE").linear(ItemStatistic.SUGAR_CANE_FORTUNE, 5)),
    TURBO_CARROT(StatEnchantment.of("TURBO_CARROT").linear(ItemStatistic.CARROT_FORTUNE, 5)),
    TURBO_COCO(StatEnchantment.of("TURBO_COCO").linear(ItemStatistic.COCOA_BEANS_FORTUNE, 5)),
    TURBO_MELON(StatEnchantment.of("TURBO_MELON").linear(ItemStatistic.MELON_FORTUNE, 5)),
    TURBO_MOONFLOWER(StatEnchantment.of("TURBO_MOONFLOWER").linear(ItemStatistic.MOONFLOWER_FORTUNE, 5)),
    TURBO_MUSHROOMS(StatEnchantment.of("TURBO_MUSHROOMS").linear(ItemStatistic.MUSHROOM_FORTUNE, 5)),
    TURBO_POTATO(StatEnchantment.of("TURBO_POTATO").linear(ItemStatistic.POTATO_FORTUNE, 5)),
    TURBO_PUMPKIN(StatEnchantment.of("TURBO_PUMPKIN").linear(ItemStatistic.PUMPKIN_FORTUNE, 5)),
    TURBO_ROSE(StatEnchantment.of("TURBO_ROSE").linear(ItemStatistic.WILD_ROSE_FORTUNE, 5)),
    TURBO_SUNFLOWER(StatEnchantment.of("TURBO_SUNFLOWER").linear(ItemStatistic.SUNFLOWER_FORTUNE, 5)),
    TURBO_WARTS(StatEnchantment.of("TURBO_WARTS").linear(ItemStatistic.NETHER_WART_FORTUNE, 5)),
    TURBO_WHEAT(StatEnchantment.of("TURBO_WHEAT").linear(ItemStatistic.WHEAT_FORTUNE, 5)),
    BANK(CatalogueEnchantment.of("BANK")),
    BOBBIN_TIME(CatalogueEnchantment.of("BOBBIN_TIME")),
    COMBO(EnchantmentCombo.class),
    CROP_FEVER(CatalogueEnchantment.of("CROP_FEVER")),
    FIRST_IMPRESSION(CatalogueEnchantment.of("FIRST_IMPRESSION")),
    FLOWSTATE(CatalogueEnchantment.of("FLOWSTATE")),
    JERRY(CatalogueEnchantment.of("JERRY")),
    LAST_STAND(CatalogueEnchantment.of("LAST_STAND")),
    LEGION(CatalogueEnchantment.of("LEGION")),
    MISSILE(CatalogueEnchantment.of("MISSILE")),
    NO_PAIN_NO_GAIN(CatalogueEnchantment.of("NO_PAIN_NO_GAIN")),
    ONE_FOR_ALL(StatEnchantment.of("ONE_FOR_ALL").linear(ItemStatistic.DAMAGE, 500)),
    REITERATE(CatalogueEnchantment.of("REITERATE")),
    REND(CatalogueEnchantment.of("REND")),
    SOUL_EATER(EnchantmentSoulEater.class),
    SUNSET(CatalogueEnchantment.of("SUNSET")),
    SWARM(EnchantmentSwarm.class),
    THE_ONE(CatalogueEnchantment.of("THE_ONE")),
    WISDOM(CatalogueEnchantment.of("WISDOM")),
    WISE(EnchantmentWise.class),
    ;

    private final Class<? extends Ench> clazz;
    private final Ench ench;

    @SneakyThrows
    EnchantmentType(Class<? extends Ench> ench) {
        this.clazz = ench;

        this.ench = ench.getConstructor().newInstance();
    }

    EnchantmentType(Ench ench) {
        this.clazz = ench.getClass();
        this.ench = ench;
    }

    public int getApplyCost(int level, SkyBlockPlayer player) {
        if (level < 1 || level > ench.getLevelsToApply(player).maximumLevel())
            throw new IllegalArgumentException("level cannot be less than 1 and more than " +
                    ench.getLevelsToApply(player).maximumLevel() + " for " + name());
        return ench.getLevelsToApply(player).get(level);
    }

    public String getDescription(int level, SkyBlockPlayer player) {
        int maximumLevel = Math.max(ench.getLevelsToApply(player).maximumLevel(), ench.getEndcapLevel());
        if (level < 1 || level > maximumLevel)
            return ("level cannot be less than 1 and more than " + maximumLevel + " for " + name());
        return ench.getDescription(level);
    }

    public @Nullable EnchFromTable getEnchFromTable() {
        if (ench instanceof EnchFromTable)
            return (EnchFromTable) ench;
        return null;
    }

    public String getName() {
        return switch (this) {
            case MAGMARIZER -> "Pyroclasm";
            case STRONG_MANA -> "Strong Vitality";
            case HARDENED_MANA -> "Hardened Vitality";
            case FEROCIOUS_MANA -> "Vivacious Vitality";
            case MANA_VAMPIRE -> "Vampiric Vitality";
            default -> {
                CatalogueEntry entry = EnchantmentCatalogue.find(name());
                yield entry != null ? entry.name() : StringUtility.toNormalCase(this.name());
            }
        };
    }
}
