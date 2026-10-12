package net.swofty.type.skyblockgeneric.accessory;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.Recombobulation;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.staff.MaxAccessories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessoryFrameworkTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Set<String> YAML_ACCESSORIES = new TreeSet<>();

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) {
                    Map<String, Object> item = (Map<String, Object>) entry;
                    ItemConfigParser.parseItem(item);
                    if (item.get("components") instanceof List<?> components && components.stream().anyMatch(c ->
                            c instanceof Map<?, ?> m && ("ACCESSORY".equals(m.get("id")) || "TALISMAN".equals(m.get("id"))))) {
                        YAML_ACCESSORIES.add(String.valueOf(item.get("id")));
                    }
                }
            }
        }
    }

    private static SkyBlockItem item(String id) {
        ItemType type = ItemType.get(id);
        assertNotNull(type, id + " is missing from ItemType; regenerate it");
        return new SkyBlockItem(type);
    }

    private static List<OwnedAccessory> bag(SkyBlockItem... items) {
        List<OwnedAccessory> owned = new ArrayList<>();
        for (SkyBlockItem item : items) owned.add(OwnedAccessory.of(item, true));
        return owned;
    }

    @Test
    void everyAccessoryParsesItsAccessoryBlock() {
        List<String> failures = new ArrayList<>();
        for (String id : YAML_ACCESSORIES) {
            ItemType type = ItemType.get(id);
            if (type == null) {
                failures.add(id + ": not in ItemType");
                continue;
            }
            if (!new SkyBlockItem(type).hasComponent(AccessoryComponent.class)) failures.add(id + ": ACCESSORY block failed to parse");
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void familiesLoadAndOnlyHoldAccessories() {
        AccessoryFamilies families = AccessoryFamilies.get();
        assertEquals(95, families.all().size());
        List<String> failures = new ArrayList<>();
        for (AccessoryFamily family : families.all()) {
            for (String member : family.members()) {
                ItemType type = ItemType.get(member);
                if (type != null && !new SkyBlockItem(type).hasComponent(AccessoryComponent.class))
                    failures.add(family.id() + ": " + member + " exists but is not an accessory");
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
        assertEquals("SUMSUNG_GG_ABICASE", families.byId("ABICASES").best());
        assertEquals("SPEED_TALISMAN", families.familyOf("SPEED_RING").id());
    }

    @Test
    void onlyTheHighestMemberOfALineCounts() {
        List<OwnedAccessory> active = AccessoryOwnership.dedupe(bag(item("SPEED_TALISMAN"), item("SPEED_ARTIFACT"),
                item("SPEED_RING"), item("SPEED_ARTIFACT")));
        assertEquals(1, active.size());
        assertEquals(ItemType.SPEED_ARTIFACT, active.getFirst().type());

        List<OwnedAccessory> cases = AccessoryOwnership.dedupe(bag(item("SUMSUNG_G3_ABICASE"), item("REZAR_ABICASE")));
        assertEquals(1, cases.size(), "only one Abicase counts");

        List<OwnedAccessory> mixed = AccessoryOwnership.dedupe(bag(item("SPEED_RING"), item("BAT_TALISMAN"), item("BAT_RING")));
        assertEquals(2, mixed.size());
    }

    @Test
    void bagStatsApplyOncePerLine() {
        List<OwnedAccessory> active = AccessoryOwnership.dedupe(bag(item("SPEED_TALISMAN"), item("SPEED_RING"),
                item("SPEED_ARTIFACT"), item("SPEED_ARTIFACT"), item("BAT_ARTIFACT")));
        ItemStatistics stats = AccessoryStats.of(null, active, ignored -> ItemStatistics.empty());
        assertEquals(5 + 3, stats.getOverall(ItemStatistic.SPEED), 0.0001);
        assertEquals(5, stats.getOverall(ItemStatistic.HEALTH), 0.0001);
    }

    @Test
    void recombobulationRaisesAccessoryPower() {
        SkyBlockItem artifact = item("SPEED_ARTIFACT");
        int before = AccessoryPower.itemPower(artifact, 0, false);
        assertTrue(Recombobulation.canRecombobulate(artifact));
        artifact.getAttributeHandler().setRecombobulated(true);
        assertEquals(Rarity.RARE.getAccessoryPower(), before);
        assertEquals(Rarity.EPIC.getAccessoryPower(), AccessoryPower.itemPower(artifact, 0, false));
        assertFalse(Recombobulation.canRecombobulate(artifact), "only once");

        assertFalse(Recombobulation.canRecombobulate(item("VOTER_BADGE_SUPREME")));
        assertFalse(Recombobulation.canRecombobulate(item("RIFT_PRISM")));
    }

    @Test
    void rarityUpgradeStopsAtTheTop() {
        assertEquals(Rarity.MYTHIC, Rarity.LEGENDARY.upgrade());
        assertEquals(Rarity.DIVINE, Rarity.MYTHIC.upgrade());
        assertEquals(Rarity.DIVINE, Rarity.DIVINE.upgrade());
        assertEquals(Rarity.VERY_SPECIAL, Rarity.SPECIAL.upgrade());
        assertEquals(Rarity.VERY_SPECIAL, Rarity.VERY_SPECIAL.upgrade());
        assertEquals(Rarity.ADMIN, Rarity.ADMIN.upgrade());
        assertFalse(Rarity.DIVINE.canUpgrade());
    }

    @Test
    void specialAccessoryPowerRules() {
        SkyBlockItem hegemony = item("HEGEMONY_ARTIFACT");
        assertEquals(32, AccessoryPower.itemPower(hegemony, 0, false));
        hegemony.getAttributeHandler().setRecombobulated(true);
        assertEquals(44, AccessoryPower.itemPower(hegemony, 0, false));

        assertEquals(8 + 41, AccessoryPower.itemPower(item("SUMSUNG_GG_ABICASE"), 82, false));

        SkyBlockItem skull = item("MASTER_SKULL_TIER_7");
        assertEquals(32, AccessoryPower.itemPower(skull, 0, true));
        assertEquals(16, AccessoryPower.itemPower(skull, 0, false));

        List<OwnedAccessory> withPrism = bag(item("RIFT_PRISM"), item("SPEED_ARTIFACT"));
        assertEquals(8 + 8, AccessoryPower.of(withPrism, 0, false, false));
        assertEquals(11 + 8, AccessoryPower.of(withPrism, 0, true, false), "an imbued prism replaces the item's power");

        List<OwnedAccessory> inventoryOnly = List.of(OwnedAccessory.of(item("SPEED_ARTIFACT"), false));
        assertEquals(0, AccessoryPower.of(inventoryOnly, 0, false, false), "inventory accessories give no Accessory Power");
    }

    @Test
    void bagPagesRoundTripWithoutDuplicatesOrLoss() {
        DatapointAccessoryBag.PlayerAccessoryBag bag = new DatapointAccessoryBag.PlayerAccessoryBag();
        for (int slot = 0; slot < 60; slot++) bag.setInSlot(slot, item(slot % 2 == 0 ? "SPEED_TALISMAN" : "BAT_TALISMAN"));
        int total = 90;
        assertEquals(2, AccessoryBagPages.pages(total));
        assertEquals(45, AccessoryBagPages.slotsOnPage(1, total));

        for (int page = 0; page < AccessoryBagPages.pages(total); page++) {
            int start = AccessoryBagPages.firstSlot(page);
            AccessoryBagPages.writePage(bag, page, total, i -> bag.getInSlot(start + i));
        }
        assertEquals(60, bag.getAccessoryMap().size());

        AccessoryBagPages.writePage(bag, 1, total, i -> i == 0 ? item("HEGEMONY_ARTIFACT") : null);
        assertEquals(46, bag.getAccessoryMap().size(), "page 1 replaced, page 0 untouched");
        assertEquals(ItemType.HEGEMONY_ARTIFACT, bag.getInSlot(45).getAttributeHandler().getPotentialType());
        assertEquals(ItemType.SPEED_TALISMAN, bag.getInSlot(0).getAttributeHandler().getPotentialType());
    }

    @Test
    void bagSerializerKeepsDiscoveredAccessories() {
        DatapointAccessoryBag.PlayerAccessoryBag bag = new DatapointAccessoryBag.PlayerAccessoryBag();
        bag.setInSlot(3, item("SPEED_RING"));
        bag.addDiscoveredAccessory(ItemType.SPEED_RING);
        bag.addDiscoveredAccessory(ItemType.BAT_TALISMAN);
        DatapointAccessoryBag.PlayerAccessoryBag copy = DatapointAccessoryBag.serializer.deserialize(
                DatapointAccessoryBag.serializer.serialize(bag));
        assertEquals(List.of(ItemType.SPEED_RING, ItemType.BAT_TALISMAN), copy.getDiscoveredAccessories());
        assertEquals(ItemType.SPEED_RING, copy.getInSlot(3).getAttributeHandler().getPotentialType());
    }

    @Test
    void maxProfileCoversEveryAccessoryAndIsSafeToRerun() {
        List<ItemType> plan = MaxAccessories.plan();
        AccessoryFamilies families = AccessoryFamilies.get();
        List<String> failures = new ArrayList<>();
        for (String id : YAML_ACCESSORIES) {
            ItemType type = ItemType.get(id);
            AccessoryComponent definition = new SkyBlockItem(type).getComponent(AccessoryComponent.class);
            if (ConfigurableSkyBlockItem.getFromID(id).getUnavailable() != null) {
                if (plan.contains(type)) failures.add(id + ": unavailable accessory was given");
                continue;
            }
            if (!definition.getRealm().countsForPower()) {
                if (plan.contains(type)) failures.add(id + ": Rift/Bingo-only accessory was given");
                continue;
            }
            AccessoryFamily family = families.familyOf(id);
            boolean covered = family == null ? plan.contains(type)
                    : plan.stream().anyMatch(given -> family.members().contains(given.name()));
            if (!covered) failures.add(id + ": not covered by /maxprofile");
        }
        for (ItemType given : plan) {
            SkyBlockItem maxed = MaxAccessories.maxed(given);
            if (Recombobulation.canRecombobulate(new SkyBlockItem(given)) && !maxed.getAttributeHandler().isRecombobulated())
                failures.add(given + ": not recombobulated");
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
        assertTrue(plan.size() <= AccessoryBagSlots.BASE + 48 + 2 * AccessoryBagSlots.MAX_JACOBUS_UPGRADES,
                "every planned accessory must fit a maxed bag");

        DatapointAccessoryBag.PlayerAccessoryBag bag = new DatapointAccessoryBag.PlayerAccessoryBag();
        assertEquals(0, MaxAccessories.fill(bag, AccessoryBagSlots.MAXIMUM));
        int placed = bag.getAccessoryMap().size();
        assertEquals(plan.size(), placed);
        assertEquals(0, MaxAccessories.fill(bag, AccessoryBagSlots.MAXIMUM));
        assertEquals(placed, bag.getAccessoryMap().size(), "a rerun adds nothing");
        for (int slot = 1; slot < placed; slot++) {
            Rarity previous = bag.getInSlot(slot - 1).getAttributeHandler().getEffectiveRarity();
            Rarity current = bag.getInSlot(slot).getAttributeHandler().getEffectiveRarity();
            assertTrue(previous.ordinal() >= current.ordinal(),
                    "slot " + slot + " (" + current + ") sorts after " + previous);
        }
        assertEquals(281, AccessoryBagSlots.MAXIMUM);
    }
}
