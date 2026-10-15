package dev.daweapon.type.bedwarsgame.events;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import dev.daweapon.commons.mc.HypixelPosition;
import dev.daweapon.type.bedwarsgame.TypeBedWarsGameLoader;
import dev.daweapon.type.bedwarsgame.entity.TntEntity;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.GameState;
import dev.daweapon.type.game.replay.dispatcher.BlockChangeDispatcher;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import org.tinylog.Logger;

// Validates and processes block placement in Bedwars games (height limits, spawn protection, replay).
public class ActionGamePlace implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(PlayerBlockPlaceEvent event) {
		BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
		BedWarsGame game = player.getGame();
		if (game == null) {
			Logger.info("Player {} tried to place a block but is not in a game!", player.getUsername());
			event.setCancelled(true); // Prevent placing if not in a game
			return;
		}

		if (!game.isPlayerCurrentlyPlaying(player.getUuid())) {
			event.setCancelled(true);
			return;
		}

		if (event.getBlockPosition().y() >= 105) {
			player.sendMessage("<c>You cannot place blocks this high!");
			event.setCancelled(true);
			return;
		}

		if (game.getState() != GameState.IN_PROGRESS) {
			event.setCancelled(true);
			return;
		}

		Point blockPosition = event.getBlockPosition();
		for (MapTeam team : game.getMapEntry().getConfiguration().getTeams().values()) {
			HypixelPosition spawnPos = team.getSpawn();
			if (spawnPos != null) {
				Point spawnPoint = new Pos(spawnPos.x(), spawnPos.y(), spawnPos.z());
				if (blockPosition.distance(spawnPoint) <= 6) {
					player.sendMessage("<c>You cannot build here.");
					event.setCancelled(true);
					return;
				}
			}
		}

		if (event.getBlock().material() == Material.TNT) {
			TntEntity entity = new TntEntity(event.getPlayer());
			entity.setFuse(50);
			entity.setInstance(event.getInstance(), blockPosition.add(0.5, 0, 0.5));
			entity.getViewersAsAudience().playSound(Sound.sound(
					SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK,
					1.0f, 1.0f
			), entity);
			ScheduleUtility.nextTick(
					() -> player.getInstance().setBlock(blockPosition, Block.AIR)
			);
			return;
		}

		Block placedBlock = event.getBlock().withTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG, true);
		if (game.getReplayManager().isRecording()) {
			BlockChangeDispatcher blockDispatcher = game.getReplayManager().getBlockChangeDispatcher();
			if (blockDispatcher != null) {
				Block previousBlock = event.getInstance().getBlock(blockPosition);
				blockDispatcher.recordBlockChange(
					blockPosition.blockX(),
					blockPosition.blockY(),
					blockPosition.blockZ(),
					previousBlock.stateId(),
					placedBlock.stateId()
				);
			}
		}
		event.setBlock(placedBlock);
        if (player.allowsPersistentProgress()) {
            player.getAchievementHandler().addProgressByTrigger("bedwars.blocks_placed", 1);
        }
	}

}
