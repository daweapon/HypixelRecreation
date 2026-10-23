package dev.daweapon.type.skyblockgeneric.user.island.building;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BuildUndo {
    private static final long EXPIRY_MILLIS = 60_000;
    private static final Map<UUID, Entry> LAST = new ConcurrentHashMap<>();

    private BuildUndo() {
    }

    public enum Kind {
        WAND, ZAP
    }

    public record Entry(Kind kind, List<Point> positions, Block placed, Block removed, ItemType item, int amount, long at) {
    }

    public static void remember(SkyBlockPlayer player, Entry entry) {
        LAST.put(player.getUuid(), entry);
    }

    public static boolean undo(SkyBlockPlayer player, Kind kind) {
        Entry entry = LAST.get(player.getUuid());
        if (entry == null || entry.kind() != kind) {
            player.sendMessage(kind == Kind.ZAP ? "<c>You have nothing to unzap!" : "<c>You have nothing to undo!");
            return false;
        }
        LAST.remove(player.getUuid());
        if (System.currentTimeMillis() - entry.at() > EXPIRY_MILLIS) {
            player.sendMessage(kind == Kind.ZAP ? "<c>The undo on this zap has expired!" : "<c>The undo on this build has expired!");
            return false;
        }
        if (!BuildArea.onOwnIsland(player)) {
            player.sendMessage("<c>You can only undo this on your own island!");
            return false;
        }
        if (kind == Kind.WAND) {
            int undone = 0;
            for (Point point : entry.positions()) {
                if (!player.getInstance().getBlock(point).compare(entry.placed())) continue;
                player.getInstance().setBlock(point, Block.AIR);
                undone++;
            }
            player.addAndUpdateItem(entry.item(), undone);
            player.sendMessage("<e>Undid latest Grand Architect use of <c>{} <e>blocks!", undone);
        } else {
            int restored = 0;
            for (Point point : entry.positions()) {
                if (!player.getInstance().getBlock(point).isAir()) continue;
                player.getInstance().setBlock(point, entry.removed());
                restored++;
            }
            player.takeItem(entry.item(), restored);
            player.sendMessage("<e>Unzapped <c>{} <e>blocks away!", restored);
        }
        return true;
    }
}
