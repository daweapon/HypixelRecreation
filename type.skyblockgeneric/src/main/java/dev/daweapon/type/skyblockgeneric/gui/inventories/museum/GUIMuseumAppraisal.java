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
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIMuseumAppraisal extends StatelessView {
    private record Rank(int slot, String color, String name, double value, List<String> rewards, List<String> quote) {}

    private static final List<Rank> RANKS = List.of(
            new Rank(20, "<b>", "Wealthy", 10_000_000D, List.of("<d>Deluxe <7>Bank Upgrade"),
                    List.of("You are not only surviving,", "you are thriving!")),
            new Rank(21, "<d>", "Prosperous", 100_000_000D, List.of("<5>Super Deluxe <7>Bank Upgrade", "<d>The Hex"),
                    List.of("You are climbing the rungs", "of the social ladder!")),
            new Rank(22, "<6>", "Gilded", 500_000_000D, List.of("<c>Premier <7>Bank Upgrade", "<6>Shen's Auction"),
                    List.of("Not quite a", "billionaire...yet.")),
            new Rank(23, "<5>", "Affluent", 1_000_000_000D, List.of("<3>Luxurious <7>Bank Upgrade"),
                    List.of("Your wealth grants you", "influence in even the most", "exclusive circles!")),
            new Rank(24, "<4>", "Palatial", 2_500_000_000D, List.of("<4>Palatial <7>Bank Upgrade", "<6>Shen's Special"),
                    List.of("Your worth cannot be", "described by just a single", "number. Well actually...it can", "and that number is very large.")));

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Museum Appraisal", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(4, (s, c) -> ItemStacks.item(Material.DIAMOND, 1, Text.of("<e>Museum Appraisal Service"), List.of(
                Text.of("<6>Madame Goldsworth <7>offers an"),
                Text.of("<7>appraisal service for Museums."),
                Text.of("<7>When unlocked, she will appraise"),
                Text.of("<7>the value of your Museum each"),
                Text.of("<7>time you add or remove items."),
                Text.empty(),
                Text.of("<7>This service also allows you to"),
                Text.of("<7>appear on the <6>Top Valued"),
                Text.of("<7>filter in the <9>Museum"),
                Text.of("<9>Browser<7>."),
                Text.empty(),
                Text.of("<7>Cost"),
                Text.of("<6>{} Coins", StringUtility.commaify(MuseumMilestones.APPRAISAL_COST)),
                Text.empty(),
                Text.of("<e>Click to unlock!"))), (click, c) -> c.push(new Confirm()));
        for (Rank rank : RANKS) {
            layout.slot(rank.slot(), (s, c) -> rankItem(rank, ((SkyBlockPlayer) c.player()).getMuseumData()));
        }
        Components.close(layout, 40);
    }

    private static net.minestom.server.item.ItemStack.Builder rankItem(Rank rank, DatapointMuseum.MuseumData data) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Required Museum Value"));
        lore.add(Text.of("<6>{} Coins", StringUtility.commaify(rank.value())));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards:"));
        rank.rewards().forEach(reward -> lore.add(Text.of(reward)));
        lore.add(Text.empty());
        rank.quote().forEach(line -> lore.add(Text.of("<8><italic>{}", line)));
        lore.add(Text.empty());
        if (data.isHasBoughtAppraisalService() && MuseumService.museumValue(data) >= rank.value()) {
            lore.add(Text.of("<a>You have reached this rank!"));
        } else {
            lore.add(Text.of("<c>You have not reached this"));
            lore.add(Text.of("<c>rank!"));
        }
        return ItemStacks.item(Material.PAPER, 1, Text.of(rank.color() + rank.name()), lore);
    }

    private static final class Confirm extends StatelessView {
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
}
