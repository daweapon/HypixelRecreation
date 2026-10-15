package dev.daweapon.type.skyblockgeneric;

import dev.daweapon.commons.reflection.PackageScanner;
import dev.daweapon.type.skyblockgeneric.hotm.HotmRegistry;
import dev.daweapon.type.skyblockgeneric.achievement.SkyBlockAchievements;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryEconomyHooks;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import dev.daweapon.commons.*;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.item.reforge.ReforgeLoader;
import dev.daweapon.commons.text.Text;
import dev.daweapon.proxyapi.PlayerTransferDataCache;
import dev.daweapon.proxyapi.ProxyPlayer;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.utility.TpsMeter;
import dev.daweapon.type.generic.HypixelTypeLoader;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.mongodb.AttributeDatabase;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.generic.entity.hologram.ServerHolograms;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.skyblockgeneric.user.BitsService;
import dev.daweapon.type.skyblockgeneric.gifts.BottleOfJyrre;
import dev.daweapon.type.skyblockgeneric.gifts.SnowmanPet;
import dev.daweapon.type.skyblockgeneric.potion.CookieBuffService;
import dev.daweapon.type.skyblockgeneric.potion.GodPotionService;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContests;
import dev.daweapon.type.skyblockgeneric.fishing.festival.FishingFestivalAnnouncer;
import dev.daweapon.type.skyblockgeneric.fishing.hotspot.FishingHotspotRenderer;
import dev.daweapon.type.skyblockgeneric.stonks.StonksAuctions;
import dev.daweapon.type.skyblockgeneric.shen.ShenAuctions;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.packet.HypixelPacketClientListener;
import dev.daweapon.type.generic.packet.HypixelPacketServerListener;
import dev.daweapon.type.generic.redis.RedisOriginServer;
import dev.daweapon.type.generic.resourcepack.ResourcePackManager;
import dev.daweapon.type.generic.user.categories.CustomGroups;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.text.SkyBlockTextScopes;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import dev.daweapon.type.skyblockgeneric.block.attribute.BlockAttribute;
import dev.daweapon.type.skyblockgeneric.block.placement.BlockPlacementManager;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.generic.data.PlayerWipeService;
import dev.daweapon.type.generic.data.domain.AccountDomain;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.skyblockgeneric.data.CoopSync;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDomain;
import dev.daweapon.type.skyblockgeneric.data.crystals.CrystalCatalog;
import dev.daweapon.type.skyblockgeneric.data.monogdb.*;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.entity.ServerCrystalImpl;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobRegistry;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.hunting.HuntrapService;
import dev.daweapon.type.skyblockgeneric.compactor.PersonalCompactorService;
import dev.daweapon.type.skyblockgeneric.deletor.PersonalDeletorService;
import dev.daweapon.type.skyblockgeneric.staff.MaxStickService;
import dev.daweapon.type.skyblockgeneric.carnival.Carnival;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.CraftableComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ServerOrbComponent;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredPassiveAbility;
import dev.daweapon.type.skyblockgeneric.item.set.impl.SetRepeatable;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.levels.CustomLevelAward;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import dev.daweapon.type.skyblockgeneric.levels.unlocks.CustomLevelUnlock;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.MissionRepeater;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.noteblock.SkyBlockSongsHandler;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockBiomeConfiguration;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegenConfiguration;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.resourcepack.SkyblockPack;
import dev.daweapon.type.skyblockgeneric.server.attribute.SkyBlockServerAttributes;
import dev.daweapon.type.skyblockgeneric.server.eventcaller.CustomEventCaller;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockScoreboard;
import dev.daweapon.type.skyblockgeneric.user.StashReminder;
import dev.daweapon.type.skyblockgeneric.user.fairysouls.FairySoul;
import dev.daweapon.type.skyblockgeneric.user.island.CoopContributionTracker;
import dev.daweapon.type.skyblockgeneric.entity.mob.island.PrivateIslandMobSpawner;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.statistics.PlayerStatistics;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import dev.daweapon.type.skyblockgeneric.utility.LaunchPads;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

// Top-level bootstrap for the SkyBlock server type: wires up databases, registries, schedulers,
// event/command/entity scanning, and the tablist/scoreboard loops when the server starts.
public record SkyBlockGenericLoader(HypixelTypeLoader typeLoader) {
    @Getter
    private static MinecraftServer server;

