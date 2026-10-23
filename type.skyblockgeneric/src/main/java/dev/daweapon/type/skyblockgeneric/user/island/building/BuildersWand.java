package dev.daweapon.type.skyblockgeneric.user.island.building;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.Material;

import java.util.List;

public final class BuildersWand {
    public static final int LIMIT = 241;

    private BuildersWand() {
    }

    public static boolean extend(SkyBlockPlayer player, SkyBlockItem wand, Point origin, BlockFace face) {
        if (origin == null || face == null) return false;
        if (!BuildArea.onOwnIsland(player)) {
            player.sendMessage("<c>You can only use this item on your own island!");
            return false;
        }
        Instance instance = player.getInstance();
        Block source = instance.getBlock(origin);
        ItemType type = ItemType.fromMaterial(Material.fromKey(source.key()));
        if (type == null || source.isAir() || source.handler() != null) {
            player.sendMessage("<c>You can't extend this block!");
            return false;
        }
        Vec offset = BuildArea.offset(face);
        List<Point> base = BuildArea.facePlane(instance, origin, face, true,
                point -> instance.getBlock(point).compare(source) && instance.getBlock(point.add(offset)).isAir());
        if (base.size() > LIMIT) {
            player.sendMessage("<c>You may not Grand Architect that many blocks! ({}/{})", base.size(), LIMIT);
            return false;
        }
        List<Point> targets = base.stream().map(point -> point.add(offset)).filter(point -> BuildArea.withinIsland(player, point)).toList();
        int available = player.countItem(type) + WandStorage.count(wand, type);
        int placing = Math.min(targets.size(), available);
        if (placing == 0) {
            player.sendMessage("<c>You cannot place any blocks! You do not have enough blocks to place with your Builder's Wand!");
            return false;
        }
        List<Point> placed = targets.subList(0, placing);

        int fromInventory = Math.min(placing, player.countItem(type));
        if (fromInventory > 0) player.takeItem(type, fromInventory);
        if (placing > fromInventory) {
            WandStorage.take(wand, type, placing - fromInventory);
            player.setItemInMainHand(PlayerItemUpdater.playerUpdate(player, wand.getItemStack()).build());
        }

        Block block = source.withTag(BuildArea.PLAYER_PLACED, true);
        placed.forEach(point -> instance.setBlock(point, block));
        BuildUndo.remember(player, new BuildUndo.Entry(BuildUndo.Kind.WAND, List.copyOf(placed), block, null, type, placing, System.currentTimeMillis()));
        player.sendMessage("<e>You built <a>{}<e> blocks! <hover:'<e>Click to undo!'><click:run:'/undobuild wand'><a><l>UNDO</l></click></hover>", placing);
        return true;
    }

    public static boolean openStorage(SkyBlockPlayer player) {
        player.openView(new WandStorageView());
        return true;
    }
}
