package dev.daweapon.type.galatea.gui;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

public class GUITeddyTips extends StatelessView {
    private static final int[] GRID = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

    public record Tip(String key, String name, Material icon, long coins) {
    }

    public static final List<Tip> TIPS = List.of(
            new Tip("mossybit", "Mossybit", Material.SLIME_BALL, 5000),
            new Tip("mudworm", "Mudworm", Material.DIRT, 5000),
            new Tip("coralot", "Coralot", Material.AXOLOTL_BUCKET, 10000),
            new Tip("hideonleaf", "Hideonleaf", Material.SHULKER_SHELL, 10000),
            new Tip("lumisquid", "Lumisquid", Material.GLOW_INK_SAC, 5000),
            new Tip("joydive", "Joydive", Material.HEART_OF_THE_SEA, 10000),
            new Tip("cod", "Cod", Material.COD, 5000),
            new Tip("pandas", "Bambuleaf and Mochibear", Material.BAMBOO, 20000),
            new Tip("salmon", "Salmon", Material.SALMON, 5000),
            new Tip("invisibug", "Invisibug", Material.SPIDER_EYE, 10000),
            new Tip("shellwise", "Shellwise", Material.NAUTILUS_SHELL, 15000),
            new Tip("spike", "Spike", Material.PUFFERFISH, 15000));

    private final HypixelNPC teddy;

    public GUITeddyTips(HypixelNPC teddy) {
        this.teddy = teddy;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Teddy", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        for (int index = 0; index < TIPS.size(); index++) {
            Tip tip = TIPS.get(index);
            layout.slot(GRID[index], ItemStacks.item(tip.icon(), "<a>" + tip.name() + "\n<7>Ask the legendary hunter how to\n<7>catch this one, for a price.\n\n<7>Cost\n<6>" + StringUtility.commaify(tip.coins()) + " Coins\n\n<e>Click to pay!"),
                    (_, c) -> {
                        SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                        ShopPrice price = new CoinShopPrice(tip.coins());
                        if (!price.canAfford(player)) {
                            player.sendMessage("<c>You don't have enough {}!", price.getNamePlural());
                            return;
                        }
                        price.processPurchase(player);
                        player.playSuccessSound();
                        player.closeInventory();
                        teddy.setDialogue(player, "tip-" + tip.key());
                    });
        }
    }
}
