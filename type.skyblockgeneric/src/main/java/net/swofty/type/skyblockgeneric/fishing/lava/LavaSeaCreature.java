package net.swofty.type.skyblockgeneric.fishing.lava;

import lombok.Getter;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.swofty.type.skyblockgeneric.fishing.lava.LavaDrop.always;
import static net.swofty.type.skyblockgeneric.fishing.lava.LavaDrop.book;
import static net.swofty.type.skyblockgeneric.fishing.lava.LavaDrop.chance;

/**
 * The 16 Crimson Isle lava Sea Creatures (wiki: Sea_Creatures/List/Crimson_Isle and /Hotspot, plus each
 * creature's page, after the 0.27.2 level/XP rebalance): 9 island-wide, 6 hotspot-only and Plhlegblast. Catch
 * weights and Fishing level requirements live in sea_creatures.yml and tables.yml; everything about the
 * spawned mob lives here. Every Magmatic Sea Creature can also drop Carmine Dye (1 in 5M / 2.5M / 50k by rarity).
 */
@Getter
public enum LavaSeaCreature {
    MAGMA_SLUG("Magma Slug", Rarity.UNCOMMON, 186, 500_000, 6_000, 1_860, 224, 186,
            "From beneath the lava appears a Magma Slug.",
            List.of(MobType.MAGMATIC, MobType.CUBIC), EntityType.MAGMA_CUBE, 40,
            List.of(always("MAGMAFISH", 5), always("LUMP_OF_MAGMA", 1, 2), chance("SLUG_BOOTS", 2)),
            new Bestiary("Moves. Very. Slowly.", 4, 15), "38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429"),
    MOOGMA("Moogma", Rarity.UNCOMMON, 203, 750_000, 7_000, 2_030, 243, 203,
            "You hear a faint Moo from the lava... A Moogma appears.",
            List.of(MobType.MAGMATIC, MobType.ANIMAL), EntityType.MOOSHROOM, 90,
            List.of(always("MAGMAFISH", 8), always("MOOGMA_PELT", 1, 2), chance("MOOGMA_LEGGINGS", 2)),
            new Bestiary("Produces 100% fresh magma on a daily basis!", 4, 15), "2b52841f2fd589e0bc84cbabf9e1c27cb70cac98f8d6b3dd065e55a4dcb70d77"),
    LAVA_LEECH("Lava Leech", Rarity.RARE, 215, 1_000_000, 8_000, 2_687, 280, 268,
            "A small but fearsome Lava Leech emerges.",
            List.of(MobType.MAGMATIC, MobType.ARTHROPOD), EntityType.SILVERFISH, 110,
            List.of(always("MAGMAFISH", 20), always("CUP_OF_BLOOD", 1), chance("BLADE_OF_THE_VOLCANO", 5),
                    chance("PITCHIN_KOI", 0.4)),
            new Bestiary("It's hard to love-a leech.", 6, 20), "5a1a0831aa03afb4212adcbb24e5dfaa7f476a1173fce259ef75a85855"),
    PYROCLASTIC_WORM("Pyroclastic Worm", Rarity.RARE, 221, 1_200_000, 8_000, 2_762, 288, 276,
            "You feel the heat radiating as a Pyroclastic Worm surfaces.",
            List.of(MobType.MAGMATIC, MobType.CUBIC), EntityType.ENDERMITE, 100,
            List.of(always("MAGMAFISH", 10), always("PYROCLASTIC_SCALE", 1), book("CHARM", 1, 1)),
            new Bestiary("Travels up to 10 miles downhill during eruptions.", 6, 20), "38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429"),
    LAVA_FLAME("Lava Flame", Rarity.RARE, 219, 1_000_000, 9_000, 2_737, 285, 273,
            "A Lava Flame flies out from beneath the lava.",
            List.of(MobType.MAGMATIC, MobType.INFERNAL), EntityType.BLAZE, 100,
            List.of(always("MAGMAFISH", 40), always("FLAMING_HEART", 1), chance("FLAMING_CHESTPLATE", 2)),
            new Bestiary("Incarnation of pure heat.", 6, 20), "b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0"),
    FIRE_EEL("Fire Eel", Rarity.RARE, 243, 2_000_000, 9_500, 3_037, 316, 303,
            "A Fire Eel slithers out of the depths.",
            List.of(MobType.MAGMATIC, MobType.ANIMAL), EntityType.PHANTOM, 120,
            List.of(always("MAGMAFISH", 50), always("ORB_OF_ENERGY", 1), chance("STAFF_OF_THE_VOLCANO", 5)),
            new Bestiary("Tastes like battery acid.", 6, 20), "c63704a7fc7d437f7b923e23e9a08ae3bbe28937df4dafa9e3e8725b2ce4afa5"),
    TAURUS("Taurus", Rarity.EPIC, 262, 3_000_000, 11_000, 3_930, 393, 393,
            "Taurus and his steed emerge.",
            List.of(MobType.MAGMATIC, MobType.HUMANOID, MobType.ANIMAL), EntityType.HOGLIN, 110,
            List.of(always("HORN_OF_TAURUS", 1), always("SILVER_MAGMAFISH", 1), chance("TAURUS_HELMET", 2)),
            new Bestiary("Could never date a Pisces.", 7, 20), "1642a06cd75ef307c1913ba7a224fb2082d8a2c5254fd1bf006125a087a9a868"),
    THUNDER("Thunder", Rarity.LEGENDARY, 324, 35_000_000, 5_000, 6_480, 811, 648,
            "<c><l>You hear a massive rumble as Thunder emerges.",
            List.of(MobType.MAGMATIC, MobType.ARCANE, MobType.ELUSIVE), EntityType.GUARDIAN, 90,
            List.of(always("THUNDER_FRAGMENT", 1), always("SILVER_MAGMAFISH", 8, 16), book("FLASH", 1, 1.5)),
            new Bestiary("This is what happens on a bad forecast.", 6, 15), "e92089618435a0ef63e95ee95a92b83073f8c33fa77dc5365199bad33b6256"),
    LORD_JAWBUS("Lord Jawbus", Rarity.MYTHIC, 597, 100_000_000, 10_000, 14_925, 2_985, 2_985,
            "<c><l>You have angered a legendary creature... Lord Jawbus has arrived.",
            List.of(MobType.MAGMATIC, MobType.CONSTRUCT, MobType.ELUSIVE), EntityType.IRON_GOLEM, 80,
            List.of(always("MAGMA_LORD_FRAGMENT", 1), always("SILVER_MAGMAFISH", 16, 32), chance("SHARD_LORD_JAWBUS", 2),
                    chance("BOBBIN_SCRIPTURES", 1), chance("RADIOACTIVE_VIAL", 0.5)),
            new Bestiary("The mighty magma lord.", 8, 15), "22bcaceeb4162f400d44743315932ac820d3119ac8986a0161a726161ccc93fc"),
    FRIED_CHICKEN("Fried Chicken", Rarity.COMMON, 86, 65_000, 550, 645, 94, 64,
            "Smells of burning. Must be a Fried Chicken.",
            List.of(MobType.MAGMATIC, MobType.ANIMAL), EntityType.CHICKEN, 110,
            List.of(always("MAGMAFISH", 1, 2), chance("FRIED_FEATHER", 10)),
            new Bestiary("Is it ready to eat, then?", 4, 15), null),
    VOLCANIC_SNAIL("Volcanic Snail", Rarity.UNCOMMON, 90, 100_000, 450, 900, 108, 90,
            "You feel a burning sensation as you reel in a Volcanic Snail!",
            List.of(MobType.MAGMATIC, MobType.ANIMAL), EntityType.TURTLE, 35,
            List.of(always("MAGMAFISH", 2, 4), chance("SEARED_ESCARGOT", 4), chance("SILVER_MAGMAFISH", 1)),
            new Bestiary("Moves slow but with great purpose.", 4, 15), "9bf9d3bd4c3dcca350a438804dd42fae2b3474dc0928fa0ad547ad40702acf71"),
    FIREPROOF_WITCH("Fireproof Witch", Rarity.RARE, 150, 1_000_000, 750, 1_875, 195, 187,
            "Trouble's brewing, it's a Fireproof Witch!",
            List.of(MobType.MAGMATIC, MobType.HUMANOID, MobType.ARCANE), EntityType.WITCH, 90,
            List.of(always("MAGMAFISH", 8, 16), always("GUNPOWDER", 8), chance("ENCHANTED_GLOWSTONE_DUST", 20),
                    chance("ENCHANTED_GUNPOWDER", 5), chance("SINGED_POWDER", 1), chance("ENCHANTED_GLOWSTONE", 0.25),
                    book("FIRE_PROTECTION", 6, 0.01)),
            new Bestiary("Her brewing sessions have seen much success as of late.", 6, 20), "fce6604157fc4ab5591e4bcf507a749918ee9c41e357d47376e0ee7342074c90"),
    MAGMA_PILLAR("Magma Pillar", Rarity.EPIC, 220, 1_250_000, 7_500, 3_300, 331, 330,
            "A Magma Pillar rises from the lava.",
            List.of(MobType.MAGMATIC, MobType.CUBIC), EntityType.MAGMA_CUBE, 0,
            List.of(always("SILVER_MAGMAFISH", 2, 4), always("ENCHANTED_MAGMA_CREAM", 2, 4), book("PYROCLASM|MAGMARIZER", 6, 0.05)),
            new Bestiary("Supa hot fire.", 7, 20), "38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429"),
    FIERY_SCUTTLER("Fiery Scuttler", Rarity.LEGENDARY, 291, 10_000_000, 7_500, 5_820, 729, 582,
            "<c>A Fiery Scuttler inconspicuously waddles up to you, friends in tow.",
            List.of(MobType.MAGMATIC, MobType.ANIMAL, MobType.ELUSIVE), EntityType.ARMADILLO, 100,
            List.of(always("SCORCHED_CRAB_STICK", 1), always("SILVER_MAGMAFISH", 8, 16), chance("SEVERED_PINCER", 0.2),
                    chance("SCUTTLER_SHELL", 0.1)),
            new Bestiary("Small guy, big attitude.", 6, 15), "55b194025806687642e2bc239895d646a6d8c193d9253b61bfce908f6ce1b84a"),
    RAGNAROK("Ragnarok", Rarity.MYTHIC, 612, 125_000_000, 10_000, 15_300, 3_061, 3_060,
            "<c><l>The sky darkens and the air thickens. The end times are upon us: Ragnarok is here.",
            List.of(MobType.MAGMATIC, MobType.SKELETAL, MobType.ELUSIVE), EntityType.WITHER_SKELETON, 100,
            List.of(always("SILVER_MAGMAFISH", 16, 32), chance("BOBBIN_SCRIPTURES", 1), chance("CHAIN_OF_THE_END_TIMES", 1),
                    chance("BRIMSTONE_HANDLE", 0.5), chance("BURNT_TEXTS", 0.25)),
            new Bestiary("Once tried to bring about the end of the world, until he was betrayed.", 8, 15), "a8e1fe214b71f6ea69c541a861c64bafda7bf9b85de5dd17ab2b6ccd1d32b039"),
    PLHLEGBLAST("Plhlegblast", Rarity.MYTHIC, 210, 500_000_000, 0, 5_250, 1_053, 1_050,
            "WOAH! A Plhlegblast appeared.",
            List.of(MobType.MAGMATIC, MobType.ELUSIVE, MobType.ANIMAL), EntityType.SQUID, 30,
            List.of(always("INK_SAC", 30, 50), always("LILY_PAD", 1), always("ENCHANTED_LILY_PAD", 2, 3),
                    chance("ENCHANTED_INK_SAC", 0.5), chance("SQUID_BOOTS", 1), chance("BOBBIN_SCRIPTURES", 1)),
            new Bestiary("Hard to spell, even harder to find.", 7, 5), "d8705624daa2956aa45956c81bab5f4fdb2c74a596051e24192039aea3a8b8"),
    ;

