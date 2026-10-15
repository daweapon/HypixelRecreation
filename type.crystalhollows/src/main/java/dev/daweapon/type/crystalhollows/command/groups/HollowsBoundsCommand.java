package dev.daweapon.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.crystalhollows.layout.HollowsRegions;
import dev.daweapon.type.crystalhollows.layout.HollowsWorld;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CommandParameters(description = "Captures the world bounds in two steps (run once per corner)",
        labels = "bounds",
        usage = "/hollows bounds set",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsBoundsCommand extends HypixelCommand {
    private static final Map<UUID, int[]> PENDING = new ConcurrentHashMap<>();

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            int x = player.getPosition().blockX();
            int z = player.getPosition().blockZ();
            int[] first = PENDING.remove(player.getUuid());
            if (first == null) {
                PENDING.put(player.getUuid(), new int[]{x, z});
                player.sendMessage("<a>Bounds corner 1 captured at <e>{} {}<a>. Walk to the opposite corner and run the command again.", x, z);
                return;
            }
            HollowsRegions.Box box = new HollowsRegions.Box(first[0], first[1], x, z).normalised();
            HollowsWorld.get().withBounds(new HollowsWorld.Bounds(box.x1(), box.z1(), box.x2(), box.z2())).write(HollowsWorld.FILE);
            HollowsWorld.reload();
            player.sendMessage("<a>World bounds set to x <e>{}..{}<a>, z <e>{}..{}<a>.", box.x1(), box.x2(), box.z1(), box.z2());
        }, ArgumentType.Literal("set"));
    }
}
