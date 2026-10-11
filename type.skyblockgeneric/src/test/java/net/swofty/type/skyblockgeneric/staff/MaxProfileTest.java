package net.swofty.type.skyblockgeneric.staff;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.proxyapi.PlayerTransferDataCache;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.domain.AccountDomain;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowerType;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowers;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.v2.SlotBehavior;
import net.swofty.type.generic.gui.v2.ViewComponent;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.faction.ReputationTier;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUISackOfSacks;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraCollectionTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDomain;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointQuiver;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSackOfSacks;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.bson.Document;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxProfileTest {
    private static final int FULL_QUIVER = 36 * 64;
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        Path configuration = Path.of("configuration");
        if (!Files.exists(configuration)) Files.createSymbolicLink(configuration, Path.of("../configuration"));
        Assumptions.assumeTrue(Files.exists(configuration.resolve("skyblock/shards.yml")),
                "the server configuration must be reachable at ./configuration");
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
            }
        }
        SkyBlockLevelCause.initializeCauses();
        CustomCollectionAward.cacheFromCollections();
    }

    static SkyBlockPlayer player() {
        UUID uuid = UUID.randomUUID();
        PlayerDataService.store(AccountDomain.KEY, uuid, HypixelDataHandler.initUserWithDefaultData(uuid));
        PlayerDataService.store(SkyBlockDomain.KEY, uuid, SkyBlockDataHandler.initUserWithDefaultData(uuid, UUID.randomUUID()));
        PlayerConnection connection = new PlayerConnection() {
            @Override
            public void sendPacket(SendablePacket packet) {
            }

            @Override
            public SocketAddress getRemoteAddress() {
                return new InetSocketAddress(0);
            }
        };
        return new SkyBlockPlayer(new GameProfile(uuid, "MaxTester"), connection);
    }

    @Test
    void maxProfileFillsPetsBagAndPowersAndTheySurviveAServerSwitch() {
        SkyBlockPlayer player = player();
        SkyBlockItem summoned = new SkyBlockItem(ItemType.LION_PET);
        summoned.getAttributeHandler().setRarity(Rarity.COMMON);
        player.getPetData().addPet(summoned);
        player.getPetData().setEnabled(summoned, true);

        long start = System.nanoTime();
        List<String> failed = MaxStickService.runSteps(player);
        long millis = (System.nanoTime() - start) / 1_000_000;
        System.out.println("/maxprofile took " + millis + " ms; failed steps: " + failed);
        assertTrue(failed.stream().noneMatch(List.of("Pets", "Accessory Bag", "Accessory powers", "Kuudra clears", "Sack of Sacks", "Quiver")::contains), failed.toString());
        assertTrue(KuudraProgress.collectionPoints(player) >= KuudraCollectionTier.V.getRequiredProgress());
        assertEquals(SkyBlockXPSource.DEFEAT_KUUDRA.getMaxXP(), player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.DEFEAT_KUUDRA));
        for (KuudraTier tier : KuudraTier.values()) {
            assertEquals(KuudraConfig.get().maxWave(), KuudraProgress.highestWave(player, tier), tier + " highest wave");
        }
        BestiaryData bestiaryData = new BestiaryData();
        for (BestiaryCategory.KUUDRA family : BestiaryCategory.KUUDRA.values()) {
            BestiaryMob mob = family.getMobs().getFirst();
            assertEquals(bestiaryData.getTotalKillsForMaxTier(mob), player.getBestiaryData().getAmount(mob), family + " bestiary");
        }

        int pets = MaxPets.petTypes().size();
        int accessories = player.getAccessoryBag().getAllAccessories().size();
        assertEquals(pets, player.getPetData().getPetsMap().size());
        assertTrue(accessories > 100, "only " + accessories + " accessories placed");
        assertEquals(new HashSet<>(AccessoryPowerType.stonePowers()),
                new HashSet<>(AccessoryPowers.data(player).getLearned()));
        int sacks = MaxBags.sackPlan().size();
        assertEquals(sacks, player.getSackOfSacks().getAllSacks().size());
        assertEquals(FULL_QUIVER, player.getQuiver().getAmountOfArrows(ItemType.FLINT_ARROW));

        assertEquals(failed, MaxStickService.runSteps(player));
        assertEquals(pets, player.getPetData().getPetsMap().size());
        assertEquals(accessories, player.getAccessoryBag().getAllAccessories().size());
        assertEquals(sacks, player.getSackOfSacks().getAllSacks().size());
        assertEquals(FULL_QUIVER, player.getQuiver().getAmountOfArrows(ItemType.FLINT_ARROW));

        SkyBlockDataHandler handler = player.getSkyblockDataHandler();
        String json = handler.toProfileDocument().toJson();
        System.out.println("profile document: " + json.length() / 1024 + " KB");
        handler.getDatapoints().values().stream()
                .sorted((a, b) -> Integer.compare(serializedLength(b), serializedLength(a)))
                .limit(8)
                .forEach(dp -> System.out.println("  " + dp.getKey() + ": " + serializedLength(dp) / 1024 + " KB"));

        String transferred = new String(Base64.getDecoder().decode(PlayerTransferDataCache.encodeDocument(json)), StandardCharsets.UTF_8);
        SkyBlockDataHandler loaded = SkyBlockDataHandler.createFromProfile(player.getUuid(), handler.getCurrentProfileId(),
                Document.parse(transferred));
        assertEquals(pets, loaded.get(SkyBlockDataHandler.Data.PET_DATA, DatapointPetData.class).getValue().getPetsMap().size());
        assertEquals(accessories, loaded.get(SkyBlockDataHandler.Data.ACCESSORY_BAG, DatapointAccessoryBag.class)
                .getValue().getAllAccessories().size());
        assertEquals(AccessoryPowerType.stonePowers().size(), loaded.get(SkyBlockDataHandler.Data.ACCESSORY_POWERS,
                DatapointAccessoryPowers.class).getValue().getLearned().size());
        assertEquals(sacks, loaded.get(SkyBlockDataHandler.Data.SACK_OF_SACKS, DatapointSackOfSacks.class)
                .getValue().getAllSacks().size());
        assertEquals(FULL_QUIVER, loaded.get(SkyBlockDataHandler.Data.QUIVER, DatapointQuiver.class)
                .getValue().getAmountOfArrows(ItemType.FLINT_ARROW));

        ViewLayout<GUISackOfSacks.SackOfSacksState> layout = new ViewLayout<>(InventoryType.CHEST_5_ROW);
        GUISackOfSacks sackOfSacks = new GUISackOfSacks();
        ViewContext context = new ViewContext(player, null, null);
        sackOfSacks.layout(layout, sackOfSacks.initialState(), context);
        for (int slot = 0; slot < sacks; slot++) {
            ViewComponent<GUISackOfSacks.SackOfSacksState> component = layout.components().get(slot);
            assertEquals(SlotBehavior.EDITABLE, component.behavior());
            assertTrue(component.render().apply(sackOfSacks.initialState(), context).build().material() != Material.AIR,
                    "Sack of Sacks slot " + slot + " renders empty");
        }
    }

    @Test
    void crimsonFactionProgressBelongsToTheProfile() {
        SkyBlockPlayer player = player();
        CrimsonReputationService.setFaction(player, CrimsonFaction.BARBARIAN);
        CrimsonReputationService.maxOut(player);
        CrimsonQuestlines.maxOut(player);
        assertEquals(CrimsonFaction.BARBARIAN, CrimsonReputationService.faction(player));

        SkyBlockDataHandler saved = SkyBlockDataHandler.createFromProfileOnly(player.getSkyblockDataHandler().toProfileDocument());
        DatapointCrimsonIsle.CrimsonIsleData reloaded = saved.get(SkyBlockDataHandler.Data.CRIMSON_ISLE, DatapointCrimsonIsle.class).getValue();
        assertEquals(CrimsonFaction.BARBARIAN, reloaded.getFaction());
        assertEquals(ReputationTier.HERO, ReputationTier.of(reloaded.getReputation(CrimsonFaction.MAGE)));

        SkyBlockDataHandler otherProfile = SkyBlockDataHandler.initUserWithDefaultData(player.getUuid(), UUID.randomUUID());
        DatapointCrimsonIsle.CrimsonIsleData fresh = otherProfile.get(SkyBlockDataHandler.Data.CRIMSON_ISLE, DatapointCrimsonIsle.class).getValue();
        assertNull(fresh.getFaction());
        assertEquals(0, fresh.getReputation(CrimsonFaction.BARBARIAN));
        assertEquals(0, fresh.getQuestlineStage(CrimsonQuestlines.BARBARIAN));
    }

    @Test
    void maxProfileMaxesTheHeartOfTheMountain() {
        SkyBlockPlayer player = player();
        net.swofty.type.skyblockgeneric.hotm.HotmService.maxOut(player);
        var data = net.swofty.type.skyblockgeneric.hotm.HotmService.data(player);
        var registry = net.swofty.type.skyblockgeneric.hotm.HotmRegistry.get();
        assertEquals(10, data.getTier());
        assertEquals(registry.maxCoreLevel(), data.getCoreLevel());
        registry.perks().values().forEach(perk -> assertEquals(perk.maxLevel(), data.perkLevel(perk.id()), perk.id()));
        assertTrue(data.getAvailableTokens() >= 0);
        for (var powder : net.swofty.type.skyblockgeneric.hotm.HotmPowder.values()) assertTrue(data.powder(powder) > 0, powder.name());
    }

    @Test
    void maxProfileUnlocksTheSecretRailroadPass() {
        SkyBlockPlayer player = player();
        assertFalse(net.swofty.type.skyblockgeneric.glacite.RailroadPass.isUnlocked(player));
        net.swofty.type.skyblockgeneric.hotm.HotmService.maxOut(player);
        assertTrue(net.swofty.type.skyblockgeneric.glacite.RailroadPass.isUnlocked(player));
        assertTrue(net.swofty.type.skyblockgeneric.glacite.RailroadPass.meetsRequirement(player));
    }

    @Test
    void glaciteWalkerMatchesCurrentHypixelStats() {
        var walker = new net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.MobGlaciteWalker();
        assertEquals(24, walker.getLevel());
        assertEquals(net.minestom.server.entity.EntityType.PLAYER, walker.getEntityType());
        assertNotNull(((net.swofty.type.skyblockgeneric.entity.mob.impl.MobPlayerSkin) walker).getSkinTexture());
        assertEquals(888D, walker.getBaseStatistics().getOverall(net.swofty.commons.skyblock.statistics.ItemStatistic.HEALTH));
    }

    @Test
    void dailyGlacitePowderIsClaimedOncePerDay() {
        SkyBlockPlayer player = player();
        net.swofty.type.skyblockgeneric.hotm.HotmService.maxOut(player);
        long before = net.swofty.type.skyblockgeneric.hotm.HotmService.data(player).getGlacitePowder();
        assertTrue(net.swofty.type.skyblockgeneric.hotm.HotmService.claimDailyGlacite(player));
        assertFalse(net.swofty.type.skyblockgeneric.hotm.HotmService.claimDailyGlacite(player));
        assertEquals(before + 500L * 10, net.swofty.type.skyblockgeneric.hotm.HotmService.data(player).getGlacitePowder());
    }

    @Test
    void everyHotmMenuRendersEverySlotBeforeAndAfterMaxing() {
        SkyBlockPlayer player = player();
        for (int pass = 0; pass < 2; pass++) {
            if (pass == 1) net.swofty.type.skyblockgeneric.hotm.HotmService.maxOut(player);
            ViewContext context = new ViewContext(player, null, null);
            var views = List.<net.swofty.type.generic.gui.v2.StatelessView>of(
                    new net.swofty.type.skyblockgeneric.gui.inventories.hotm.GUIHotm(1),
                    new net.swofty.type.skyblockgeneric.gui.inventories.hotm.GUIHotm(5),
                    new net.swofty.type.skyblockgeneric.gui.inventories.hotm.GUIHotm(6),
                    new net.swofty.type.skyblockgeneric.gui.inventories.hotm.GUIHotmReset());
            for (var perk : net.swofty.type.skyblockgeneric.hotm.HotmRegistry.get().perks().values()) {
                String tooltip = net.swofty.type.skyblockgeneric.gui.inventories.hotm.HotmTooltips.perk(player, perk);
                assertTrue(!tooltip.contains("{"), perk.id() + ": " + tooltip);
            }
            for (var view : views) {
                ViewLayout<net.swofty.type.generic.gui.v2.DefaultState> layout = new ViewLayout<>(view.configuration().getInventoryType());
                view.layout(layout, new net.swofty.type.generic.gui.v2.DefaultState(), context);
                for (var component : layout.components().values()) {
                    assertTrue(component.render().apply(new net.swofty.type.generic.gui.v2.DefaultState(), context).build() != null);
                }
            }
        }
    }

    @Test
    void forgeMenusRenderAndProcessesPersist() {
        SkyBlockPlayer player = player();
        net.swofty.type.skyblockgeneric.hotm.HotmService.maxOut(player);
        var registry = net.swofty.type.skyblockgeneric.forge.ForgeRegistry.get();
        assertEquals(124, registry.all().size());
        var refinedMithril = registry.recipe("REFINED_MITHRIL");
        assertTrue(refinedMithril.available());
        assertEquals(20D, net.swofty.type.skyblockgeneric.forge.ForgeService.reductionPercent(player), 0.0001);
        assertEquals(17_280_000L, net.swofty.type.skyblockgeneric.forge.ForgeService.durationMillis(player, refinedMithril));
        assertEquals(7, net.swofty.type.skyblockgeneric.forge.ForgeService.slots(player));
        assertTrue(net.swofty.type.skyblockgeneric.forge.ForgeService.start(player, refinedMithril, 1) != null);

        var data = net.swofty.type.skyblockgeneric.hotm.HotmService.data(player);
        data.getForgeProcesses().add(new net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM.ForgeProcess(1, "REFINED_MITHRIL", System.currentTimeMillis(), System.currentTimeMillis() + 60_000));
        net.swofty.type.skyblockgeneric.hotm.HotmService.save(player, data);
        var datapoint = net.swofty.type.skyblockgeneric.hotm.HotmService.datapoint(player);
        var copy = new net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM("hotm");
        copy.deserializeValue(datapoint.getSerializedValue());
        assertEquals(1, copy.getValue().getForgeProcesses().size());

        ViewContext context = new ViewContext(player, null, null);
        var views = new java.util.ArrayList<net.swofty.type.generic.gui.v2.StatelessView>();
        views.add(new net.swofty.type.skyblockgeneric.gui.inventories.forge.GUIForge());
        views.add(new net.swofty.type.skyblockgeneric.gui.inventories.forge.GUIForgeProcess(1));
        for (var category : net.swofty.type.skyblockgeneric.forge.ForgeCategory.values()) {
            views.add(new net.swofty.type.skyblockgeneric.gui.inventories.forge.GUIForgeRecipes(category, 0, 1));
            views.add(new net.swofty.type.skyblockgeneric.gui.inventories.forge.GUIForgeRecipes(category, 1, 1));
        }
        views.add(new net.swofty.type.skyblockgeneric.gui.inventories.forge.GUIForgeConfirm(refinedMithril, 1));
        for (int pass = 0; pass < 2; pass++) {
            for (var view : views) {
                ViewLayout<net.swofty.type.generic.gui.v2.DefaultState> layout = new ViewLayout<>(view.configuration().getInventoryType());
                view.layout(layout, new net.swofty.type.generic.gui.v2.DefaultState(), context);
                for (var component : layout.components().values()) {
                    assertTrue(component.render().apply(new net.swofty.type.generic.gui.v2.DefaultState(), context).build() != null);
                }
            }
            net.swofty.type.skyblockgeneric.forge.ForgeService.finishAll(player);
        }
    }

    @Test
    void bubusPickaxesExistAndPuzzlerCooldownPersists() {
        for (String id : List.of("FRACTURED_MITHRIL_PICKAXE", "BANDAGED_MITHRIL_PICKAXE", "MITHRIL_PICKAXE", "REFINED_MITHRIL_PICKAXE",
                "RUSTY_TITANIUM_PICKAXE", "TITANIUM_PICKAXE", "REFINED_TITANIUM_PICKAXE")) {
            assertTrue(ItemType.get(id) != null, id);
            assertTrue(new SkyBlockItem(ItemType.get(id)).getDisplayName().length() > 0, id);
        }
        var data = new net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM.PlayerHOTMData();
        data.setPuzzlerReadyAt(123_456L);
        var datapoint = new net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM("hotm", data);
        var copy = new net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM("hotm");
        copy.deserializeValue(datapoint.getSerializedValue());
        assertEquals(123_456L, copy.getValue().getPuzzlerReadyAt());
    }

    @Test
    void onlyBazaarCommoditiesCarryAShopStockLimit() {
        assertTrue(net.swofty.type.skyblockgeneric.shop.ShopStockLimits.applies(new SkyBlockItem(ItemType.ENCHANTED_COBBLESTONE)));
        assertTrue(!net.swofty.type.skyblockgeneric.shop.ShopStockLimits.applies(new SkyBlockItem(ItemType.FRACTURED_MITHRIL_PICKAXE)));
        assertTrue(!net.swofty.type.skyblockgeneric.shop.ShopStockLimits.applies(new SkyBlockItem(ItemType.TORCH)));
    }

    @Test
    void buyingAPerkSpendsTokensAndPowderAndResetRefundsThem() {
        SkyBlockPlayer player = player();
        var data = net.swofty.type.skyblockgeneric.hotm.HotmService.data(player);
        data.setMithrilPowder(1_000);
        assertEquals(null, net.swofty.type.skyblockgeneric.hotm.HotmService.buyPerk(player, "mining_speed"));
        assertEquals(1, data.perkLevel("mining_speed"));
        assertEquals(0, data.getAvailableTokens());
        assertEquals(null, net.swofty.type.skyblockgeneric.hotm.HotmService.buyPerk(player, "mining_speed"));
        assertEquals(1_000 - 27, data.getMithrilPowder());
        net.swofty.type.skyblockgeneric.hotm.HotmService.reset(player);
        assertEquals(1_000, data.getMithrilPowder());
        assertEquals(1, data.getAvailableTokens());
        assertEquals(0, data.perkLevel("mining_speed"));
    }

    @Test
    void sackPlanHoldsTheLargestSackOfEveryFamilyAndFillsTheSackOfSacks() {
        List<ItemType> plan = MaxBags.sackPlan();
        assertEquals(plan.size(), plan.stream().map(type -> MaxBags.family(type.name())).distinct().count());
        assertTrue(plan.containsAll(List.of(ItemType.LARGE_AGRONOMY_SACK, ItemType.LARGE_ENCHANTED_MINING_SACK,
                ItemType.SILVER_TROPHY_FISHING_SACK, ItemType.RUNE_SACK, ItemType.DUNGEON_SACK)), plan.toString());
        assertTrue(plan.stream().noneMatch(type -> type.name().startsWith("SMALL_") || type.name().startsWith("MEDIUM_")
                || type.name().startsWith("BRONZE_")), plan.toString());
        assertEquals(18, plan.size(), plan.toString());
    }

    @Test
    void petItemsAndSkinsCanBeGivenByIdAndName() {
        SkyBlockPlayer player = player();
        List<String> failures = new java.util.ArrayList<>();
        for (String input : List.of("PET_SKIN_ELEPHANT_PINK", "pet_skin_elephant_pink", "PET_ITEM_HARDENED_SCALES_UNCOMMON",
                "Hardened Scales", "PET_ITEM_BIG_TEETH_COMMON", "Big Teeth", "SUPER_SCRUBBER", "Super Scrubber")) {
            if (net.swofty.type.skyblockgeneric.item.ItemTypeResolver.resolve(input) == null) failures.add(input + ": unresolved");
        }
        for (ItemType type : ItemType.values()) {
            if (!type.name().startsWith("PET_SKIN_") && !type.name().startsWith("PET_ITEM_") && type != ItemType.SUPER_SCRUBBER) continue;
            try {
                SkyBlockItem item = new SkyBlockItem(type);
                net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater.playerUpdate(player, item.getItemStack(), true).build();
                new net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater(item).getUpdatedItem().build();
            } catch (RuntimeException exception) {
                failures.add(type + ": " + exception);
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    private static int serializedLength(net.swofty.type.generic.data.Datapoint<?> datapoint) {
        try {
            return datapoint.getSerializedValue().length();
        } catch (Exception e) {
            return -1;
        }
    }
}
