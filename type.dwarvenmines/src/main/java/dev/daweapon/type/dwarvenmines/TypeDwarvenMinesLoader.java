package dev.daweapon.type.dwarvenmines;

import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.dwarvenmines.gui.GUIFossilExcavator;
import dev.daweapon.type.dwarvenmines.gui.GUIGemstoneGrinder;
import dev.daweapon.type.skyblockgeneric.glacite.ColdService;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventScheduler;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventZone;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.entity.TextDisplayEntity;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;

// Type loader that initializes the Dwarven Mines server: its NPCs, events, tablist, and dimension.
public class TypeDwarvenMinesLoader implements SkyBlockTypeLoader {
    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_DWARVEN_MINES;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeDwarvenMinesLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        ColdService.start();
        MiningEventScheduler.start(MiningEventZone.DWARVEN_MINES);
        Point gemstoneGrinderPos = new Pos(85.5, 199, -116.5);

        InteractionEntity gemstoneGrinder = new InteractionEntity(1.1f, 1.1f, (p, event) -> {
           new GUIGemstoneGrinder().open(p);
        });
        TextDisplayEntity gemstoneGrinderText = new TextDisplayEntity("<d>Gemstone Grinder", meta -> {});
        TextDisplayEntity gemstoneGrinderClick = new TextDisplayEntity("<e><l>CLICK", meta -> {});

        gemstoneGrinder.setInstance(HypixelConst.getInstanceContainer(), gemstoneGrinderPos);
        gemstoneGrinderText.setInstance(HypixelConst.getInstanceContainer(), gemstoneGrinderPos.add(0, 1.3, 0));
        gemstoneGrinderClick.setInstance(HypixelConst.getInstanceContainer(), gemstoneGrinderPos.add(0, 0.9, 0));

        Point excavatorPos = new Pos(18.5, 121, 227.5);
        InteractionEntity excavator = new InteractionEntity(1.4f, 1.4f, (p, event) -> p.openView(new GUIFossilExcavator()));
        TextDisplayEntity excavatorText = new TextDisplayEntity("<6><l>FOSSIL EXCAVATOR", meta -> {});
        TextDisplayEntity excavatorClick = new TextDisplayEntity("<e>Click to open!", meta -> {});
        excavator.setInstance(HypixelConst.getInstanceContainer(), excavatorPos);
        excavatorText.setInstance(HypixelConst.getInstanceContainer(), excavatorPos.add(0, 1.9, 0));
        excavatorClick.setInstance(HypixelConst.getInstanceContainer(), excavatorPos.add(0, 1.5, 0));
    }

	@Override
	public LoaderValues getLoaderValues() {
		return new LoaderValues(
				(_) -> new Pos(-48.5, 200, -121.5, -90, 0),
				true
		);
	}

    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.DWARVEN_MINES);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.dwarvenmines.events",
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
                "dev.daweapon.type.dwarvenmines.npcs",
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
        return CustomWorlds.SKYBLOCK_DWARVEN_MINES;
    }

	@Override
	public @Nullable RegistryKey<DimensionType> getDimensionType() {
		return MinecraftServer.getDimensionTypeRegistry().register(
				Key.key("skyblock:dwarven_mines"),
				DimensionType.builder()
						.ambientLight(1f)
						.build());
	}

}
