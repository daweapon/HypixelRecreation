package dev.daweapon.type.skyblockgeneric.technoblade;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Weather;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class TechnobladeShrine {
    private static final long REFRESH_MILLIS = 2_000;
    private static final long ANNOUNCE_WINDOW_MILLIS = 60_000;

    private static volatile ShrineSnapshot snapshot = ShrineSnapshot.EMPTY;
    private static Set<Integer> announcedRelics;
    private static Long announcedCompletion;
    private static String announcedEffect;
    private static boolean thunderApplied;

    private TechnobladeShrine() {
    }

    public static void initialize() {
        Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    Thread.sleep(REFRESH_MILLIS);
                } catch (InterruptedException interrupted) {
                    return;
                }
                refresh();
            }
        });
    }

    public static ShrineSnapshot snapshot() {
        return snapshot;
    }

    public static ShrineConfig config() {
        return ShrineConfig.get();
    }

    public static boolean perkActive() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.SHRINE_OF_THE_BLOOD_GOD);
    }

    public static int relicCount() {
        return config().relics().size();
    }

    public static boolean complete() {
        return snapshot.complete(relicCount());
    }

    public static boolean standing() {
        return snapshot.standing(perkActive(), relicCount(), config().effectWindowMillis(), System.currentTimeMillis());
    }

    public static boolean technoshopOpen() {
        return snapshot.technoshopOpen(perkActive(), relicCount());
    }

    public static boolean ancientPigPresent() {
        return complete() && standing();
    }

    public static @Nullable AncientPigEffect activeEffect() {
        return snapshot.activeEffect(System.currentTimeMillis());
    }

    public static void claimRelic(SkyBlockPlayer player, int index) {
        if (!ShrineStore.available() || snapshot.relicFound(index)) {
            player.sendMessage("<c>Someone already found this relic!");
            return;
        }
        String finder = player.getUsername();
        Thread.startVirtualThread(() -> {
            boolean claimed = ShrineStore.claimRelic(index, finder, relicCount());
            refresh();
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                if (!claimed) {
                    player.sendMessage("<c>Someone already found this relic!");
                    return;
                }
                player.addAndUpdateItem(new SkyBlockItem(ItemType.SHINY_RELIC));
                player.sendMessage("<d>You found a <d>Shiny Relic<d>! It floats over the Blood God Shrine and a copy is yours.");
            });
        });
    }

    public static boolean forceRelic(int index, String finder) {
        if (!ShrineStore.available() || index < 0 || index >= relicCount()) return false;
        Thread.startVirtualThread(() -> {
            ShrineStore.claimRelic(index, finder, relicCount());
            refresh();
        });
        return true;
    }

    public static void forceEffect(AncientPigEffect effect) {
        Thread.startVirtualThread(() -> {
            long now = System.currentTimeMillis();
            long interval = config().effectIntervalMillis();
            ShrineStore.writeEffect(new ShrineSnapshot.ActiveEffect(effect, now, now + interval), interval);
            refresh();
        });
    }

    public static void reset() {
        Thread.startVirtualThread(() -> {
            ShrineStore.reset();
            refresh();
        });
    }

    public static String status() {
        ShrineSnapshot current = snapshot;
        AncientPigEffect effect = activeEffect();
        return "relics " + current.relics().size() + "/" + relicCount() + ", built " + current.built()
                + ", standing " + standing() + ", effect " + (effect == null ? "none" : effect.name());
    }

    private static synchronized void refresh() {
        try {
            if (!ShrineStore.available()) return;
            if (perkActive() && !snapshot.built()) ShrineStore.markBuilt();
            ShrineSnapshot current = ShrineStore.read();
            snapshot = current;
            rollIfDue(current);
            announceChanges(current);
            syncWeather(current);
        } catch (Exception exception) {
            Logger.error(exception, "Blood God Shrine refresh failed");
        }
    }

    private static void rollIfDue(ShrineSnapshot current) {
        long now = System.currentTimeMillis();
        long interval = config().effectIntervalMillis();
        if (!current.complete(relicCount()) || !current.standing(perkActive(), relicCount(), config().effectWindowMillis(), now)) return;
        ShrineSnapshot.ActiveEffect effect = current.effect();
        if (effect != null && now < effect.startedAt() + interval) return;
        if (!ShrineStore.tryLockRoll(interval)) return;
        AncientPigEffect[] effects = AncientPigEffect.values();
        AncientPigEffect rolled = effects[ThreadLocalRandom.current().nextInt(effects.length)];
        ShrineStore.writeEffect(new ShrineSnapshot.ActiveEffect(rolled, now, now + interval), interval);
        snapshot = ShrineStore.read();
    }

    private static void announceChanges(ShrineSnapshot current) {
        Set<Integer> relics = new HashSet<>(current.relics().keySet());
        if (announcedRelics != null) {
            for (int index : relics) {
                if (announcedRelics.contains(index)) continue;
                broadcast("<d><l>[PIG SHRINE] <r><e>" + current.relics().get(index) + " <e>found a <d>Shiny Relic<e>! <8>("
                        + relics.size() + "/" + relicCount() + ")");
            }
        }
        announcedRelics = relics;

        if (announcedCompletion != null && current.completedAt() != 0 && announcedCompletion != current.completedAt()) {
            broadcast("<d><l>[PIG SHRINE] <r><e>All <d>7 Shiny Relics <e>were found! The <6>Ancient Pig <e>has awoken above the shrine!");
        }
        announcedCompletion = current.completedAt();

        ShrineSnapshot.ActiveEffect effect = current.effect();
        String raw = effect == null ? null : effect.serialize();
        boolean changed = announcedEffect == null ? effect != null && System.currentTimeMillis() - effect.startedAt() < ANNOUNCE_WINDOW_MILLIS
                : !announcedEffect.equals(raw);
        if (changed && effect != null) {
            broadcast("<d><l>[PIG SHRINE] <r><e>The <6>" + effect.type().displayName() + " <e>shrine buff is now active! " + effect.type().description());
        }
        announcedEffect = raw == null ? "" : raw;
    }

    private static void syncWeather(ShrineSnapshot current) {
        boolean wantThunder = current.activeEffect(System.currentTimeMillis()) == AncientPigEffect.THUNDER;
        if (wantThunder == thunderApplied) return;
        thunderApplied = wantThunder;
        Weather weather = wantThunder ? Weather.THUNDER : Weather.CLEAR;
        MinecraftServer.getSchedulerManager().scheduleNextTick(() ->
                MinecraftServer.getInstanceManager().getInstances().forEach(instance -> instance.setWeather(weather)));
    }

    private static void broadcast(String message) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                .filter(SkyBlockPlayer.class::isInstance)
                .forEach(player -> ((SkyBlockPlayer) player).sendMessage(message));
    }
}
