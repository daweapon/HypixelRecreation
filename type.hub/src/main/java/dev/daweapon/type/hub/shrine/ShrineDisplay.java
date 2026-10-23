package dev.daweapon.type.hub.shrine;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.technoblade.ShrineConfig;
import dev.daweapon.type.skyblockgeneric.technoblade.ShrineDialogue;
import dev.daweapon.type.skyblockgeneric.technoblade.ShrineSnapshot;
import dev.daweapon.type.skyblockgeneric.technoblade.TechnobladeShrine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ShrineDisplay {
    private static final String RELIC_TEXTURE = "522dc618e396f96b0ff7bb50135e3cb00b71f0d62f392ecb0f82732ce339f102";
    private static final double WHISPER_RANGE = 8;
    private static final long WHISPER_COOLDOWN_MILLIS = 60_000;
    private static final float HIDDEN_SIZE = 0.8f;

    private final Instance instance;
    private final Map<Integer, List<Entity>> hidden = new HashMap<>();
    private final Map<Integer, List<Entity>> hovering = new HashMap<>();
    private final Map<UUID, Long> whisperedAt = new HashMap<>();
    private HologramEntity title;
    private HologramEntity subtitle;

    private ShrineDisplay(Instance instance) {
        this.instance = instance;
    }

    public static void start(Instance instance) {
        ShrineDisplay display = new ShrineDisplay(instance);
        MinecraftServer.getSchedulerManager().buildTask(display::sync)
                .delay(TaskSchedule.seconds(5)).repeat(TaskSchedule.seconds(2)).schedule();
    }

    private void sync() {
        try {
            ShrineConfig config = TechnobladeShrine.config();
            ShrineSnapshot snapshot = TechnobladeShrine.snapshot();
            boolean standing = TechnobladeShrine.standing();
            for (ShrineConfig.RelicSpot spot : config.relics()) {
                boolean found = snapshot.relicFound(spot.index());
                reconcile(hidden, spot.index(), standing && !found, () -> spawnHidden(spot));
                reconcile(hovering, spot.index(), standing && found,
                        () -> spawnHovering(config, spot.index(), snapshot.relics().get(spot.index())));
            }
            syncTitle(config, snapshot, standing);
            if (standing) whisper(config);
        } catch (RuntimeException exception) {
            org.tinylog.Logger.error(exception, "Blood God Shrine display failed");
        }
    }

    private void reconcile(Map<Integer, List<Entity>> spawned, int index, boolean wanted, java.util.function.Supplier<List<Entity>> spawn) {
        List<Entity> current = spawned.get(index);
        if (wanted && current == null) {
            spawned.put(index, spawn.get());
        } else if (!wanted && current != null) {
            current.forEach(Entity::remove);
            spawned.remove(index);
        }
    }

    private List<Entity> spawnHidden(ShrineConfig.RelicSpot spot) {
        List<Entity> entities = new ArrayList<>();
        entities.add(head(spot.position().add(0, 0.4, 0), 0.5f));
        InteractionEntity click = new InteractionEntity(HIDDEN_SIZE, HIDDEN_SIZE, (player, _) ->
                TechnobladeShrine.claimRelic((dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer) player, spot.index()));
        click.setInstance(instance, spot.position());
        entities.add(click);
        return entities;
    }

    private List<Entity> spawnHovering(ShrineConfig config, int index, String finder) {
        List<Entity> entities = new ArrayList<>();
        Pos position = config.relicHoverPosition(index);
        entities.add(head(position, 0.6f));
        HologramEntity name = new HologramEntity(Text.of("<d>Relic #{} <8>- <e>{}", index + 1, finder));
        name.setInstance(instance, position.add(0, 0.6, 0));
        entities.add(name);
        return entities;
    }

    private Entity head(Pos position, float scale) {
        Entity display = new Entity(EntityType.ITEM_DISPLAY);
        display.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(ItemStacks.head(RELIC_TEXTURE, Text.of("<d>Shiny Relic"), List.of()).build());
            meta.setDisplayContext(ItemDisplayMeta.DisplayContext.HEAD);
            meta.setScale(new Vec(scale, scale, scale));
            meta.setHasNoGravity(true);
        });
        display.setNoGravity(true);
        display.setInstance(instance, position);
        return display;
    }

    private void syncTitle(ShrineConfig config, ShrineSnapshot snapshot, boolean standing) {
        if (!standing) {
            if (title != null) {
                title.remove();
                subtitle.remove();
                title = null;
                subtitle = null;
            }
            return;
        }
        Text progress = TechnobladeShrine.complete()
                ? Text.of("<6>The Ancient Pig watches over the Hub")
                : Text.of("<7>Relics found: <d>{}<7>/<d>{}", snapshot.relics().size(), TechnobladeShrine.relicCount());
        if (title == null) {
            title = new HologramEntity(Text.of("<d><l>BLOOD GOD SHRINE"));
            title.setInstance(instance, config.shrine().add(0, 2.4, 0));
            subtitle = new HologramEntity(progress);
            subtitle.setInstance(instance, config.shrine().add(0, 2.1, 0));
            return;
        }
        subtitle.setText(progress);
    }

    private void whisper(ShrineConfig config) {
        long now = System.currentTimeMillis();
        SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
            if (player.getInstance() != instance || player.getPosition().distance(config.shrine()) > WHISPER_RANGE) return;
            if (now - whisperedAt.getOrDefault(player.getUuid(), 0L) < WHISPER_COOLDOWN_MILLIS) return;
            whisperedAt.put(player.getUuid(), now);
            if (!TechnobladeShrine.complete()) player.sendMessage(ShrineDialogue.whisper());
        });
    }
}
