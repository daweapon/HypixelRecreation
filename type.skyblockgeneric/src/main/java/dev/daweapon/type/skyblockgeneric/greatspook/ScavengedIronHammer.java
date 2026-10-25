package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

public final class ScavengedIronHammer {
    public static final String HANDLER_ID = "SCAVENGED_IRON_HAMMER";

    private ScavengedIronHammer() {
    }

    public static boolean isPair(String first, String second) {
        return pair(first, second, "DWARVEN_GOLD_HAMMER", "DWARVEN_EMERALD_HAMMER")
                || pair(first, second, "DWARVEN_EMERALD_HAMMER", "DWARVEN_GOLD_HAMMER");
    }

    private static boolean pair(String first, String second, String a, String b) {
        return a.equals(first) && b.equals(second);
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.registerResult(HANDLER_ID, (upgrade, sacrifice) -> new SkyBlockItem(ItemType.DWARVEN_IRON_HAMMER));
        AnvilCombineRegistry.register(HANDLER_ID, new AnvilCombineHandler(
                (upgrade, sacrifice) -> {
                },
                (player, upgrade, sacrifice) -> isPair(upgrade.getAttributeHandler().getTypeAsString(),
                        sacrifice.getAttributeHandler().getTypeAsString())
                        && GreatSpookEvent.isActive() && inGoldMine(),
                (upgrade, sacrifice, player) -> 0,
                null,
                (upgrade, sacrifice) -> {
                    if (!isPair(upgrade.getAttributeHandler().getTypeAsString(),
                            sacrifice.getAttributeHandler().getTypeAsString())) return null;
                    if (!GreatSpookEvent.isActive()) return "The Tyashoi Alchemist is not in the Hub";
                    return inGoldMine() ? null : "Only an Anvil in the Gold Mine can do this";
                }));
    }

    private static boolean inGoldMine() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_GOLD_MINE;
    }
}