    @SneakyThrows
    public void initialize(MinecraftServer server) {
        SkyBlockGenericLoader.server = server;
        SkyBlockTextScopes.init();
        CustomWorlds mainInstance = typeLoader.getMainInstance();

        /**
         * Setup launchpads
         */
        if (mainInstance != null) {
            LaunchPads.register(MinecraftServer.getSchedulerManager());
        }

        /**
         * Register SkyBlock databases
         */
        ConnectionString cs = new ConnectionString(ConfigProvider.settings().getMongodb());
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
        MongoClient mongoClient = MongoClients.create(settings);

        IslandDatabase.connect(mongoClient);
        CoopDatabase.connect(mongoClient);

        PlayerWipeService.onIslandDropped(SkyBlockIsland::discard);

        /**
         * Register items
         */
        ItemAttribute.registerItemAttributes();
        HuntrapService.initialize();
        PersonalDeletorService.initialize();
        PersonalCompactorService.initialize();
        MaxStickService.initialize();
        SkyBlockAchievements.initialize();
        Carnival.initialize();
        PlayerItemUpdater.updateLoop(MinecraftServer.getSchedulerManager());
        File configDir = new File("./configuration/skyblock");
        File itemsDir = new File(configDir, "items");
        try {
            List<File> yamlFiles = YamlFileUtils.getYamlFiles(itemsDir);
            Logger.info("Found " + yamlFiles.size() + " YAML files to load");
            for (File file : yamlFiles) {
                try {
                    Map<String, Object> data = YamlFileUtils.loadYaml(file);
                    List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

                    if (items != null) {
                        for (Map<String, Object> itemConfig : items) {
                            try {
                                ItemConfigParser.parseItem(itemConfig);
                            } catch (Exception e) {
                                Logger.error(e, "Failed to parse item configuration: {}", itemConfig.get("id"));
                            }
                        }
                    } else {
                        Logger.warn("No items found in " + file.getName());
                    }
                } catch (IOException e) {
                    Logger.error("Failed to load " + file.getName(), e);
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to scan for YAML files", e);
        }
        FishingRegistry.loadAll();
        SlayerRegistry.loadAll();
        HotmRegistry.get();

        /**
         * Register commands
         */
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.commands", HypixelCommand.class).forEach(command -> {
            try {
                HypixelCommand.register(command);
            } catch (Exception e) {
                Logger.error(e, "Failed to register command {} in class {}",
                    command.getCommand().getName(), command.getClass().getSimpleName());
            }
        });

        dev.daweapon.type.skyblockgeneric.command.SkyBlockCommandGroups.register();

        loopThroughPackage("dev.daweapon.type.skyblockgeneric.abiphone.impl", AbiphoneNPC.class)
            .forEach(AbiphoneRegistry::registerContact);

        // Register entities
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.entity.mob.mobs", SkyBlockMob.class)
            .forEach(mob -> MobRegistry.registerExtraMob(mob.getClass()));

        ScheduleUtility.delay(() -> SkyBlockMob.runRegionPopulators(MinecraftServer.getSchedulerManager()), 50);

        /**
         * Start generic SkyBlock tablist
         */
        MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event ->
            HypixelGenericLoader.LAST_TICK.set(event.getTickMonitor()));
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<SkyBlockPlayer> players = getLoadedPlayers();
            if (players.isEmpty())
                return;

            long ramUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            ramUsage /= (long) 1e6; // bytes to MB
            double TPS = TpsMeter.tps();

            if (TPS < TpsMeter.WARN_BELOW_TPS) {
                HypixelGenericLoader.forEachLoadedPlayer(player -> {
                    player.getLogHandler().debug(Text.of("<c>Server TPS is below 20! TPS: {}", TPS));
                });
                Logger.error("Server TPS is below 20! TPS: " + TPS);
            }

            final Text header = Text.of("""
                <b>You are playing on <e><l>MC.HYPIXEL.NET</l>
                <7>RAM USAGE: <8>{0} MB
                <7>TPS: <8>{1}
                """, ramUsage, TPS);

