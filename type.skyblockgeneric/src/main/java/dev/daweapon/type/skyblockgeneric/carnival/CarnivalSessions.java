package dev.daweapon.type.skyblockgeneric.carnival;

import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.event.actions.player.ActionPlayerChangeHypixelMenuDisplay;
import dev.daweapon.type.skyblockgeneric.user.ChallengeSidebar;
import dev.daweapon.type.skyblockgeneric.user.MenuSlotOverride;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/** Tracks who is playing which Carnival game and routes the game tools' clicks to it. */
public final class CarnivalSessions {
    private static final Map<UUID, CarnivalGameSession> SESSIONS = new ConcurrentHashMap<>();

    private CarnivalSessions() {}

    private static final ItemStack LEAVE_GAME = ItemStack.builder(Material.RED_TERRACOTTA)
            .customName(Text.of("<c>Leave Game <7>(Click)").asComponent().decoration(TextDecoration.ITALIC, false))
            .lore(Stream.of("<7>Leave your current carnival", "<7>minigame. Will count as a <c>fail<7>!", "",
                            "<e>Click to leave minigame.")
                    .map(line -> Text.of(line).asComponent().decoration(TextDecoration.ITALIC, false)).toList())
            .build();

    public static void initialize() {
        register("CARNIVAL_SHOVEL_INTERACT", CarnivalGame.FRUIT_DIGGING);
        register("CARNIVAL_ROD_INTERACT", CarnivalGame.CATCH_A_FISH);
        register("CARNIVAL_DART_TUBE_INTERACT", CarnivalGame.ZOMBIE_SHOOTOUT);
        ChallengeSidebar.register(player -> {
            CarnivalGameSession session = get(player);
            return session == null ? null : session.sidebarLines();
        });
        ChallengeSidebar.hidePurseWhen(CarnivalSessions::isPlaying);
        MenuSlotOverride.register(player -> isPlaying(player) ? LEAVE_GAME : null, player -> {
            CarnivalGameSession session = get(player);
            if (session != null) session.leave();
        });
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
        if (player.isOnline()) ActionPlayerChangeHypixelMenuDisplay.setMainMenu(player);
    }

    public static void end(SkyBlockPlayer player, CarnivalGameSession session) {
        SESSIONS.remove(player.getUuid(), session);
        removeTools(player);
        if (player.isOnline()) ActionPlayerChangeHypixelMenuDisplay.setMainMenu(player);
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
