package net.swofty.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crystalhollows.gui.GUIShopChunk;
import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCChunk extends AbstractStructureNPC {
    public NPCChunk() {
        super("Chunk", "<a>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY1ZjMxYzFjMjE1YTU3ZTkzN2ZkNzVhYjM1NzgyZjg1ZWMyNDJhMWIxZjk1MGEyNmE0MmJiNWUwYWE1Y2JlZGEifX19", "chunk_stand", "chunk", new Pos(797.5, 128.5, 708.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().openView(new GUIShopChunk());
    }
}