            // Send per-player footer with their active effects
            for (SkyBlockPlayer player : players) {
                Text footer = Text.of("\n<a><l>Active Effects\n");

                List<TemporaryStatistic> activeEffects = new ArrayList<>(player.getStatistics().getDisplayableActiveEffects());
                activeEffects.removeIf(CookieBuffService::isCookieBuff);
                long godPotionMs = activeEffects.stream().filter(GodPotionService::isGodPotionEffect)
                        .mapToLong(TemporaryStatistic::getRemainingMs).max().orElse(0);
                if (godPotionMs > 0) activeEffects.removeIf(GodPotionService::isGodPotionEffect);

                if (activeEffects.isEmpty() && godPotionMs <= 0) {
                    footer = footer.append(
                        "<7>No effects active. Drink potions or splash them on the\n<7>ground to buff yourself!");
                } else {
                    if (godPotionMs > 0) {
                        footer = footer.append("<c>God Potion <f>{}\n", formatEffectDuration(godPotionMs));
                    }
                    for (TemporaryStatistic effect : activeEffects) {
                        String color = effect.getDisplayColor() != null ? effect.getDisplayColor() : "<7>";
                        String name = effect.getDisplayName();
                        String duration = formatEffectDuration(effect.getRemainingMs());
                        footer = footer.append(color + "{} <f>{}\n", name, duration);
                    }
                }

                footer = footer.append("\n<d><l>Cookie Buff</l>\n");
                footer = player.isBoosterCookieActive()
                        ? footer.append("<7>{}\n", CookieBuffService.formatRemaining(player))
                        : footer.append("<7>Not active! Obtain booster cookies from the community\n<7>shop in the hub.\n");

                footer = footer.append("""

                    <a>Ranks, Boosters & MORE! <c><l>STORE.HYPIXEL.NET""");

                player.sendPlayerListHeaderAndFooter(header, footer);
            }
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();


        /**
         * Handle server attributes
         */
        SkyBlockCalendar.tick(MinecraftServer.getSchedulerManager());
        SkyBlockServerAttributes.loadAttributes(AttributeDatabase.getDocument("attributes"));
        SkyBlockServerAttributes.saveAttributeLoop();
        /**
         * Register Placement Rules
         */
        BlockPlacementManager.registerAll();

        /**
         * Start data loop
         */
        SkyBlockDataHandler.startRepeatSetValueLoop();
        BitsService.startFarming();
        AccessoryEconomyHooks.start();
        BottleOfJyrre.startAging();
        dev.daweapon.type.skyblockgeneric.garden.greenhouse.RosewaterFlask.startAging();
        SnowmanPet.startBlizzard();
        CoopSync.subscribe();
        PlayerDataService.register(new SkyBlockDomain());
        ProfilesDatabase.setHostedProfileCheck(SkyBlockDomain::isProfileHosted);

