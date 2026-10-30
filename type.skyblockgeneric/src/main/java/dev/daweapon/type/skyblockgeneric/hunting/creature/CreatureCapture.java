package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;
import java.util.function.IntUnaryOperator;

public final class CreatureCapture {
    private CreatureCapture() {
    }

    public static Optional<AttributeDefinition> shardOf(CreatureSpec spec) {
        return spec.shard() == null ? Optional.empty() : AttributeRegistry.findByShard(spec.shard());
    }

    public static void award(SkyBlockPlayer player, HuntingCreatureMob mob) {
        CreatureSpec spec = mob.spec();
        for (CreatureSpec.Message message : spec.messages()) {
            if ("capture".equals(message.when())) player.sendMessage(dev.daweapon.commons.text.Text.parse(message.text()));
        }
        Optional<AttributeDefinition> shard = shardOf(spec);
        if (shard.isEmpty()) {
            player.sendMessage("<a><l>CAPTURED!</l> <7>You caught <e>{}<7>!", spec.name());
            return;
        }
        awardShard(player, shard.get(), spec.name(), spec.shardChance(),
                base -> mob.special().map(special -> special.shardAmount(mob, base)).orElse(base), sourceOf(spec));
    }

    public static void awardShard(SkyBlockPlayer player, AttributeDefinition definition, String name, double chance,
                                  IntUnaryOperator amountOf, ShardCaughtEvent.Source source) {
        if (!LootRoll.roll(player.getUuid(), dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent.itemType(definition), chance)) {
            player.sendMessage("<a><l>CAPTURED!</l> <7>You caught <e>{}<7> but it slipped away without a shard.", name);
            return;
        }
        int amount = amountOf.applyAsInt(AttributeEffectService.fortunateAmount(player, definition));
        player.getHuntingData().addShards(definition.id(), amount);
        HypixelEventHandler.callCustomEvent(new ShardCaughtEvent(player, definition, amount, source, false));
        player.sendMessage("<a><l>CAPTURED!</l> <7>You caught <e>{}<7> and got <e>{}x <a>{} Shard<7>!", name, amount, definition.shardName());
    }

    private static ShardCaughtEvent.Source sourceOf(CreatureSpec spec) {
        return switch (spec.capture()) {
            case FISHING_NET -> ShardCaughtEvent.Source.FISHING_NET;
            case BLACK_HOLE -> ShardCaughtEvent.Source.BLACK_HOLE;
            default -> ShardCaughtEvent.Source.LASSO;
        };
    }
}
