package net.swofty.type.jerrysworkshop.present;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.block.PlayerHeadBlockHandler;
import net.swofty.type.generic.data.datapoints.DatapointPresentYear;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Manages the White Gift present entities scattered around Jerry's Workshop. Each SkyBlock year
// places 20 gifts drawn from WhiteGiftLocations and finding them resets with the year.
public class Present {

	private static final List<Entity> spawned = new ArrayList<>();
	private static final Map<Point, Block> replacedBlocks = new HashMap<>();
	private static int spawnedYear = Integer.MIN_VALUE;

	public static void spawnAll() {
		refresh();
		MinecraftServer.getSchedulerManager().buildTask(Present::refresh)
				.repeat(TaskSchedule.seconds(10)).schedule();
	}

	private static synchronized void refresh() {
		int year = SkyBlockCalendar.getYear();
		if (year == spawnedYear) return;
		spawned.forEach(Entity::remove);
		spawned.clear();
		Instance instance = HypixelConst.getInstanceContainer();
		replacedBlocks.forEach(instance::setBlock);
		replacedBlocks.clear();
		spawnedYear = year;
		for (int poolIndex : WhiteGiftLocations.forYear(year)) {
			spawn(poolIndex + 1, openSpot(WhiteGiftLocations.position(poolIndex)));
		}
	}

	// Recorded spots predate the island's 2026 rebuild, so a gift that would sit inside a block is
	// lifted to the first open space above it rather than becoming unreachable.
	private static Point openSpot(BlockVec recorded) {
		Instance instance = HypixelConst.getInstanceContainer();
		instance.loadChunk(recorded).join();
		for (int lift = 0; lift <= 3; lift++) {
			BlockVec candidate = recorded.add(0, lift, 0);
			if (!instance.getBlock(candidate).isSolid()) return candidate;
		}
		return recorded;
	}

	public static int foundThisYear(SkyBlockPlayer player) {
		DatapointPresentYear.YearData presents = player.getSkyblockDataHandler()
				.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP, DatapointPresentYear.class).getValue();
		return presents.year() == SkyBlockCalendar.getYear() ? presents.value().size() : 0;
	}

	public static void spawn(int index, Point position) {
		Instance instance = HypixelConst.getInstanceContainer();

		String texturesJson = "{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/e47b37e67a89159bf4acc4a44d4328f4efc0181605142288e5eed1ab8aed913c\"}}}";
		String texturesEncoded = java.util.Base64.getEncoder().encodeToString(texturesJson.getBytes());

		CompoundBinaryTag profile = CompoundBinaryTag.builder().put("properties",
				ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(CompoundBinaryTag.builder()
						.putString("name", "textures")
						.putString("value", texturesEncoded)
						.build()).build()).build();
		replacedBlocks.put(position, instance.getBlock(position));
		instance.setBlock(position, Block.PLAYER_HEAD
				.withHandler(new PlayerHeadBlockHandler())
				.withTag(Tag.NBT("profile"), profile));

		InteractionEntity entity = new InteractionEntity(0.5f, 0.5f, (player, interactEvent) -> {
			SkyBlockDataHandler dataHandler = ((SkyBlockPlayer) player).getSkyblockDataHandler();
			DatapointPresentYear.YearData presents = dataHandler.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP, DatapointPresentYear.class).getValue();
			int year = presents.year();
			int currentYear = SkyBlockCalendar.getYear();
			if (year != currentYear) {
				dataHandler.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP,
						DatapointPresentYear.class
				).setValue(new DatapointPresentYear.YearData(currentYear, List.of(index)));
				player.sendMessage("<2><l>GIFT! </l><a>You found a <f>White Gift<a>! <7>(<f>1<7>/<a>{}<7>)", WhiteGiftLocations.GIFTS_PER_YEAR);
				((SkyBlockPlayer) player).addAndUpdateItem(ItemType.WHITE_GIFT);
				return;
			}
			List<Integer> value = new ArrayList<>(presents.value());

			if (value.contains(index)) {
				player.sendMessage("<c>You have already found this Gift this year!");
			} else {
				value.add(index);
				dataHandler.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP,
						DatapointPresentYear.class
				).setValue(new DatapointPresentYear.YearData(currentYear, value));
				player.sendMessage("<2><l>GIFT! </l><a>You found a <f>White Gift<a>! <7>(<f>{}<7>/<a>{}<7>)", value.size(), WhiteGiftLocations.GIFTS_PER_YEAR);
				((SkyBlockPlayer) player).addAndUpdateItem(ItemType.WHITE_GIFT);
				if (value.size() >= WhiteGiftLocations.GIFTS_PER_YEAR) {
					((SkyBlockPlayer) player).getAchievementHandler().completeAchievement("skyblock.happy_holidays");
				}
			}
		});
		entity.setInstance(instance, position.add(0.5, 0, 0.5));
		spawned.add(entity);
	}

}