        ProxyPlayer.setTransferPreparation((playerUuid, targetServer) -> CompletableFuture.supplyAsync(() -> {
            SkyBlockPlayer player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerUuid) instanceof SkyBlockPlayer skyBlockPlayer
                    ? skyBlockPlayer : null;
            if (player == null) return null;

            PlayerDataService.flushForTransfer(typeLoader.getType(), player);
            return new org.json.JSONObject()
                    .put("account_document", PlayerTransferDataCache.encodeDocument(AccountDomain.transferDocument(player)))
                    .put("profile_document", PlayerTransferDataCache.encodeDocument(SkyBlockDomain.transferDocument(player)))
                    .toString();
        }));

        // Setup Resource Pack
        SkyblockPack skyblockPack = SkyblockPack.fromConfig();
        ResourcePackManager packManager = new ResourcePackManager(skyblockPack);
        HypixelConst.setResourcePackManager(packManager);
        packManager.initialize();

        /**
         * Attempt to start the song service
         */
        boolean hasAllSongs = Arrays.stream(Songs.values()).allMatch(song -> song.getPath().toFile().exists());
        if (hasAllSongs) {
            Logger.info("All songs have been found, starting song service for this instance.");
            SkyBlockSongsHandler.isEnabled = true;
        } else {
            Logger.error("Not all songs have been found, song service will not start for this instance.");
            SkyBlockSongsHandler.isEnabled = false;
        }

        /**
         * Register packet event
         */
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.packets.client", HypixelPacketClientListener.class)
            .forEach(HypixelPacketClientListener::cacheListener);
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.packets.server", HypixelPacketServerListener.class)
            .forEach(HypixelPacketServerListener::cacheListener);
        HypixelPacketClientListener.register(HypixelConst.getEventHandler());
        HypixelPacketServerListener.register(HypixelConst.getEventHandler());

        // Load regions
        SkyBlockRegion.cacheRegions();
        SkyBlockRegenConfiguration.startRepeater(MinecraftServer.getSchedulerManager());
        MinecraftServer.getDimensionTypeRegistry().register(
            Key.key("skyblock:island"),
            DimensionType.builder()
                .ambientLight(1)
                .build());
        SkyBlockIsland.runVacantLoop(MinecraftServer.getSchedulerManager());
        if (HypixelConst.isGardenServer()) {
            SkyBlockGarden.runVacantLoop(MinecraftServer.getSchedulerManager());
        }
        PrivateIslandMobSpawner.start(MinecraftServer.getSchedulerManager());

        SkyBlockRegion.getRegions().forEach(region -> {
            if (region.getServerType() != HypixelConst.getTypeLoader().getType()) return;
            SkyBlockBiomeConfiguration biomeConfig = region.getType().getBiomeHandler();
            if (biomeConfig == null) return;
            RegistryKey<Biome> biomeKey = MinecraftServer.getBiomeRegistry().register(biomeConfig.getKey(), biomeConfig.getBiome());
            setBiome(region.getFirstLocation(), region.getSecondLocation(), biomeKey);
        });

        /**
         * Load fairy souls
         */
        FairySoul.cacheFairySouls();

        /**
         * Initialize reforges
         */
        ReforgeLoader.loadAllReforges();

        /**
         * Create audiences
         */
        CustomGroups.registerAudiences();
        PlayerStatistics.run();

        // Start repeaters
        SkyBlockScoreboard.start();
        FarmingContests.start();
        FishingFestivalAnnouncer.start(MinecraftServer.getSchedulerManager());
        FishingHotspotRenderer.start(MinecraftServer.getSchedulerManager());
        StonksAuctions.start();
        ShenAuctions.start();
        StashReminder.start(MinecraftServer.getSchedulerManager());
        PlayerHolograms.updateAll(MinecraftServer.getSchedulerManager());
        CoopContributionTracker.startFlushLoop(MinecraftServer.getSchedulerManager());

        /**
         * Register Bazaar propagator
         */
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Thread.startVirtualThread(() -> {
                forEachLoadedPlayer(player -> {
                    player.getBazaarConnector().processAllPendingTransactions();
                });
            });
            return TaskSchedule.seconds(15);
        });

        /**
         * Register holograms and fairy souls
         */
        if (mainInstance != null) {
            ServerHolograms.spawnAll(HypixelConst.getInstanceContainer());
            FairySoul.spawnEntities(HypixelConst.getInstanceContainer(), typeLoader.getType());
        }

        /**
         * Spawn server crystals
         */
        if (HypixelConst.getInstanceContainer() != null) {
            Thread.startVirtualThread(() -> {
                CrystalCatalog.getAllCrystals().forEach(crystal -> {
                    if (crystal.serverType != HypixelConst.getTypeLoader().getType()) return;

                    ItemType type = crystal.itemType;
                    SkyBlockItem item = new SkyBlockItem(type);
                    ServerOrbComponent asCrystal = item.getComponent(ServerOrbComponent.class);
                    ServerCrystalImpl crystalImpl = new ServerCrystalImpl(
                        asCrystal.getSpawnMaterialFunction(),
                        crystal.url,
                        asCrystal.getValidBlocks()
                    );

                    crystalImpl.setInstance(HypixelConst.getInstanceContainer(),
                        new Pos(crystal.position.x(), crystal.position.y(), crystal.position.z()));
                });
            });
        }

        /**
         * Register blocks
         */
        BlockAttribute.registerBlockAttributes();

        /**
         * Register event
         */
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.enchantment.impl", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.event.custom", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.event.actions", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.item.events", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.mission.missions", HypixelEventClass.class).forEach(HypixelEventHandler::registerEventMethods);

        // Register missions
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.mission.missions", SkyBlockMission.class)
            .forEach((event) -> {
                MissionData.registerMission(event.getClass());
            });
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.mission.missions", MissionRepeater.class)
            .forEach((event) -> {
                event.getTask(MinecraftServer.getSchedulerManager());
            });
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.item.set.sets", SetRepeatable.class)
            .forEach((event) -> {
                event.getTask(MinecraftServer.getSchedulerManager());
            });
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.enchantment.impl", SkyBlockValueEvent.class)
            .forEach(SkyBlockValueEvent::cacheEvent);
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.item.set.sets", SkyBlockValueEvent.class)
            .forEach(SkyBlockValueEvent::cacheEvent);
        loopThroughPackage("dev.daweapon.type.skyblockgeneric.item.events", SkyBlockValueEvent.class)
            .forEach(SkyBlockValueEvent::cacheEvent);
        SkyBlockValueEvent.register(); // Value events are SkyBlock-specific
        CustomEventCaller.start(); // Value events are SkyBlock-specific
        HypixelEventHandler.register(HypixelConst.getEventHandler());

        AbilityRegistry.getRegisteredAbilities().forEach(((_, registeredAbility) -> {
            if (registeredAbility instanceof RegisteredPassiveAbility passiveAbility) {
                passiveAbility.getPassiveAction().forEach(RegisteredPassiveAbility.Action::register);
            }
        }));

        /**
         * Cache SkyBlock levels
         */
        SkyBlockLevelRequirement.loadFromYaml();

        /**
         * Load item recipes
         */
        Arrays.stream(ItemType.values()).forEach(type -> {
            SkyBlockItem item = new SkyBlockItem(type);
            if (item.hasComponent(CraftableComponent.class)) {
                CraftableComponent craftableComponent = item.getComponent(CraftableComponent.class);

                try {
                    List<SkyBlockRecipe<?>> recipes = craftableComponent.getRecipes();
                    if (recipes != null && !recipes.isEmpty()) {
                        recipes.forEach(SkyBlockRecipe::init);
                        Logger.debug("Initialized " + recipes.size() + " recipe(s) for item: " + type.name());
                    }
                } catch (Exception e) {
                    Logger.error(e, "Failed to initialize recipe for item type: {}", type.name());
                }
            }
        });
        CollectionCategories.getCategories().forEach(category -> {
            Arrays.stream(category.getCollections()).forEach(collection -> {
                List<SkyBlockRecipe<?>> recipes = new ArrayList<>();
                Arrays.stream(collection.rewards()).forEach(reward -> {
                    Arrays.stream(reward.unlocks()).forEach(unlock -> {
                        if (unlock instanceof CollectionCategory.UnlockRecipe recipe) {
                            try {
                                List<SkyBlockRecipe<?>> recipeInstances = recipe.getRecipes();

                                recipeInstances.forEach(recipeInstance -> {
                                    var previousCheck = recipeInstance.getCanCraft();
                                    recipeInstance.setCanCraft((player) -> {
                                        if (previousCheck != null) {
                                            SkyBlockRecipe.CraftingResult previous = previousCheck.apply(player);
                                            if (!previous.allowed()) return previous;
                                        }
                                        int amount = player.getCollection().get(collection.type());
                                        return new SkyBlockRecipe.CraftingResult(
                                            amount >= reward.requirement(),
                                            new String[]{"<7>You must have <c>" + collection.type().getDisplayName()
                                                + " Collection "
                                                + StringUtility.getAsRomanNumeral(collection.getPlacementOf(reward))}
                                        );
                                    });
                                    recipes.add(recipeInstance);
                                });
                            } catch (Exception e) {
                                Logger.error(e, "Failed to parse collection recipe for {} with requirement {}",
                                    collection.type(), reward.requirement());
                            }
                        }
                    });
                });
                recipes.forEach(SkyBlockRecipe::init);
            });
        });
        dev.daweapon.type.skyblockgeneric.slayer.SlayerRecipeGates.install();
        dev.daweapon.type.skyblockgeneric.experiments.ExperimentRecipeGate.install();

        /**
         * Cache custom collections
         */
        Thread.startVirtualThread(() -> {
            CustomCollectionAward.cacheFromCollections();

            // Level Unlocks
            Arrays.stream(SkyBlockLevelRequirement.values()).forEach(requirement -> {
                requirement.getUnlocks().forEach(unlock -> {
                    if (unlock instanceof CustomLevelUnlock award) {
                        CustomLevelAward.addToCache(requirement.asInt(), award.getAward());
                    }
                });
            });
        });

        /**
         * Register SkyBlock levels
         */
        SkyBlockLevelCause.initializeCauses();

        /**
         * Handle ConnectionManager
         */
        MinecraftServer.getConnectionManager().setPlayerProvider((gameProfile, playerConnection) -> {
            SkyBlockPlayer player = new SkyBlockPlayer(playerConnection, gameProfile);

            UUID uuid = gameProfile.getPlayer().getUuid();
            String username = gameProfile.getPlayer().getUsername();

            ServerType originServer = RedisOriginServer.consume(uuid);

            if (originServer != null) {

                player.setOriginServer(originServer);
            }

            Logger.info("Received new player: " + username + " (" + uuid + ")");

            return player;
        });
    }

    public void afterInitialize() {
        CompletableFuture.runAsync(() -> {
            ElectionManager.loadFromService();
            ElectionManager.checkElectionCycle();
        }).thenRun(() -> MinecraftServer.getSchedulerManager().buildTask(ElectionManager::checkElectionCycle).repeat(10, TimeUnit.SECOND).schedule());
    }

    public static void forEachLoadedPlayer(java.util.function.Consumer<SkyBlockPlayer> action) {
        for (SkyBlockPlayer player : getLoadedPlayers()) {
            try {
                action.accept(player);
            } catch (RuntimeException exception) {
                Logger.warn(exception, "Periodic task failed for {}", player.getUuid());
            }
        }
    }

    public static List<SkyBlockPlayer> getLoadedPlayers() {
        List<SkyBlockPlayer> players = new ArrayList<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers()
            .stream()
            .filter(player -> {
                try {
                    SkyBlockDataHandler.getUser(player.getUuid());
                } catch (Exception e) {
                    return false;
                }
                return true;
            })
            .filter(player -> player.getInstance() != null)
            .forEach(player -> players.add((SkyBlockPlayer) player));
        return players;
    }

    public static @Nullable SkyBlockPlayer getFromUUID(UUID uuid) {
        return getLoadedPlayers().stream().filter(player -> player.getUuid().toString().equalsIgnoreCase(uuid.toString())).findFirst().orElse(null);
    }

    public static @Nullable SkyBlockPlayer getPlayerFromProfileUUID(UUID uuid) {
        return getLoadedPlayers().stream().filter(player -> player.getProfiles().getCurrentlySelected().equals(uuid)).findFirst().orElse(null);
    }

    public static <T> Stream<T> loopThroughPackage(String packageName, Class<T> clazz) {
        return PackageScanner.instantiateSubtypes(packageName, clazz);
    }

    private record ChunkKey(int chunkX, int chunkZ) {
    }

    private void setBiome(Pos start, Pos end, RegistryKey<Biome> biome) {
        int minX = Math.min(start.blockX(), end.blockX());
        int maxX = Math.max(start.blockX(), end.blockX());

        int minY = Math.min(start.blockY(), end.blockY());
        int maxY = Math.max(start.blockY(), end.blockY());

        int minZ = Math.min(start.blockZ(), end.blockZ());
        int maxZ = Math.max(start.blockZ(), end.blockZ());

        int minChunkX = CoordConversion.globalToChunk(minX);
        int maxChunkX = CoordConversion.globalToChunk(maxX);
        int minChunkZ = CoordConversion.globalToChunk(minZ);
        int maxChunkZ = CoordConversion.globalToChunk(maxZ);

        Map<ChunkKey, CompletableFuture<Chunk>> loadedChunks = new HashMap<>();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                ChunkKey key = new ChunkKey(chunkX, chunkZ);

                CompletableFuture<Chunk> chunkFuture = loadedChunks.computeIfAbsent(
                    key,
                    k -> HypixelConst.getInstanceContainer().loadChunk(k.chunkX(), k.chunkZ())
                );

                final int chunkStartX = chunkX << 4;
                final int chunkStartZ = chunkZ << 4;
                final int chunkEndX = chunkStartX + 15;
                final int chunkEndZ = chunkStartZ + 15;

                final boolean fullChunkX = minX <= chunkStartX && maxX >= chunkEndX;
                final boolean fullChunkZ = minZ <= chunkStartZ && maxZ >= chunkEndZ;

                final int startXInChunk = fullChunkX ? 0 : Math.max(0, minX - chunkStartX);
                final int endXInChunk = fullChunkX ? 15 : Math.min(15, maxX - chunkStartX);

                final int startZInChunk = fullChunkZ ? 0 : Math.max(0, minZ - chunkStartZ);
                final int endZInChunk = fullChunkZ ? 15 : Math.min(15, maxZ - chunkStartZ);

                chunkFuture.thenAccept(chunk -> {
                    for (int localX = startXInChunk; localX <= endXInChunk; localX += 4) {
                        int worldX = chunkStartX + localX;

                        for (int y = minY; y <= maxY; y += 4) {
                            for (int localZ = startZInChunk; localZ <= endZInChunk; localZ += 4) {
                                int worldZ = chunkStartZ + localZ;
                                chunk.setBiome(worldX, y, worldZ, biome);
                            }
                        }
                    }
                });
            }
        }
    }

    private static String formatEffectDuration(long durationMs) {
        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return seconds + "s";
        }
    }
}
