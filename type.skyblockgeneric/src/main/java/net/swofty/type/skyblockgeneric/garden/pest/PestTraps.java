package net.swofty.type.skyblockgeneric.garden.pest;

import net.minestom.server.coordinate.Point;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class PestTraps {
    public static final long CATCH_INTERVAL_MILLIS = 15 * 60_000L;
    public static final int MAX_TRAPS = 3;
    public static final int BAIT_LIMIT = 64;

    public record Kind(String id, ItemType item, String name, int capacity, double mouseChancePercent, String model) {
    }

    public static final Map<String, Kind> KINDS = Map.of(
            "PEST", new Kind("PEST", ItemType.PEST_TRAP, "Pest Trap", 2, 2.78, "pest_trap"),
            "MOUSE", new Kind("MOUSE", ItemType.MOUSE_PEST_TRAP, "Mouse Trap", 3, 7.89, "mouse_pest_trap"),
            "VERMIN", new Kind("VERMIN", ItemType.VERMIN_PEST_TRAP, "Vermin Trap", 4, 10.26, "vermin_pest_trap"));

    private PestTraps() {
    }

    public static void place(SkyBlockPlayer player, SkyBlockItem item, String kindId) {
        if (!PestVacuum.inGarden(player)) {
            player.sendMessage(Text.of("<c>You can only place Traps in your Garden!"));
            return;
        }
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        Point target = player.getTargetBlockPosition(6);
        if (target == null) return;
        int plot = GardenLayout.plotAt(target.x(), target.z());
        if (plot == GardenLayout.NONE || plot == GardenLayout.BARN || !profile.isPlotUnlocked(plot)) {
            player.sendMessage(Text.of("<c>You can only place Traps in one of your plots!"));
            return;
        }
        if (data.getTraps().size() >= MAX_TRAPS) {
            player.sendMessage(Text.of("<c>You can only have {} Traps placed at once!", MAX_TRAPS));
            return;
        }
        net.swofty.type.skyblockgeneric.consumable.TimedConsumables.consumeHeld(player, item);
        PestData.Trap trap = new PestData.Trap(java.util.UUID.randomUUID().toString(), kindId, plot,
                target.x() + 0.5, target.y() + 1, target.z() + 0.5);
        data.getTraps().add(trap);
        Gardens.save(player, profile);
        PestWorld.reconcileTraps(player.getInstance(), data);
        player.sendMessage(Text.of("<a>Placed a {}<a>! Click it with a spray material to add bait.", KINDS.get(kindId).name()));
    }

    public static void tick(PestData data, long now) {
        for (PestData.Trap trap : data.getTraps()) {
            Kind kind = KINDS.get(trap.getKind());
            int guard = 0;
            while (kind != null && trap.getBait() > 0 && trap.getCaught().size() < kind.capacity()
                    && trap.getNextAt() > 0 && now >= trap.getNextAt() && guard++ < 16) {
                trap.setBait(trap.getBait() - 1);
                trap.getCaught().add(catchOne(kind, trap));
                trap.setNextAt(trap.getNextAt() + CATCH_INTERVAL_MILLIS);
            }
            if (trap.getBait() == 0 || trap.getCaught().size() >= (kind == null ? 0 : kind.capacity())) trap.setNextAt(0);
        }
    }

    private static String catchOne(Kind kind, PestData.Trap trap) {
        if ("MOONDEW".equals(trap.getMaterial())) return "LUNAR_MOTH";
        if (ThreadLocalRandom.current().nextDouble() * 100 < kind.mouseChancePercent()) return "FIELD_MOUSE";
        List<PestCatalogue.Pest> pool = PestCatalogue.get().natural();
        int total = 0;
        int[] weights = new int[pool.size()];
        for (int i = 0; i < pool.size(); i++) {
            weights[i] = PestRules.BASE_WEIGHT + (trap.getMaterial().equals(pool.get(i).spray()) ? PestRules.SPRAY_ATTRACT_WEIGHT : 0);
            total += weights[i];
        }
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (int i = 0; i < pool.size(); i++) {
            roll -= weights[i];
            if (roll < 0) return pool.get(i).id();
        }
        return pool.getLast().id();
    }

    public static void interact(SkyBlockPlayer player, String trapId) {
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        PestData.Trap trap = data.getTraps().stream().filter(t -> t.getId().equals(trapId)).findFirst().orElse(null);
        Kind kind = trap == null ? null : KINDS.get(trap.getKind());
        if (kind == null) return;
        tick(data, System.currentTimeMillis());
        if (player.isSneaking()) {
            pickUp(player, profile, trap, kind);
            return;
        }
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        String heldId = held.getAttributeHandler().getTypeAsString();
        if ((PestItems.MATERIALS.contains(heldId) || "MOONDEW".equals(heldId)) && !held.isNA()) {
            addBait(player, profile, trap, held, heldId);
            return;
        }
        if (!trap.getCaught().isEmpty()) {
            collect(player, profile, trap);
            return;
        }
        player.sendMessage(Text.of("<7>{}<7>: bait <a>{}x {}<7>, caught <a>{}<7>/<a>{}<7>. <8>Sneak-click to pick it up.",
                kind.name(), trap.getBait(), trap.getMaterial().isEmpty() ? "None" : PestItems.materialName(trap.getMaterial()),
                trap.getCaught().size(), kind.capacity()));
    }

    private static void addBait(SkyBlockPlayer player, GardenProfile profile, PestData.Trap trap, SkyBlockItem held, String id) {
        if (!trap.getMaterial().isEmpty() && !trap.getMaterial().equals(id) && trap.getBait() > 0) {
            player.sendMessage(Text.of("<c>This Trap already holds a different bait!"));
            return;
        }
        int amount = Math.min(held.getAmount(), BAIT_LIMIT - trap.getBait());
        ItemType type = ItemType.get(id);
        if (amount <= 0 || type == null) {
            player.sendMessage(Text.of("<c>This Trap is full of bait!"));
            return;
        }
        player.removeItemFromPlayer(type, amount);
        trap.setMaterial(id);
        trap.setBait(trap.getBait() + amount);
        if (trap.getNextAt() == 0) trap.setNextAt(System.currentTimeMillis() + CATCH_INTERVAL_MILLIS);
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<a>Added {}x {} <a>as bait.", amount, PestItems.materialName(id)));
    }

    private static void collect(SkyBlockPlayer player, GardenProfile profile, PestData.Trap trap) {
        int count = trap.getCaught().size();
        PestData data = profile.pests();
        for (String type : List.copyOf(trap.getCaught())) {
            PestCatalogue.Pest pest = PestCatalogue.get().pest(type);
            if (pest == null) continue;
            data.addKill(type);
            data.setStored(data.getStored() + 1);
            PestRewards.award(player, profile, pest);
        }
        trap.getCaught().clear();
        if (trap.getBait() > 0 && trap.getNextAt() == 0) trap.setNextAt(System.currentTimeMillis() + CATCH_INTERVAL_MILLIS);
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<a>You collected <2>{} Pests <a>from the Trap!", count));
    }

    private static void pickUp(SkyBlockPlayer player, GardenProfile profile, PestData.Trap trap, Kind kind) {
        if (!trap.getCaught().isEmpty()) {
            collect(player, profile, trap);
        }
        ItemType bait = ItemType.get(trap.getMaterial());
        if (bait != null && trap.getBait() > 0) PestRewards.give(player, new SkyBlockItem(bait, trap.getBait()));
        profile.pests().getTraps().remove(trap);
        Gardens.save(player, profile);
        PestWorld.reconcileTraps(player.getInstance(), profile.pests());
        player.addAndUpdateItem(new SkyBlockItem(kind.item()));
        player.sendMessage(Text.of("<a>You picked up the {}<a>.", kind.name()));
    }
}
