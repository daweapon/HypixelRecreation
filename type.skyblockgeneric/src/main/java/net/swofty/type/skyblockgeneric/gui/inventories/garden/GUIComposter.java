package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterConfig;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterEngine;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterService;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterState;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GUIComposter extends StatelessView {
    private static final int[] MATTER_PANES = {37, 28, 19, 10, 1};
    private static final int[] FUEL_PANES = {43, 34, 25, 16, 7};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Composter"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        for (int i = 0; i < 5; i++) {
            int index = i;
            layout.slot(MATTER_PANES[i], (_, c) -> gauge((SkyBlockPlayer) c.player(), index, false));
            layout.slot(FUEL_PANES[i], (_, c) -> gauge((SkyBlockPlayer) c.player(), index, true));
        }

        layout.slot(13, (_, c) -> collect((SkyBlockPlayer) c.player()), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            ComposterService.collect(player);
            c.session(DefaultState.class).refresh();
        });
        layout.slot(22, (_, _) -> ItemStacks.item(Material.HOPPER, Text.of("<a>Composter Upgrades"), List.of(
                        Text.of("<7>Upgrade your composter to increase"),
                        Text.of("<7>your compost production."),
                        Text.empty(),
                        Text.of("<e>Click to view upgrades!"))),
                (_, c) -> c.push(new GUIComposterUpgrades()));
        layout.slot(46, (_, c) -> meter((SkyBlockPlayer) c.player(), false));
        layout.slot(52, (_, c) -> meter((SkyBlockPlayer) c.player(), true));
        layout.slot(48, (_, c) -> insert((SkyBlockPlayer) c.player(), ComposterService.Source.INVENTORY, false),
                (_, c) -> insertAll(c, ComposterService.Source.INVENTORY, false));
        layout.slot(50, (_, c) -> insert((SkyBlockPlayer) c.player(), ComposterService.Source.INVENTORY, true),
                (_, c) -> insertAll(c, ComposterService.Source.INVENTORY, true));
        layout.slot(39, (_, c) -> insert((SkyBlockPlayer) c.player(), ComposterService.Source.SACKS, false),
                (_, c) -> insertAll(c, ComposterService.Source.SACKS, false));
        layout.slot(41, (_, c) -> insert((SkyBlockPlayer) c.player(), ComposterService.Source.SACKS, true),
                (_, c) -> insertAll(c, ComposterService.Source.SACKS, true));
    }

    private ItemStack.Builder gauge(SkyBlockPlayer player, int pane, boolean fuel) {
        ComposterState state = ComposterService.state(player);
        ComposterConfig config = ComposterConfig.get();
        double amount = fuel ? state.getFuel() : state.getOrganicMatter();
        double cap = fuel ? ComposterEngine.fuelCap(state, config) : ComposterEngine.organicMatterCap(state, config);
        double filled = Math.max(0, Math.min(1, amount / cap * 5 - pane));
        Material material = filled >= 1 ? Material.LIME_STAINED_GLASS_PANE : filled > 0 ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        return ItemStacks.item(material, Text.of(fuel ? "<a>Fuel" : "<e>Organic Matter"), List.of(bar(amount, cap)));
    }

    private Text bar(double amount, double cap) {
        int filled = (int) Math.round(Math.min(1, amount / cap) * 20);
        return Text.of("<a>" + "-".repeat(filled) + "<f>" + "-".repeat(20 - filled) + " <e>{}<6>/<e>{}", format(amount), abbreviate(cap));
    }

    private ItemStack.Builder collect(SkyBlockPlayer player) {
        ComposterState state = ComposterService.state(player);
        int stored = state.getStoredCompost();
        long seconds = state.running() ? (long) Math.ceil(state.getRemainingSeconds()) : 0;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Compost Available: <a>{}", StringUtility.commaify(stored)));
        lore.add(Text.of("<7>Next Compost: <a>{}", clock(seconds)));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to pickup a stack!"));
        lore.add(Text.of("<e>Shift-click to fill your inventory!"));
        SkyBlockItem compost = new SkyBlockItem(ItemType.COMPOST);
        return ItemStacks.head(compost.getComponent(SkullHeadComponent.class).getSkullTexture(compost),
                Math.max(1, Math.min(64, stored)), Text.of("<e>Collect Compost"), lore);
    }

    private ItemStack.Builder meter(SkyBlockPlayer player, boolean fuel) {
        ComposterState state = ComposterService.state(player);
        ComposterConfig config = ComposterConfig.get();
        double amount = fuel ? state.getFuel() : state.getOrganicMatter();
        double cap = fuel ? ComposterEngine.fuelCap(state, config) : ComposterEngine.organicMatterCap(state, config);
        List<Text> lore = new ArrayList<>();
        lore.add(bar(amount, cap));
        lore.add(Text.empty());
        if (fuel) {
            lore.add(Text.of("<7>Fill your composter with machine fuel,"));
            lore.add(Text.of("<7>like <9>Biofuel<7> to power the composter"));
            lore.add(Text.of("<7>to turn <e>Organic Matter<7> into <6>Compost<7>."));
            lore.add(Text.empty());
            lore.add(Text.of("<7>The composter must have <b>{} Fuel", StringUtility.commaify((long) ComposterEngine.fuelPerCycle(state, config))));
            lore.add(Text.of("<7>stored to start making compost."));
        } else {
            lore.add(Text.of("<7>Fill your composter with crops, like"));
            lore.add(Text.of("<a>Wheat<7> or <a>Enchanted Potato<7>, to turn"));
            lore.add(Text.of("<7>them into <e>Organic Matter<7>. Organic"));
            lore.add(Text.of("<7>Matter is used to make <6>Compost<7>."));
            lore.add(Text.empty());
            lore.add(Text.of("<7>The composter must have <b>{}", StringUtility.commaify((long) ComposterEngine.matterPerCycle(state, config))));
            lore.add(Text.of("<7>organic matter stored to start"));
            lore.add(Text.of("<7>making compost."));
        }
        return ItemStacks.item(fuel ? Material.BARREL : Material.POTATO, Text.of(fuel ? "<a>Fuel Meter" : "<e>Crop Meter"), lore);
    }

    private ItemStack.Builder insert(SkyBlockPlayer player, ComposterService.Source source, boolean fuel) {
        double worth = ComposterService.worth(player, source, fuel);
        boolean sacks = source == ComposterService.Source.SACKS;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Grab as " + (fuel ? "much fuel" : "many crops") + " that will fit into"));
        lore.add(Text.of("<7>the composter from your " + (sacks ? "sacks." : "inventory.")));
        lore.add(Text.empty());
        String noun = fuel ? "Fuel" : "Organic Matter";
        lore.add(Text.of("<7>In your " + (sacks ? "sacks" : "inventory") + ": " + (worth > 0 ? "<e>" + abbreviate(worth) + " " + noun : "<c>No " + noun)));
        lore.add(Text.empty());
        if (worth > 0) {
            lore.add(Text.of("<e>" + (sacks ? "Left-click to grab from sacks!" : "Click to insert!")));
        } else {
            lore.add(Text.of("<c>No " + (fuel ? "fuel" : "crops") + " in your " + (sacks ? "sacks" : "inventory") + "!"));
        }
        Material icon = sacks ? Material.CAULDRON : fuel ? Material.KELP : Material.HAY_BLOCK;
        return ItemStacks.item(icon, Text.of("<a>Insert " + (fuel ? "Fuel" : "Crops") + " from " + (sacks ? "Sacks" : "Inventory")), lore);
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) click.player();
        ItemStack stack = player.getInventory().getItemStack(click.slot());
        if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return false;
        ItemType type = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
        if (type == null) return false;
        ComposterConfig config = ComposterConfig.get();
        boolean matter = config.organicMatterValue(type.name()) > 0;
        boolean fuel = config.fuelValue(type.name()) > 0;
        if (!matter && !fuel) return false;

        int requested = click.click() instanceof Click.Right ? 1 : stack.amount();
        int added = fuel
                ? ComposterService.addFuel(player, type, requested, ComposterService.Source.INVENTORY)
                : ComposterService.addMatter(player, type, requested, ComposterService.Source.INVENTORY);
        if (added <= 0) {
            player.sendMessage(Text.of("<c>The composter can't hold any more " + (fuel ? "fuel" : "organic matter") + "!"));
            player.playSound(Sound.sound(Key.key("entity.villager.no"), Sound.Source.PLAYER, 1, 1));
        } else {
            player.playSound(Sound.sound(Key.key("block.composter.fill_success"), Sound.Source.PLAYER, 1, 1));
        }
        ctx.session(DefaultState.class).refresh();
        return false;
    }

    private void insertAll(ViewContext ctx, ComposterService.Source source, boolean fuel) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        double added = fuel ? ComposterService.insertAllFuel(player, source) : ComposterService.insertAllMatter(player, source);
        if (added <= 0) {
            player.sendMessage(Text.of("<c>There is nothing to insert!"));
        }
        ctx.session(DefaultState.class).refresh();
    }

    static String clock(long seconds) {
        return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
    }

    static String format(double amount) {
        return String.format(Locale.ROOT, "%,.1f", amount);
    }

    static String abbreviate(double amount) {
        if (amount >= 1_000_000) return trim(amount / 1_000_000) + "M";
        if (amount >= 1_000) return trim(amount / 1_000) + "k";
        return trim(amount);
    }

    private static String trim(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
}
