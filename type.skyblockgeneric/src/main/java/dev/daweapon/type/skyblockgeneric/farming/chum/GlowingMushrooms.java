package dev.daweapon.type.skyblockgeneric.farming.chum;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GlowingMushrooms {
    public static final double GLOW_CHANCE = 12;
    private static final int RESPAWN_TICKS = 20 * 30;
    private static final Map<Long, Boolean> DEPLETED = new ConcurrentHashMap<>();

    private GlowingMushrooms() {
    }

    public static boolean handle(SkyBlockPlayer player, Point position, Block block) {
        if (player.getSkyblockDataHandler() == null) return false;
        boolean red = block.compare(Block.RED_MUSHROOM);
        if (!red && !block.compare(Block.BROWN_MUSHROOM)) return false;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(position);
        if (region == null || region.getType() != RegionType.GLOWING_MUSHROOM_CAVE) return false;
        Instance instance = player.getInstance();
        if (instance == null) return true;
        long key = ((long) position.blockX() & 0x3FFFFFFL) << 38 | ((long) position.blockZ() & 0x3FFFFFFL) << 12 | (position.blockY() & 0xFFFL);
        if (DEPLETED.putIfAbsent(key, Boolean.TRUE) != null) return true;
        instance.setBlock(position, Block.AIR);
        ScheduleUtility.delay(() -> {
            instance.setBlock(position, block);
            DEPLETED.remove(key);
        }, RESPAWN_TICKS);
        harvest(player, position, red ? ItemType.RED_MUSHROOM : ItemType.BROWN_MUSHROOM, red ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
        return true;
    }

    private static void harvest(SkyBlockPlayer player, Point position, ItemType mushroom, Material source) {
        deliver(player, mushroom, 1);
        List<SkyBlockItem> drops = new java.util.ArrayList<>();
        drops.add(new SkyBlockItem(mushroom, 1));
        if (LootRoll.roll(player.getUuid(), ItemType.GLOWING_MUSHROOM, GLOW_CHANCE)) {
            int amount = shears(player) ? 2 : 1;
            deliver(player, ItemType.GLOWING_MUSHROOM, amount);
            drops.add(new SkyBlockItem(ItemType.GLOWING_MUSHROOM, amount));
            player.sendMessage("<a>The mushroom was glowing! <8>+{} Glowing Mushroom", amount);
        }
        HypixelEventHandler.callCustomEvent(new CustomBlockBreakEvent(player, source, position, drops, false));
    }

    private static boolean shears(SkyBlockPlayer player) {
        return new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType() == ItemType.MOBYS_SHEARS;
    }

    private static void deliver(SkyBlockPlayer player, ItemType item, int amount) {
        if (player.canInsertItemIntoSacks(item, amount)) {
            player.getSackItems().increase(item, amount);
            return;
        }
        player.addAndUpdateItem(item, amount);
    }
}
