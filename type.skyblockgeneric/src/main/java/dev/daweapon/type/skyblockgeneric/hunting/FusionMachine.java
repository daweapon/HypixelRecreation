package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hunting.GUIAttributeFusion;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;

public final class FusionMachine {
    private static final int MIN_X = -607;
    private static final int MAX_X = -605;
    private static final int MIN_Y = 115;
    private static final int MAX_Y = 117;
    private static final int MIN_Z = -16;
    private static final int MAX_Z = -14;

    private static boolean hologramSpawned;

    private FusionMachine() {
    }

    public static void register(Scheduler scheduler) {
        scheduler.scheduleTask(FusionMachine::spawnHologram, TaskSchedule.seconds(1), TaskSchedule.seconds(5), ExecutionType.TICK_END);
    }

    public static boolean handle(SkyBlockPlayer player, Point block) {
        if (!onGalatea() || !contains(block)) return false;
        if (!HotfService.isUnlocked(player)) {
            player.sendMessage("<c>The Fusion Machine does not respond. Talk to Hina to get a Heart of the Forest first!");
            return true;
        }
        player.openView(new GUIAttributeFusion());
        return true;
    }

    static boolean contains(Point block) {
        return block.blockX() >= MIN_X && block.blockX() <= MAX_X && block.blockY() >= MIN_Y && block.blockY() <= MAX_Y
                && block.blockZ() >= MIN_Z && block.blockZ() <= MAX_Z;
    }

    private static boolean onGalatea() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_GALATEA;
    }

    private static void spawnHologram() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (hologramSpawned || instance == null || !onGalatea()) return;
        hologramSpawned = true;
        new HologramEntity("<d><l>FUSION MACHINE").setInstance(instance, new Pos(-604.5, 117.9, -14.5));
        new HologramEntity("<e><l>Click to view!").setInstance(instance, new Pos(-604.5, 117.55, -14.5));
    }
}
