package dev.daweapon.type.skyblockgeneric.gui.inventories.slayer;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTier;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;

import java.util.Arrays;

public enum SlayerMenuStyle {
    REVENANT_HORROR(SlayerType.REVENANT_HORROR, Material.ROTTEN_FLESH, "Zombies",
            "<7>Abhorrent Zombie stuck\n<7>between life and death for\n<7>an eternity."),
    TARANTULA_BROODFATHER(SlayerType.TARANTULA_BROODFATHER, Material.COBWEB, "Spiders",
            "<7>Monstrous Spider who poisons\n<7>and devours its victims."),
    SVEN_PACKMASTER(SlayerType.SVEN_PACKMASTER, Material.MUTTON, "Wolves",
            "<7>Rabid Wolf genetically\n<7>modified by a famous mad\n<7>scientist. Eats bones and\n<7>flesh."),
    VOIDGLOOM_SERAPH(SlayerType.VOIDGLOOM_SERAPH, Material.ENDER_PEARL, "Endermen",
            "<7>If Necron is the right-hand\n<7>of the Wither King, this\n<7>dark demigod is the\n<7>left-hand."),
    RIFTSTALKER_BLOODFIEND(SlayerType.RIFTSTALKER_BLOODFIEND, Material.REDSTONE, "Vampires",
            "<7>A half-vampire, half-thrall,\n<7>immortal golem creation\n<7>representing the multiverse\n<7>coven in combat."),
    INFERNO_DEMONLORD(SlayerType.INFERNO_DEMONLORD, Material.BLAZE_POWDER, "Blazes",
            "<7>Even demons fear this\n<7>incarnation of pure evil,\n<7>constantly feeding on its\n<7>burning desire for\n<7>destruction.");

    private static final String[] TIER_COLOURS = {"<a>", "<e>", "<c>", "<4>", "<5>"};
    private static final String[] TIER_DIFFICULTIES = {"Beginner", "Strong", "Challenging", "Deadly", "Excruciating"};

    private final SlayerType type;
    private final Material material;
    private final String mobPlural;
    private final String description;

    SlayerMenuStyle(SlayerType type, Material material, String mobPlural, String description) {
        this.type = type;
        this.material = material;
        this.mobPlural = mobPlural;
        this.description = description;
    }

    public static SlayerMenuStyle of(SlayerType type) {
        return Arrays.stream(values()).filter(style -> style.type == type).findFirst().orElseThrow();
    }

    public static String tierColour(SlayerTier tier) {
        return TIER_COLOURS[tier.ordinal()];
    }

    public static String tierDifficulty(SlayerTier tier) {
        return TIER_DIFFICULTIES[tier.ordinal()];
    }

    public SlayerType type() {
        return type;
    }

    public Material material() {
        return material;
    }

    public String mobPlural() {
        return mobPlural;
    }

    public String description() {
        return description;
    }

    public boolean riftOnly() {
        return type == SlayerType.RIFTSTALKER_BLOODFIEND;
    }
}
