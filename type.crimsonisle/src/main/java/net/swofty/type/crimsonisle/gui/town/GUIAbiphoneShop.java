package net.swofty.type.crimsonisle.gui.town;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * The "Abiphone Shop" front menu shared by Udel and Nuvian (wiki Udel/UI, Nuvian/UI). Each model
 * opens its own catalogue of colours. Nuvian's menu still shows the Flip+ as coming soon.
 */
public class GUIAbiphoneShop extends StatelessView {
    private record Model(String name, ItemType icon, String shop, List<String> features) {
    }

    private static final List<Model> MODELS = List.of(
            new Model("Abiphone X", ItemType.ABIPHONE_X_RED, "abiphone_x", List.of("<a><l> Contacts Directory")),
            new Model("Abiphone XI", ItemType.ABIPHONE_XI_YELLOW, "abiphone_xi", List.of("<a><l> Contacts Directory")),
            new Model("Abiphone XII", ItemType.ABIPHONE_XII_AMBER, "abiphone_xii",
                    List.of("<a><l> Contacts Directory", "<c><l> Do Not Disturb Mode")),
            new Model("Abiphone XIII", ItemType.ABIPHONE_XIII_JADE, "abiphone_xiii",
                    List.of("<a><l> Contacts Directory", "<c><l> Do Not Disturb Mode", "<d><l> Snake")),
            new Model("Abiphone XIV", ItemType.ABIPHONE_XIV_ORANGE, "abiphone_xiv",
                    List.of("<a><l> Contacts Directory", "<c><l> Do Not Disturb Mode", "<d><l> Tic Tac Toe", "<d><l> Snake")),
            new Model("Abiphone Flip", ItemType.ABIPHONE_FLIP_DRAGON, "abiphone_flip",
                    List.of("<a><l> Contacts Directory", "<c><l> Do Not Disturb Mode", "<6><l> Ringtones",
                            "<d><l> Tic Tac Toe", "<d><l> Snake")),
            new Model("Abiphone Flip+", ItemType.ABIPHONE_FLIP_PLUS_FLOWER, "abiphone_flip_plus",
                    List.of("<a><l> Contacts Directory", "<c><l> Do Not Disturb Mode", "<6><l> Ringtones",
                            "<d><l> Tic Tac Toe", "<d><l> Snake", "<b><l> Speed Dial")));

    private final boolean flipPlusOnSale;

    public GUIAbiphoneShop(boolean flipPlusOnSale) {
        this.flipPlusOnSale = flipPlusOnSale;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Abiphone Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        for (int i = 0; i < MODELS.size(); i++) {
            Model model = MODELS.get(i);
            boolean onSale = flipPlusOnSale || !model.shop().equals("abiphone_flip_plus");
            layout.slot(19 + i, (_, c) -> icon((SkyBlockPlayer) c.player(), model, onSale), (_, c) -> {
                if (onSale) c.push(new CatalogueShopView(model.shop()));
            });
        }
    }

    private static ItemStack.Builder icon(SkyBlockPlayer player, Model model, boolean onSale) {
        List<Component> lore = new java.util.ArrayList<>();
        lore.add(line("<7>Features:"));
        model.features().forEach(feature -> lore.add(line(feature)));
        lore.add(Component.empty());
        lore.add(line(onSale ? "<e>Click to browse!" : "<c><l>COMING SOON!"));
        return PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(model.icon()).getItemStack())
                .set(DataComponents.CUSTOM_NAME, line("<a>" + model.name()))
                .set(DataComponents.LORE, lore);
    }

    private static Component line(String markup) {
        return Text.read(markup).asComponent().decoration(TextDecoration.ITALIC, false);
    }
}
