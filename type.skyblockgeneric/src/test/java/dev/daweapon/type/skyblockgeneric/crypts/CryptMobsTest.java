package dev.daweapon.type.skyblockgeneric.crypts;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.data.regions.RegionCatalog;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobCryptGhoul;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobGoldenGhoul;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import dev.daweapon.type.skyblockgeneric.loottable.ForcedDrops;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRegistry;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptMobsTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        SlayerRegistry.loadAll(new File("../configuration/skyblock/slayers.yml"));
    }

    private static RegionType hubZoneAt(Pos position) {
        return RegionCatalog.getAllRegions().stream()
                .filter(region -> region.getServerType() == ServerType.SKYBLOCK_HUB)
                .filter(region -> region.insideRegion(position))
                .min(Comparator.comparingLong(SkyBlockRegion::getVolume))
                .map(SkyBlockRegion::getType)
                .orElse(null);
    }

    @Test
    void cryptGhoulMatchesTheWikiInfobox() {
        CrimsonMobProfile ghoul = CryptMobs.CRYPT_GHOUL;
        assertEquals(33, ghoul.level());
        assertEquals(2_000, ghoul.health());
        assertEquals(350, ghoul.damage());
        assertEquals(49, ghoul.combatXp());
        assertEquals(11, ghoul.coins());
        assertEquals(36, ghoul.orbs());
        assertEquals(2, ghoul.bracket());
        assertEquals(20, ghoul.maxTier());
        assertEquals(List.of(MobType.UNDEAD), ghoul.mobTypes());
    }

    @Test
    void goldenGhoulMatchesTheWikiInfobox() {
        CrimsonMobProfile ghoul = CryptMobs.GOLDEN_GHOUL;
        assertEquals(65, ghoul.level());
        assertEquals(20_000, ghoul.health());
        assertEquals(500, ghoul.damage());
        assertEquals(97, ghoul.combatXp());
        assertEquals(21, ghoul.coins());
        assertEquals(71, ghoul.orbs());
        assertEquals(4, ghoul.bracket());
        assertEquals(15, ghoul.maxTier());
    }

    @Test
    void cryptGhoulDropsRottenFleshAndTheGhoulPet() {
        List<MobDrop> drops = CryptMobs.CRYPT_GHOUL.drops();
        assertEquals(4, drops.size());
        assertEquals(100, drops.get(0).chancePercent());
        assertEquals(50, drops.get(1).chancePercent());
        assertEquals(List.of("GHOUL_PET", "GHOUL_PET"), List.of(drops.get(2).choices().getFirst(), drops.get(3).choices().getFirst()));
        assertEquals(Rarity.EPIC, drops.get(2).rarity());
        assertEquals(0.003, drops.get(2).chancePercent());
        assertEquals(Rarity.LEGENDARY, drops.get(3).rarity());
        assertEquals(0.001, drops.get(3).chancePercent());
    }

    @Test
    void goldenGhoulDropsGoldAndGoldenPowder() {
        List<MobDrop> drops = CryptMobs.GOLDEN_GHOUL.drops();
        assertEquals(2, drops.get(0).min());
        assertEquals("GOLD_INGOT", drops.get(1).choices().getFirst());
        assertEquals(9, drops.get(2).min());
        assertEquals(35, drops.get(2).chancePercent());
        assertEquals("GOLDEN_POWDER", drops.get(3).choices().getFirst());
        assertEquals(0.05, drops.get(3).chancePercent());
    }

    @Test
    void everyDropIsARealItem() {
        for (CrimsonMobProfile profile : CryptMobs.all()) {
            for (MobDrop drop : profile.drops()) assertTrue(drop.isAvailable(), profile.id() + " " + drop.choices());
        }
    }

    @Test
    void goldenPowderGoesThroughLootRollSoForceDropWorks() {
        UUID player = UUID.randomUUID();
        List<ItemType> powder = CryptMobs.GOLDEN_GHOUL.drops().get(3).availableItems();
        assertNull(CrimsonMobLoot.decide(player, powder, 0, new Random(1)));
        ForcedDrops.force(player, ItemType.get("GOLDEN_POWDER"));
        assertEquals(ItemType.get("GOLDEN_POWDER"), CrimsonMobLoot.decide(player, powder, 0, new Random(1)));
    }

    @Test
    void ghoulsWearTheirHypixelGear() {
        MobCryptGhoul crypt = new MobCryptGhoul();
        assertEquals(Material.IRON_SWORD, crypt.getItemInMainHand().material());
        assertEquals(Material.CHAINMAIL_CHESTPLATE, crypt.getChestplate().material());
        assertEquals(Material.CHAINMAIL_BOOTS, crypt.getBoots().material());
        assertTrue(crypt.getHelmet().isAir());
        MobGoldenGhoul golden = new MobGoldenGhoul();
        assertEquals(Material.GOLDEN_SWORD, golden.getItemInMainHand().material());
        assertEquals(Material.GOLDEN_LEGGINGS, golden.getLeggings().material());
    }

    @Test
    void bestiaryEntriesUseTheRealMobs() {
        List<BestiaryMob> crypt = BestiaryCategory.HUB.CRYPT_GHOUL.getMobs();
        List<BestiaryMob> golden = BestiaryCategory.HUB.GOLDEN_GHOUL.getMobs();
        assertTrue(crypt.getFirst() instanceof MobCryptGhoul);
        assertTrue(golden.getFirst() instanceof MobGoldenGhoul);
        assertEquals("CRYPT_GHOUL", crypt.getFirst().getMobID());
        assertEquals("GOLDEN_GHOUL", golden.getFirst().getMobID());
        assertTrue(new MobCryptGhoul().countsForBestiary());
        assertTrue(new MobGoldenGhoul().countsForBestiary());
    }

    @Test
    void bothGhoulsCountForRevenantHorror() {
        assertTrue(SlayerRegistry.get(SlayerType.REVENANT_HORROR).accepts(new MobCryptGhoul()));
        assertTrue(SlayerRegistry.get(SlayerType.REVENANT_HORROR).accepts(new MobGoldenGhoul()));
    }

    @Test
    void cryptGhoulSpawnTableMatchesTheWiki() {
        assertEquals(15, CryptsSpawner.CRYPT_GHOUL_SPOTS.size());
        assertEquals(2, CryptsSpawner.cryptGhoulCap(false));
        assertEquals(4, CryptsSpawner.cryptGhoulCap(true));
        assertEquals(14_000, CryptsSpawner.CRYPT_GHOUL_DELAY_MILLIS);
        assertEquals(2, CryptsSpawner.GOLDEN_GHOUL_SPOTS.size());
    }

    @Test
    void everySpotIsInsideTheCryptsZone() {
        List<Pos> spots = new ArrayList<>(CryptsSpawner.CRYPT_GHOUL_SPOTS);
        spots.addAll(CryptsSpawner.GOLDEN_GHOUL_SPOTS);
        for (Pos spot : spots) assertEquals(RegionType.CRYPTS, hubZoneAt(spot), spot.toString());
    }

    @Test
    void goldenGhoulsSpawnInTheTwoLargeRooms() {
        Pos upper = CryptsSpawner.GOLDEN_GHOUL_SPOTS.get(0);
        Pos lower = CryptsSpawner.GOLDEN_GHOUL_SPOTS.get(1);
        assertTrue(upper.x() > -141 && upper.x() < -117 && upper.z() > -151 && upper.z() < -126 && upper.y() == 42, upper.toString());
        assertTrue(lower.x() > -113 && lower.x() < -91 && lower.z() > -98 && lower.z() < -73 && lower.y() == 39, lower.toString());
    }

    @Test
    void wikiAndVideoCryptPositionsResolveToTheCrypts() {
        assertEquals(RegionType.CRYPTS, hubZoneAt(new Pos(-104.3, 39, -84.7)));
        assertEquals(RegionType.CRYPTS, hubZoneAt(new Pos(-93.9, 47, -104.3)));
        assertEquals(RegionType.CRYPTS, hubZoneAt(new Pos(-99.1, 49, -120.3)));
        assertEquals(RegionType.CRYPTS, hubZoneAt(new Pos(-146, 57, -100)));
    }

    @Test
    void aSpotGainsOneMobPerDelayUpToItsCap() {
        CryptsSpawner.SpawnClock clock = new CryptsSpawner.SpawnClock(14_000);
        assertTrue(clock.due(0, 2, 0));
        assertFalse(clock.due(1, 2, 13_999));
        assertTrue(clock.due(1, 2, 14_000));
        assertFalse(clock.due(2, 2, 60_000));
        assertTrue(clock.due(1, 2, 60_001));
    }
}
