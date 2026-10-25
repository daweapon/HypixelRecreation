package dev.daweapon.type.skyblockgeneric.greatspook;

import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FreeSpiders {
    public static final String HAMMER_ID = "DWARVEN_IRON_HAMMER";
    private static final Map<UUID, Long> LAST_FREED = new ConcurrentHashMap<>();

    private FreeSpiders() {
    }

    public static boolean hit(SkyBlockPlayer player, Block block, int x, int y, int z) {
        if (!GreatSpookEvent.isActive() || !block.compare(Block.IRON_BARS) || !SpookNpcIngredients.inCage(x, y, z)) return false;
        if (!HAMMER_ID.equals(WornArmor.id(player.getItemInMainHand()))) return false;
        long now = System.currentTimeMillis();
        long left = SpookNpcIngredients.cooldownLeft(now, LAST_FREED.getOrDefault(player.getUuid(), 0L));
        if (left > 0) {
            player.sendMessage("<c>The spiders are still shaking the bars! Try again in a few seconds.");
            return true;
        }
        LAST_FREED.put(player.getUuid(), now);
        SkyBlockItem spider = new SkyBlockItem(ItemType.FREE_SPIDER);
        player.addAndUpdateItem(spider);
        player.sendMessage("<7>You smashed the bars and freed a <a>Trapped Spider<7>! You received <a>1x {}<7>!", spider.getDisplayName());
        return true;
    }
}
