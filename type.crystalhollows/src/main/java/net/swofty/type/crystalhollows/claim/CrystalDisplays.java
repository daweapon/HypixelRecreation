package net.swofty.type.crystalhollows.claim;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusConfig;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class CrystalDisplays {
    public static final double VIEW_DISTANCE = 64;
    private static final float MODEL_SCALE = 2.5f;
    private static final float HITBOX_SIZE = 2.5f;
    private static final int REFRESH_TICKS = 10;

    public enum Kind {
        CLAIM,
        PEDESTAL
    }

    private record Key(Instance instance, NucleusCrystal crystal, Kind kind) {
    }

    private static final class Display {
        private final Entity model;
        private final InteractionEntity hitbox;
        private final CompletableFuture<Void> ready;
        private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

        private Display(Entity model, InteractionEntity hitbox, CompletableFuture<Void> ready) {
            this.model = model;
            this.hitbox = hitbox;
            this.ready = ready;
        }
    }

    private static final Map<Key, Display> DISPLAYS = new ConcurrentHashMap<>();
    private static boolean started;

    private CrystalDisplays() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(CrystalDisplays::tick)
                .repeat(TaskSchedule.tick(REFRESH_TICKS)).schedule();
    }

    public static void refresh(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        for (NucleusCrystal crystal : NucleusCrystal.values()) {
            update(player, instance, crystal, Kind.CLAIM, claimPosition(crystal), CrystalClaims.collectable(player, crystal));
            update(player, instance, crystal, Kind.PEDESTAL, pedestalPosition(crystal),
                    CrystalNucleus.state(player, crystal) == CrystalNucleus.State.PLACED);
        }
    }

    public static void reset() {
        DISPLAYS.values().forEach(display -> {
            display.model.remove();
            if (display.hitbox != null) display.hitbox.remove();
        });
        DISPLAYS.clear();
    }

    public static void clear(UUID player) {
        DISPLAYS.values().forEach(display -> display.viewers.remove(player));
    }

    private static void tick() {
        try {
            List<SkyBlockPlayer> players = SkyBlockGenericLoader.getLoadedPlayers();
            Set<UUID> online = players.stream().map(SkyBlockPlayer::getUuid).collect(Collectors.toSet());
            DISPLAYS.values().forEach(display -> display.viewers.retainAll(online));
            players.forEach(CrystalDisplays::refresh);
        } catch (RuntimeException exception) {
            org.tinylog.Logger.error(exception, "Crystal display refresh failed");
        }
    }

    private static Pos claimPosition(NucleusCrystal crystal) {
        ClaimSpot spot = ClaimSpots.of(crystal);
        return spot == null ? null : spot.crystalPosition();
    }

    private static Pos pedestalPosition(NucleusCrystal crystal) {
        Vec at = NucleusConfig.get().pedestal(crystal);
        return at == null ? null : new Pos(at.x(), at.y(), at.z());
    }

    private static void update(SkyBlockPlayer player, Instance instance, NucleusCrystal crystal, Kind kind, Pos position,
                               boolean wanted) {
        Key key = new Key(instance, crystal, kind);
        if (position == null) return;
        boolean shown = wanted && player.getPosition().distance(position) <= VIEW_DISTANCE;
        Display display = DISPLAYS.get(key);
        if (!shown) {
            if (display != null && display.viewers.remove(player.getUuid())) hide(display, player);
            return;
        }
        if (display == null) display = DISPLAYS.computeIfAbsent(key, _ -> create(instance, crystal, kind, position));
        if (!display.viewers.add(player.getUuid())) return;
        Display target = display;
        target.ready.thenRun(() -> show(target, player));
        if (kind == Kind.CLAIM) ClaimWalls.open(player, instance, ClaimSpots.of(crystal));
    }

    private static void show(Display display, SkyBlockPlayer player) {
        if (!display.viewers.contains(player.getUuid())) return;
        display.model.addViewer(player);
        if (display.hitbox != null) display.hitbox.addViewer(player);
    }

    private static void hide(Display display, SkyBlockPlayer player) {
        display.model.removeViewer(player);
        if (display.hitbox != null) display.hitbox.removeViewer(player);
    }

    private static Display create(Instance instance, NucleusCrystal crystal, Kind kind, Pos position) {
        Entity model = new Entity(EntityType.ITEM_DISPLAY);
        model.setNoGravity(true);
        model.setAutoViewable(false);
        model.setGlowing(true);
        model.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(ItemStack.builder(Material.PAPER).set(DataComponents.ITEM_MODEL, crystal.itemModel()).build());
            meta.setScale(new Vec(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE));
            meta.setGlowColorOverride(crystal.getGlowColour().value());
            meta.setViewRange(2f);
        });
        CompletableFuture<Void> ready = model.setInstance(instance, position);
        InteractionEntity hitbox = null;
        if (kind == Kind.CLAIM) {
            hitbox = new InteractionEntity(HITBOX_SIZE, HITBOX_SIZE, (player, _) -> CrystalClaims.claim((SkyBlockPlayer) player, crystal));
            hitbox.setAutoViewable(false);
            CompletableFuture<Void> hitboxReady = hitbox.setInstance(instance, position.sub(0, HITBOX_SIZE / 2.0, 0));
            ready = ready.thenCombine(hitboxReady, (_, _) -> null);
        }
        return new Display(model, hitbox, ready);
    }
}
