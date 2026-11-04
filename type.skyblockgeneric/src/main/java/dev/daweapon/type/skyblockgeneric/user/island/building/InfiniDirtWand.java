package dev.daweapon.type.skyblockgeneric.user.island.building;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

public final class InfiniDirtWand {
    public static final String ABILITY_ID = "PLACE_DIRT";
    public static final int COIN_COST = 1;

    private InfiniDirtWand() {
    }

    public static void register() {
        AbilityRegistry.register(new RegisteredAbility(
                ABILITY_ID,
                "Place Dirt",
                (_, _) -> "<7>Place a dirt block. Costs <6>" + COIN_COST + " coin<7>.",
                RegisteredAbility.AbilityActivation.RIGHT_CLICK_BLOCK,
                0,
                new RegisteredAbility.NoAbilityCost(),
                InfiniDirtWand::place));
    }

    public static boolean place(SkyBlockPlayer player, SkyBlockItem wand, Point origin, BlockFace face) {
        if (origin == null || face == null) return false;
        if (!BuildArea.onOwnIsland(player)) {
            player.sendMessage("<c>You can only use this item on your own island!");
            return false;
        }
        Point target = origin.add(BuildArea.offset(face));
        Instance instance = player.getInstance();
        if (!instance.getBlock(target).isAir() || !BuildArea.withinIsland(player, target)) return false;
        if (player.getCoins() < COIN_COST) {
            player.sendMessage("<c>You don't have enough coins to place dirt!");
            return false;
        }
        player.removeCoins(COIN_COST);
        instance.setBlock(target, Block.DIRT.withTag(BuildArea.PLAYER_PLACED, true));
        return true;
    }
}
