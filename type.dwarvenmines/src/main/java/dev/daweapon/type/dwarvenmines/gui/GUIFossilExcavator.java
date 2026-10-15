package dev.daweapon.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.essence.FossilEssenceShop;
import dev.daweapon.type.skyblockgeneric.fossil.ChiselStats;
import dev.daweapon.type.skyblockgeneric.fossil.ExcavatorGame;
import dev.daweapon.type.skyblockgeneric.fossil.ExcavatorLoot;
import dev.daweapon.type.skyblockgeneric.fossil.FossilService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// The Fossil Excavator: pick a Chisel and a Suspicious Scrap, then clear dirt tiles (one Chisel charge each) to uncover the fossil and treasure.
public class GUIFossilExcavator extends StatelessView {
    private static final int CHISEL_SLOT = 12;
    private static final int SCRAP_SLOT = 14;
    private static final int START_SLOT = 31;

    private static final Map<UUID, ExcavatorGame> GAMES = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SELECTED = new ConcurrentHashMap<>();
    private static final Set<UUID> PAID = ConcurrentHashMap.newKeySet();

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Fossil Excavator", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        for (int slot = 0; slot < ExcavatorGame.SIZE; slot++) {
            int index = slot;
            layout.slot(slot, (_, c) -> render((SkyBlockPlayer) c.player(), index), (_, c) -> {
                click((SkyBlockPlayer) c.player(), index);
                c.session(DefaultState.class).refresh();
            });
        }
    }

    public static boolean hasActiveGame(SkyBlockPlayer player) {
        ExcavatorGame game = GAMES.get(player.getUuid());
        return game != null && !game.isOver();
    }

    private static ItemStack.Builder render(SkyBlockPlayer player, int slot) {
        ExcavatorGame game = GAMES.get(player.getUuid());
        return game == null ? setupSlot(player, slot) : tile(game, slot);
    }

    private static ItemStack.Builder setupSlot(SkyBlockPlayer player, int slot) {
        List<ItemStack> chisels = FossilService.chiselsIn(player);
        return switch (slot) {
            case CHISEL_SLOT -> chisels.isEmpty()
                    ? ItemStacks.item(Material.ARMOR_STAND, "<a>Chisel\n<7>Place a <9>Chisel <7>here to start\n<7>excavating.\n\n<c>You have no Chisel!")
                    : ItemStacks.appendLore(PlayerItemUpdater.playerUpdate(player, selectedChisel(player, chisels)),
                    chisels.size() > 1 ? "\n<e>Click to switch Chisel!" : "");
            case SCRAP_SLOT -> ItemStacks.appendLore(PlayerItemUpdater.playerUpdate(player,
                    new SkyBlockItem(ItemType.SUSPICIOUS_SCRAP).getItemStack()),
                    "\n<7>You have <a>{} <7>in your inventory.", player.countItem(ItemType.SUSPICIOUS_SCRAP));
            case START_SLOT -> ItemStacks.item(Material.LIME_DYE, """
                    <a>Start Excavator
                    <7>Place your <9>Chisel <7>and a <9>Suspicious
                    <9>Scrap <7>in the relevant slots to start
                    <7>excavating!

                    <7>Your <9>Suspicious Scrap <7>will be
                    <7>consumed!

                    <e>Click to start excavating!""");
            default -> ItemStacks.item(Material.GRAY_STAINED_GLASS_PANE, " ");
        };
    }

    private static ItemStack selectedChisel(SkyBlockPlayer player, List<ItemStack> chisels) {
        int index = Math.floorMod(SELECTED.getOrDefault(player.getUuid(), 0), chisels.size());
        return chisels.get(index);
    }

    private static ItemStack.Builder tile(ExcavatorGame game, int slot) {
        String charges = "<7>Chisel Charges Remaining: <a>" + game.charges();
        if (!game.isCleared(slot)) {
            if (game.isOver()) return ItemStacks.item(Material.YELLOW_STAINED_GLASS_PANE, "<6>Dirt\n" + charges + "\n\n<e>Click to excavate again!");
            if (game.isHighlighted(slot)) {
                return ItemStacks.item(Material.COAL_BLOCK, "<6>Treasure\n<7>Something valuable is buried here!\n" + charges + "\n\n<e>Click to clear away the dirt!");
            }
            return ItemStacks.item(Material.BROWN_STAINED_GLASS_PANE, "<6>Dirt\n" + charges + "\n\n<e>Click to clear away the dirt!");
        }
        if (game.isFossilTile(slot)) {
            return ItemStacks.item(Material.WHITE_STAINED_GLASS_PANE, "<d>Fossil\n<7>Uncover the rest of the fossil before\n<7>running out of charges!\n\n<7>Fossil Excavation Progress: <e>"
                    + StringUtility.decimalify(game.progressPercent(), 1) + "%\n" + charges);
        }
        ExcavatorLoot.Drop drop = game.uncoveredTreasure(slot);
        if (drop != null) return treasure(drop, charges);
        return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<7>Cleared\n" + charges);
    }

    private static ItemStack.Builder treasure(ExcavatorLoot.Drop drop, String charges) {
        String amount = StringUtility.commaify(drop.amount());
        return switch (drop.kind()) {
            case ESSENCE -> ItemStacks.head(FossilService.ESSENCE_TEXTURE, "<d>Fossil Essence <8>x" + amount + "\n" + charges);
            case POWDER -> ItemStacks.item(HotmPowder.GLACITE.getIcon(), HotmPowder.GLACITE.coloured() + " <8>x" + amount + "\n" + charges);
            case BOOK -> FossilService.paleontologistBook().getItemStack().builder();
            case ITEM -> new SkyBlockItem(drop.item(), drop.amount()).getItemStack().builder();
        };
    }

    private static void click(SkyBlockPlayer player, int slot) {
        UUID id = player.getUuid();
        ExcavatorGame game = GAMES.get(id);
        if (game == null) {
            if (slot == CHISEL_SLOT) SELECTED.merge(id, 1, Integer::sum);
            if (slot == START_SLOT) start(player);
            return;
        }
        if (game.isOver()) {
            GAMES.remove(id);
            PAID.remove(id);
            return;
        }
        game.dig(slot);
        if (game.isOver() && PAID.add(id)) FossilService.award(player, game);
    }

    private static void start(SkyBlockPlayer player) {
        List<ItemStack> chisels = FossilService.chiselsIn(player);
        if (chisels.isEmpty()) {
            player.sendMessage("<c>You need a Chisel to start excavating!");
            return;
        }
        ItemStack chisel = selectedChisel(player, chisels);
        ChiselStats stats = FossilService.statsOf(chisel);
        if (!FossilService.canUse(player, stats)) {
            player.sendMessage("<c>This Chisel requires Heart of the Mountain Tier {}.", stats.hotmTier());
            return;
        }
        if (!player.removeItemsFromPlayer(Map.of(ItemType.SUSPICIOUS_SCRAP, 1))) {
            player.sendMessage("<c>You need a Suspicious Scrap to start excavating!");
            return;
        }
        GAMES.put(player.getUuid(), ExcavatorGame.start(player.getUuid(), stats, FossilEssenceShop.startingCharges(player), ThreadLocalRandom.current()));
        PAID.remove(player.getUuid());
    }
}
