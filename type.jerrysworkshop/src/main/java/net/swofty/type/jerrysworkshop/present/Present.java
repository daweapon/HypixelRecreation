package net.swofty.type.jerrysworkshop.present;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointPresentYear;
import net.swofty.type.generic.entity.BlockDisplayEntity;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// Manages the White Gift present entities scattered around Jerry's Workshop, one findable per real-life year.
public class Present {

	static List<BlockVec> positions = List.of(
			new BlockVec(-1, 76, 81),
			new BlockVec(-9, 76, 104),
			new BlockVec(-1, 79, 113),
			new BlockVec(-34, 76, 81),
			new BlockVec(-25, 76, 69),
			new BlockVec(16, 95, 96),
			new BlockVec(26, 86, 43),
			new BlockVec(-17, 82, 30),
			new BlockVec(-12, 76, 9),
			new BlockVec(9, 77, 9)
	);

	public static void spawnAll() {
		for (int i = 0; i < positions.size(); i++) {
			spawn(i + 1, positions.get(i));
		}
	}

	public static void spawn(int index, Point position) {
		Instance instance = HypixelConst.getInstanceContainer();

		// Real Hypixel-Wiki-sourced White Gift skull texture (reused from the existing "White Gift
		// Talisman" accessory item, configuration/skyblock/items/accessories/gift.yml). The
		// previous hardcoded texture value here was a truncated/invalid Mojang texture hash (60
		// chars instead of the required 64) - Minecraft silently falls back to a blank default
		// head when the value can't be resolved, which is exactly the bug the user reported
		// ("any placed player heads should be a white gift"). Built the same simple
		// signature-less way PlayerItemUpdater builds skull textures for held items, rather than
		// a hardcoded signed profile blob - a signature only validates against the exact value it
		// was issued for, so hand-copying one for a different texture would never have worked.
		String texturesJson = "{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/e47b37e67a89159bf4acc4a44d4328f4efc0181605142288e5eed1ab8aed913c\"}}}";
		String texturesEncoded = java.util.Base64.getEncoder().encodeToString(texturesJson.getBytes());

		Block playerHead = Block.PLAYER_HEAD.withTag(
				Tag.NBT("profile"),
				CompoundBinaryTag.builder().put(
						"properties",
						ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(
								CompoundBinaryTag.builder()
										.putString("name", "textures")
										.putString("value", texturesEncoded)
										.build()
						).build()
				).build());

		BlockDisplayEntity displayEntity = new BlockDisplayEntity(playerHead, _ -> {});
		displayEntity.setInstance(instance, position);
		InteractionEntity entity = new InteractionEntity(0.5f, 0.5f, (player, interactEvent) -> {
			SkyBlockDataHandler dataHandler = ((SkyBlockPlayer) player).getSkyblockDataHandler();
			DatapointPresentYear.YearData presents = dataHandler.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP, DatapointPresentYear.class).getValue();
			int year = presents.year();
			int currentYear = SkyBlockCalendar.getYear();
			if (year != currentYear) {
				dataHandler.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP,
						DatapointPresentYear.class
				).setValue(new DatapointPresentYear.YearData(currentYear, List.of(index)));
				player.sendMessage("<2><l>GIFT! </l><a>You found a <f>White Gift<a>! <7>(<f>1<7>/<a>20<7>)");
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
				player.sendMessage("<2><l>GIFT! </l><a>You found a <f>White Gift<a>! <7>(<f>{}<7>/<a>20<7>)", value.size());
				((SkyBlockPlayer) player).addAndUpdateItem(ItemType.WHITE_GIFT);
			}
		});
		entity.setInstance(instance, position.add(0.5, 0, 0.5));
	}

}
