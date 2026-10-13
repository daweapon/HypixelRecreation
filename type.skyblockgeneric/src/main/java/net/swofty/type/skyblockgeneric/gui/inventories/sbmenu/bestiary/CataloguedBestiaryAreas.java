package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.CataloguedBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

public final class CataloguedBestiaryAreas {
    private CataloguedBestiaryAreas() {
    }

    public record CataloguedFamily(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) implements BestiaryEntry {
        @Override public String getName() { return name; }
        @Override public String getDescription() { return description; }
        @Override public GUIMaterial getGuiMaterial() { return guiMaterial; }
        @Override public List<BestiaryMob> getMobs() { return mobs; }
    }

    public static List<BestiaryMob> placeholder(String mobId, String displayName, int bracket, int maxTier, MobType type, Material icon) {
        return List.of(CataloguedBestiaryMob.of(new CataloguedBestiaryMob.Family(
                mobId, displayName, bracket, maxTier, new GUIMaterial(icon), type)));
    }

    public static CataloguedFamily family(String mobId, String displayName, String description, int bracket, int maxTier, MobType type, Material icon) {
        GUIMaterial material = new GUIMaterial(icon);
        return new CataloguedFamily("<a>" + displayName, description, material, List.of(CataloguedBestiaryMob.of(
                new CataloguedBestiaryMob.Family(mobId, displayName, bracket, maxTier, material, type))));
    }

    public static final BestiaryEntry[] FARMING_ISLANDS = {
            family("CHICKEN", "Chicken", "Wishes it could fly.", 1, 5, MobType.ANIMAL, Material.CHICKEN_SPAWN_EGG),
            family("COW", "Cow", "Natural predator of grass.", 1, 5, MobType.ANIMAL, Material.COW_SPAWN_EGG),
            family("MUSHROOM_COW", "Mushroom Cow", "Technically a health code violation.", 1, 5, MobType.ANIMAL, Material.RED_MUSHROOM),
            family("PIG", "Pig", "Smells like Bacon.", 1, 5, MobType.ANIMAL, Material.PORKCHOP),
            family("RABBIT", "Rabbit", "Never stops hopping.", 1, 5, MobType.ANIMAL, Material.RABBIT_FOOT),
            family("SHEEP", "Sheep", "Beep beep I'm a sheep.", 1, 5, MobType.ANIMAL, Material.WHITE_WOOL),
    };

    public static final BestiaryEntry[] GARDEN = {
            family("BEETLE", "Beetle", "A particularly gross pest.", 6, 15, MobType.PEST, Material.NETHER_WART),
            family("CRICKET", "Cricket", "Usually just a hop, skip, and a jump away.", 6, 15, MobType.PEST, Material.CARROT),
            family("DRAGONFLY", "Dragonfly", "Glides by in a blur of wings and whispers.", 6, 15, MobType.PEST, Material.SUNFLOWER),
            family("EARTHWORM", "Earthworm", "Big, scary, and pink.", 6, 15, MobType.PEST, Material.MELON_SLICE),
            family("FIELD_MOUSE", "Field Mouse", "A shy little fellow!", 7, 15, MobType.PEST, Material.WHEAT_SEEDS),
            family("FIREFLY", "Firefly", "Flickers softly - here one moment, gone the next.", 6, 15, MobType.PEST, Material.BLUE_ORCHID),
            family("FLY", "Fly", "A common Garden pest, known for its big eyes.", 6, 15, MobType.PEST, Material.WHEAT),
            family("LOCUST", "Locust", "Pretty loud, and pretty annoying.", 6, 15, MobType.PEST, Material.POTATO),
            family("LUNAR_MOTH", "Lunar Moth", "Drawn to the light and dew of the moon.", 7, 15, MobType.PEST, Material.SEA_LANTERN),
            family("MITE", "Mite", "Try and you just mite.", 6, 15, MobType.PEST, Material.CACTUS),
            family("MOSQUITO", "Mosquito", "One of nature's most useless creatures.", 6, 15, MobType.PEST, Material.SUGAR_CANE),
            family("MOTH", "Moth", "Fluttering around a Garden near you.", 6, 15, MobType.PEST, Material.COCOA_BEANS),
            family("PRAYING_MANTIS", "Praying Mantis", "Strikes fast - blink - and it's already gone.", 6, 15, MobType.PEST, Material.ROSE_BUSH),
            family("RAT", "Rat", "Quick, yet nasty.", 6, 15, MobType.PEST, Material.PUMPKIN),
            family("SLUG", "Slug", "Methodical in movement, ineffective in survival.", 6, 15, MobType.PEST, Material.RED_MUSHROOM),
            family("TIMESTALK_CLONE", "Timestalk Clone", "There can only be one, sometimes two apparently.", 7, 10, MobType.PEST, Material.CLOCK),
            family("ZOMBUDDY", "Zombuddy", "Zombuddy that I used to know.", 7, 15, MobType.UNDEAD, Material.ROTTEN_FLESH),
    };

