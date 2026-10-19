package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.firesale.ActiveFireSale;
import dev.daweapon.type.skyblockgeneric.firesale.FireSaleService;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

public final class FireSalesWidget implements TabWidget {
    @Override
    public String id() {
        return "fire_sales";
    }

    @Override
    public String displayName() {
        return "Fire Sales";
    }

    @Override
    public Material icon() {
        return Material.GOLD_INGOT;
    }

    @Override
    public int defaultPriority() {
        return 40;
    }

    @Override
    public String description() {
        return "Shows the Fire Sale that is active right now.";
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return FireSaleService.active().map(FireSalesWidget::render).orElse(List.of());
    }

    private static List<Text> render(ActiveFireSale active) {
        String stock = active.sale().unlimited() ? "Unlimited" : StringUtility.commaify((int) active.remaining());
        return List.of(
                Text.of("<6><l>Fire Sales:"),
                Text.of(" {}", active.sale().name()),
                Text.of(" <e>{} <7>left, ends in <e>{}", stock, TabTime.format(active.endsAt() - System.currentTimeMillis())));
    }
}
