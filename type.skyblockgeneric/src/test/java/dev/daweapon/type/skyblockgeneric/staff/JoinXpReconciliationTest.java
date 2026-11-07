package dev.daweapon.type.skyblockgeneric.staff;

import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.domain.AccountDomain;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryBagXP;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryPower;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDomain;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsZone;
import dev.daweapon.type.skyblockgeneric.event.actions.custom.ActionSyncSkyBlockXPOnJoin;
import dev.daweapon.type.skyblockgeneric.event.actions.custom.levels.ActionChangeHypixelXP;
import dev.daweapon.type.skyblockgeneric.event.custom.SkyBlockXPModificationEvent;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JoinXpReconciliationTest {
    @BeforeAll
    static void init() throws Exception {
        MaxProfileTest.init();
    }

    @AfterEach
    void restoreZone() {
        CatacombsZone.useServerType();
    }

    @Test
    void accessoryBagXpIsTheSameInsideAndOutsideTheCatacombs() {
        SkyBlockPlayer player = MaxProfileTest.player();
        MaxStickService.runSteps(player);

        CatacombsZone.use(() -> false);
        int outside = AccessoryPower.of(player);
        AccessoryBagXP.sync(player);
        double xpOutside = player.getSkyBlockExperience().getTotalXP();

        CatacombsZone.use(() -> true);
        assertNotEquals(outside, AccessoryPower.of(player), "a maxed bag holds dungeon accessories, which count double in dungeons");
        AccessoryBagXP.sync(player);

        assertEquals(outside, player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.ACCESSORY_BAG));
        assertEquals(xpOutside, player.getSkyBlockExperience().getTotalXP());
    }

    @Test
    void joiningAnyServerReconcilesToTheSameTotal() {
        SkyBlockPlayer player = MaxProfileTest.player();
        MaxStickService.runSteps(player);

        CatacombsZone.use(() -> false);
        join(player);
        double hub = player.getSkyBlockExperience().getTotalXP();

        CatacombsZone.use(() -> true);
        join(player);
        assertEquals(hub, player.getSkyBlockExperience().getTotalXP(), "joining the Catacombs changed SkyBlock XP");

        SkyBlockDataHandler reloaded = SkyBlockDataHandler.createFromProfileOnly(player.getSkyblockDataHandler().toProfileDocument());
        assertEquals(hub, reloaded.get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE,
                dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience.class).getValue().getTotalXP());

        CatacombsZone.use(() -> false);
        join(player);
        assertEquals(hub, player.getSkyBlockExperience().getTotalXP(), "returning from the Catacombs changed SkyBlock XP");
    }

    @Test
    void aJoinReconciliationNeverAnnouncesALevelUp() {
        List<SendablePacket> sent = new CopyOnWriteArrayList<>();
        SkyBlockPlayer player = player(sent);
        double from = SkyBlockLevelRequirement.getLevel(541).getCumulativeExperience();
        double to = SkyBlockLevelRequirement.getLevel(542).getCumulativeExperience();

        new ActionChangeHypixelXP().run(new SkyBlockXPModificationEvent(player, null, from, to, true));
        assertFalse(sent.stream().anyMatch(SystemChatPacket.class::isInstance), "a join reconciliation printed chat");

        new ActionChangeHypixelXP().run(new SkyBlockXPModificationEvent(player, null, from, to));
        assertTrue(sent.stream().anyMatch(SystemChatPacket.class::isInstance), "a real level-up must still be announced");
    }

    private static void join(SkyBlockPlayer player) {
        ActionSyncSkyBlockXPOnJoin.reconcile(player);
        AccessoryBagXP.sync(player);
    }

    private static SkyBlockPlayer player(List<SendablePacket> sent) {
        UUID uuid = UUID.randomUUID();
        PlayerDataService.store(AccountDomain.KEY, uuid, HypixelDataHandler.initUserWithDefaultData(uuid));
        PlayerDataService.store(SkyBlockDomain.KEY, uuid, SkyBlockDataHandler.initUserWithDefaultData(uuid, UUID.randomUUID()));
        PlayerConnection connection = new PlayerConnection() {
            @Override
            public void sendPacket(SendablePacket packet) {
                sent.add(packet);
            }

            @Override
            public SocketAddress getRemoteAddress() {
                return new InetSocketAddress(0);
            }
        };
        return new SkyBlockPlayer(new GameProfile(uuid, "XpTester"), connection);
    }
}
