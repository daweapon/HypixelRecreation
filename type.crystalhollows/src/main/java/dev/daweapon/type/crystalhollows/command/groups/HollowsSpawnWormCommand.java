package dev.daweapon.type.crystalhollows.command.groups;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobScatha;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobStoneworm;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Spawns a Stoneworm or a Scatha at your feet",
        labels = "spawnworm",
        usage = "/hollows spawnworm [stoneworm|scatha]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsSpawnWormCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord kind = ArgumentType.Word("kind").from("stoneworm", "scatha");
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            spawn((SkyBlockPlayer) sender, false);
        });
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            spawn((SkyBlockPlayer) sender, "scatha".equals(context.get(kind)));
        }, kind);
    }

    private static void spawn(SkyBlockPlayer player, boolean scatha) {
        SkyBlockMob worm = scatha ? new MobScatha() : new MobStoneworm();
        worm.setInstance(player.getInstance(), player.getPosition());
        player.sendMessage("<a>Spawned a <e>{}<a>.", scatha ? "Scatha" : "Stoneworm");
    }
}
