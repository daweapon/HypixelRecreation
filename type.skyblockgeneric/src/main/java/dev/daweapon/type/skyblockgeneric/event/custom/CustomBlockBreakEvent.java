package dev.daweapon.type.skyblockgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@Getter
// Fired when a SkyBlock block finishes breaking, carrying its drops and whether it was player-placed
public class CustomBlockBreakEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    private final Material material;
    private final Point point;
    private final List<SkyBlockItem> drops;
    private final Boolean playerPlaced;

    public CustomBlockBreakEvent(SkyBlockPlayer player, Material material, Point point, List<SkyBlockItem> drops, Boolean playerPlaced) {
        this.player = player;
        this.material = material;
        this.point = point;
        this.drops = drops;
        this.playerPlaced = playerPlaced;
    }
}
