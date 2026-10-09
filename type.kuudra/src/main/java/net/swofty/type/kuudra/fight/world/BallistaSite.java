package net.swofty.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.kuudra.fight.BallistaBuild;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The Ballista site: the six drop-off piles with their holograms, the Ballista itself (display entities that
 * appear as the build progresses) and its seat. Its model faces north until someone rides it; then it turns with
 * the rider's look.
 */
public final class BallistaSite {
    private record Part(double x, double y, double z, Block block, double sx, double sy, double sz) {
    }

    private static final List<Part> MODEL = List.of(
            new Part(0, 0, 0, Block.DARK_OAK_PLANKS, 3.2, 0.4, 4.2),
            new Part(-1.3, 0.4, -1.6, Block.STRIPPED_DARK_OAK_LOG, 0.5, 1.4, 0.5),
            new Part(1.3, 0.4, -1.6, Block.STRIPPED_DARK_OAK_LOG, 0.5, 1.4, 0.5),
            new Part(-1.3, 0.4, 1.6, Block.STRIPPED_DARK_OAK_LOG, 0.5, 1.4, 0.5),
            new Part(1.3, 0.4, 1.6, Block.STRIPPED_DARK_OAK_LOG, 0.5, 1.4, 0.5),
            new Part(0, 1.8, 0, Block.SPRUCE_PLANKS, 2.8, 0.3, 3.6),
            new Part(0, 2.1, 0.4, Block.DARK_OAK_LOG, 0.7, 0.7, 4.6),
            new Part(0, 2.1, -2.2, Block.POLISHED_BLACKSTONE, 1.0, 0.9, 0.6),
            new Part(-1.4, 2.4, -1.6, Block.SPRUCE_LOG, 2.4, 0.35, 0.35),
            new Part(1.4, 2.4, -1.6, Block.SPRUCE_LOG, 2.4, 0.35, 0.35),
            new Part(-2.7, 2.4, -1.2, Block.SPRUCE_LOG, 0.35, 0.35, 0.9),
            new Part(2.7, 2.4, -1.2, Block.SPRUCE_LOG, 0.35, 0.35, 0.9),
            new Part(0, 2.55, -0.3, Block.COBWEB, 5.2, 0.12, 0.12),
            new Part(0, 2.85, 0.4, Block.IRON_BARS, 0.3, 0.3, 4.0),
            new Part(0, 2.8, -2.5, Block.IRON_BLOCK, 0.5, 0.5, 0.5),
            new Part(0, 1.2, 2.3, Block.ANVIL, 1.0, 0.8, 1.0));

    private final Instance instance;
    private final Pos base;
    private final Map<String, Pos> piles;
    private final Map<String, Entity> pileLabels = new LinkedHashMap<>();
    private final Map<String, Entity> pileCrates = new LinkedHashMap<>();
    private final Map<String, Entity> pileHitboxes = new LinkedHashMap<>();
    private static final float NORTH = 180;
    private static final float TURN_STEP = 1;
    private static final Vec SEAT = new Vec(0, 1.4, 2.6);
    private static final Vec MUZZLE = new Vec(0, 2.8, -2.5);

    private final List<Entity> parts = new ArrayList<>();
    private final Entity overall;
    private final Entity seat;
    private final Consumer<SkyBlockPlayer> onSeatUse;
    private @Nullable Entity bodyHitbox;
    private @Nullable SkyBlockPlayer rider;
    private float facing;

    public BallistaSite(Instance instance, KuudraArena arena, BiConsumer<String, SkyBlockPlayer> onPileUse,
                        Consumer<SkyBlockPlayer> onSeatUse) {
        this.instance = instance;
        this.base = arena.centre();
        this.facing = restingFacing(base, arena.kuudraSides().get("FRONT"));
        this.piles = arena.piles();
        piles.forEach((name, at) -> {
            pileLabels.put(name, Displays.text(instance, at.add(0, 1.2, 0), "<e><l>BRING SUPPLY CHEST HERE"));
            pileHitboxes.put(name, Displays.hitbox(instance, at.withY(arena.floorY()), 1.6f, 1.4f,
                    player -> onPileUse.accept(name, player)));
        });
        overall = Displays.text(instance, base.add(0, 4.2, 0), collectedText(0, piles.size()));
        this.onSeatUse = onSeatUse;
        seat = new Entity(EntityType.ITEM_DISPLAY);
        seat.setNoGravity(true);
        seat.setInstance(instance, placed(SEAT));
    }

    /** Kuudra first surfaces on the FRONT side, so the idle Ballista points its muzzle there. */
    public static float restingFacing(Pos base, @Nullable Pos front) {
        if (front == null) return 0;
        return (float) Math.toDegrees(Math.atan2(-(front.x() - base.x()), front.z() - base.z()));
    }

    /** A model offset turned to where the Ballista faces, as a position whose view matches the turn. */
    Pos placed(Vec offset) {
        return placed(base, facing, offset);
    }

    public static Pos placed(Pos base, float facing, Vec offset) {
        double turn = Math.toRadians(facing - NORTH);
        double cos = Math.cos(turn);
        double sin = Math.sin(turn);
        return base.add(offset.x() * cos - offset.z() * sin, offset.y(), offset.x() * sin + offset.z() * cos)
                .withView(facing - NORTH, 0);
    }

