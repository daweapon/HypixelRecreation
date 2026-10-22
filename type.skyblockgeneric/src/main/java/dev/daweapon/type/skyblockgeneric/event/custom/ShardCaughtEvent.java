package dev.daweapon.type.skyblockgeneric.event.custom;

import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;

@Getter
public class ShardCaughtEvent implements PlayerInstanceEvent {
    public enum Source { CHARM, HUNTRAP, FUSION, TREE_GIFT, HONEYHIVE, FISHING, LASSO, FISHING_NET, BLACK_HOLE, BEACON }

    private final SkyBlockPlayer player;
    private final AttributeDefinition shard;
    private final int amount;
    private final Source source;
    private final boolean saltActive;

    public ShardCaughtEvent(SkyBlockPlayer player, AttributeDefinition shard, int amount, Source source, boolean saltActive) {
        this.player = player;
        this.shard = shard;
        this.amount = amount;
        this.source = source;
        this.saltActive = saltActive;
    }
}