    private final String displayName;
    private final Rarity rarity;
    private final int level;
    private final double health;
    private final double damage;
    private final long fishingXp;
    private final int xpOrbs;
    private final int coins;
    private final String catchMessage;
    private final List<MobType> mobTypes;
    private final EntityType entityType;
    private final double speed;
    private final List<LavaDrop> drops;
    private final Bestiary bestiary;
    private final @Nullable String headTexture;
    private final SeaCreatureProfile profile;

    LavaSeaCreature(String displayName, Rarity rarity, int level, double health, double damage, long fishingXp,
                    int xpOrbs, int coins, String catchMessage, List<MobType> mobTypes, EntityType entityType,
                    double speed, List<LavaDrop> drops, Bestiary bestiary, @Nullable String headTexture) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.level = level;
        this.health = health;
        this.damage = damage;
        this.fishingXp = fishingXp;
        this.xpOrbs = xpOrbs;
        this.coins = coins;
        this.catchMessage = catchMessage;
        this.mobTypes = mobTypes;
        this.entityType = entityType;
        this.speed = speed;
        List<LavaDrop> withCarmine = new ArrayList<>(drops);
        withCarmine.add(LavaDrop.carmine(carmineOdds(rarity)));
        this.drops = List.copyOf(withCarmine);
        this.bestiary = bestiary;
        this.headTexture = headTexture;
        this.profile = SeaCreatureProfile.builder(name())
                .displayName(displayName)
                .level(level)
                .entityType(entityType)
                .health(health).damage(damage).speed(speed)
                .mobTypes(mobTypes.toArray(MobType[]::new))
                .behaviour(speed <= 0 || damage <= 0 ? SeaCreatureBehaviour.passive(4) : SeaCreatureBehaviour.aggressive(1.6, 20, 20))
                .fishingXpReward(fishingXp).xpOrbs(xpOrbs)
                .catchMessage(catchMessage)
                .build();
    }

    private static double carmineOdds(Rarity rarity) {
        return switch (rarity) {
            case COMMON, UNCOMMON -> 5_000_000;
            case RARE, EPIC -> 2_500_000;
            default -> 50_000;
        };
    }

    public boolean isElusive() {
        return mobTypes.contains(MobType.ELUSIVE);
    }

    public boolean canBeCorrupted() {
        return this != LORD_JAWBUS;
    }

    public GUIMaterial getGuiMaterial() {
        return headTexture != null ? new GUIMaterial(headTexture) : new GUIMaterial(Material.COOKED_CHICKEN);
    }

    public static @Nullable LavaSeaCreature byId(String id) {
        for (LavaSeaCreature creature : values()) {
            if (creature.name().equals(id)) return creature;
        }
        return null;
    }

    public record Bestiary(String description, int bracket, int maxTier) {
    }
}
