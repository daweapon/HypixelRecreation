package dev.daweapon.type.hub.gui;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.greatspook.EerieUpgrades;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookFear;
import dev.daweapon.type.skyblockgeneric.greatspook.TyashoiExperiment;
import dev.daweapon.type.skyblockgeneric.greatspook.TyashoiExperiments;
import dev.daweapon.type.skyblockgeneric.greatspook.TyashoiProgress;
import dev.daweapon.type.skyblockgeneric.greatspook.TyashoiService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUITyashoiExperiments implements View<GUITyashoiExperiments.State> {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    public record State() {
        public static State of(SkyBlockPlayer player) {
            return new State();
        }
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("The Great Spook Experiments", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        List<TyashoiExperiment> experiments = TyashoiExperiments.all().values().stream()
                .filter(experiment -> experiment.requires() == null
                        || TyashoiProgress.claimed(viewer, experiment.requires()))
                .toList();
        for (int i = 0; i < experiments.size() && i < SLOTS.length; i++) {
            TyashoiExperiment experiment = experiments.get(i);
            layout.slot(SLOTS[i], (_, c) -> icon((SkyBlockPlayer) c.player(), experiment), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                player.closeInventory();
                TyashoiService.Result result = TyashoiService.attempt(player, experiment);
                if (result == TyashoiService.Result.BOUGHT) player.playSuccessSound();
            });
        }
        Components.close(layout, 49);
        layout.slot(48, (_, c) -> fearIcon((SkyBlockPlayer) c.player()));
    }

    private static ItemStack.Builder fearIcon(SkyBlockPlayer player) {
        double fear = GreatSpookFear.fear(player);
        long cooldown = Math.max(15, 360 - (long) (fear * 3));
        return ItemStacks.item(Material.CAULDRON, "<5>Fear\n<7>Your Fear: <5>{}\n \n<7>The Fear stat has a couple uses:\n<7>- You take <a>1% <7>less damage from <c>Primal Fears <7>per <a>1 <7>Fear.\n<7>- <c>Primal Fear <7>monsters are a bit more common the more Fear you have.\n<7>- Your current cooldown between spawning <c>Primal Fears <7>is <a>{}<7>.",
                StringUtility.commaify(fear), cooldown >= 60 && cooldown % 60 == 0 ? cooldown / 60 + " Minutes" : cooldown + " Seconds");
    }

    private static ItemStack.Builder icon(SkyBlockPlayer player, TyashoiExperiment experiment) {
        ItemStack.Builder stack = base(player, experiment);
        List<Text> lore = ItemStacks.loreText(stack);
        lore.add(Text.empty());
        lore.add(Text.of("<8><o>This item does not get an edition number."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost"));
        List<TyashoiExperiment.Cost> costs = experiment.costs();
        boolean claimed = TyashoiProgress.claimed(player, experiment.id());
        for (int i = 0; i < costs.size(); i++) {
            lore.add(revealed(player, costs.get(i), i == 0 || claimed) ? costLine(player, costs.get(i), claimed)
                    : Text.of("<7>???" + (costs.get(i).amount() > 1 ? " <8>x" + costs.get(i).amount() : "")));
        }
        lore.add(Text.empty());
        if (TyashoiProgress.claimed(player, experiment.id())) lore.add(Text.of("<a>You already traded for this!"));
        else lore.add(Text.of("<e>Click to trade!"));
        return ItemStacks.lore(stack, lore);
    }

    private static ItemStack.Builder base(SkyBlockPlayer player, TyashoiExperiment experiment) {
        SkyBlockItem item = switch (experiment.kind()) {
            case ITEM -> new SkyBlockItem(ItemType.get(experiment.resultItem()));
            case PET -> EerieUpgrades.newPet(player);
            case BOOK -> {
                SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
                book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(
                        EnchantmentType.valueOf(experiment.enchantment()), experiment.enchantmentLevel()));
                yield book;
            }
        };
        return PlayerItemUpdater.playerUpdate(player, item.getItemStackBuilder().build());
    }

    private static boolean revealed(SkyBlockPlayer player, TyashoiExperiment.Cost cost, boolean known) {
        if (known || cost.isCoins()) return true;
        ItemType type = cost.type();
        return type != null && player.countItem(type) > 0;
    }

    private static Text costLine(SkyBlockPlayer player, TyashoiExperiment.Cost cost, boolean claimed) {
        if (cost.isCoins()) return Text.of("<6>{} Coins", StringUtility.commaify(cost.coins()));
        ItemType type = cost.type();
        Object color = type == null ? "white" : type.rarity.getColor();
        String amount = cost.amount() > 1 ? " <8>x" + cost.amount() : "";
        return Text.of("<color:{}>{}" + amount, color, cost.label());
    }
}
