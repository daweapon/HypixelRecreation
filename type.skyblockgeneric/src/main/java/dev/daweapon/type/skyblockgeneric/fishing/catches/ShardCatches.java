package dev.daweapon.type.skyblockgeneric.fishing.catches;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;

public final class ShardCatches {
    private static final String PREFIX = "SHARD_";

    private ShardCatches() {
    }

    public static Optional<AttributeDefinition> definitionOf(String itemId) {
        if (itemId == null || !itemId.startsWith(PREFIX)) return Optional.empty();
        return AttributeRegistry.findByShard(itemId.substring(PREFIX.length()).replace('_', ' '));
    }

    public static void award(SkyBlockPlayer player, AttributeDefinition shard, int amount, String header) {
        player.getHuntingData().addShards(shard.id(), amount);
        HypixelEventHandler.callCustomEvent(new ShardCaughtEvent(player, shard, amount, ShardCaughtEvent.Source.FISHING, false));
        player.sendMessage(Text.of("{} <b>You found <8>{}x <color:{}>{} Shard<b>.", header, amount,
                shard.rarity().itemRarity().getColor(), shard.shardName()));
    }
}
