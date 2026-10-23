package dev.daweapon.type.skyblockgeneric.user.island.building;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Set;

public final class BlockZapper {
    public static final int LIMIT = 164;
    private static final Set<Block> CROPS = Set.of(Block.WHEAT, Block.CARROTS, Block.POTATOES, Block.BEETROOTS, Block.NETHER_WART,
            Block.MELON_STEM, Block.PUMPKIN_STEM, Block.ATTACHED_MELON_STEM, Block.ATTACHED_PUMPKIN_STEM, Block.SUGAR_CANE,
            Block.CACTUS, Block.COCOA, Block.SWEET_BERRY_BUSH, Block.BROWN_MUSHROOM, Block.RED_MUSHROOM, Block.MELON, Block.PUMPKIN);

    private BlockZapper() {
    }

    public static boolean zap(SkyBlockPlayer player, SkyBlockItem zapper, Point origin, BlockFace face) {
        if (origin == null || face == null) return false;
        if (!BuildArea.onOwnIsland(player)) {
            player.sendMessage("<c>You cannot use this item on a public island!");
            return false;
        }
        Instance instance = player.getInstance();
        Block source = instance.getBlock(origin);
        if (!zappable(instance, origin, source)) {
            player.sendMessage(BuildArea.isPlayerPlaced(source) || source.isAir() ? "<c>Can't zap this!" : "<c>You can only zap blocks you've placed!");
            return false;
        }
        boolean singleOnly = source.compare(Block.TERRACOTTA) || source.name().endsWith("_terracotta");
        List<Point> targets = singleOnly ? List.of(origin) : BuildArea.facePlane(instance, origin, face, false,
                point -> instance.getBlock(point).compare(source) && zappable(instance, point, instance.getBlock(point)));
        List<Point> zapped = targets.subList(0, Math.min(LIMIT, targets.size()));

        Material drop = source.compare(Block.MYCELIUM) ? Material.DIRT : Material.fromKey(source.key());
        ItemType type = drop == null ? null : ItemType.fromMaterial(drop);
        if (type == null) {
            player.sendMessage("<c>Can't zap this!");
            return false;
        }
        Block restore = source;
        zapped.forEach(point -> instance.setBlock(point, Block.AIR));
        player.addAndUpdateItem(type, zapped.size());
        BuildUndo.remember(player, new BuildUndo.Entry(BuildUndo.Kind.ZAP, List.copyOf(zapped), null, restore, type, zapped.size(), System.currentTimeMillis()));
        player.sendMessage("<e>Zapped <a>{} <e>blocks! <hover:'<e>Click to undo!'><click:run:'/undobuild zap'><a><l>UNDO</l></click></hover>", zapped.size());
        return true;
    }

    private static boolean zappable(Instance instance, Point point, Block block) {
        if (block.isAir() || !BuildArea.isPlayerPlaced(block)) return false;
        if (block.handler() != null || block.name().contains("prismarine")) return false;
        if (CROPS.stream().anyMatch(crop -> crop.compare(block))) return false;
        Block above = instance.getBlock(point.add(new Vec(0, 1, 0)));
        return CROPS.stream().noneMatch(crop -> crop.compare(above));
    }
}
