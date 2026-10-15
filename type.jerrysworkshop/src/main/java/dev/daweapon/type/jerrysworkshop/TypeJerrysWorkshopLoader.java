package dev.daweapon.type.jerrysworkshop;

import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.jerrysworkshop.defense.JerryWorkshopDefenseManager;
import dev.daweapon.type.jerrysworkshop.chickenrace.GulliversChicken;
import dev.daweapon.type.jerrysworkshop.present.GiftCompass;
import dev.daweapon.type.jerrysworkshop.present.Present;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.entity.GlassDisplay;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.tabmodules.AccountInformationModule;
import dev.daweapon.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import dev.daweapon.type.generic.tab.AreaServerModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Loader that wires up Jerry's Workshop server type: spawn point, tablist, presents, NPCs, and events.
public class TypeJerrysWorkshopLoader implements SkyBlockTypeLoader {
    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_JERRYS_WORKSHOP;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeJerrysWorkshopLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        // Repurposed from dead "not implemented" props into a shelf previewing the Defend Jerry's
        // Workshop reward tiers (Roadmap #20) - the actual arena entry point is St. Jerry.
        GlassDisplay.create(new SkyBlockItem(ItemType.GREEN_GIFT), HypixelConst.getInstanceContainer(), new Pos(14, 77, 91),
                (player, _) -> player.sendMessage("<a>Green Gifts are earned by helping defend Jerry's Workshop!"));
        GlassDisplay.create(new SkyBlockItem(ItemType.RED_GIFT), HypixelConst.getInstanceContainer(), new Pos(16, 77, 91),
                (player, _) -> player.sendMessage("<c>Red Gifts are earned by dealing top damage in a defense round!"));
        GlassDisplay.create(new SkyBlockItem(ItemType.WHITE_GIFT), HypixelConst.getInstanceContainer(), new Pos(13, 77, 97),
                (player, _) -> player.sendMessage("<f>White Gifts are hidden around the Workshop - go find them!"));
        GlassDisplay.create(new SkyBlockItem(ItemType.PAPER), HypixelConst.getInstanceContainer(), new Pos(13, 77, 99),
                (player, _) -> player.sendMessage("<7>Buy a <9>Snow Cannon <7>from <a>Frosty <7>to defend the gift piles when Mount Jerry erupts."));
        GlassDisplay.create(new SkyBlockItem(ItemType.NETHERRACK), HypixelConst.getInstanceContainer(), new Pos(13, 77, 101),
                (player, _) -> player.sendMessage("<7>Magma Cubes attack in waves during the Season of Jerry - defend the Workshop!"));

        Present.spawnAll();
        GiftCompass.start();
        GulliversChicken.spawn();

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            JerryWorkshopDefenseManager.checkAndAutoStart();
            return net.minestom.server.timer.TaskSchedule.seconds(10);
        });
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
            (_) -> new Pos(-4.5, 76, 100.5, -180, 0), // Spawn position
            true // Announce death messages
        );
    }

    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(
                    new SkyBlockPlayersOnlineModule(1),
                    new SkyBlockPlayersOnlineModule(2),
                    new AreaServerModule("tablist.server_info.area.jerrys_workshop"),
                    new AccountInformationModule()
                ));
            }
        };
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
            "dev.daweapon.type.jerrysworkshop.events",
            HypixelEventClass.class
        ));
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return new ArrayList<>();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return new ArrayList<>(SkyBlockGenericLoader.loopThroughPackage(
            "dev.daweapon.type.jerrysworkshop.npcs",
            HypixelNPC.class
        ).toList());
    }


    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return new ArrayList<>();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.SKYBLOCK_JERRYS_WORKSHOP;
    }
}
