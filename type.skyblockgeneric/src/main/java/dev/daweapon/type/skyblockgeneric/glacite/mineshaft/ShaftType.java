package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum ShaftType {
    TITA("Titanium", Material.POLISHED_DIORITE, MineableBlock.TITANIUM, false, "1"),
    TUNG("Tungsten", Material.CLAY, MineableBlock.TUNGSTEN_ORE, false, "1"),
    UMBE("Umber", Material.BROWN_TERRACOTTA, MineableBlock.UMBER_ORE, false, "1"),
    JADE("Jade", Material.LIME_STAINED_GLASS, MineableBlock.JADE_ORE, false, "1"),
    AMBE("Amber", Material.ORANGE_STAINED_GLASS, MineableBlock.AMBER_ORE, false, "1"),
    TOPA("Topaz", Material.YELLOW_STAINED_GLASS, MineableBlock.TOPAZ_ORE, false, "1"),
    SAPP("Sapphire", Material.LIGHT_BLUE_STAINED_GLASS, MineableBlock.SAPPHIRE_ORE, false, "1"),
    AMET("Amethyst", Material.PURPLE_STAINED_GLASS, MineableBlock.AMETHYST_ORE, false, "1"),
    JASP("Jasper", Material.MAGENTA_STAINED_GLASS, MineableBlock.JASPER_ORE, true, "1"),
    RUBY("Ruby", Material.RED_STAINED_GLASS, MineableBlock.RUBY_ORE, true, "1", "2"),
    OPAL("Opal", Material.WHITE_STAINED_GLASS, null, true, "1"),
    ONYX("Onyx", Material.BLACK_STAINED_GLASS, MineableBlock.ONYX_ORE, true, "1", "2"),
    PERI("Peridot", Material.GREEN_STAINED_GLASS, MineableBlock.PERIDOT_ORE, true, "1", "2"),
    CITR("Citrine", Material.BROWN_STAINED_GLASS, MineableBlock.CITRINE_ORE, true, "1", "2"),
    AQUA("Aquamarine", Material.BLUE_STAINED_GLASS, MineableBlock.AQUAMARINE_ORE, true, "1", "2"),
    FAIR("Fairy", Material.GOLD_BLOCK, null, false, "1"),
    LITT("Littlefoot's Den", Material.BLUE_STAINED_GLASS, null, false, "L");

    public static final String CRYSTAL_SUFFIX = "C";

    private final String displayName;
    private final Material vein;
    private final @Nullable MineableBlock matchingBlock;
    private final boolean crystal;
    private final List<String> plainSuffixes;

    ShaftType(String displayName, Material vein, @Nullable MineableBlock matchingBlock, boolean crystal, String... plainSuffixes) {
        this.displayName = displayName;
        this.vein = vein;
        this.matchingBlock = matchingBlock;
        this.crystal = crystal;
        this.plainSuffixes = List.of(plainSuffixes);
    }

    public String displayName() {
        return displayName;
    }

    public Material vein() {
        return vein;
    }

    public boolean hasCrystalLayout() {
        return crystal;
    }

    public List<ShaftLayout> plainLayouts() {
        return plainSuffixes.stream().map(suffix -> new ShaftLayout(this, suffix)).toList();
    }

    public @Nullable ShaftLayout crystalLayout() {
        return crystal ? new ShaftLayout(this, CRYSTAL_SUFFIX) : null;
    }

    public List<ShaftLayout> layouts() {
        List<ShaftLayout> layouts = new ArrayList<>(plainLayouts());
        if (crystal) layouts.add(crystalLayout());
        return List.copyOf(layouts);
    }

    public static @Nullable ShaftType matching(@Nullable MineableBlock block) {
        if (block == null) return null;
        for (ShaftType type : values()) {
            if (type.matchingBlock == block) return type;
        }
        return null;
    }

    public static @Nullable ShaftLayout parseLayout(String code) {
        for (ShaftType type : values()) {
            for (ShaftLayout layout : type.layouts()) {
                if (layout.code().equalsIgnoreCase(code)) return layout;
            }
        }
        return null;
    }
}
