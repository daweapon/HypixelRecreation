package dev.daweapon.type.skyblockgeneric.carnival;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks who is playing which Carnival game and routes the game tools' clicks to it. */
public final class CarnivalSessions {
    private static final Map<UUID, CarnivalGameSession> SESSIONS = new ConcurrentHashMap<>();

    private CarnivalSessions() {}

    public static void initialize() {
        register("CARNIVAL_SHOVEL_INTERACT", CarnivalGame.FRUIT_DIGGING);
        register("CARNIVAL_ROD_INTERACT", CarnivalGame.CATCH_A_FISH);
        register("CARNIVAL_DART_TUBE_INTERACT", CarnivalGame.ZOMBIE_SHOOTOUT);
    }

    private static void register(String handlerId, CarnivalGame game) {
        InteractableRegistry.register(handlerId, InteractableItemConfig.builder()
                .rightClickHandler((player, _) -> route(player, game, true))
                .leftClickHandler((player, _) -> route(player, game, false))
                .build());
    }

    private static void route(SkyBlockPlayer player, CarnivalGame game, boolean right) {
        CarnivalGameSession session = SESSIONS.get(player.getUuid());
        if (session == null || session.game() != game) {
            removeTools(player);
            return;
        }
        if (right) session.onRightClick();
        else session.onLeftClick();
    }

    public static @Nullable CarnivalGameSession get(SkyBlockPlayer player) {
        return SESSIONS.get(player.getUuid());
    }

    public static boolean isPlaying(SkyBlockPlayer player) {
        return SESSIONS.containsKey(player.getUuid());
    }

    public static void begin(SkyBlockPlayer player, CarnivalGameSession session) {
        SESSIONS.put(player.getUuid(), session);
    }

    public static void end(SkyBlockPlayer player, CarnivalGameSession session) {
        SESSIONS.remove(player.getUuid(), session);
        removeTools(player);
    }

    public static void abandon(SkyBlockPlayer player) {
        CarnivalGameSession session = SESSIONS.remove(player.getUuid());
        if (session != null) session.abandon();
    }

    /** Finds an empty hotbar slot for a game tool, or -1 when the hotbar is full. */
    public static int freeHotbarSlot(SkyBlockPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItemStack(slot).isAir()) return slot;
        }
        return -1;
    }

    public static void removeTools(SkyBlockPlayer player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            ItemType type = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
            if (type == ItemType.CARNIVAL_SHOVEL || type == ItemType.CARNIVAL_ROD || type == ItemType.CARNIVAL_DART_TUBE) {
                player.getInventory().setItemStack(slot, ItemStack.AIR);
            }
        }
    }
}