    /** While ridden, the Ballista swings round to follow the rider's look. */
    public void tick() {
        if (rider == null || !isRiding(rider)) return;
        float wanted = rider.getPosition().yaw();
        if (Math.abs(Pos.fixYaw(wanted - facing)) < TURN_STEP) return;
        facing = wanted;
        for (int i = 0; i < parts.size(); i++) {
            Part part = MODEL.get(i);
            parts.get(i).teleport(placed(new Vec(part.x(), part.y(), part.z())));
        }
        seat.teleport(placed(SEAT));
    }

    public float facing() {
        return facing;
    }

    public void supplyReceived(String pile) {
        Pos at = piles.get(pile);
        if (at == null || pileCrates.containsKey(pile)) return;
        pileCrates.put(pile, Displays.block(instance, at.withY(base.y()), Block.CHEST, new Vec(0.9, 0.9, 0.9)));
        Displays.setText(pileLabels.get(pile), "<a><l>✓ SUPPLIES RECEIVED ✓");
    }

    public void showCollected(int collected, int needed) {
        Displays.setText(overall, collectedText(collected, needed));
    }

    private static String collectedText(int collected, int needed) {
        return "<e><l>Collect supply chests from the\n<e><l>lava and bring them to the\n<e><l>center!\n<e><l>Collected: <7>"
                + collected + "/" + needed;
    }

    /** The Burning capture's build holograms: SNEAK + PUNCH and PROGRESS per pile, Building Progress overall. */
    public void showBuild(BallistaBuild build) {
        piles.keySet().forEach(pile -> Displays.setText(pileLabels.get(pile), pileText(build.progress(pile))));
        String bar = progressBar(build.overall() / 100);
        Displays.setText(overall, bar + "\n<b>Building Progress <e>" + build.overallPercent() + "% <8>("
                + build.helping() + " Players Helping)\n" + bar);
        reveal(build.overall() / 100);
    }

    static String pileText(double progress) {
        if (progress >= 100) return "<7><l>PROGRESS: <a><l>COMPLETE";
        int percent = (int) progress;
        String colour = percent < 50 ? "<c>" : percent < 75 ? "<6>" : "<e>";
        return "<e><l>SNEAK<f><l> + <e><l>PUNCH\n<7><l>PROGRESS: " + colour + percent + "%";
    }

    private static String progressBar(double fraction) {
        int width = 36;
        int filled = (int) Math.round(Math.max(0, Math.min(1, fraction)) * width);
        return "<e><m>" + "=".repeat(filled) + "</m><7><m>" + "=".repeat(width - filled) + "</m>";
    }

    public void showReady(int fuel, int needed) {
        piles.keySet().forEach(pile -> {
            Displays.remove(pileLabels.get(pile));
            Displays.remove(pileCrates.get(pile));
            Displays.remove(pileHitboxes.get(pile));
        });
        reveal(1);
        if (bodyHitbox == null) bodyHitbox = Displays.hitbox(instance, base, 3.6f, 3.2f, onSeatUse);
        int charge = needed <= 0 ? 100 : Math.min(100, fuel * 100 / needed);
        Displays.setText(overall, "<f>Energy Charge: <a>" + charge + "%\n<c><l>BALLISTA\n<e><l>RIGHT-CLICK TO MOUNT");
    }

    public void reveal(double fraction) {
        int wanted = (int) Math.ceil(MODEL.size() * Math.max(0, Math.min(1, fraction)));
        while (parts.size() < wanted) {
            Part part = MODEL.get(parts.size());
            parts.add(Displays.block(instance, placed(new Vec(part.x(), part.y(), part.z())), part.block(),
                    new Vec(part.sx(), part.sy(), part.sz())));
        }
        while (parts.size() > wanted) Displays.remove(parts.removeLast());
    }

    public boolean mount(SkyBlockPlayer player) {
        if (rider != null && rider.isOnline() && seat.getPassengers().contains(rider)) return false;
        rider = player;
        seat.addPassenger(player);
        return true;
    }

    public void dismount() {
        if (rider != null && seat.getPassengers().contains(rider)) seat.removePassenger(rider);
        rider = null;
    }

    public boolean isRiding(SkyBlockPlayer player) {
        return rider == player && seat.getPassengers().contains(player);
    }

    public Pos muzzle() {
        return placed(MUZZLE);
    }

    public @Nullable String pileOf(Entity entity) {
        return pileHitboxes.entrySet().stream().filter(entry -> entry.getValue() == entity)
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    public Pos pile(String name) {
        return piles.get(name);
    }

    public Pos base() {
        return base;
    }

    public void remove() {
        dismount();
        pileLabels.values().forEach(Displays::remove);
        pileCrates.values().forEach(Displays::remove);
        pileHitboxes.values().forEach(Displays::remove);
        parts.forEach(Displays::remove);
        Displays.remove(overall);
        if (bodyHitbox != null) Displays.remove(bodyHitbox);
        Displays.remove(seat);
    }
}
