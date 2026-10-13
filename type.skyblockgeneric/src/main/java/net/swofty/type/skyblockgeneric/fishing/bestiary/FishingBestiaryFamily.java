package net.swofty.type.skyblockgeneric.fishing.bestiary;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.CataloguedBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfiles;
import net.swofty.type.skyblockgeneric.fishing.festival.FestivalShark;
import net.swofty.type.skyblockgeneric.fishing.festival.SharkMob;
import net.swofty.type.skyblockgeneric.fishing.lava.LavaSeaCreature;
import net.swofty.type.skyblockgeneric.fishing.lava.LavaSeaCreatures;
import net.swofty.type.skyblockgeneric.fishing.winter.WinterSeaCreature;
import net.swofty.type.skyblockgeneric.fishing.winter.WinterSeaCreatureMob;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryEntry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Every Fishing Bestiary family on Hypixel, in in-game (alphabetical) order per section, with the
 * wiki's lore, bracket and max tier (wiki: Bestiary/List, Fishing tab) and NEU's icons
 * (constants/bestiary.json). {@code mobKeys} are the Bestiary kill keys of the mobs this server
 * can spawn for the family; a family with none is catalogued but can never be found yet.
 */
@Getter
public enum FishingBestiaryFamily implements BestiaryEntry {
    ABYSSAL_MINER(FishingBestiarySection.FISHING, "Abyssal Miner", "Didn't escape before the cave-in.", 6, 15,
            head("58be05cfae2c6a7d47da2ce88b3e00c72a145cc3218f041b3dd5bd5fa5ca827"), "ABYSSAL_MINER"),
    AGARIMOO(FishingBestiarySection.FISHING, "Agarimoo", "Galactose-based heterogenous polysaccharide derived from rift algae.", 6, 20,
            head("3d597f77cde32c9ac9b06f82fcf7c9cb500facc14bff166222b24be39962f0ef")),
    BLUE_RINGED_OCTOPUS(FishingBestiarySection.FISHING, "Blue Ringed Octopus", "Quite the escape artist.", 6, 15,
            head("b2b6074d0c9d6b89a494cf4f74158282a64ee23ba8a0725633ad70932ada1a8f")),
    CARROT_KING(FishingBestiarySection.FISHING, "Carrot King", "Has a vicious streak a mile wide.", 6, 15,
            head("1f59851de93f4c6547f809ca3aed189e94bbf4f888f1f75208e94c3733852a1"), "CARROT_KING"),
    CATFISH(FishingBestiarySection.FISHING, "Catfish", "Looked different in the pictures.", 6, 20,
            head("e18f77331b2cab64e2b430fa8e4273e4db7f78fcdfa4b1a9a418af47375056eb"), "CATFISH"),
    DEEP_SEA_PROTECTOR(FishingBestiarySection.FISHING, "Deep Sea Protector", "Nobody knows what it protects.", 7, 20,
            head("22bcaceeb4162f400d44743315932ac820d3119ac8986a0161a726161ccc93fc")),
    FROG_MAN(FishingBestiarySection.FISHING, "Frog Man", "You'd think it'd make a normal frog sound, but it actually just says 'Ribbit' out loud.", 4, 15,
            head("6157f19da077a3df49b2925fb6e8b400222ba6e559e86815f9b296d9e9667dd7")),
    GUARDIAN_DEFENDER(FishingBestiarySection.FISHING, "Guardian Defender", "Defends the sea.", 7, 20,
            head("221025434045bda7025b3e514b316a4b770c6faa4ba9adb4be3809526db77f9d"), "GUARDIAN_DEFENDER"),
    INKLING(FishingBestiarySection.FISHING, "Inkling", "You gotta be squiddin' me!", 4, 15,
            head("d8705624daa2956aa45956c81bab5f4fdb2c74a596051e24192039aea3a8b8"), "NIGHT_SQUID"),
    MANTA_RAY(FishingBestiarySection.FISHING, "Manta Ray", "A gentle giant. It just doesn't like you specifically.", 7, 20,
            head("e31cc7118e4c40b398ff80d23f5ee50797fc4c2609fed903fe5eb7528baba110")),
    MITHRIL_GRUBBER(FishingBestiarySection.FISHING, "Mithril Grubber", "I die a little every time I take out a Grubber...", 3, 10,
            head("811a1173af3bead305e6339f555662e990d5faadb87e07299fa68ca828a6d2fb")),
    OASIS_RABBIT(FishingBestiarySection.FISHING, "Oasis Rabbit", "Loyal subject of the Carrot King.", 3, 10,
            head("1f59851de93f4c6547f809ca3aed189e94bbf4f888f1f75208e94c3733852a1")),
    OASIS_SHEEP(FishingBestiarySection.FISHING, "Oasis Sheep", "A test subject to see if a sheep shrinks when it dries.", 3, 10,
            head("292df216ecd27624ac771bacfbfe006e1ed84a79e9270be0f88e9c8791d1ece4")),
    POISONED_WATER_WORM(FishingBestiarySection.FISHING, "Poisoned Water Worm", "Somebody's poisoned the waterhole!", 6, 20,
            head("5a1a0831aa03afb4212adcbb24e5dfaa7f476a1173fce259ef75a85855"), "POISONED_WATER_WORM"),
    RIDER_OF_THE_DEEP(FishingBestiarySection.FISHING, "Rider of the Deep", "The chicken is the one in charge.", 4, 15,
            head("cfb7dbbe002f69463768113c1e925848197f59b62694ce105792dd5a52dc17a1"), "MONSTER_OF_THE_DEEP"),
    SEA_ARCHER(FishingBestiarySection.FISHING, "Sea Archer", "Though a gifted archer, has a love for fistfights.", 4, 15,
            head("88eb68a4d38e9f446b8e99275f160302f366ef10216abf948487e582512bd0f3"), "SEA_ARCHER"),
    SEA_LEECH(FishingBestiarySection.FISHING, "Sea Leech", "See, a leech!", 6, 20,
            head("da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540"), "SEA_LEECH"),
    SEA_WALKER(FishingBestiarySection.FISHING, "Sea Walker", "Why swim when you can simply walk?", 4, 15,
            head("d88ba8bb50b79e441e47b7e452764d5fff6693779d2dadd9f7f52f98d7ea0"), "SEA_WALKER"),
    SEA_WITCH(FishingBestiarySection.FISHING, "Sea Witch", "She'll turn you into a newt!", 4, 15,
            head("fce6604157fc4ab5591e4bcf507a749918ee9c41e357d47376e0ee7342074c90"), "SEA_WITCH"),
    SNAPPING_TURTLE(FishingBestiarySection.FISHING, "Snapping Turtle", "Doesn't look like its very good at it's job.", 6, 20,
            head("e08fc1ae87a7035d32b0b0da58de4801463aaf8b238618024aacb0c515ae3bba")),
    SQUID(FishingBestiarySection.FISHING, "Squid", "Just a lowly squid.", 4, 15,
            head("d8705624daa2956aa45956c81bab5f4fdb2c74a596051e24192039aea3a8b8"), "SQUID"),
    WATER_HYDRA(FishingBestiarySection.FISHING, "Water Hydra", "Unlike a regular hydra, this one grows another body as well!", 6, 15,
            head("21ffbda92442873d54051e4e17ea47278cb3b747d798070ba83bc4a9de58dd"), "WATER_HYDRA"),
    WATER_WORM(FishingBestiarySection.FISHING, "Water Worm", "Hasn't been poisoned...yet.", 6, 20,
            head("da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540"), "WATER_WORM"),
    WIKI_TIKI(FishingBestiarySection.FISHING, "Wiki Tiki", "An ancient fragment of the Kalhuiki tribe, lost from long before they went underground.", 8, 15,
            head("f3c802e580bfefc18c4af94cceb82968b5b4aeab0d832346a633a7473a41dfac")),

