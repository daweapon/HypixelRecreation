package dev.daweapon.type.skyblockgeneric.fishing.winter;

import lombok.Getter;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The six Winter Sea Creatures, the only Sea Creatures that can be fished up on Jerry's Workshop
 * since the March 2026 revamp (wiki: Frozen Steve, Frosty, Grinch, Nutcracker, Yeti, Reindrake;
 * game update 0.27.2). Fishing level requirements and catch weights live in sea_creatures.yml and
 * tables.yml; everything about the spawned mob lives here.
 */
@Getter
public enum WinterSeaCreature {
    FROZEN_STEVE("Frozen Steve", Rarity.COMMON, 1, 150, 10, 0, 100, 25, 1, 1,
            "Frozen Steve fell into the pond long ago, never to resurface...until now!",
            List.of(MobType.AQUATIC, MobType.FROZEN, MobType.HUMANOID), EntityType.MANNEQUIN, 1, 1,
            List.of(WinterDrop.always(ItemType.ICE, 2, 4),
                    WinterDrop.always(ItemType.WHITE_GIFT, 0, 1),
                    WinterDrop.chance(ItemType.ICY_SINKER, 1),
                    WinterDrop.oneIn(ItemType.DYE_ICEBERG, 2_500_000),
                    WinterDrop.oneIn(ItemType.DYE_AQUAMARINE, 5_000_000)),
            new Bestiary("\"Fell\" into Jerry's Pond.", 4, 15),
            "54690f5aa6d0e800f9b8d1890fc158b921819a81dfd7342a2170e7efc46b9ed7", null),
    FROSTY_THE_SNOWMAN("Frosty", Rarity.UNCOMMON, 3, 250, 15, 0, 60, 30, 3, 3,
            "It's a snowman! He looks harmless.",
            List.of(MobType.AQUATIC, MobType.FROZEN, MobType.CONSTRUCT), EntityType.SNOW_GOLEM, 2, 4,
            List.of(WinterDrop.always(ItemType.ICE, 16, 32),
                    WinterDrop.always(ItemType.SNOW_BLOCK, 16, 32),
                    WinterDrop.always(ItemType.ICE_HUNK, 1),
                    WinterDrop.always(ItemType.WHITE_GIFT, 0, 1),
                    WinterDrop.always(ItemType.GREEN_GIFT, 0, 1),
                    WinterDrop.chance(ItemType.ENCHANTED_SNOW_BLOCK, 4),
                    WinterDrop.chance(ItemType.ENCHANTED_ICE, 4),
                    WinterDrop.chance(ItemType.RED_GIFT, 2),
                    WinterDrop.oneIn(ItemType.DYE_ICEBERG, 2_500_000),
                    WinterDrop.oneIn(ItemType.DYE_AQUAMARINE, 5_000_000)),
            new Bestiary("*Was* a jolly happy soul.", 4, 15),
            null, Material.CARVED_PUMPKIN),
    GRINCH("Grinch", Rarity.RARE, 25, 3, 1, 0, 400, 312, 31, 33,
            "The Grinch stole Jerry's gifts... Get them back!",
            List.of(MobType.TIMID, MobType.FROZEN, MobType.SHIELDED), EntityType.MANNEQUIN, 4, 8,
            List.of(WinterDrop.always(ItemType.ICE, 32, 64),
                    WinterDrop.always(ItemType.WHITE_GIFT, 1),
                    WinterDrop.always(ItemType.GREEN_GIFT, 0, 1),
                    WinterDrop.chance(ItemType.RED_GIFT, 10),
                    WinterDrop.chance(ItemType.ENCHANTED_PACKED_ICE, 0.5),
                    WinterDrop.oneIn(ItemType.DYE_ICEBERG, 500_000),
                    WinterDrop.oneIn(ItemType.DYE_AQUAMARINE, 2_500_000)),
            new Bestiary("How the Grinch Stole Jerry Season.", 6, 20),
            "2508e4a2f88502c019652b2437b76c82fedff9091389d88118ecc673f628b547", null),
    NUTCRACKER("Nutcracker", Rarity.EPIC, 115, 400_000, 400, 100, 300, 1_725, 172, 172,
            "You found a forgotten Nutcracker laying beneath the ice.",
            List.of(MobType.AQUATIC, MobType.FROZEN, MobType.HUMANOID), EntityType.MANNEQUIN, 12, 24,
            List.of(WinterDrop.always(ItemType.ENCHANTED_ICE, 1),
                    WinterDrop.always(ItemType.RED_GIFT, 1),
                    WinterDrop.chance(ItemType.WALNUT, 10),
                    WinterDrop.chance(ItemType.ENCHANTED_PACKED_ICE, 2),
                    WinterDrop.oneIn(ItemType.DYE_ICEBERG, 500_000),
                    WinterDrop.oneIn(ItemType.DYE_AQUAMARINE, 2_500_000)),
            new Bestiary("A toy brought to life.", 7, 20),
            "c2407e66c81b1443c2e7dfc4d6583eb19c622fa22f34fbf99fe6c45f76a", null),
    YETI("Yeti", Rarity.LEGENDARY, 175, 2_000_000, 1_000, 0, 20, 3_500, 350, 439,
            "What is this creature!?",
            List.of(MobType.AQUATIC, MobType.FROZEN, MobType.ELUSIVE), EntityType.MANNEQUIN, 100, 100,
            List.of(WinterDrop.always(ItemType.ENCHANTED_ICE, 32, 64),
                    WinterDrop.always(ItemType.ENCHANTED_RAW_FISH, 16, 32),
                    WinterDrop.always(ItemType.RED_GIFT, 1, 2),
                    WinterDrop.always(ItemType.ENCHANTED_PACKED_ICE, 1),
                    WinterDrop.always(ItemType.BLUE_ICE_HUNK, 1, 2),
                    WinterDrop.chance(ItemType.BABY_YETI_PET, 2),
                    WinterDrop.chance(ItemType.HILT_OF_TRUE_ICE, 1),
                    WinterDrop.oneIn(ItemType.DYE_ICEBERG, 50_000),
                    WinterDrop.oneIn(ItemType.DYE_AQUAMARINE, 50_000)),
            new Bestiary("The cooler cousin of Bigfoot.", 6, 15),
            "4dd2d3c6d01c276226c7b0d377122e1a647b2ffb5b9b54fa98eac37bb1d09d3a", null),
    REINDRAKE("Reindrake", Rarity.MYTHIC, 186, 2_500, 0, 0, 0, 4_650, 930, 934,
            "A Reindrake forms from the depths.",
            List.of(MobType.FROZEN, MobType.SHIELDED, MobType.ELUSIVE), EntityType.GHAST, 0, 0,
            List.of(WinterDrop.always(ItemType.ENCHANTED_BOOK, 1),
                    WinterDrop.chance(ItemType.BOBBIN_SCRIPTURES, 1),
                    WinterDrop.oneIn(ItemType.DYE_ICEBERG, 50_000),
                    WinterDrop.oneIn(ItemType.DYE_AQUAMARINE, 50_000)),
            new Bestiary("A Reindrake forms from the depths.", 8, 15),
            null, Material.DRAGON_EGG),
    ;

