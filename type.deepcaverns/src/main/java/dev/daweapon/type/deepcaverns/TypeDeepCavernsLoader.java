package dev.daweapon.type.deepcaverns;

import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.tab.AreaServerModule;
import dev.daweapon.type.generic.SkyBlockTypeLoader;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;

import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.tabmodules.AccountInformationModule;
import dev.daweapon.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Loader that wires up the Deep Caverns server type: spawn point, tablist, NPCs, and events.
public class TypeDeepCavernsLoader implements SkyBlockTypeLoader {
	@Override
	public ServerType getType() {
		return ServerType.SKYBLOCK_DEEP_CAVERNS;
	}

	@Override
	public void onInitialize(MinecraftServer server) {
		Logger.info("TypeDeepCavernsLoader initialized!");
	}

	@Override
	public void afterInitialize(MinecraftServer server) {

	}

	@Override
	public LoaderValues getLoaderValues() {
		return new LoaderValues(
				(type) -> switch (type) {
					default -> new Pos(4, 157, 85, -180, 0);
				}, // Spawn position
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
						new AreaServerModule("tablist.server_info.area.deep_caverns"),
						new AccountInformationModule()
				));
			}
		};
	}

	@Override
	public List<HypixelEventClass> getTraditionalEvents() {
		return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
				"dev.daweapon.type.deepcaverns.events",
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
                "dev.daweapon.type.deepcaverns.npcs",
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
		return CustomWorlds.SKYBLOCK_DEEP_CAVERNS;
	}
}
