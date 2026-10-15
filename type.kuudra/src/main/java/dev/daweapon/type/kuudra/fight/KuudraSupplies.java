package dev.daweapon.type.kuudra.fight;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.kuudra.fight.world.KuudraItems;
import dev.daweapon.type.kuudra.fight.world.LavaCargo;
import dev.daweapon.type.kuudra.run.KuudraPhase;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.accessory.handler.UsableAccessory;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraTierSpec;
import dev.daweapon.type.skyblockgeneric.essence.CrimsonEssencePerk;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

final class KuudraSupplies {
    record Carried(LavaCargo.Kind kind, String spot, Pos home, CargoCarry carry, ItemStack replaced) {
    }

    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraTier tier;
    private final KuudraFightSpec spec;
    private final KuudraTierSpec tierSpec;
    private final KuudraArena arena;
    private final KuudraDialogue dialogue;
    private final Instance instance;
    private final KuudraTokens tokens;
    private final KuudraPerks perks;
    private final KuudraLives lives;
    private final BallistaFuel fuel;
    private final KuudraStage stage;

    final List<LavaCargo> cargo = new CopyOnWriteArrayList<>();
    final Map<UUID, Carried> carrying = new ConcurrentHashMap<>();
    final Set<String> filledPiles = ConcurrentHashMap.newKeySet();

    KuudraSupplies(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.tier = fight.tier;
        this.spec = fight.spec;
        this.tierSpec = fight.tierSpec;
        this.arena = fight.arena;
        this.dialogue = fight.dialogue;
        this.instance = fight.instance;
        this.tokens = fight.tokens;
        this.perks = fight.perks;
        this.lives = fight.lives;
        this.fuel = fight.fuel;
        this.stage = fight.stage;
    }

    void spawnCargo(LavaCargo.Kind kind, int count) {
        if (count <= 0) return;
        var supplies = spec.supplies();
        List<String> spots = SupplySpots.pick(arena.supplySpots().keySet(), Math.min(count, arena.supplySpots().size()),
                ThreadLocalRandom.current());
        for (String spot : spots) {
            Pos home = SupplySpots.crateFor(arena, arena.supplySpots().get(spot), supplies.crateDistance(),
                    supplies.crateJitter(), ThreadLocalRandom.current());
            cargo.add(new LavaCargo(instance, kind, spot, home, this::useCargo));
        }
        run.onlineMembers().forEach(player -> player.playSound(
                Sound.sound(Key.key("entity.ender_dragon.growl"), Sound.Source.HOSTILE, 3f, 0.49f)));
    }

    private Pos shoreOf(LavaCargo item) {
        return arena.supplySpots().get(item.spot()).position();
    }

    void clearCargo() {
        cargo.forEach(LavaCargo::remove);
        cargo.clear();
    }

    void reelElleRod(SkyBlockPlayer player, Pos bobber) {
        synchronized (run) {
            reel(player, bobber);
        }
    }

    /** A Lava Rod reeled in next to a crate or Fuel Cell pulls it toward the player (Strongarm pulls further). */
    boolean reel(SkyBlockPlayer player, Pos bobber) {
        synchronized (run) {
            if (fight.stopped || lives.isGhost(player.getUuid())) return false;
            LavaCargo nearest = null;
            double best = spec.supplies().hookRadius();
            for (LavaCargo each : cargo) {
                double distance = Math.hypot(each.position().x() - bobber.x(), each.position().z() - bobber.z());
                if (distance <= best && each.pickup() == null) {
                    best = distance;
                    nearest = each;
                }
            }
            if (nearest == null) return false;
            double pull = spec.supplies().reelPullBlocks() * (1 + EssencePerks.value(player, CrimsonEssencePerk.STRONGARM) / 100);
            Pos moved = SupplySpots.reel(nearest.position(), player.getPosition(), pull, stage::floats);
            if (moved.distance(nearest.position()) < 0.25) {
                player.sendMessage("<c>The supplies can't move any further in your direction!");
            }
            nearest.moveTo(moved);
            return true;
        }
    }

