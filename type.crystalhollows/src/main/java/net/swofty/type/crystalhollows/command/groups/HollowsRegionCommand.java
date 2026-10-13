package net.swofty.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.type.crystalhollows.command.HollowsSupport;
import net.swofty.type.crystalhollows.layout.HollowsRegions;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CommandParameters(description = "Captures a Crystal Hollows region box in two steps (run once per corner)",
        labels = "region",
        usage = "/hollows region set <regionId>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsRegionCommand extends HypixelCommand {
    private static final int DEFAULT_MIN_Y = 31;
    private static final int DEFAULT_MAX_Y = 188;
    private static final Map<UUID, Corner> PENDING = new ConcurrentHashMap<>();

    private record Corner(String regionId, int x, int z) {
    }

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord region = HollowsSupport.suggesting("regionId", HollowsRegions::ids);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String id = context.get(region);
            RegionType type = HollowsRegions.zones().get(id);
            if (type == null) {
                player.sendMessage("<c>Unknown Crystal Hollows region <e>{}<c>. Press Tab to see them all.", id);
                return;
            }
            Pos feet = player.getPosition();
            Corner first = PENDING.get(player.getUuid());
            if (first == null || !first.regionId().equals(id)) {
                PENDING.put(player.getUuid(), new Corner(id, feet.blockX(), feet.blockZ()));
                player.sendMessage("<a>Corner 1 of <e>{}<a> captured at <e>{} {}<a>. Walk to the opposite corner and run the command again.", id, feet.blockX(), feet.blockZ());
                return;
            }
            PENDING.remove(player.getUuid());
            HollowsRegions.Box box = new HollowsRegions.Box(first.x(), first.z(), feet.blockX(), feet.blockZ()).normalised();
            save(id, type, box);
            player.sendMessage("<a>Region <e>{}<a> set to x <e>{}..{}<a>, z <e>{}..{}<a> (height range kept).", id, box.x1(), box.x2(), box.z1(), box.z2());
        }, ArgumentType.Literal("set"), region);
    }

    private static void save(String id, RegionType type, HollowsRegions.Box box) {
        SkyBlockRegion existing = SkyBlockRegion.getFromID(id);
        int minY = existing == null ? DEFAULT_MIN_Y : Math.min(existing.getFirstLocation().blockY(), existing.getSecondLocation().blockY());
        int maxY = existing == null ? DEFAULT_MAX_Y : Math.max(existing.getFirstLocation().blockY(), existing.getSecondLocation().blockY());
        SkyBlockRegion updated = new SkyBlockRegion(id, new Pos(box.x1(), minY, box.z1()), new Pos(box.x2(), maxY, box.z2()),
                type, ServerType.SKYBLOCK_CRYSTAL_HOLLOWS);
        updated.save();
    }
}
