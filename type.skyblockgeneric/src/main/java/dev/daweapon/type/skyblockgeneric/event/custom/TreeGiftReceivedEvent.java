package dev.daweapon.type.skyblockgeneric.event.custom;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;

@Getter
public class TreeGiftReceivedEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    private final String treeType;

    public TreeGiftReceivedEvent(SkyBlockPlayer player, String treeType) {
        this.player = player;
        this.treeType = treeType;
    }
}