    void useCargo(LavaCargo item, SkyBlockPlayer player) {
        synchronized (run) {
            if (fight.stopped || !cargo.contains(item) || lives.isGhost(player.getUuid()) || carrying.containsKey(player.getUuid())) return;
            CargoPickup current = item.pickup();
            if (current != null) {
                if (!current.player().equals(player.getUuid())) player.sendMessage(dialogue.announcement("supplies_taken", Map.of()));
                return;
            }
            if (cargo.stream().anyMatch(other -> other.pickup() != null && other.pickup().player().equals(player.getUuid()))) return;
            if (player.getPosition().distance(item.position()) > spec.supplies().pickupRange()) {
                player.sendMessage("<c>It's too far away! Pull it closer with a Lava Rod.");
                return;
            }
            double bonus = perks.fraction(player.getUuid(), "STEADY_HANDS") + organ(player, spec.supplies().organBonus());
            int ticks = CargoPickup.ticksNeeded(spec.supplies().pickupSeconds().get(tier), bonus);
            item.setPickup(new CargoPickup(player.getUuid(), player.getPosition().x(), player.getPosition().z(),
                    spec.supplies().moveTolerance(), ticks));
            player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) spec.supplies().pickupSlownessAmplifier(), ticks + 10));
            player.playSound(Sound.sound(Key.key("entity.lightning_bolt.thunder"), Sound.Source.PLAYER, 2f, 2f));
        }
    }

    void tickPickups() {
        for (LavaCargo item : cargo) {
            if (fight.tick % 5 == 0) item.setReachable(SupplySpots.atShore(item.position(), shoreOf(item), stage::floats));
            CargoPickup pickup = item.pickup();
            if (pickup == null) continue;
            SkyBlockPlayer player = fight.member(pickup.player());
            if (player == null || lives.isGhost(player.getUuid())) {
                item.setPickup(null);
                continue;
            }
            switch (pickup.tick(player.getPosition().x(), player.getPosition().z())) {
                case PROGRESS -> {
                    if (fight.tick % 5 == 0) {
                        player.showTitle(Text.parse(pickup.bar()), Text.parse("<c>Don't Move!"),
                                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO));
                    }
                }
                case SLIPPED -> {
                    item.setPickup(null);
                    player.removeEffect(PotionEffect.SLOWNESS);
                    player.sendMessage(dialogue.announcement("supplies_slipped", Map.of()));
                }
                case DONE -> {
                    cargo.remove(item);
                    item.remove();
                    player.removeEffect(PotionEffect.SLOWNESS);
                    player.showTitle(Text.parse(pickup.bar()), Text.parse("<c>Don't Move!"),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO));
                    stage.playAt(item.position(), "block.chest.close", 0.49f);
                    carry(player, item.kind(), item.spot(), item.home());
                }
            }
        }
    }

    /** The crate goes into the last hotbar slot and stays selected (the Burning capture: "Elle's Supplies"). */
    private void carry(SkyBlockPlayer player, LavaCargo.Kind kind, String spot, Pos home) {
        ItemStack replaced = player.getInventory().getItemStack(KuudraItems.CARRY_SLOT);
        player.getInventory().setItemStack(KuudraItems.CARRY_SLOT, KuudraItems.create(kind == LavaCargo.Kind.SUPPLIES
                ? KuudraItems.Kind.ELLES_SUPPLIES : KuudraItems.Kind.FUEL_CELL));
        player.setHeldItemSlot((byte) KuudraItems.CARRY_SLOT);
        carrying.put(player.getUuid(), new Carried(kind, spot, home,
                new CargoCarry(player.getUuid(), spec.supplies().carryTimeoutSeconds()), replaced));
        player.sendMessage(kind == LavaCargo.Kind.SUPPLIES ? dialogue.announcement("supplies_retrieved", Map.of())
                : "<a>You retrieved a Ballista Fuel Cell from the Lava!");
        player.playSound(Sound.sound(Key.key("entity.item.pickup"), Sound.Source.PLAYER, 1f, 0.8f));
    }

    void tickCarries() {
        for (Map.Entry<UUID, Carried> entry : carrying.entrySet()) {
            SkyBlockPlayer player = fight.member(entry.getKey());
            Carried carried = entry.getValue();
            if (player == null) {
                dropCarried(entry.getKey(), false);
                continue;
            }
            if (carried.carry().tick()) {
                dropCarried(player.getUuid(), true);
                continue;
            }
            fight.display.actionBar(player, "<e>Carrying " + carried.kind().itemName() + " <7>(" + carried.carry().secondsLeft() + "s)");
        }
    }

    /** Right-clicking a pile while holding Elle's Supplies drops them off (the Burning capture). */
    void usePile(String pile, SkyBlockPlayer player) {
        synchronized (run) {
            Carried carried = carrying.get(player.getUuid());
            if (fight.stopped || carried == null || carried.kind() != LavaCargo.Kind.SUPPLIES) return;
            if (run.state().phase() != KuudraPhase.SUPPLIES) return;
            if (filledPiles.contains(pile)) {
                player.sendMessage("<c>This pile already has its supplies!");
                return;
            }
            if (KuudraStage.horizontal(player.getPosition(), arena.piles().get(pile)) > spec.supplies().placeRange()) return;
            finishCarry(player.getUuid());
            filledPiles.add(pile);
            fight.suppliesPlaced++;
            if (fight.ballista != null) {
                fight.ballista.supplyReceived(pile);
                fight.ballista.showCollected(fight.suppliesPlaced, spec.supplies().crates());
            }
            stage.playAt(arena.piles().get(pile), "block.ladder.break", 1.5f);
            run.broadcast(dialogue.announcement("supply_recovered",
                    Map.of("player", player.getFullDisplayName().toString(), "count", fight.suppliesPlaced)));
            stage.playAll("block.note_block.pling", 2f);
            tokens.gain(spec.tokens().supplyRecovered());
            if (fight.suppliesPlaced >= spec.supplies().crates()) run.advancePhase();
        }
    }

    boolean tryLoadFuel(SkyBlockPlayer player) {
        Carried carried = carrying.get(player.getUuid());
        if (carried == null || carried.kind() != LavaCargo.Kind.FUEL) return false;
        if (fight.ballista == null || fuel.armed() || !fuelPhase(run.state().phase())) return true;
        if (KuudraStage.horizontal(player.getPosition(), fight.ballista.base()) > spec.fuel().loadRange()) {
            player.sendMessage("<c>Get closer to the Ballista to load the Fuel Cell!");
            return true;
        }
        finishCarry(player.getUuid());
        BallistaFuel.Load load = fuel.load();
        run.broadcast(player.getFullDisplayName() + " <a>loaded a Ballista Fuel Cell! <7>(" + fuel.loaded() + "/" + fuel.cells() + ")");
        stage.playAll("block.anvil.use", 1.2f);
        tokens.gain(spec.tokens().fuelLoaded());
        fight.ballista.showReady(fuel.loaded(), fuel.cells());
        boolean fuelling = run.state().phase() == KuudraPhase.FUEL;
        if (load == BallistaFuel.Load.FIRST_FULL && fuelling) run.advancePhase();
        if (load == BallistaFuel.Load.REFUELLED) {
            run.broadcast("<a>The Ballista is fuelled! <e>Mount it and fire at Kuudra!");
            if (fuelling) run.resumePhase(tierSpec.stunRequired() ? KuudraPhase.STUN : KuudraPhase.DPS);
        }
        return true;
    }

    private static boolean fuelPhase(@Nullable KuudraPhase phase) {
        return phase == KuudraPhase.FUEL || phase == KuudraPhase.STUN || phase == KuudraPhase.DPS;
    }

    /** Fuel Cells still needed for a full load, after those loaded, floating in the lava and being carried. */
    int missingFuel() {
        long floating = cargo.stream().filter(item -> item.kind() == LavaCargo.Kind.FUEL).count();
        long carried = carrying.values().stream().filter(each -> each.kind() == LavaCargo.Kind.FUEL).count();
        return fuel.missing(floating, carried);
    }

    /** One shot empties the Ballista: everyone fishes up a fresh load of Fuel Cells before it fires again. */
    void spendFuel() {
        if (!fuel.fire()) return;
        if (fight.ballista != null) {
            fight.ballista.dismount();
            fight.ballista.showReady(0, spec.fuel().cells());
        }
        run.broadcast("<e>The Ballista is out of fuel! <c>Fish up " + spec.fuel().cells() + " more Fuel Cells <e>to fire it again.");
        KuudraPhase phase = run.state().phase();
        if (phase == KuudraPhase.STUN || (phase == KuudraPhase.DPS && !tierSpec.stunRequired())) {
            run.resumePhase(KuudraPhase.FUEL);
        }
        spawnCargo(LavaCargo.Kind.FUEL, missingFuel());
    }

    private void finishCarry(UUID player) {
        Carried carried = carrying.remove(player);
        if (carried == null) return;
        SkyBlockPlayer member = fight.member(player);
        if (member == null) return;
        if (KuudraItems.kindOf(member.getInventory().getItemStack(KuudraItems.CARRY_SLOT)) != null) {
            member.getInventory().setItemStack(KuudraItems.CARRY_SLOT, carried.replaced());
        }
    }

    /** The carried crate or Fuel Cell falls back into the lava at its spot. */
    void dropCarried(UUID player, boolean announce) {
        Carried carried = carrying.get(player);
        if (carried == null) return;
        finishCarry(player);
        if (!fight.stopped) cargo.add(new LavaCargo(instance, carried.kind(), carried.spot(), carried.home(), this::useCargo));
        SkyBlockPlayer member = fight.member(player);
        if (announce && member != null) {
            run.broadcast(dialogue.announcement("supplies_dropped", Map.of("player", member.getFullDisplayName().toString())));
        }
    }

    boolean isCarrying(SkyBlockPlayer player) {
        return carrying.containsKey(player.getUuid());
    }

    static double organ(SkyBlockPlayer player, Map<String, Double> bonuses) {
        double best = 0;
        for (Map.Entry<String, Double> organ : bonuses.entrySet()) {
            ItemType type = ItemType.get(organ.getKey());
            if (type != null && UsableAccessory.usable(player, type)) best = Math.max(best, organ.getValue() / 100);
        }
        return best;
    }
}