    FIERY_SCUTTLER(LavaSeaCreature.FIERY_SCUTTLER),
    FIRE_EEL(LavaSeaCreature.FIRE_EEL),
    FIREPROOF_WITCH(LavaSeaCreature.FIREPROOF_WITCH),
    FLAMING_WORM(FishingBestiarySection.LAVA, "Flaming Worm", "Like a regular worm, but on fire.", 6, 20,
            head("da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540"), "FLAMING_WORM"),
    FRIED_CHICKEN(LavaSeaCreature.FRIED_CHICKEN),
    LAVA_BLAZE(FishingBestiarySection.LAVA, "Lava Blaze", "Is it made of lava or does it just live in it?", 7, 20,
            head("b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0"), "LAVA_BLAZE"),
    LAVA_FLAME(LavaSeaCreature.LAVA_FLAME),
    LAVA_LEECH(LavaSeaCreature.LAVA_LEECH),
    LAVA_PIGMAN(FishingBestiarySection.LAVA, "Lava Pigman", "Normally, dunking pork in lava causes it to get charred. But not this one!", 7, 20,
            head("74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"), "LAVA_PIGMAN"),
    LORD_JAWBUS(LavaSeaCreature.LORD_JAWBUS),
    MAGMA_PILLAR(LavaSeaCreature.MAGMA_PILLAR),
    MAGMA_SLUG(LavaSeaCreature.MAGMA_SLUG),
    MOOGMA(LavaSeaCreature.MOOGMA),
    PLHLEGBLAST(LavaSeaCreature.PLHLEGBLAST),
    PYROCLASTIC_WORM(LavaSeaCreature.PYROCLASTIC_WORM),
    RAGNAROK(LavaSeaCreature.RAGNAROK),
    TAURUS(LavaSeaCreature.TAURUS),
    THUNDER(LavaSeaCreature.THUNDER),
    VOLCANIC_SNAIL(LavaSeaCreature.VOLCANIC_SNAIL),

