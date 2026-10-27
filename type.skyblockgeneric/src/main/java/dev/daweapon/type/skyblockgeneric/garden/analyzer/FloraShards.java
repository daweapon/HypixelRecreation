package dev.daweapon.type.skyblockgeneric.garden.analyzer;

import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class FloraShards {
    public static final double COPPER_PER_SHARD = 10_000D / 3;
    private static final String SHARD = "Flora";

    private FloraShards() {
    }

    public static int award(SkyBlockPlayer player, GardenProfile profile, double copper) {
        int amount = profile.takeFloraShards(copper, COPPER_PER_SHARD);
        if (amount <= 0) return 0;
        AttributeRegistry.findByShard(SHARD).ifPresent(definition -> {
            player.getHuntingData().addShards(definition.id(), amount);
            HypixelEventHandler.callCustomEvent(new ShardCaughtEvent(player, definition, amount, ShardCaughtEvent.Source.ANALYZER, false));
            player.sendMessage("<a>The analysis yielded <e>{}x <a>{}<7>!", amount, definition.shardName());
        });
        return amount;
    }
}
