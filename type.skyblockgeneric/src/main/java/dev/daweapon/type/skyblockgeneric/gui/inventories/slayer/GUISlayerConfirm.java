package dev.daweapon.type.skyblockgeneric.gui.inventories.slayer;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerService;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTierDefinition;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

public class GUISlayerConfirm extends StatelessView {
    private final String confirmLore;
    private final Consumer<SkyBlockPlayer> onConfirm;

    private GUISlayerConfirm(String confirmLore, Consumer<SkyBlockPlayer> onConfirm) {
        this.confirmLore = confirmLore;
        this.onConfirm = onConfirm;
    }

    public static GUISlayerConfirm startQuest(SlayerMenuStyle style, SlayerTierDefinition tier) {
        return new GUISlayerConfirm("<7>Kill " + style.mobPlural() + " to spawn the boss!\n\n<e>Click to start quest!", player -> {
            SlayerService.StartResult result = SlayerService.startQuest(player, style.type(), tier.tier());
            if (!result.success()) {
                player.sendMessage(Text.of("<c>{}", result.message()));
            }
        });
    }

    public static GUISlayerConfirm cancelQuest() {
        return new GUISlayerConfirm("<7>Cancel your ongoing Slayer quest.\n<c>The coins you paid are not refunded.\n\n<e>Click to cancel quest!",
                SlayerService::cancelQuest);
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Confirm", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, ItemStacks.item(Material.LIME_TERRACOTTA, "<a>Confirm\n" + confirmLore), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            player.closeInventory();
            onConfirm.accept(player);
        });
        layout.slot(15, ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel"), (click, c) -> c.backOrClose());
    }
}