    GRIM_REAPER(FishingBestiarySection.SPOOKY_FESTIVAL, "Grim Reaper", "An old friend.", 8, 15,
            head("68a6194a5b217b9f5a3dfecce5f3efe6967405039b82fa0c4e8959175f32e75a")),
    JUMPIN_JACK(FishingBestiarySection.SPOOKY_FESTIVAL, "Jumpin' Jack", "He's just happy to see ya.", 4, 15,
            new GUIMaterial(Material.CARVED_PUMPKIN)),
    NIGHTMARE(FishingBestiarySection.SPOOKY_FESTIVAL, "Nightmare", "The end is neigh.", 6, 20,
            head("578211e1b4d99d1c7bfda4838e48fc884c3eae376f58d932bc2f78b0a919f8e7")),
    PHANTOM_FISHER(FishingBestiarySection.SPOOKY_FESTIVAL, "Phantom Fisher", "Who is fishing who?", 6, 15,
            head("6c9bc01f299f98d565a27ba10a1293915ae8beeefb8a67845e2331dbe6fd6fd6")),
    SCARECROW(FishingBestiarySection.SPOOKY_FESTIVAL, "Scarecrow", "Also found at a yellow brick road.", 4, 15,
            new GUIMaterial(Material.CARVED_PUMPKIN)),
    WEREWOLF(FishingBestiarySection.SPOOKY_FESTIVAL, "Werewolf", "A very careful and aware wolf.", 7, 20,
            head("ce4606c6d973a999aec1687c7e075f7d37db8185e88b844507f16b3e2b3eb690")),

    BLUE_SHARK(FestivalShark.BLUE_SHARK),
    GREAT_WHITE_SHARK(FestivalShark.GREAT_WHITE_SHARK),
    NURSE_SHARK(FestivalShark.NURSE_SHARK),
    TIGER_SHARK(FestivalShark.TIGER_SHARK),

    FROSTY(WinterSeaCreature.FROSTY_THE_SNOWMAN),
    FROZEN_STEVE(WinterSeaCreature.FROZEN_STEVE),
    GRINCH(WinterSeaCreature.GRINCH),
    NUTCRACKER(WinterSeaCreature.NUTCRACKER),
    REINDRAKE(WinterSeaCreature.REINDRAKE),
    YETI(WinterSeaCreature.YETI),

    ALLIGATOR(FishingBestiarySection.BACKWATER_BAYOU, "Alligator", "Just because he has thick skin doesn't mean he doesn't get sad sometimes.", 6, 15,
            head("af27a546bb8f2ee3664e4d156bec075c7bde1d6245e7265047ec0b4b1a736c75")),
    BANSHEE(FishingBestiarySection.BACKWATER_BAYOU, "Banshee", "Scream, scream, let it all out!", 6, 20,
            head("30ccc3c9a06de657b98f881e23a57ecaeb252c364ddb7b92564f5ed2b8087e3b")),
    BAYOU_SLUDGE(FishingBestiarySection.BACKWATER_BAYOU, "Bayou Sludge", "Swampy swampy swamp.", 7, 20,
            head("895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c")),
    DUMPSTER_DIVER(FishingBestiarySection.BACKWATER_BAYOU, "Dumpster Diver", "Everybody was dumpster divin'!", 4, 15,
            head("f5c5eb5ee072c06580986d12a029e28010c1290875534810c53140bc76dabfeb")),
    TITANOBOA(FishingBestiarySection.BACKWATER_BAYOU, "Titanoboa", "Yet another great reason to keep away from the Backwater Bayou.", 8, 15,
            head("645f2c0bbfe3b8b19b7452072db69a5f59da38ff61415545156e5701e1be756d")),
    TRASH_GOBBLER(FishingBestiarySection.BACKWATER_BAYOU, "Trash Gobbler", "Yummers!", 4, 15,
            head("18ae7046da98dcb33f3ed42f1dc41d08ac8dfa5db3a3860de5b1b5c056804187")),
    ;

