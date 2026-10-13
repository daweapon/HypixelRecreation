package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
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
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorData;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorOffer;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIVisitor extends StatelessView {
    private final String visitorId;

    public GUIVisitor(String visitorId) {
        this.visitorId = visitorId;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        VisitorCatalogue.Visitor visitor = VisitorCatalogue.get().visitor(visitorId);
        return new ViewConfiguration<>(Text.of(visitor == null ? "Visitor" : visitor.name()), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);

        layout.slot(13, (_, c) -> head((SkyBlockPlayer) c.player()));
        layout.slot(29, (_, c) -> accept((SkyBlockPlayer) c.player()), (_, c) -> give(c));
        layout.slot(33, (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, Text.of("<c>Refuse Offer"), List.of(
                Text.of("<7>Send this visitor away. You will"),
                Text.of("<7>not receive any rewards."),
                Text.empty(),
                Text.of("<e>Click to refuse!"))), (_, c) -> refuse(c));
    }

    private ItemStack.Builder head(SkyBlockPlayer player) {
        VisitorCatalogue.Visitor visitor = VisitorCatalogue.get().visitor(visitorId);
        VisitorData data = Gardens.profile(player).visitors();
        List<Text> lore = new ArrayList<>();
        if (visitor != null) {
            lore.add(Text.of(visitor.rarity().getColour() + "<l>" + visitor.rarity().getLabel()));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Times Visited: <a>{}", data.timesVisited(visitorId)));
        lore.add(Text.of("<7>Offers Accepted: <a>{}", data.offersAccepted(visitorId)));
        return ItemStacks.item(Material.PLAYER_HEAD, Text.of(visitor == null ? visitorId : visitor.coloured()), lore);
    }

    private ItemStack.Builder accept(SkyBlockPlayer player) {
        VisitorData.Present present = Gardens.profile(player).visitors().find(visitorId);
        if (present == null) {
            return ItemStacks.item(Material.BARRIER, Text.of("<c>This visitor has left!"), List.of());
        }
        VisitorOffer offer = present.getOffer();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Items Required:"));
        for (VisitorOffer.Required required : offer.required()) {
            lore.add(Text.of(" <a>{} <8>x<a>{}", required.name(), StringUtility.commaify(required.remaining())));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards:"));
        lore.add(Text.of(" <8>+<3>{} Farming XP", StringUtility.shortenNumber(offer.farmingXp())));
        lore.add(Text.of(" <8>+<a>{} Garden Experience", offer.gardenXp()));
        lore.add(Text.of(" <8>+<c>{} Copper", offer.copper()));
        lore.add(Text.of(" <8>+<b>{} Bits", offer.bits()));
        lore.add(Text.empty());
        if (!VisitorService.hasAnythingToGive(player, offer)) {
            lore.add(Text.of("<c>Missing items to accept!"));
        } else {
            lore.add(Text.of("<e>Click to give some of the items!"));
        }
        lore.add(Text.of("<7>This visitor accepts any quantity of the"));
        lore.add(Text.of("<7>required items until you've given the"));
        lore.add(Text.of("<7>full amount!"));
        return ItemStacks.item(Material.GREEN_TERRACOTTA, Text.of("<a>Accept Offer"), lore);
    }

    private void give(ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        VisitorService.GiveResult result = VisitorService.give(player, visitorId);
        switch (result) {
            case NOTHING -> player.sendMessage(Text.of("<c>You don't have any of the items this visitor wants!"));
            case PARTIAL -> ctx.session(DefaultState.class).refresh();
            case COMPLETED -> player.closeInventory();
        }
    }

    private void refuse(ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenProfile profile = Gardens.profile(player);
        if (profile.visitors().find(visitorId) != null) {
            VisitorService.refuse(player, visitorId);
        }
        player.closeInventory();
    }
}
