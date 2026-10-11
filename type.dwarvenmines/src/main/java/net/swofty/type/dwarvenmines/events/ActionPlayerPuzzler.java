package net.swofty.type.dwarvenmines.events;

import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.swofty.type.dwarvenmines.npcs.NPCPuzzler;
import net.swofty.type.dwarvenmines.puzzler.PuzzlerService;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Checks the block a player mines against the Puzzler's active puzzle.
public class ActionPlayerPuzzler implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
	public void run(PlayerBlockBreakEvent event) {
		if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
		PuzzlerService.Puzzle puzzle = PuzzlerService.active(player);
		if (puzzle == null || !PuzzlerService.isInPuzzleArea(puzzle, event.getBlockPosition())) return;

		event.setCancelled(true);
		if (!PuzzlerService.isTarget(puzzle, event.getBlockPosition())) {
			NPCPuzzler.say(player, "<f>▶▲Wrong▲ ◀block◀ ▼▶try again▶▶");
			return;
		}
		long gained = PuzzlerService.solve(player);
		NPCPuzzler.say(player, "<f>✌✓");
		NPCPuzzler.say(player, "<f>▶▶Nice! ▲Here, ◀have ▼some◀ ▶Mithril Powder!▲");
		player.sendMessage("<d>Puzzler gave you <2>" + String.format("%,d", gained) + " Mithril Powder <d>for solving the puzzle!");
	}
}
