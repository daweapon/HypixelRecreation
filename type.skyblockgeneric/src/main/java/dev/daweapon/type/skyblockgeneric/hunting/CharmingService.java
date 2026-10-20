package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.skyblockgeneric.consumable.Salt;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

// Rolls a chance to "charm" (capture a shard from) a mob on kill, based on hunting skill level
// and the Charmed attribute bonus. Elusive mobs are always charmed.
public final class CharmingService {
    private CharmingService() {
    }

    public static boolean tryCharm(SkyBlockPlayer player, SkyBlockMob mob) {
        AttributeDefinition definition = shardFor(mob);
        if (definition == null) return false;
        int huntingLevel = player.getSkills().getCurrentLevel(SkillCategories.HUNTING);
        double skillChance = huntingLevel * 0.0004D;
        double charmedChance = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("L11")) / 100D;
        boolean guaranteed = mob.getMobTypes().contains(MobType.ELUSIVE);
        if (!guaranteed && !LootRoll.roll(player.getUuid(), AttributeShardComponent.itemType(definition),
                (skillChance + charmedChance) * 100 + Salt.charmChanceBonus(player, mob))) return false;
        int amount = AttributeEffectService.fortunateAmount(player, definition);
        player.getHuntingData().addShards(definition.id(), amount);
        HypixelEventHandler.callCustomEvent(new ShardCaughtEvent(player, definition, amount, ShardCaughtEvent.Source.CHARM,
                Salt.charmChanceBonus(player, mob) > 0));
        player.getSkills().increase(player, SkillCategories.HUNTING, huntingExperience(definition));
        player.sendMessage("<d><l>CHARM! </l><a>Captured <e>{}x <a>{} <7>from <c>{}<7>!",
                amount, definition.shardName(), mob.getDisplayName());
        return true;
    }

    private static AttributeDefinition shardFor(SkyBlockMob mob) {
        if (!(mob instanceof BestiaryMob bestiaryMob)) return null;
        String mobId = bestiaryMob.getMobID();
        return AttributeRegistry.values().stream()
                .filter(definition -> definition.sources().stream()
                        .anyMatch(source -> source.mobIds().stream().anyMatch(mobId::equalsIgnoreCase)))
                .findFirst().orElse(null);
    }

    private static double huntingExperience(AttributeDefinition definition) {
        return switch (definition.rarity()) {
            case COMMON -> 75;
            case UNCOMMON -> 150;
            case RARE -> 300;
            case EPIC -> 500;
            case LEGENDARY -> 1000;
        };
    }
}
