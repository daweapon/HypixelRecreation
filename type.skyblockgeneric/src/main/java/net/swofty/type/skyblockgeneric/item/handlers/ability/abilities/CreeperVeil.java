package net.swofty.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.region.rules.TempleLockdown;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CreeperVeil {
    public static final String ABILITY_ID = "CREEPER_VEIL";
    private static final double MANA_FRACTION = 0.2;
    private static final Map<UUID, List<ItemStack>> ACTIVE = new ConcurrentHashMap<>();
    private static boolean watching;

    private CreeperVeil() {
    }

    public static boolean isActive(SkyBlockPlayer player) {
        return ACTIVE.containsKey(player.getUuid());
    }

    public static boolean toggle(SkyBlockPlayer player) {
        if (TempleLockdown.isLocked(player)) {
            player.sendMessage("<c>The Jungle Temple's magic suppresses your Creeper Veil!");
            return false;
        }
        if (isActive(player)) {
            deactivate(player, "<c>Creeper Veil <4><l>De-activated<c>!");
            return true;
        }
        ACTIVE.put(player.getUuid(), armor(player));
        startWatching();
        player.sendMessage("<a>Creeper Veil <2><l>Activated<a>!");
        return true;
    }

    public static boolean absorb(SkyBlockPlayer player) {
        if (!isActive(player)) return false;
        if (TempleLockdown.isLocked(player)) {
            deactivate(player, "<c>Creeper Veil <4><l>De-activated<c>!");
            return false;
        }
        if (!ACTIVE.get(player.getUuid()).equals(armor(player))) {
            deactivate(player, "<c>Creeper Veil <4><l>De-activated<c>! <7>(armor changed)");
            return false;
        }
        float cost = player.getMaxMana() * (float) MANA_FRACTION;
        if (player.getMana() < cost) {
            deactivate(player, "<c>Creeper Veil <4><l>De-activated<c>! <7>(out of mana)");
            return false;
        }
        player.setMana(player.getMana() - cost);
        SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.MANA,
                Text.of("<b>-{} (<6>Creeper Veil</6>)", Math.round(cost)), 20, 2);
        return true;
    }

    public static void clear(UUID player) {
        ACTIVE.remove(player);
    }

    private static void deactivate(SkyBlockPlayer player, String message) {
        if (ACTIVE.remove(player.getUuid()) == null) return;
        player.sendMessage(message);
    }

    private static List<ItemStack> armor(SkyBlockPlayer player) {
        return List.of(player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots());
    }

    private static synchronized void startWatching() {
        if (watching) return;
        watching = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (UUID id : ACTIVE.keySet()) {
                if (!(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) instanceof SkyBlockPlayer player)) {
                    ACTIVE.remove(id);
                    continue;
                }
                if (TempleLockdown.isLocked(player)) {
                    deactivate(player, "<c>Creeper Veil <4><l>De-activated<c>!");
                } else if (!ACTIVE.get(id).equals(armor(player))) {
                    deactivate(player, "<c>Creeper Veil <4><l>De-activated<c>! <7>(armor changed)");
                }
            }
            return TaskSchedule.tick(5);
        });
    }
}
