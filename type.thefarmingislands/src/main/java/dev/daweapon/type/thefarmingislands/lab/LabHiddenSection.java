package dev.daweapon.type.thefarmingislands.lab;

import dev.daweapon.type.skyblockgeneric.farming.lab.LabQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The bookshelf door between Jake's workshop and the hidden part of the lab. In the captured world the door is shut
 * (bookshelves at x 178, y 28-30, z -500..-499) with the stuck lever (177, 29, -498) outside it and a second lever
 * (179, 29, -500) in the alcove behind it, from which stairs lead down to the Crop Analyzer. Pulling the stuck lever
 * opens the door for that player only; the inside lever shuts and reopens it from within.
 */
public final class LabHiddenSection {
    public static final Pos INSIDE_LEVER = new Pos(179.5, 29, -499.5);
    private static final List<Vec> DOOR = List.of(
            new Vec(178, 28, -500), new Vec(178, 28, -499), new Vec(178, 29, -500), new Vec(178, 29, -499));
    private static final Vec STUCK_LEVER_BLOCK = new Vec(177, 29, -498);
    private static final Vec INSIDE_LEVER_BLOCK = new Vec(179, 29, -500);
    private static final Set<UUID> OPEN = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> SHUT_FROM_INSIDE = ConcurrentHashMap.newKeySet();

    private LabHiddenSection() {
    }

    public static boolean unlocked(SkyBlockPlayer player) {
        return LabQuest.stage(player).compareTo(LabQuest.Stage.LAB2_LEVER) >= 0;
    }

    public static void sync(SkyBlockPlayer player) {
        if (!unlocked(player) || SHUT_FROM_INSIDE.contains(player.getUuid())) return;
        if (OPEN.add(player.getUuid())) open(player);
    }

    public static void leftLab(SkyBlockPlayer player) {
        OPEN.remove(player.getUuid());
        SHUT_FROM_INSIDE.remove(player.getUuid());
    }

    public static void openWithStuckLever(SkyBlockPlayer player) {
        OPEN.add(player.getUuid());
        SHUT_FROM_INSIDE.remove(player.getUuid());
        open(player);
        player.playSound(Sound.sound(Key.key("block.piston.contract"), Sound.Source.BLOCK, 1f, 0.8f), Sound.Emitter.self());
    }

    public static void pullInsideLever(SkyBlockPlayer player) {
        if (!unlocked(player)) return;
        player.playSound(Sound.sound(Key.key("block.lever.click"), Sound.Source.BLOCK, 1f, 1f), Sound.Emitter.self());
        if (SHUT_FROM_INSIDE.remove(player.getUuid())) {
            OPEN.add(player.getUuid());
            open(player);
            return;
        }
        SHUT_FROM_INSIDE.add(player.getUuid());
        OPEN.remove(player.getUuid());
        shut(player);
    }

    private static void open(SkyBlockPlayer player) {
        for (Vec block : DOOR) player.sendPacket(new BlockChangePacket(block, Block.AIR));
        setLever(player, STUCK_LEVER_BLOCK, true);
        setLever(player, INSIDE_LEVER_BLOCK, false);
    }

    private static void shut(SkyBlockPlayer player) {
        for (Vec block : DOOR) player.sendPacket(new BlockChangePacket(block, Block.BOOKSHELF));
        setLever(player, INSIDE_LEVER_BLOCK, true);
    }

    private static void setLever(SkyBlockPlayer player, Vec position, boolean powered) {
        if (player.getInstance() == null) return;
        Block lever = player.getInstance().getBlock(position);
        if (!lever.compare(Block.LEVER)) return;
        player.sendPacket(new BlockChangePacket(position, lever.withProperty("powered", String.valueOf(powered))));
    }
}
