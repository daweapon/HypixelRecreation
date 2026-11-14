package dev.daweapon.type.lobby.gui;

import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.lobby.ServerInfoCache;
import dev.daweapon.type.lobby.game.GameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GUIGameMenu extends StatelessView {
    private static final int[] GAME_SLOTS = {
        10, 11, 12, 14, 15, 16,
        28, 29, 30, 31, 32, 33,
        37, 38, 39, 40, 41, 42, 43
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Game Menu", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        ServerInfoCache.getServers();

        layout.slot(4, ItemStacks.item(Material.BOOKSHELF, """
                <a>Main Lobby

                <7>Return to the Main Lobby."""), (_, c) -> c.player().sendTo(ServerType.MAIN_LOBBY));
        layout.slot(13, ItemStacks.item(Material.GRASS_BLOCK, """
                <a>Hypixel SMP
                <8>Persistent Game

                <7>Create your own SMP server on
                <7>Hypixel and play with your friends.

                <a>► Click to Connect"""), (_, c) -> c.player().notImplemented());

        layout.slot(22, createGameItem(GameType.RAVENGARD), (_, c) -> connect(c, GameType.RAVENGARD));

        int index = 0;
        for (GameType game : GameType.values()) {
            if (index >= GAME_SLOTS.length) break;
            if (game == GameType.RAVENGARD) continue;
            layout.slot(GAME_SLOTS[index++], createGameItem(game), (_, c) -> connect(c, game));
        }
        layout.slot(49, createRandomGameItem(), (_, c) -> connectRandom(c));
    }

    private ItemStack.Builder createGameItem(GameType game) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{}", StringUtility.toNormalCase(game.getCategory().name())));
        lore.add(Text.empty());
        lore.addAll(game.getLore());
        lore.add(Text.empty());
        lore.add(game.isImplemented() ? Text.of("<a>► Click to Connect") : Text.of("<c>► Coming soon"));
        lore.add(game.isImplemented()
            ? Text.of("<7>{:,} currently playing!", game.getPlayerCount())
            : Text.of("<7>0 currently playing!"));
        return ItemStacks.name(ItemStacks.appendLore(ItemStacks.copy(game.getItem().build()), lore),
            "<a>{}", game.getDisplayName());
    }

    private ItemStack.Builder createRandomGameItem() {
        ItemStack base = GameType.values()[ThreadLocalRandom.current().nextInt(GameType.values().length)].getItem().build();
        ItemStack.Builder builder = ItemStacks.raw(base.material(), """
                <a>Random Game
                <7>Join a random game.

                <e>Click to Play""");
        ResolvableProfile profile = base.get(DataComponents.PROFILE);
        return profile == null ? builder : builder.set(DataComponents.PROFILE, profile);
    }

    private void connect(ViewContext ctx, GameType game) {
        if (!game.isImplemented()) {
            ctx.player().sendMessage("<c>This game is not yet available!");
            return;
        }
        ctx.player().closeInventory();
        ctx.player().sendTo(game.getLobbyType());
    }

    private void connectRandom(ViewContext ctx) {
        List<GameType> implemented = Arrays.stream(GameType.values()).filter(GameType::isImplemented).toList();
        if (implemented.isEmpty()) {
            ctx.player().sendMessage("<c>No games available!");
            return;
        }
        connect(ctx, implemented.get(ThreadLocalRandom.current().nextInt(implemented.size())));
    }
}
