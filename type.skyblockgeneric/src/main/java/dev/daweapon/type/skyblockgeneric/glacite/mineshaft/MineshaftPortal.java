package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.entity.TextDisplayEntity;
import dev.daweapon.type.skyblockgeneric.gui.inventories.mineshaft.GUIMineshaftPortal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MineshaftPortal {
    public static final String SPAWN_MESSAGE = "<b><l>MINESHAFT! <r><f>A mineshaft portal spawned nearby!";
    private static final double DISTANCE = 3;
    private static final Map<UUID, MineshaftPortal> ACTIVE = new ConcurrentHashMap<>();

    private final UUID owner;
    private final ShaftLayout layout;
    private final InteractionEntity hitbox;
    private final TextDisplayEntity title;
    private final TextDisplayEntity prompt;
    private final Task despawn;

    private MineshaftPortal(SkyBlockPlayer owner, ShaftLayout layout, Pos position) {
        this.owner = owner.getUuid();
        this.layout = layout;
        this.hitbox = new InteractionEntity(1.6f, 2.6f, (clicker, _) -> click((SkyBlockPlayer) clicker));
        this.title = new TextDisplayEntity("<b><l>MINESHAFT PORTAL", meta -> {});
        this.prompt = new TextDisplayEntity("<e>Click to open!", meta -> {});
        place(owner, hitbox, position);
        place(owner, title, position.add(0, 2.9, 0));
        place(owner, prompt, position.add(0, 2.5, 0));
        this.despawn = MinecraftServer.getSchedulerManager().buildTask(this::remove)
                .delay(TaskSchedule.duration(Duration.ofMillis(MineshaftService.PORTAL_MILLIS))).schedule();
    }

    private static void place(SkyBlockPlayer viewer, Entity entity, Pos position) {
        entity.setAutoViewable(false);
        entity.setInstance(viewer.getInstance(), position).thenRun(() -> entity.addViewer(viewer));
    }

    public static boolean isActive(UUID player) {
        return ACTIVE.containsKey(player);
    }

    public static void spawn(SkyBlockPlayer player, ShaftLayout layout) {
        if (player.getInstance() == null) return;
        MineshaftPortal previous = ACTIVE.remove(player.getUuid());
        if (previous != null) previous.discard();
        Pos position = player.getPosition().add(player.getPosition().direction().withY(0).normalize().mul(DISTANCE));
        ACTIVE.put(player.getUuid(), new MineshaftPortal(player, layout, position));
        player.sendMessage(SPAWN_MESSAGE);
    }

    public static void remove(UUID player) {
        MineshaftPortal portal = ACTIVE.remove(player);
        if (portal != null) portal.discard();
    }

    private void remove() {
        ACTIVE.remove(owner, this);
        discard();
    }

    private void discard() {
        despawn.cancel();
        hitbox.remove();
        title.remove();
        prompt.remove();
    }

    private void click(SkyBlockPlayer player) {
        if (!player.getUuid().equals(owner)) return;
        player.openView(new GUIMineshaftPortal(clicker -> MineshaftService.enterFromPortal(clicker, layout)));
    }
}
