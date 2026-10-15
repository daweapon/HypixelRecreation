package dev.daweapon.type.skyblockgeneric.gui.inventories.museum;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import dev.daweapon.type.skyblockgeneric.museum.MuseumCategory;
import dev.daweapon.type.skyblockgeneric.museum.MuseumMilestones;
import dev.daweapon.type.skyblockgeneric.museum.MuseumService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIYourMuseum extends StatelessView {
    private static final String MUSEUM_TEXTURE = "597e4e27a04afa5f06108265a9bfb797630391c7f3d880d244f610bb1ff393d8";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Your Museum", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        MuseumService.sync((SkyBlockPlayer) ctx.player());
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(4, (s, c) -> museumInfo(data(c)));

        for (MuseumCategory category : MuseumCategory.values()) {
            layout.slot(category.getMenuSlot(), (s, c) -> categoryButton(data(c), category),
                    (click, c) -> c.push(new GUIMuseumCategory(), GUIMuseumCategory.State.of(category)));
        }

        layout.slot(48, (s, c) -> milestonesButton(data(c)), (click, c) -> c.push(new GUIMuseumMilestones()));
        Components.close(layout, 49);
        layout.slot(50, (s, c) -> searchButton(), (click, c) -> openSearch(c));
        layout.slot(51, (s, c) -> appraisalButton(data(c)), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            DatapointMuseum.MuseumData data = player.getMuseumData();
            if (data.isHasBoughtAppraisalService()) return;
            if (MuseumService.milestone(data) < MuseumMilestones.APPRAISAL_MILESTONE) {
                player.sendMessage("<c>You need Museum Milestone {} to unlock the Appraisal Service!",
                        MuseumMilestones.APPRAISAL_MILESTONE);
                return;
            }
            c.push(new GUIMuseumAppraisal());
        });
    }

    static void openSearch(ViewContext ctx) {
        new HypixelSignGUI(ctx.player()).open(new String[]{"Enter query", ""}).thenAccept(line -> {
            if (line == null || line.isBlank()) return;
            ctx.push(new GUIMuseumCategory(), GUIMuseumCategory.State.search(line.trim()));
        });
    }

    static ItemStack.Builder searchButton() {
        return ItemStacks.item(Material.OAK_SIGN, 1, Text.of("<a>Museum Search"), List.of(
                Text.of("<7>Search your entire Museum for a"),
                Text.of("<7>specific item or armor set!"),
                Text.empty(),
                Text.of("<e>Click to search for an item!")));
    }

    static Text progressBar(long done, long total) {
        String bar = "--------------------";
        int filled = total == 0 ? 0 : (int) Math.min(bar.length(), done * bar.length() / total);
        return Text.of("<2><m>{}<f><m>{}<r> <b>{}<9>/<b>{}", bar.substring(0, filled), bar.substring(filled),
                StringUtility.commaify(done), StringUtility.commaify(total));
    }

    static String percent(long done, long total) {
        if (total == 0) return "0";
        double value = Math.round(done * 10000D / total) / 100D;
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static DatapointMuseum.MuseumData data(ViewContext ctx) {
        return ((SkyBlockPlayer) ctx.player()).getMuseumData();
    }

    private static ItemStack.Builder museumInfo(DatapointMuseum.MuseumData data) {
        long donated = MuseumService.donatedCount(data);
        long total = MuseumService.catalogue().progressEntries().size();
        long unavailable = MuseumService.catalogue().progressEntries().stream().filter(entry -> !entry.isAvailable()).count();
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>The <9>Museum <7>is a compendium"),
                Text.of("<7>of all of your items in"),
                Text.of("<7>SkyBlock. Donate items to your"),
                Text.of("<7>Museum to unlock rewards."),
                Text.empty(),
                Text.of("<7>Other players can visit your"),
                Text.of("<7>Museum at any time! Display your"),
                Text.of("<7>best items proudly for all to"),
                Text.of("<7>see."),
                Text.empty(),
                Text.of("<7>Items Donated: <e>{}<6>%", percent(donated, total)),
                progressBar(donated, total)));
        if (unavailable > 0) {
            lore.add(Text.empty());
            lore.add(Text.of("<8>{} of these items aren't in this", unavailable));
            lore.add(Text.of("<8>server yet."));
        }
        return ItemStacks.head(MUSEUM_TEXTURE, Text.of("<9>Museum"), lore);
    }

    private static ItemStack.Builder categoryButton(DatapointMuseum.MuseumData data, MuseumCategory category) {
        long donated = MuseumService.donatedCount(data, category);
        List<Text> lore = new ArrayList<>();
        if (category.isSpecial()) {
            lore.add(Text.of("<7>View all of the <d>Special Items <7>that you"));
            lore.add(Text.of("<7>have donated to the <9>Museum<7>!"));
            lore.add(Text.empty());
            lore.add(Text.of("<7>These items don't count towards"));
            lore.add(Text.of("<7>Museum progress and rewards, but"));
            lore.add(Text.of("<7>are cool nonetheless. Items that are"));
            lore.add(Text.of("<9>rare <7>and <6>prestigious <7>fit into this"));
            lore.add(Text.of("<7>category, and can be displayed in"));
            lore.add(Text.of("<7>the Museum."));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Items Donated: <b>{}<9>/<b>{}", donated, MuseumCategory.SPECIAL_ITEM_LIMIT));
        } else {
            long total = MuseumService.catalogue().entries(category).size();
            lore.add(Text.of("<7>View all of the items related to the"));
            lore.add(Text.of(category.getColor() + "{} Skill <7>that you have donated", category.getDisplayName()));
            lore.add(Text.of("<7>to the <9>Museum<7>!"));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Items Donated: <e>{}<6>%", percent(donated, total)));
            lore.add(progressBar(donated, total));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view!"));
        return ItemStacks.item(category.getIcon(), 1, Text.of(category.coloredName()), lore);
    }

    private static ItemStack.Builder milestonesButton(DatapointMuseum.MuseumData data) {
        return ItemStacks.item(Material.GOLD_BLOCK, 1, Text.of("<6>Museum Milestones"), List.of(
                Text.of("<7>Every <b>100 SkyBlock XP <7>obtained from"),
                Text.of("<7>your Museum, <6>Eleanor <7>will reward you."),
                Text.empty(),
                Text.of("<d>Special Items <7>do not reward SkyBlock"),
                Text.of("<7>XP."),
                Text.empty(),
                Text.of("<7>Total XP: <b>{}<3>/<b>{}", StringUtility.commaify(MuseumService.museumXp(data)),
                        StringUtility.commaify(MuseumService.catalogue().totalXp())),
                Text.of("<7>Milestone: <e>{}<6>/<e>{}", MuseumService.milestone(data), MuseumMilestones.MAX_MILESTONE),
                Text.empty(),
                Text.of("<e>Click to view!")));
    }

    private static ItemStack.Builder appraisalButton(DatapointMuseum.MuseumData data) {
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<6>Madame Goldsworth <7>offers an"),
                Text.of("<7>appraisal service for Museums. She"),
                Text.of("<7>will appraise the value of your"),
                Text.of("<7>Museum each time you add or remove"),
                Text.of("<7>items."),
                Text.empty(),
                Text.of("<7>This service also allows you to"),
                Text.of("<7>appear on the <6>Top Valued <7>filter in"),
                Text.of("<7>the <9>Museum Browser<7>."),
                Text.empty()));
        if (data.isHasBoughtAppraisalService()) {
            lore.add(Text.of("<7>Your Museum Value"));
            lore.add(Text.of("<6>{} Coins", StringUtility.commaify(MuseumService.museumValue(data))));
            lore.add(Text.empty());
            lore.add(Text.of("<8>The Museum Browser's Top Valued"));
            lore.add(Text.of("<8>ranking isn't in this server yet."));
        } else if (MuseumService.milestone(data) < MuseumMilestones.APPRAISAL_MILESTONE) {
            lore.add(Text.of("<c>Requires Museum Milestone {}!", MuseumMilestones.APPRAISAL_MILESTONE));
        } else {
            lore.add(Text.of("<7>Cost"));
            lore.add(Text.of("<6>{} Coins", StringUtility.commaify(MuseumMilestones.APPRAISAL_COST)));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to unlock!"));
        }
        return ItemStacks.item(Material.DIAMOND, 1, Text.of("<6>Museum Appraisal Service"), lore);
    }
}
