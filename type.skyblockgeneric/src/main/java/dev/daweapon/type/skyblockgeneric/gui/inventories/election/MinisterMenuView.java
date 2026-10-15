package dev.daweapon.type.skyblockgeneric.gui.inventories.election;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.PlayerSkin;
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
import dev.daweapon.type.skyblockgeneric.elections.ElectionData;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.PerkLore;

import java.util.ArrayList;
import java.util.List;

// GUI showing the current SkyBlock minister and their active perk
public class MinisterMenuView extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        SkyBlockMayor minister = ElectionManager.getCurrentMinister();
        if (minister == null) {
            return ViewConfiguration.translatable("gui_election.minister.title_fallback", InventoryType.CHEST_4_ROW);
        }
        return ViewConfiguration.withText(
                (s, ctx) -> Text.key("gui_election.minister.title", minister.getDisplayName()),
                InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);

        SkyBlockMayor minister = ElectionManager.getCurrentMinister();
        if (minister == null) return;

        ElectionData data = ElectionManager.getElectionData();
        String ministerColor = data.getCurrentMinisterColor();
        SkyBlockMayor.Perk activePerk = data.getMinisterPerkEnum();

        layout.slot(11, (s, c) -> {
            List<Text> lore = new ArrayList<>();
            lore.add(Text.key("gui_election.minister.perk_label"));
            lore.add(Text.empty());
            lore.add(Text.of("<8><m>--------------------------"));

            if (activePerk != null) {
                lore.add(ElectionData.colored(ministerColor, activePerk.getDisplayName()));
                lore.addAll(PerkLore.description(activePerk, 50));
            }

            lore.add(Text.of("<8><m>--------------------------"));
            lore.add(Text.empty());
            lore.add(Text.key("gui_election.minister.perk_footer_1"));
            lore.add(Text.key("gui_election.minister.perk_footer_2"));
            lore.add(Text.key("gui_election.minister.perk_footer_3"));

            return ItemStacks.head(
                    new PlayerSkin(minister.getTexture(), minister.getSignature()),
                    ElectionData.colored(ministerColor, "Minister " + minister.getDisplayName()),
                    lore
            );
        });

        if (DianaOffice.isDiana(minister)) {
            DianaOffice.layoutSpade(layout, DianaOffice.MINISTER_SPADE_SLOT);
        }

        layout.slot(15, (s, c) -> {
            ElectionData.ElectionResult lastResult = data.getLastElectionResult();
            if (lastResult == null) {
                return ItemStacks.item(Material.JUKEBOX, 1, Text.key("gui_election.mayor.results_title"), List.of(
                        Text.key("gui_election.mayor.results_no_data_1"),
                        Text.key("gui_election.mayor.results_no_data_2")
                ));
            }

            SkyBlockMayor currentMayor = ElectionManager.getCurrentMayor();
            String mayorName = currentMayor != null ? currentMayor.getDisplayName() : "???";
            List<Text> resultLore = new ArrayList<>();
            resultLore.add(Text.key("gui_election.mayor.results_year", lastResult.getYear()));
            resultLore.add(Text.empty());

            List<ElectionData.CandidateResult> results = lastResult.getCandidateResults();
            for (int i = 0; i < results.size(); i++) {
                ElectionData.CandidateResult cr = results.get(i);
                TextColor clr = ElectionData.colorForIndex(i);
                SkyBlockMayor m = null;
                try { m = SkyBlockMayor.valueOf(cr.getMayorName()); } catch (IllegalArgumentException ignored) {}
                String name = m != null ? m.getDisplayName() : cr.getMayorName();
                resultLore.add(Text.of("<color:{0}>{1}", clr, String.format("%.1f%%", cr.getPercentage()))
                        .append("<8> ○ ")
                        .append("<color:{0}>{1} votes", clr, String.format("%,d", cr.getVotes()))
                        .append("<8> | ")
                        .append("<color:{0}>{1}", clr, name));
            }

            resultLore.add(Text.empty());
            resultLore.add(Text.key("gui_election.mayor.results_footer_1"));
            resultLore.add(Text.key("gui_election.mayor.results_footer_2", mayorName));
            resultLore.add(Text.key("gui_election.mayor.results_footer_3"));

            return ItemStacks.item(Material.JUKEBOX, 1, Text.key("gui_election.mayor.results_title"), resultLore);
        });
    }
}
