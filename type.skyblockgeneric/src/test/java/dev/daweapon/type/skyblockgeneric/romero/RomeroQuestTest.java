package dev.daweapon.type.skyblockgeneric.romero;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RomeroQuestTest {
    private final RomeroDialogue dialogue = RomeroDialogue.load(Path.of("../configuration/skyblock/romero_juliette/dialogue.yml"));

    @Test
    void tenStepsInOrderWithUniqueQuestItems() {
        assertEquals(10, RomeroStep.values().length);
        Set<String> items = new HashSet<>();
        for (int i = 0; i < RomeroStep.values().length; i++) {
            RomeroStep step = RomeroStep.values()[i];
            assertEquals(i + 1, step.getNumber());
            assertTrue(items.add(step.getQuestItemId()), step.name());
            assertEquals(step, RomeroStep.of(i + 1));
        }
    }

    @Test
    void romeroHasEveryLineEachStepCanPlay() {
        Map<String, List<String>> romero = dialogue.romero();
        for (RomeroStep step : RomeroStep.values()) {
            assertTrue(romero.containsKey("r" + step.getNumber() + "-start") || step == RomeroStep.WEDDING, step.name());
            assertTrue(romero.containsKey("r" + step.getNumber() + "-complete") || step == RomeroStep.WEDDING, step.name());
        }
        for (String key : List.of("bring-item", "bring-item-missing", "r2-wrong-flower", "r2-1-poppy", "r2-2-7-poppies", "r2-8-11-poppies",
                "r2-12-14-poppies", "r3-insufficient", "r3-enchanted-emerald", "r3-enchanted-block", "r3-block", "r4-red-mushroom",
                "r4-brown-mushroom", "r4-red-block", "r4-brown-block", "r4-enchanted-brown-block", "r5-wrong-potion", "r5-rabbit-not-6",
                "r5-no-coffee", "r6-flint", "r6-lava", "r6-magical-lava", "r7-int-250", "r7-int-500", "r7-int-750", "r7-int-900",
                "r7-int-1150", "r8-jerry", "r8-emerald", "r8-weak-sword", "r10-before-suit", "r10-after-suit", "idle")) {
            assertTrue(romero.containsKey(key), key);
        }
    }

    @Test
    void julietteHasStartAndCompletionForEveryStep() {
        Map<String, List<String>> juliette = dialogue.juliette();
        for (int step = 1; step <= 9; step++) {
            assertTrue(juliette.containsKey("j" + step + "-start"), "start " + step);
            assertTrue(juliette.containsKey("j" + step + "-complete"), "complete " + step);
        }
        for (String key : List.of("not-holding", "j10-before-suit", "j10-after-suit", "idle")) assertTrue(juliette.containsKey(key), key);
    }

    @Test
    void dialogueCarriesNoRawWikiCodesOrEmptyLines() {
        List<String> all = new ArrayList<>();
        dialogue.romero().values().forEach(all::addAll);
        dialogue.juliette().values().forEach(all::addAll);
        for (String line : all) {
            assertFalse(line.contains("&"), line);
            assertFalse(line.isBlank(), line);
        }
    }

    @Test
    void ringChainMatchesTheItemCatalogue() throws IOException {
        Map<String, Object> root = new Yaml().load(Files.newInputStream(Path.of("../configuration/skyblock/items/accessories/ac2_ring_of_love.yml")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) root.get("items");
        assertEquals(RomeroRing.values().length, items.size());
        for (int i = 0; i < items.size(); i++) {
            assertEquals("WEDDING_RING_" + i, items.get(i).get("id"));
            assertEquals(RomeroRing.values()[i].getDisplayName(), items.get(i).get("name"));
        }
    }

    @Test
    void upgradeMessagesNameBothRings() {
        assertEquals("<e>Your Shiny Yellow Rock <e>was upgraded to Yellow Rock of Love<e>!",
                RomeroRing.SHINY_YELLOW_ROCK.upgradeMessage(RomeroRing.YELLOW_ROCK_OF_LOVE));
        assertEquals("<e>Your <a>Mediocre Ring of Love <e>was upgraded to <a>Rubbish Ring of Love<e>!",
                RomeroRing.MEDIOCRE.upgradeMessage(RomeroRing.RUBBISH));
    }

    @Test
    void potionDataKeepsItsCoffeeBase() {
        dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePotionData attribute =
                new dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePotionData();
        attribute.setValue(attribute.loadFromString("RABBIT:6:180:false:false:CHEAP_COFFEE"));
        assertEquals("CHEAP_COFFEE", attribute.getValue().getBase());
        assertEquals("RABBIT:6:180:false:false:CHEAP_COFFEE", attribute.saveIntoString());
        attribute.setValue(attribute.loadFromString("SPEED:3:180:true:false"));
        assertEquals(null, attribute.getValue().getBase());
        assertEquals("SPEED:3:180:true:false:", attribute.saveIntoString());
    }
}
