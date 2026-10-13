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

    public static CataloguedFamily family(String mobId, String displayName, String description, int bracket, int maxTier, MobType type, String texture) {
        GUIMaterial material = new GUIMaterial(texture);
        return new CataloguedFamily("<a>" + displayName, description, material, List.of(CataloguedBestiaryMob.of(
                new CataloguedBestiaryMob.Family(mobId, displayName, bracket, maxTier, material, type))));
    }

    public static List<BestiaryMob> placeholder(String mobId, String displayName, int bracket, int maxTier, MobType type, String texture) {
        return List.of(CataloguedBestiaryMob.of(new CataloguedBestiaryMob.Family(
                mobId, displayName, bracket, maxTier, new GUIMaterial(texture), type)));
    }

    public static CataloguedFamily family(String mobId, String displayName, String description, int bracket, int maxTier, MobType type, Material icon) {
        GUIMaterial material = new GUIMaterial(icon);
        return new CataloguedFamily("<a>" + displayName, description, material, List.of(CataloguedBestiaryMob.of(
                new CataloguedBestiaryMob.Family(mobId, displayName, bracket, maxTier, material, type))));
    }

    public static final BestiaryEntry[] FARMING_ISLANDS = {
            family("CHICKEN", "Chicken", "Wishes it could fly.", 1, 5, MobType.ANIMAL, "ca3582ce4889333dad329e4e24372a03a5daa2c34280c56256af5283edb043f8"),
            family("COW", "Cow", "Natural predator of grass.", 1, 5, MobType.ANIMAL, "5d6c6eda942f7f5f71c3161c7306f4aed307d82895f9d2b07ab4525718edc5"),
            family("MUSHROOM_COW", "Mushroom Cow", "Technically a health code violation.", 1, 5, MobType.ANIMAL, "a163bc416b8e6058f92b231e9a524b7fe118eb6e7eeab4ad16d1b52a3ec04fcd"),
            family("PIG", "Pig", "Smells like Bacon.", 1, 5, MobType.ANIMAL, "621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4"),
            family("RABBIT", "Rabbit", "Never stops hopping.", 1, 5, MobType.ANIMAL, "8652c62a166203db8e1edee6039b4962224690b1e25b1fe00ca2338c2bf5fb78"),
            family("SHEEP", "Sheep", "Beep beep I'm a sheep.", 1, 5, MobType.ANIMAL, "30f50394c6d7dbc03ea59fdf504020dc5d6548f9d3bc9dcac896bb5ca08587a"),
    };

    public static final BestiaryEntry[] GARDEN = {
            family("BEETLE", "Beetle", "A particularly gross pest.", 6, 15, MobType.PEST, "70a1e836bf1968b2eaa4837227a19204f17295d870ee9e754bd6b6d60ddbed3c"),
            family("CRICKET", "Cricket", "Usually just a hop, skip, and a jump away.", 6, 15, MobType.PEST, "a24c69f96ce556221e195c8ef2bfad71ebf7f95f5ae914a484a8d0ec21672674"),
            family("DRAGONFLY", "Dragonfly", "Glides by in a blur of wings and whispers.", 6, 15, MobType.PEST, "254aff4c0b2dce3a672349cc0ee9e6f3a9deebe4b3556e84611eca250a7821bf"),
            family("EARTHWORM", "Earthworm", "Big, scary, and pink.", 6, 15, MobType.PEST, "6403ba4027a333d8d2fd32ab59d1cfdbaa7d908d80d2381db2a69cbe65450ad8"),
            family("FIELD_MOUSE", "Field Mouse", "A shy little fellow!", 7, 15, MobType.PEST, "f379e09252817314bd0b694f7d53b48af2c7fa8499109802a41bb294d2f93e3e"),
            family("FIREFLY", "Firefly", "Flickers softly - here one moment, gone the next.", 6, 15, MobType.PEST, "4ce79e90adf34718f313ec24d6c6135b69b3788c618498446ccc83ca640c0b14"),
            family("FLY", "Fly", "A common Garden pest, known for its big eyes.", 6, 15, MobType.PEST, "9d90e777826a52461368e26d1b2e19bfa1ba582d602483e545f4124d0f731842"),
            family("LOCUST", "Locust", "Pretty loud, and pretty annoying.", 6, 15, MobType.PEST, "4b24a482a32db1ea78fb98060b0c2fa4a373cbd18a68edddeb7419455a59cda9"),
            family("LUNAR_MOTH", "Lunar Moth", "Drawn to the light and dew of the moon.", 7, 15, MobType.PEST, "bee4fcc5aac27bdbb0be210df08b05bca5aebc89bf2821f608a55fd2cf0434be"),
            family("MITE", "Mite", "Try and you just mite.", 6, 15, MobType.PEST, "be6baf6431a9daa2ca604d5a3c26e9a761d5952f0817174a4fe0b764616e21ff"),
            family("MOSQUITO", "Mosquito", "One of nature's most useless creatures.", 6, 15, MobType.PEST, "52a9fe05bc663efcd12e56a3ccc5ec035bf577b78708548b6f4ffcf1d30eccfe"),
            family("MOTH", "Moth", "Fluttering around a Garden near you.", 6, 15, MobType.PEST, "65485c4b34e5b5470be94de100e61f7816f81bc5a11dfdf0eccf890172da5d0a"),
            family("PRAYING_MANTIS", "Praying Mantis", "Strikes fast - blink - and it's already gone.", 6, 15, MobType.PEST, "1e04bb6367caa4e88f5fd0ee80f0745d137a6060223dbbc42a16471fdf64bb83"),
            family("RAT", "Rat", "Quick, yet nasty.", 6, 15, MobType.PEST, "a8abb471db0ab78703011979dc8b40798a941f3a4dec3ec61cbeec2af8cffe8"),
            family("SLUG", "Slug", "Methodical in movement, ineffective in survival.", 6, 15, MobType.PEST, "7a79d0fd677b54530961117ef84adc206e2cc5045c1344d61d776bf8ac2fe1ba"),
            family("TIMESTALK_CLONE", "Timestalk Clone", "There can only be one, sometimes two apparently.", 7, 10, MobType.PEST, "1b9add43e0e770b7c4ccdcf6708f8b9e875ff13b2a75ab63ff69f2f0e57af9e5"),
            family("ZOMBUDDY", "Zombuddy", "Zombuddy that I used to know.", 7, 15, MobType.UNDEAD, "b2c4994b7a1c45231b623b8245c117382b267c8856c57cffa2d808c241027a51"),
    };

    public static final BestiaryEntry[] SPIDERS_DEN = {
            family("ARACHNE", "Arachne", "The strongest spider in the den.", 7, 15, MobType.ARTHROPOD, "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1"),
            family("ARACHNES_BROOD", "Arachne's Brood", "Brood of Arachne.", 4, 15, MobType.ARTHROPOD, "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1"),
            family("ARACHNES_KEEPER", "Arachne's Keeper", "Keeper of Arachne.", 6, 15, MobType.ARTHROPOD, "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1"),
            family("BROODMOTHER", "Broodmother", "The Broodmother to end all Broodmothers.", 7, 15, MobType.ARTHROPOD, "cf0622b3998d42b34d5bc760bb2c83fdbc6e68fab05b7ea17b35097ed81190d6"),
            family("DASHER_SPIDER", "Dasher Spider", "Dashingly striking, watch for this spider's long jumps!", 2, 15, MobType.ARTHROPOD, "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1"),
            family("FLINT_SKELETON", "Flint Skeleton", "These Skeletons just never stay dead.", 4, 15, MobType.SKELETAL, "88eb68a4d38e9f446b8e99275f160302f366ef10216abf948487e582512bd0f3"),
            family("RAIN_SLIME", "Rain Slime", "These Slimes enjoy rain...and jumping to their deaths.", 4, 15, MobType.CUBIC, "895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c"),
            family("DEN_SILVERFISH", "Silverfish", "Though weak alone, they find strength in numbers.", 2, 15, MobType.ARTHROPOD, "da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540"),
            family("SPIDER_JOCKEY", "Spider Jockey", "The Skeletons have been controlling Spiders since the dawn of minekind.", 4, 15, MobType.ARTHROPOD, "70939373cafe4b1f5397aafd09f3bb1663e7b629a41a75fbdc1860b6bf8b475f"),
            family("SPLITTER_SPIDER", "Splitter Spider", "If you think you've killed this spider, think again.", 4, 20, MobType.ARTHROPOD, "2acf69fc7af5497a1741891d1e5bf32b96ae0d6c0bbd3c14758ea44a03c57283"),
            family("VORACIOUS_SPIDER", "Voracious Spider", "This spider is quite voracious.", 4, 20, MobType.ARTHROPOD, "8300986ed0a04ea79904f6ae53f49ed3a0ff5b1df62bba622ecbd3777f156df8"),
            family("WEAVER_SPIDER", "Weaver Spider", "Spins a web of lies.", 4, 20, MobType.ARTHROPOD, "921438f646dc04515967e8195ccc3d31e23b02f9aa0ac914edc2822f98394b86"),
    };

    public static final BestiaryEntry[] THE_END = {
            family("DRAGONS", "Dragons", "Inside the Dragon's Den, there is no escape from the fury of the beasts.", 6, 15, MobType.ENDER, Material.DRAGON_HEAD),
            family("END_STONE_PROTECTOR", "End Stone Protector", "Risen from the debris of Endstone.", 8, 20, MobType.CONSTRUCT, "22bcaceeb4162f400d44743315932ac820d3119ac8986a0161a726161ccc93fc"),
            family("END_ENDERMAN", "Enderman", "Not your mother's endermen.", 7, 25, MobType.ENDER, "96c0b36d53fff69a49c7d6f3932f2b0fe948e032226d5e8045ec58408a36e951"),
            family("ENDERMITE", "Endermite", "Hidden in the stone of The End.", 7, 25, MobType.ENDER, "5a1a0831aa03afb4212adcbb24e5dfaa7f476a1173fce259ef75a85855"),
            family("OBSIDIAN_DEFENDER", "Obsidian Defender", "Forged from the forces of hot and cold.", 7, 25, MobType.CONSTRUCT, Material.OBSIDIAN),
            family("SEER", "Seer", "Always watching.", 7, 25, MobType.ENDER, "daa8fc8de6417b48d48c80b443cf5326e3d9da4dbe9b25fcd49549d96168fc0"),
            family("VOIDLING_EXTREMIST", "Voidling Extremist", "The top lieutenants of the void heraldry.", 4, 15, MobType.ENDER, "eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3"),
            family("VOIDLING_FANATIC", "Voidling Fanatic", "A step beyond zealotry.", 5, 25, MobType.ENDER, "e53b7bcd56f0b95f87dd79ed17636beb9d8346744014228ea2f6b110b148c1"),
            family("ZEALOT", "Zealot", "Unwavering in their devotion to the Dragon.", 5, 25, MobType.ENDER, Material.ENDER_CHEST),
    };

    public static final BestiaryEntry[] THE_PARK = {
            family("HOWLING_SPIRIT", "Howling Spirit", "Howling at the moon.", 3, 15, MobType.ANIMAL, "69d1d3113ec43ac2961dd59f28175fb4718873c6c448dfca8722317d67"),
            family("PACK_SPIRIT", "Pack Spirit", "Spirits of long-dead wolves, still prowling the caves for prey.", 3, 15, MobType.ANIMAL, "69d1d3113ec43ac2961dd59f28175fb4718873c6c448dfca8722317d67"),
            family("SOUL_OF_THE_ALPHA", "Soul of the Alpha", "Ghastly leader of the pack.", 4, 15, MobType.ANIMAL, "d359537c15534f61c1cd886bc118774ed22280e7cdab6613870160aad4ca39"),
    };

    public static final BestiaryEntry[] JERRY = {
            family("GREEN_JERRY", "Green Jerry", "Jerry.", 4, 10, MobType.HUMANOID, "822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b"),
            family("BLUE_JERRY", "Blue Jerry", "Jerry?", 5, 10, MobType.HUMANOID, "822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b"),
            family("PURPLE_JERRY", "Purple Jerry", "Jerry!", 6, 10, MobType.HUMANOID, "822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b"),
            family("GOLDEN_JERRY", "Golden Jerry", "Jerry?!", 7, 10, MobType.TIMID, "822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b"),
    };
}
