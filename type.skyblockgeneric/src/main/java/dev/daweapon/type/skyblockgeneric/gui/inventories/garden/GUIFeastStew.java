package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
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
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastCalendar;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastData;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIFeastStew extends StatelessView {
    private static final int PROGRESS_SLOT = 4;
    private static final int ROW_START = 18;
    private static final int ROW_WIDTH = 9;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Communal Stew"), InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 39, ctx);
        Components.close(layout, 40);
        layout.slot(PROGRESS_SLOT, (_, c) -> progressItem((SkyBlockPlayer) c.player()));

        int tiers = FeastService.targets().length;
        int offset = ROW_START + (ROW_WIDTH - tiers) / 2;
        for (int tier = 0; tier < tiers; tier++) {
            int index = tier;
            layout.slot(offset + tier, (_, c) -> tierItem((SkyBlockPlayer) c.player(), index), (_, c) -> click(c, index));
        }
    }

    private ItemStack.Builder progressItem(SkyBlockPlayer player) {
        FeastData data = FeastService.data(Gardens.profile(player));
        int[] targets = FeastService.targets();
        int claimed = data.getClaimed();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Seasoning you find while farming is"));
        lore.add(Text.of("<7>donated to the Communal Stew."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Seasoning: <a>{}", StringUtility.commaify(data.getSeasoning())));
        if (claimed < targets.length) {
            lore.add(Text.of("<7>Next tier: <a>{}<7>/<a>{}", StringUtility.commaify(data.getSeasoning()), StringUtility.commaify(targets[claimed])));
        } else {
            lore.add(Text.of("<a>All donation tiers claimed!"));
        }
        if (FeastCalendar.grand()) lore.add(Text.of("<7>Kernels: <e>{}", StringUtility.commaify(data.getKernels())));
        return ItemStacks.item(Material.BREAD, Text.of("<6>Communal Stew"), lore);
    }

    private ItemStack.Builder tierItem(SkyBlockPlayer player, int tier) {
        GardenProfile profile = Gardens.profile(player);
        FeastData data = FeastService.data(profile);
        int target = FeastService.targets()[tier];
        boolean grand = FeastCalendar.grand();
        boolean last = tier == FeastService.targets().length - 1;
        String roman = StringUtility.getAsRomanNumeral(tier + 1);
        boolean claimed = data.getClaimed() > tier;
        boolean reached = data.getSeasoning() >= target;

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Donate <a>{} Seasoning <7>to the", StringUtility.commaify(target)));
        lore.add(Text.of("<7>Communal Stew."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards:"));
        lore.add(Text.of(" <5>{}x Feast Flask", FeastService.flasksFor(player, tier)));
        lore.add(Text.of(" <a>{} Carnival Tickets", FeastService.TICKETS_PER_MILESTONE));
        if (grand) lore.add(Text.of(" <e>{} Kernels", FeastService.GRAND_KERNELS[tier]));
        if (last && !grand) lore.add(Text.of(" <6>Commemorative Mason Jar"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Progress: <a>{}<7>/<a>{}", StringUtility.commaify(Math.min(data.getSeasoning(), target)), StringUtility.commaify(target)));
        lore.add(Text.empty());

        if (claimed) {
            lore.add(Text.of("<a>Reward claimed!"));
            return ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, tier + 1, Text.of("<a>Tier " + roman), lore);
        }
        if (reached && data.getClaimed() == tier) {
            lore.add(Text.of("<e>Click to claim!"));
            return ItemStacks.item(Material.YELLOW_STAINED_GLASS_PANE, tier + 1, Text.of("<e>Tier " + roman), lore);
        }
        lore.add(reached ? Text.of("<c>Claim the previous tier first!") : Text.of("<c>Find more Seasoning!"));
        return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, tier + 1, Text.of("<c>Tier " + roman), lore);
    }

    private void click(ViewContext ctx, int tier) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        FeastData data = FeastService.data(Gardens.profile(player));
        if (tier < data.getClaimed()) return;
        if (tier > data.getClaimed()) {
            player.sendMessage(Text.of("<c>Claim the previous tier first!"));
            return;
        }
        if (!FeastService.claim(player)) player.sendMessage(Text.of("<c>You haven't donated enough Seasoning for this tier yet!"));
        ctx.session(DefaultState.class).refresh();
    }
}
