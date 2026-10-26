package dev.daweapon.type.skyblockgeneric.hunting;

public final class HuntingXp {
    private static final double CHARM_MULTIPLIER = 10;
    private static final double FUSION_MULTIPLIER = 0.25;
    private static final double REDUCED_SHARD_MULTIPLIER = 0.5;

    private HuntingXp() {
    }

    public static double hunt(AttributeDefinition definition) {
        return base(definition.rarity()) * shardMultiplier(definition);
    }

    public static double charm(AttributeDefinition definition) {
        return hunt(definition) * CHARM_MULTIPLIER;
    }

    public static double fusion(AttributeDefinition definition) {
        return base(definition.rarity()) * FUSION_MULTIPLIER;
    }

    private static double shardMultiplier(AttributeDefinition definition) {
        return definition.shard().equals("Glacite Walker") || definition.shard().equals("Ghost")
                ? REDUCED_SHARD_MULTIPLIER : 1;
    }

    private static double base(AttributeRarity rarity) {
        return switch (rarity) {
            case COMMON -> 75;
            case UNCOMMON -> 150;
            case RARE -> 300;
            case EPIC -> 500;
            case LEGENDARY -> 1000;
        };
    }
}
