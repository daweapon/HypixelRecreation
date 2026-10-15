package dev.daweapon.type.skyblockgeneric.gui.inventories.museum;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.museum.MuseumMilestones;
import dev.daweapon.type.skyblockgeneric.museum.MuseumService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIMuseumAppraisal extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Confirm Appraisal Service", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (s, c) -> ItemStacks.item(Material.GREEN_TERRACOTTA, 1, Text.of("<a>Confirm"), List.of(
                Text.of("<7>Unlock the <6>Museum Appraisal Service<7>."),
                Text.empty(),
                Text.of("<7>Cost"),
                Text.of("<6>{} Coins", StringUtility.commaify(MuseumMilestones.APPRAISAL_COST)),
                Text.empty(),
                Text.of("<e>Click to confirm!"))), (click, c) -> {
            MuseumService.buyAppraisal((SkyBlockPlayer) c.player());
            c.backOrClose();
        });
        layout.slot(15, (s, c) -> ItemStacks.item(Material.RED_TERRACOTTA, 1, Text.of("<c>Cancel"), List.of()),
                (click, c) -> c.backOrClose());
    }
}
