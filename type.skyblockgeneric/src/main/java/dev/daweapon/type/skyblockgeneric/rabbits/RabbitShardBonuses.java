package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class RabbitShardBonuses {
    private static final AttributeId LEGENDARY_LUCK = AttributeId.parse("U106");
    private static final AttributeId MYTHIC_LUCK = AttributeId.parse("R94");
    private static final AttributeId DIVINE_LUCK = AttributeId.parse("E46");
    private static final AttributeId HITMAN_COOLDOWN = AttributeId.parse("C22");
    private static final AttributeId RABBIT_YOUTH = AttributeId.parse("C48");
    private static final AttributeId CHOCOLATE_SPEED = AttributeId.parse("C61");
    private static final AttributeId RABBIT_CREW = AttributeId.parse("R38");
    private static final AttributeId EGG_FINDER = AttributeId.parse("L10");

    private RabbitShardBonuses() {
    }

    private static double value(SkyBlockPlayer player, AttributeId id) {
        if (player == null || player.getSkyblockDataHandler() == null) return 0;
        return AttributeEffectService.value(player.getHuntingData(), id);
    }

    public static double legendaryLuck(SkyBlockPlayer player) {
        return value(player, LEGENDARY_LUCK);
    }

    public static double mythicLuck(SkyBlockPlayer player) {
        return value(player, MYTHIC_LUCK);
    }

    public static double divineLuck(SkyBlockPlayer player) {
        return value(player, DIVINE_LUCK);
    }

    public static double hitmanCooldownPercent(SkyBlockPlayer player) {
        return value(player, HITMAN_COOLDOWN);
    }

    public static int rabbitYouth(SkyBlockPlayer player) {
        return (int) value(player, RABBIT_YOUTH);
    }

    public static double chocolateSpeed(SkyBlockPlayer player) {
        return value(player, CHOCOLATE_SPEED) / 100D;
    }

    public static double rabbitCrew(SkyBlockPlayer player) {
        return value(player, RABBIT_CREW);
    }

    public static double eggFinderSeconds(SkyBlockPlayer player) {
        return value(player, EGG_FINDER);
    }
}
