package dev.daweapon.type.skyblockgeneric.item;

import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.generic.i18n.HypixelTranslator;
import dev.daweapon.type.skyblockgeneric.item.components.BowComponent;
import dev.daweapon.type.skyblockgeneric.item.components.QuiverDisplayComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ShortBowComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.BowShot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The server loads item YAML in directory order and the last definition of an id wins, so a bow or rod defined
 * twice behaves differently from one server to the next. These checks run against every file, as the server does.
 */
class BowAndRodItemsTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final List<String> SHORTBOWS = List.of("ARTISANAL_SHORTBOW", "DRAGON_SHORTBOW", "MACHINE_GUN_BOW",
            "JUJU_SHORTBOW", "SCORPION_BOW", "MOSQUITO_BOW", "ITEM_SPIRIT_BOW", "STARRED_ITEM_SPIRIT_BOW", "TERMINATOR");
    private static final Map<String, List<String>> DEFINITIONS = new HashMap<>();
    private static final Map<String, String> MATERIALS = new HashMap<>();

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        GlobalTranslator.translator().addSource(new HypixelTranslator());
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).sorted().toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) {
                    Map<String, Object> config = (Map<String, Object>) entry;
                    String id = String.valueOf(config.get("id"));
                    DEFINITIONS.computeIfAbsent(id, _ -> new ArrayList<>()).add(ITEMS.relativize(file).toString());
                    MATERIALS.put(id, String.valueOf(config.get("material")).toUpperCase());
                    ItemConfigParser.parseItem(config);
                }
            }
        }
    }

    private static List<String> lore(ItemType type) {
        return new SkyBlockItem(type).getLoreText().stream()
                .map(line -> line.plain(Locale.US).replaceAll("§.", "").stripTrailing())
                .toList();
    }

    @Test
    void everyBowAndRodIsDefinedOnce() {
        List<String> duplicates = DEFINITIONS.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .filter(entry -> List.of("BOW", "FISHING_ROD").contains(MATERIALS.get(entry.getKey())))
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .toList();
        assertTrue(duplicates.isEmpty(), "Defined more than once: " + duplicates);
    }

    @Test
    void everyBowCanShoot() {
        List<String> silent = new ArrayList<>();
        for (Map.Entry<String, String> entry : MATERIALS.entrySet()) {
            if (!entry.getValue().equals("BOW")) continue;
            SkyBlockItem bow = new SkyBlockItem(ItemType.valueOf(entry.getKey()));
            if (BowShot.bowOf(bow) == null) silent.add(entry.getKey());
        }
        assertTrue(silent.isEmpty(), "Bows with no shot handler: " + silent);
    }

    @Test
    void everyShortbowFiresInstantlyAndShowsFeathers() {
        for (String id : SHORTBOWS) {
            SkyBlockItem bow = new SkyBlockItem(ItemType.valueOf(id));
            assertTrue(bow.hasComponent(ShortBowComponent.class), id);
            assertTrue(BowShot.isShortBow(bow), id);
            assertFalse(bow.getComponent(QuiverDisplayComponent.class).isShouldBeArrow(), id);
            assertTrue(lore(ItemType.valueOf(id)).stream().anyMatch(line -> line.startsWith("Shot Cooldown: ")), id);
            assertTrue(lore(ItemType.valueOf(id)).contains("Shortbow: Instantly shoots!"), id);
        }
        assertFalse(BowShot.isShortBow(new SkyBlockItem(ItemType.BOW)));
        assertTrue(new SkyBlockItem(ItemType.BOW).hasComponent(BowComponent.class));
    }

    @Test
    void terminatorLoreMatchesHypixel() {
        List<String> lore = lore(ItemType.TERMINATOR);
        assertTrue(lore.contains("Shot Cooldown: 0.5s"), lore.toString());
        assertTrue(lore.contains("Shoots 3 arrows at once."), lore.toString());
        assertTrue(lore.stream().anyMatch(line -> line.startsWith("Ability: Salvation")), lore.toString());
        assertTrue(lore.indexOf("Shortbow: Instantly shoots!") > lore.indexOf("Soulflow Cost: 1⸎")
                || lore.stream().noneMatch(line -> line.startsWith("Soulflow Cost")), lore.toString());
        assertTrue(lore.indexOf("Shortbow: Instantly shoots!") > lore.indexOf("Shot Cooldown: 0.5s"), lore.toString());
    }

    @Test
    void magmaRodLoreMatchesTheWiki() {
        assertEquals(List.of(
                "Lava Rod",
                "",
                "Damage: +175",
                "Strength: +175",
                "Fishing Speed: +40",
                "Sea Creature Chance: +6%",
                "Trophy Chance: +15%",
                "Gemstones: [☂]",
                "",
                "ථ Hook NONE",
                "ꨃ Line NONE",
                "࿉ Sinker NONE",
                "",
                "Talk to Roddy in the Backwater",
                "Bayou to apply parts to this rod.",
                "",
                "This item can be reforged!",
                "❣ Requires Fishing Skill 27.",
                "RARE FISHING ROD"), lore(ItemType.MAGMA_ROD));
    }

    @Test
    void onlyTheStarterLavaRodIsSoulbound() {
        assertTrue(lore(ItemType.STARTER_LAVA_ROD).contains("* Co-op Soulbound *"));
        assertTrue(lore(ItemType.INFERNO_ROD).stream().noneMatch(line -> line.contains("Soulbound")));
    }
}