    private final FishingBestiarySection section;
    private final String familyName;
    private final String description;
    private final int bracket;
    private final int maxTier;
    private final GUIMaterial guiMaterial;
    private final List<String> mobKeys;
    @Getter(lombok.AccessLevel.NONE)
    private volatile List<BestiaryMob> mobs;

    FishingBestiaryFamily(FishingBestiarySection section, String familyName, String description,
                          int bracket, int maxTier, GUIMaterial guiMaterial, String... mobKeys) {
        this.section = section;
        this.familyName = familyName;
        this.description = description;
        this.bracket = bracket;
        this.maxTier = maxTier;
        this.guiMaterial = guiMaterial;
        this.mobKeys = List.of(mobKeys);
    }

    FishingBestiaryFamily(FestivalShark shark) {
        this(FishingBestiarySection.FISHING_FESTIVAL, shark.getDisplayName(), shark.getBestiary().description(),
                shark.getBestiary().bracket(), shark.getBestiary().maxTier(),
                head(shark.getHeadTexture()), shark.name());
    }

    FishingBestiaryFamily(LavaSeaCreature creature) {
        this(FishingBestiarySection.LAVA, creature.getDisplayName(), creature.getBestiary().description(),
                creature.getBestiary().bracket(), creature.getBestiary().maxTier(),
                creature.getGuiMaterial(), creature.name());
    }

    FishingBestiaryFamily(WinterSeaCreature creature) {
        this(FishingBestiarySection.WINTER, creature.getDisplayName(), creature.getBestiary().description(),
                creature.getBestiary().bracket(), creature.getBestiary().maxTier(),
                creature.getGuiMaterial(), creature.name());
    }

    private static GUIMaterial head(String texture) {
        return new GUIMaterial(texture);
    }

    private static final class SeaCreatureIndex {
        static final Map<String, FishingBestiaryFamily> BY_KEY = Arrays.stream(values())
                .flatMap(family -> family.mobKeys.stream().map(key -> Map.entry(key, family)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        static final Map<String, SeaCreatureProfile> PROFILES = SeaCreatureProfiles.CANONICAL.stream()
                .collect(Collectors.toUnmodifiableMap(SeaCreatureProfile::id, Function.identity()));
    }

    public static Optional<FishingBestiaryFamily> forMobKey(String mobKey) {
        return Optional.ofNullable(SeaCreatureIndex.BY_KEY.get(mobKey));
    }

    public static FishingBestiaryFamily[] in(FishingBestiarySection section) {
        return Arrays.stream(values())
                .filter(family -> family.section == section)
                .toArray(FishingBestiaryFamily[]::new);
    }

    @Override
    public String getName() {
        return "<a>" + familyName;
    }

    @Override
    public List<BestiaryMob> getMobs() {
        List<BestiaryMob> resolved = mobs;
        if (resolved == null) {
            resolved = mobKeys.isEmpty()
                    ? List.of(CataloguedBestiaryMob.of(new CataloguedBestiaryMob.Family(name(), familyName, bracket, maxTier, guiMaterial, MobType.AQUATIC)))
                    : mobKeys.stream().map(FishingBestiaryFamily::spawnable).toList();
            mobs = resolved;
        }
        return resolved;
    }

    private static BestiaryMob spawnable(String mobKey) {
        for (FestivalShark shark : FestivalShark.values()) {
            if (shark.name().equals(mobKey)) return SharkMob.of(shark);
        }
        for (WinterSeaCreature creature : WinterSeaCreature.values()) {
            if (creature.name().equals(mobKey)) return WinterSeaCreatureMob.of(creature, null);
        }
        LavaSeaCreature lava = LavaSeaCreature.byId(mobKey);
        if (lava != null) return LavaSeaCreatures.create(lava, null, false);
        SeaCreatureProfile profile = SeaCreatureIndex.PROFILES.get(mobKey);
        if (profile == null) throw new IllegalStateException("No sea creature for Bestiary key " + mobKey);
        return SeaCreatureMob.create(profile);
    }
}
