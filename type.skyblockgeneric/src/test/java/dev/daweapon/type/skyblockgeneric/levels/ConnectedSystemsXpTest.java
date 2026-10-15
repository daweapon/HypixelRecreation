package dev.daweapon.type.skyblockgeneric.levels;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.domain.AccountDomain;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDomain;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import dev.daweapon.type.skyblockgeneric.hotm.HotmRegistry;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerDefinition;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerProgression;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRegistry;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectedSystemsXpTest {
    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        Path configuration = Path.of("configuration");
        if (!Files.exists(configuration)) Files.createSymbolicLink(configuration, Path.of("../configuration"));
        Assumptions.assumeTrue(Files.exists(configuration.resolve("skyblock/shards.yml")),
                "the server configuration must be reachable at ./configuration");
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(Path.of("../configuration/skyblock/items"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
            }
        }
        SkyBlockLevelCause.initializeCauses();
        SlayerRegistry.loadAll();
    }

    private static SkyBlockPlayer player() {
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
        return new SkyBlockPlayer(new GameProfile(uuid, "XpTester"), connection);
    }

    @Test
    void everyEnabledSlayerTierPlusTheDisabledRiftstalkerAddsUpToTheCap() {
        int total = 0;
        for (SlayerDefinition definition : SlayerRegistry.all()) {
            for (SlayerTier tier : definition.tiers().keySet()) total += SlayerProgression.firstDefeatXp(tier);
        }
        int riftstalker = 0;
        for (SlayerTier tier : SlayerTier.values()) riftstalker += SlayerProgression.firstDefeatXp(tier);
        assertEquals(SkyBlockXPSource.DEFEAT_SLAYERS.getMaxXP(), total + riftstalker);
    }

    @Test
    void slayerFirstDefeatsAwardXpOnlyOncePerTier() {
        SkyBlockPlayer player = player();
        SlayerDefinition revenant = SlayerRegistry.all().iterator().next();
        DatapointSlayer.SlayerProgress progress = player.getSlayerData().progress(revenant.type());
        progress.addCompletion(SlayerTier.I);
        progress.addCompletion(SlayerTier.I);
        progress.addCompletion(SlayerTier.III);
        SlayerProgression.sync(player);
        assertEquals(75, player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.DEFEAT_SLAYERS));
    }

    @Test
    void brainFoodGivesFiveXpPerUseUpToTheCap() {
        SkyBlockPlayer player = player();
        MythologicalItemHandlers.setBrainFoodEaten(player, 3);
        assertEquals(15, player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.BRAIN_FOOD));
        MythologicalItemHandlers.setBrainFoodEaten(player, MythologicalItemHandlers.BRAIN_FOOD_LIMIT);
        assertEquals(SkyBlockXPSource.BRAIN_FOOD.getMaxXP(), player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.BRAIN_FOOD));
    }

    @Test
    void heartAndCoreOfTheMountainHitTheirOwnCapsWhenMaxed() {
        SkyBlockPlayer player = player();
        HotmService.maxOut(player);
        assertEquals(SkyBlockXPSource.HEART_OF_THE_MOUNTAIN.getMaxXP() - 1942,
                player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.HEART_OF_THE_MOUNTAIN));
        assertEquals(SkyBlockXPSource.CORE_OF_THE_MOUNTAIN.getMaxXP(),
                player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.CORE_OF_THE_MOUNTAIN));
    }

    @Test
    void hotmConfigurationSumsMatchTheWikiBuckets() {
        HotmRegistry registry = HotmRegistry.get();
        int tiers = 0;
        for (int tier = 1; tier <= 10; tier++) tiers += registry.tierReward(tier).skyBlockXp();
        assertEquals(1175, tiers);
        assertEquals(1000, registry.coreLevels().stream().mapToInt(level -> level.skyBlockXp()).sum());
    }
}
