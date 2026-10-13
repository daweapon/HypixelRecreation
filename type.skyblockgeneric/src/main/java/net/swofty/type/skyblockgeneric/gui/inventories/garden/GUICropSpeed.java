package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.OptionalInt;

public class GUICropSpeed extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_garden_upgrades.speed.title"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        for (int i = 0; i < GardenGrids.CROP_ORDER.size(); i++) {
            GardenCrop crop = GardenGrids.CROP_ORDER.get(i);
            layout.slot(GardenGrids.CROP_SLOTS.get(i),
                    (_, c) -> cropItem(crop, GardenService.profile((SkyBlockPlayer) c.player())),
                    (click, c) -> click(crop, click, c));
        }
    }

    private ItemStack.Builder cropItem(GardenCrop crop, GardenProfile profile) {
        String name = crop.getDisplayName();
        return GardenMenus.item(GardenMenus.icon(crop), Text.key("gui_garden_upgrades.speed.name", name), List.of(
                Text.key("gui_garden_upgrades.speed.lore.1"),
                Text.key("gui_garden_upgrades.speed.lore.2", name),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.speed.current",
                        GardenMenus.number(profile.speedCap(crop, GardenProfile.DEFAULT_SPEED_CAP))),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.speed.set"),
                Text.key("gui_garden_upgrades.speed.reset")));
    }

    private void click(GardenCrop crop, ClickContext<DefaultState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (click.click() instanceof Click.Right) {
            GardenProfile profile = GardenService.profile(player);
            profile.resetSpeedCap(crop);
            Gardens.save(player, profile);
            player.sendMessage(Text.key("gui_garden_upgrades.speed.reset_done", crop.getDisplayName(),
                    GardenMenus.number(GardenProfile.DEFAULT_SPEED_CAP)));
            ctx.session(DefaultState.class).refresh();
            return;
        }
        if (!(click.click() instanceof Click.Left)) {
            return;
        }
        player.closeInventory();
        new HypixelSignGUI(player)
                .open(new String[]{"Enter your", "Speed Cap"})
                .thenAccept(input -> applyInput(player, crop, input));
    }

    private void applyInput(SkyBlockPlayer player, GardenCrop crop, String input) {
        if (input == null) {
            return;
        }
        OptionalInt cap = GardenGrids.parseSpeedCap(input);
        if (cap.isPresent()) {
            GardenProfile profile = GardenService.profile(player);
            profile.setSpeedCap(crop, cap.getAsInt());
            Gardens.save(player, profile);
            player.sendMessage(Text.key("gui_garden_upgrades.speed.updated", crop.getDisplayName(), GardenMenus.number(cap.getAsInt())));
        } else {
            player.sendMessage(Text.key("gui_garden_upgrades.speed.invalid", GardenMenus.number(GardenGrids.MAX_SPEED_CAP)));
        }
        player.openView(new GUICropSpeed());
    }
}