    public static final BestiaryEntry[] SPIDERS_DEN = {
            family("ARACHNE", "Arachne", "The strongest spider in the den.", 7, 15, MobType.ARTHROPOD, Material.SPIDER_EYE),
            family("ARACHNES_BROOD", "Arachne's Brood", "Brood of Arachne.", 4, 15, MobType.ARTHROPOD, Material.COBWEB),
            family("ARACHNES_KEEPER", "Arachne's Keeper", "Keeper of Arachne.", 6, 15, MobType.ARTHROPOD, Material.FERMENTED_SPIDER_EYE),
            family("BROODMOTHER", "Broodmother", "The Broodmother to end all Broodmothers.", 7, 15, MobType.ARTHROPOD, Material.SPIDER_SPAWN_EGG),
            family("DASHER_SPIDER", "Dasher Spider", "Dashingly striking, watch for this spider's long jumps!", 2, 15, MobType.ARTHROPOD, Material.STRING),
            family("FLINT_SKELETON", "Flint Skeleton", "These Skeletons just never stay dead.", 4, 15, MobType.SKELETAL, Material.FLINT),
            family("RAIN_SLIME", "Rain Slime", "These Slimes enjoy rain...and jumping to their deaths.", 4, 15, MobType.CUBIC, Material.SLIME_BALL),
            family("DEN_SILVERFISH", "Silverfish", "Though weak alone, they find strength in numbers.", 2, 15, MobType.ARTHROPOD, Material.IRON_NUGGET),
            family("SPIDER_JOCKEY", "Spider Jockey", "The Skeletons have been controlling Spiders since the dawn of minekind.", 4, 15, MobType.ARTHROPOD, Material.BONE),
            family("SPLITTER_SPIDER", "Splitter Spider", "If you think you've killed this spider, think again.", 4, 20, MobType.ARTHROPOD, Material.SPIDER_EYE),
            family("VORACIOUS_SPIDER", "Voracious Spider", "This spider is quite voracious.", 4, 20, MobType.ARTHROPOD, Material.ROTTEN_FLESH),
            family("WEAVER_SPIDER", "Weaver Spider", "Spins a web of lies.", 4, 20, MobType.ARTHROPOD, Material.STRING),
    };

    public static final BestiaryEntry[] THE_END = {
            family("DRAGONS", "Dragons", "Inside the Dragon's Den, there is no escape from the fury of the beasts.", 6, 15, MobType.ENDER, Material.DRAGON_HEAD),
            family("END_STONE_PROTECTOR", "End Stone Protector", "Risen from the debris of Endstone.", 8, 20, MobType.CONSTRUCT, Material.END_STONE),
            family("END_ENDERMAN", "Enderman", "Not your mother's endermen.", 7, 25, MobType.ENDER, Material.ENDER_PEARL),
            family("ENDERMITE", "Endermite", "Hidden in the stone of The End.", 7, 25, MobType.ENDER, Material.ENDER_EYE),
            family("OBSIDIAN_DEFENDER", "Obsidian Defender", "Forged from the forces of hot and cold.", 7, 25, MobType.CONSTRUCT, Material.OBSIDIAN),
            family("SEER", "Seer", "Always watching.", 7, 25, MobType.ENDER, Material.ENDER_EYE),
            family("VOIDLING_EXTREMIST", "Voidling Extremist", "The top lieutenants of the void heraldry.", 4, 15, MobType.ENDER, Material.DRAGON_BREATH),
            family("VOIDLING_FANATIC", "Voidling Fanatic", "A step beyond zealotry.", 5, 25, MobType.ENDER, Material.DRAGON_BREATH),
            family("ZEALOT", "Zealot", "Unwavering in their devotion to the Dragon.", 5, 25, MobType.ENDER, Material.ENDER_CHEST),
    };

    public static final BestiaryEntry[] THE_PARK = {
            family("HOWLING_SPIRIT", "Howling Spirit", "Howling at the moon.", 3, 15, MobType.ANIMAL, Material.BONE),
            family("PACK_SPIRIT", "Pack Spirit", "Spirits of long-dead wolves, still prowling the caves for prey.", 3, 15, MobType.ANIMAL, Material.BONE),
            family("SOUL_OF_THE_ALPHA", "Soul of the Alpha", "Ghastly leader of the pack.", 4, 15, MobType.ANIMAL, Material.GHAST_TEAR),
    };

    public static final BestiaryEntry[] JERRY = {
            family("GREEN_JERRY", "Green Jerry", "Jerry.", 4, 10, MobType.HUMANOID, Material.GREEN_DYE),
            family("BLUE_JERRY", "Blue Jerry", "Jerry?", 5, 10, MobType.HUMANOID, Material.BLUE_DYE),
            family("PURPLE_JERRY", "Purple Jerry", "Jerry!", 6, 10, MobType.HUMANOID, Material.PURPLE_DYE),
            family("GOLDEN_JERRY", "Golden Jerry", "Jerry?!", 7, 10, MobType.TIMID, Material.GOLD_INGOT),
    };
}
