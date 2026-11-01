package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.fasttravel;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollIslands;

import java.util.ArrayList;
import java.util.List;

// GUI for fast-traveling between unlocked islands via travel scrolls
public class GUIFastTravel extends StatelessView {
    private static final java.util.Map<TravelScrollIslands, Integer> SLOTS = java.util.Map.ofEntries(
            java.util.Map.entry(TravelScrollIslands.PRIVATE_ISLAND, 10),
            java.util.Map.entry(TravelScrollIslands.SKYBLOCK_HUB, 11),
            java.util.Map.entry(TravelScrollIslands.DUNGEON_HUB, 12),
            java.util.Map.entry(TravelScrollIslands.THE_BARN, 13),
            java.util.Map.entry(TravelScrollIslands.THE_PARK, 14),
            java.util.Map.entry(TravelScrollIslands.MOONGLADE_MARSH, 15),
            java.util.Map.entry(TravelScrollIslands.TORRHUS_CANYON, 16),
            java.util.Map.entry(TravelScrollIslands.GOLD_MINE, 19),
            java.util.Map.entry(TravelScrollIslands.DEEP_CAVERNS, 20),
            java.util.Map.entry(TravelScrollIslands.DWARVEN_MINES, 21),
            java.util.Map.entry(TravelScrollIslands.CRYSTAL_HOLLOWS, 22),
            java.util.Map.entry(TravelScrollIslands.SPIDERS_DEN, 23),
            java.util.Map.entry(TravelScrollIslands.THE_END, 24),
            java.util.Map.entry(TravelScrollIslands.CRIMSON_ISLE, 25),
            java.util.Map.entry(TravelScrollIslands.THE_GARDEN, 29),
            java.util.Map.entry(TravelScrollIslands.THE_RIFT, 30),
            java.util.Map.entry(TravelScrollIslands.BACKWATER_BAYOU, 31),
            java.util.Map.entry(TravelScrollIslands.LOTUS_ATOLL, 32),
            java.util.Map.entry(TravelScrollIslands.JERRYS_WORKSHOP, 33)
    );

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Fast Travel", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        boolean shouldBePaper = player.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

        layout.slot(53, (s, c) -> {
            boolean isPaper = ((SkyBlockPlayer) c.player()).getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);
            return ItemStacks.item(isPaper ? Material.FILLED_MAP : Material.MAP, 1, """
                    <a>Paper Icons
                    <7>Use paper icons, which may load this menu
                    <7>faster on your computer.

                    <7>Enabled: {}

                    <e>Click to toggle!""", isPaper ? Text.of("<a>ON") : Text.of("<c>OFF"));
        }, (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            p.getToggles().inverse(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);
            c.replace(new GUIFastTravel());
        });

        for (TravelScrollIslands island : TravelScrollIslands.values()) {
            Integer islandSlot = SLOTS.get(island);
            if (islandSlot == null) continue;
            int slot = islandSlot;
            boolean hasSubMenu = !island.getAssociatedScrolls().isEmpty();

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean hasUnlockedIsland = island.isAvailable() && island.isUnlocked(p);
                boolean isPaper = p.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<8>/warp {}", island.getWarpCommandName()));
                lore.add(Text.literal(" "));

                lore.addAll(Text.of("<7><wrap:30>{}</wrap>", island.getDescription().apply(hasUnlockedIsland)).lines());
                lore.add(Text.literal(" "));

                if (island.getAssociatedSkill() != null) {
                    lore.add(Text.of("<7>Main Skill: <b>{}", island.getAssociatedSkill()));
                    lore.add(Text.of("<7>Island tier: <e>{}", StringUtility.getAsRomanNumeral(island.getIslandTier())));
                    lore.add(Text.literal(" "));
                }

                if (!island.isAvailable()) {
                    lore.add(Text.of("<c>Not in SkyBlock yet!"));
                    if (hasSubMenu) lore.add(Text.of("<e>Left-Click to open!"));
                } else if (!hasUnlockedIsland) {
                    lore.add(Text.of("<c>Warp not unlocked!"));
                    if (hasSubMenu) lore.add(Text.of("<e>Left-Click to open!"));
                } else {
                    if (hasSubMenu) {
                        lore.add(Text.of("<8>Right-Click to warp!"));
                        lore.add(Text.of("<e>Left-Click to open!"));
                    } else {
                        lore.add(Text.of("<e>Click to warp!"));
                    }
                }

                Text name = island.getDescriptiveName();
                if (isPaper) {
                    return ItemStacks.item(hasUnlockedIsland ? Material.PAPER : Material.BEDROCK, 1, name, lore);
                } else {
                    return ItemStacks.head(
                            hasUnlockedIsland ? island.getTexture() : "da99b05b9a1db4d29b5e673d77ae54a77eab66818586035c8a2005aeb810602a",
                            1, name, lore);
                }
            }, (click, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean hasUnlockedIsland = island.isUnlocked(p);

                if (!hasSubMenu && !island.isAvailable()) {
                    p.sendMessage(TravelScrollIslands.NOT_AVAILABLE_MESSAGE);
                    return;
                }

                if (!hasSubMenu && !hasUnlockedIsland) {
                    p.sendMessage("<c>You haven't unlocked this fast travel destination!");
                    return;
                }

                if (hasSubMenu && !(click.click() instanceof Click.Right)) {
                    c.push(new GUIFastTravelSubMenu(island));
                    return;
                }

                if (!island.isAvailable()) {
                    p.sendMessage(TravelScrollIslands.NOT_AVAILABLE_MESSAGE);
                    return;
                }

                if (!hasUnlockedIsland) {
                    p.sendMessage("<c>You haven't unlocked this fast travel destination!");
                    return;
                }

                Text blocked = island.entryBlockReason(p);
                if (blocked != null) {
                    p.sendMessage(blocked);
                    return;
                }

                if (!hasSubMenu) {
                    p.closeInventory();
                    p.sendMessage("<7>Warping you to {}<7>...", island.getDescriptiveName());
                    p.asProxyPlayer().transferToWithIndication(island.getServerType()).thenRun(() -> {
                        p.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", island.getDescriptiveName());
                    });
                    return;
                }

                p.closeInventory();
                p.sendMessage("<7>Warping you to {}<7>...", island.getDescriptiveName());
                p.asProxyPlayer().transferToWithIndication(island.getServerType()).thenRun(() -> {
                    p.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", island.getDescriptiveName());
                });
            });
        }
    }
}
