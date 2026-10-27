package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hunting.GUIHuntrap;
import dev.daweapon.type.skyblockgeneric.item.handlers.place.PlaceEventRegistry;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.torrhus.StarbornBenefactors;
import java.util.stream.Collectors;

// Manages placed Huntraps: handles placement, ticks them toward catching an eligible attribute
// shard, and lets players claim or pick them back up.
public final class HuntrapService {
    public static final int MAX_TRAPS = 5;
    private static final AttributeId FOREST_TRAP = AttributeId.parse("U11");
    private static final AttributeId COMBAT_TRAP = AttributeId.parse("U65");
    private static final Map<TrapKey, HuntrapDisplay> DISPLAYS = new HashMap<>();

    private HuntrapService() {
    }

    public static void initialize() {
        PlaceEventRegistry.register("HUNTRAP", (event, player, item) -> {
            event.setCancelled(true);
            event.setBlock(Block.AIR);
            ItemType type = item.getAttributeHandler().getPotentialType();
            if (type == null) return;
            HuntrapTier tier;
            try {
                tier = HuntrapTier.valueOf(type.name());
            } catch (IllegalArgumentException ignored) {
                return;
            }
            if (!place(player, tier, event.getBlockPosition().asPos(), false)) return;
            item.setAmount(item.getAmount() - 1);
            player.setItemInMainHand(item.getAmount() <= 0 ? ItemStack.AIR : item.getItemStack());
        });
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return TaskSchedule.seconds(1);
        }, ExecutionType.TICK_START);
    }

    public static boolean place(SkyBlockPlayer player, HuntrapTier tier, Pos position, boolean fromToolkit) {
        List<DatapointHunting.PlacedHuntrap> traps = player.getHuntingData().getTraps();
        if (traps.size() >= MAX_TRAPS) {
            player.sendMessage("<c>You can only have {} traps placed at once!", MAX_TRAPS);
            return false;
        }
        AttributeDefinition target = roll(player, position);
        if (target == null) {
            player.sendMessage("<c>No huntable creatures can be caught by a Huntrap here!");
            return false;
        }
        String world = HypixelConst.getTypeLoader().getType().name();
        long now = System.currentTimeMillis();
        long duration = HuntrapTiming.millis(target.rarity(), tier, discount(player, target),
                ThreadLocalRandom.current().nextDouble());
        DatapointHunting.PlacedHuntrap trap = new DatapointHunting.PlacedHuntrap(world, position.x(), position.y(),
                position.z(), tier.name(), now, now + duration, null, target.id().toString(), fromToolkit);
        traps.add(trap);
        spawn(player, trap);
        player.sendMessage("<a>You placed a Trap near a Shard location! <8>(<b>{}<7>/<b>{} <7>traps placed<8>)",
                traps.size(), MAX_TRAPS);
        return true;
    }

    public static void deployFromToolkit(SkyBlockPlayer player, HuntingToolkit.Slot slot) {
        if (slot.trap() == null || HuntingToolkit.stored(player, slot) == null) return;
        Pos position = new Pos(player.getPosition().blockX(), player.getPosition().blockY(), player.getPosition().blockZ());
        if (place(player, slot.trap(), position, true)) player.getHuntingData().takeToolkitItem(slot.key());
    }

    static double discount(SkyBlockPlayer player, AttributeDefinition target) {
        double discount = StarbornBenefactors.speedBonus(player);
        AttributeId attribute = switch (target.category()) {
            case FOREST -> FOREST_TRAP;
            case COMBAT -> COMBAT_TRAP;
            default -> null;
        };
        if (attribute != null) discount += AttributeEffectService.value(player.getHuntingData(), attribute) / 100D;
        return discount;
    }

    private static AttributeDefinition roll(SkyBlockPlayer player, Pos position) {
        if (player.getInstance() == null) return null;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(position);
        HuntrapMedium medium = HuntrapMedium.at(player.getInstance(), position.blockX(), position.blockY(), position.blockZ());
        return pick(player, TrapPools.candidates(HypixelConst.getTypeLoader().getType(),
                region == null ? null : region.getType(), medium));
    }

    static AttributeDefinition pick(SkyBlockPlayer player, List<TrapPools.Entry> candidates) {
        if (candidates.isEmpty()) return null;
        TrapPools.Entry entry = LootRoll.pick(player.getUuid(), candidates,
                value -> AttributeShardComponent.itemType(AttributeRegistry.get(value.shard())),
                TrapPools.Entry::weight, ThreadLocalRandom.current());
        return AttributeRegistry.get(entry.shard());
    }

    private static void tick() {
        List<SkyBlockPlayer> loadedPlayers = SkyBlockGenericLoader.getLoadedPlayers();
        for (SkyBlockPlayer player : loadedPlayers) {
            String world = HypixelConst.getTypeLoader().getType().name();
            List<DatapointHunting.PlacedHuntrap> traps = player.getHuntingData().getTraps();
            boolean newlyCaught = false;
            for (int i = 0; i < traps.size(); i++) {
                DatapointHunting.PlacedHuntrap trap = traps.get(i);
                if (!trap.world().equals(world)) continue;
                TrapKey key = TrapKey.of(player.getUuid(), trap);
                if (trap.caughtShard() == null && trap.catchAt() <= System.currentTimeMillis()) {
                    AttributeDefinition caught = trap.targetShard() == null
                            ? roll(player, new Pos(trap.x(), trap.y(), trap.z())) : AttributeRegistry.get(trap.targetShard());
                    if (caught != null) {
                        DatapointHunting.PlacedHuntrap complete = new DatapointHunting.PlacedHuntrap(trap.world(), trap.x(),
                                trap.y(), trap.z(), trap.tier(), trap.placedAt(), trap.catchAt(), caught.id().toString(),
                                caught.id().toString(), trap.fromToolkit());
                        traps.set(i, complete);
                        HuntrapDisplay old = DISPLAYS.remove(key);
                        if (old != null) old.remove();
                        trap = complete;
                        newlyCaught = true;
                    }
                }
                if (!DISPLAYS.containsKey(key)) spawn(player, trap);
            }
            if (newlyCaught) announceCatch(player);
            List<TrapKey> obsolete = DISPLAYS.keySet().stream().filter(key -> key.owner.equals(player.getUuid()))
                    .filter(key -> traps.stream().noneMatch(trap -> key.equals(TrapKey.of(player.getUuid(), trap)))).toList();
            obsolete.forEach(key -> {
                HuntrapDisplay display = DISPLAYS.remove(key);
                if (display != null) display.remove();
            });
        }
        Set<UUID> loaded = loadedPlayers.stream().map(SkyBlockPlayer::getUuid).collect(Collectors.toSet());
        DISPLAYS.keySet().stream().filter(key -> !loaded.contains(key.owner)).toList().forEach(key -> {
            HuntrapDisplay display = DISPLAYS.remove(key);
            if (display != null) display.remove();
        });
    }

    private static void announceCatch(SkyBlockPlayer player) {
        long caught = player.getHuntingData().getTraps().stream().filter(trap -> trap.caughtShard() != null).count();
        player.sendMessage("<6>HUNT <e>- <a>{} <e>of your <6>Hunting Traps <e>caught something!", caught);
        player.sendMessage(Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("Click here to collect them.", NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand("/collecthuntraps"))
                        .hoverEvent(HoverEvent.showText(Text.of("<e>Click to collect!"))))
                .append(Component.text("]", NamedTextColor.GRAY)));
    }

    public static int collectAll(SkyBlockPlayer player) {
        int collected = 0;
        for (DatapointHunting.PlacedHuntrap trap : List.copyOf(player.getHuntingData().getTraps())) {
            if (trap.caughtShard() == null) continue;
            claim(player, trap);
            collected++;
        }
        return collected;
    }

    private static void spawn(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap trap) {
        if (player.getInstance() == null) return;
        TrapKey key = TrapKey.of(player.getUuid(), trap);
        HuntrapTier tier = HuntrapTier.valueOf(trap.tier());
        AttributeDefinition caught = AttributeRegistry.get(trap.caughtShard());
        HuntrapDisplay display = new HuntrapDisplay(player.getInstance(), new Pos(trap.x(), trap.y(), trap.z()),
                player.getUuid(), tier, caught, trap.catchAt(), clicker -> GUIHuntrap.open(clicker, trap));
        DISPLAYS.put(key, display);
    }

    public static DatapointHunting.PlacedHuntrap current(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap trap) {
        return player.getHuntingData().getTraps().stream()
                .filter(value -> TrapKey.of(player.getUuid(), value).equals(TrapKey.of(player.getUuid(), trap)))
                .findFirst().orElse(null);
    }

    public static void claim(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap trap) {
        DatapointHunting.PlacedHuntrap current = current(player, trap);
        if (current == null) return;
        AttributeDefinition caught = AttributeRegistry.get(current.caughtShard());
        if (caught == null) {
            long seconds = Math.max(0, (current.catchAt() - System.currentTimeMillis()) / 1000);
            player.sendMessage("<c>This Huntrap is empty! <7>Time remaining: <a>{}",
                    "%d h %02d m %02d s".formatted(seconds / 3600, seconds / 60 % 60, seconds % 60));
            return;
        }
        int amount = AttributeEffectService.fortunateAmount(player, caught);
        player.getHuntingData().addShards(caught.id(), amount);
        dev.daweapon.type.generic.event.HypixelEventHandler.callCustomEvent(new dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent(
                player, caught, amount, dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent.Source.HUNTRAP, false));
        player.sendMessage("<a>Your Huntrap caught <e>{}x <a>{}<a>!", amount, caught.shardName());
        retrieveTrap(player, current);
    }

    public static void pickup(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap trap) {
        DatapointHunting.PlacedHuntrap current = current(player, trap);
        if (current == null) return;
        if (current.caughtShard() != null) {
            claim(player, current);
            return;
        }
        retrieveTrap(player, current);
        player.sendMessage("<a>Picked up your <e>{}<a>!", HuntrapTier.valueOf(current.tier()).displayName());
    }

    private static void retrieveTrap(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap current) {
        HuntrapTier tier = HuntrapTier.valueOf(current.tier());
        if (current.fromToolkit() && HuntingToolkit.returnTrap(player, tier))
            player.sendMessage("<a>You also picked up the trap and it got sent to your Hunting Toolkit!");
        else player.addAndUpdateItem(tier.itemType());
        player.getHuntingData().getTraps().remove(current);
        HuntrapDisplay display = DISPLAYS.remove(TrapKey.of(player.getUuid(), current));
        if (display != null) display.remove();
    }

    private record TrapKey(UUID owner, double x, double y, double z, String world) {
        static TrapKey of(UUID owner, DatapointHunting.PlacedHuntrap trap) {
            return new TrapKey(owner, trap.x(), trap.y(), trap.z(), trap.world());
        }
    }
}