    private final String displayName;
    private final Rarity rarity;
    private final int level;
    private final double health;
    private final double damage;
    private final double defense;
    private final double speed;
    private final long fishingXp;
    private final int coins;
    private final int xpOrbs;
    private final String catchMessage;
    private final List<MobType> mobTypes;
    private final EntityType entityType;
    private final int minIceEssence;
    private final int maxIceEssence;
    private final List<WinterDrop> drops;
    private final Bestiary bestiary;
    private final @Nullable String skinTexture;
    private final @Nullable Material icon;
    private final SeaCreatureProfile profile;

    WinterSeaCreature(String displayName, Rarity rarity, int level, double health, double damage, double defense,
                      double speed, long fishingXp, int coins, int xpOrbs, String catchMessage, List<MobType> mobTypes,
                      EntityType entityType, int minIceEssence, int maxIceEssence, List<WinterDrop> drops,
                      Bestiary bestiary, @Nullable String skinTexture, @Nullable Material icon) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.level = level;
        this.health = health;
        this.damage = damage;
        this.defense = defense;
        this.speed = speed;
        this.fishingXp = fishingXp;
        this.coins = coins;
        this.xpOrbs = xpOrbs;
        this.catchMessage = catchMessage;
        this.mobTypes = mobTypes;
        this.entityType = entityType;
        this.minIceEssence = minIceEssence;
        this.maxIceEssence = maxIceEssence;
        this.drops = drops;
        this.bestiary = bestiary;
        this.skinTexture = skinTexture;
        this.icon = icon;
        this.profile = SeaCreatureProfile.builder(name())
                .displayName(displayName)
                .level(level)
                .entityType(entityType)
                .health(health).damage(damage).speed(speed)
                .mobTypes(mobTypes.toArray(MobType[]::new))
                .behaviour(behaviourOf(name()))
                .fishingXpReward(fishingXp).xpOrbs(xpOrbs)
                .catchMessage(catchMessage)
                .build();
    }

    private static SeaCreatureBehaviour behaviourOf(String id) {
        return switch (id) {
            case "FROZEN_STEVE" -> SeaCreatureBehaviour.aggressive(1.4, 20, 16);
            case "FROSTY_THE_SNOWMAN" -> SeaCreatureBehaviour.aggressive(1.0, 30, 12);
            case "NUTCRACKER" -> SeaCreatureBehaviour.aggressive(1.8, 20, 24);
            case "YETI" -> SeaCreatureBehaviour.passive(2);
            default -> SeaCreatureBehaviour.of(List.of(), List.of());
        };
    }

    public GUIMaterial getGuiMaterial() {
        return skinTexture != null ? new GUIMaterial(skinTexture) : new GUIMaterial(icon);
    }

    public record Bestiary(String description, int bracket, int maxTier) {
    }
}
