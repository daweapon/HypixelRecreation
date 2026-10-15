package dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelTypeLoader;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.domain.AccountDomain;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDomain;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.loottable.ForcedDrops;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimsonMinibossLootTest {
    private static final Map<CrimsonMiniboss, CrimsonMobProfile> PROFILES = Map.of(
            CrimsonMiniboss.BLADESOUL, CrimsonMobs.BLADESOUL,
            CrimsonMiniboss.MAGE_OUTLAW, CrimsonMobs.MAGE_OUTLAW,
            CrimsonMiniboss.BARBARIAN_DUKE_X, CrimsonMobs.BARBARIAN_DUKE_X,
            CrimsonMiniboss.ASHFANG, CrimsonMobs.ASHFANG,
            CrimsonMiniboss.MAGMA_BOSS, CrimsonMobs.MAGMA_BOSS);

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        Path configuration = Path.of("configuration");
        if (!Files.exists(configuration)) Files.createSymbolicLink(configuration, Path.of("../configuration"));
        Assumptions.assumeTrue(Files.exists(configuration.resolve("skyblock/items")),
                "the server configuration must be reachable at ./configuration");
        MinecraftServer.init();
        HypixelConst.setTypeLoader((HypixelTypeLoader) Proxy.newProxyInstance(HypixelTypeLoader.class.getClassLoader(),
                new Class<?>[]{HypixelTypeLoader.class},
                (proxy, method, args) -> method.getName().equals("getType") ? ServerType.SKYBLOCK_CRIMSON_ISLE : null));
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(configuration.resolve("skyblock/items"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
            }
        }
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
        return new SkyBlockPlayer(new GameProfile(uuid, "LootTester"), connection);
    }

    private static final class TestBoss extends CrimsonMinibossMob {
        private final CrimsonMiniboss miniboss;
        private final List<UUID> sharersAtDeath = new ArrayList<>();
        private SkyBlockPlayer killedBy;

        TestBoss(CrimsonMiniboss miniboss) {
            super(PROFILES.get(miniboss));
            this.miniboss = miniboss;
        }

        @Override
        public CrimsonMiniboss miniboss() {
            return miniboss;
        }

        @Override
        protected void onKilledBy(SkyBlockPlayer killer) {
            killedBy = killer;
            sharersAtDeath.addAll(lootSharers());
        }
    }

    private static void hit(TestBoss boss, SkyBlockPlayer player, float amount) {
        boss.damage(new Damage(DamageType.PLAYER_ATTACK, player, player, player.getPosition(), amount));
    }

    @Test
    void aOneShotKillSharesTheLoot() {
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) {
            SkyBlockPlayer player = player();
            TestBoss boss = new TestBoss(miniboss);
            hit(boss, player, Float.MAX_VALUE);
            assertTrue(boss.isDead(), miniboss + " survived");
            assertEquals(player, boss.killedBy, miniboss + " lost its killer");
            assertEquals(List.of(player.getUuid()), boss.sharersAtDeath, miniboss + " gave no loot share");
        }
    }

    @Test
    void theKillingBlowCountsTowardTheShare() {
        SkyBlockPlayer player = player();
        SkyBlockPlayer helper = player();
        TestBoss boss = new TestBoss(CrimsonMiniboss.BLADESOUL);
        hit(boss, helper, 100_000);
        hit(boss, player, 200_000);
        hit(boss, helper, boss.getHealth() - 100_000);
        hit(boss, player, 200_000);
        assertTrue(boss.isDead());
        assertTrue(boss.sharersAtDeath.contains(player.getUuid()));
        assertTrue(boss.sharersAtDeath.contains(helper.getUuid()));
        assertEquals(300_000, boss.damageDealtBy(player.getUuid()), 1);
    }

    @Test
    void anObliteratedBossStillSharesTheLoot() {
        SkyBlockPlayer player = player();
        TestBoss boss = new TestBoss(CrimsonMiniboss.MAGE_OUTLAW);
        boss.obliterate(player);
        assertTrue(boss.isDead());
        assertEquals(List.of(player.getUuid()), boss.sharersAtDeath);
    }

    @Test
    void aShareRollsTheBossTable() {
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) {
            SkyBlockPlayer player = player();
            TestBoss boss = new TestBoss(miniboss);
            List<CrimsonMobLoot.Won> won = CrimsonMobLoot.rollItems(player, boss, miniboss.getDrops());
            ItemType guaranteed = miniboss.getDrops().getFirst().availableItems().getFirst();
            assertTrue(won.stream().anyMatch(entry -> entry.item().getAttributeHandler().getPotentialType() == guaranteed),
                    miniboss + " dropped no " + guaranteed);

            ItemType rare = miniboss.getDrops().stream().filter(drop -> drop.chancePercent() < 1)
                    .flatMap(drop -> drop.availableItems().stream()).findFirst().orElseThrow();
            ForcedDrops.force(player.getUuid(), rare);
            assertTrue(CrimsonMobLoot.rollItems(player, boss, miniboss.getDrops()).stream()
                    .anyMatch(entry -> entry.item().getAttributeHandler().getPotentialType() == rare && entry.rare()),
                    miniboss + " could not force " + rare);
        }
    }
}
