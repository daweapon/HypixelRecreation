package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.fasttravel;

import net.minestom.server.inventory.InventoryType;
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
import dev.daweapon.type.skyblockgeneric.warps.ScrollUnlockReason;
import dev.daweapon.type.skyblockgeneric.warps.ScrollWarp;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollIslands;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollType;

import java.util.ArrayList;
import java.util.List;

public class GUIFastTravelSubMenu extends StatelessView {
    private static final int[] FEW_SLOTS = new int[]{20, 21, 22, 23, 24};
    private static final int[] HUB_OVERFLOW_SLOTS = new int[]{20, 22, 24};
    private final TravelScrollIslands island;

    public GUIFastTravelSubMenu(TravelScrollIslands island) {
        this.island = island;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(island.getWarpMenuTitle(), rows() == 5 ? InventoryType.CHEST_5_ROW : InventoryType.CHEST_4_ROW);
    }

    private boolean crimsonLayout() {
        return island == TravelScrollIslands.CRIMSON_ISLE;
    }

    private int rows() {
        return crimsonLayout() || island == TravelScrollIslands.SKYBLOCK_HUB ? 5 : 4;
    }

    private int[] warpSlots(int count) {
        if (count == 1) return new int[]{13};
        if (count == 2) return new int[]{12, 14};
        if (count == 3) return new int[]{11, 13, 15};
        int[] slots = new int[count];
        if (count <= 7) {
            int start = 10 + (7 - count) / 2;
            for (int i = 0; i < count; i++) slots[i] = start + i;
            return slots;
        }
        for (int i = 0; i < count; i++) slots[i] = i < 7 ? 10 + i : HUB_OVERFLOW_SLOTS[Math.min(i - 7, HUB_OVERFLOW_SLOTS.length - 1)];
        return slots;
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, rows() == 5 ? 40 : 31);
        Components.back(layout, rows() == 5 ? 39 : 30, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        boolean shouldBePaper = player.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

        List<TravelScrollType> scrolls = island.getAssociatedScrolls();
        int[] allSlots = crimsonLayout() ? new int[0] : warpSlots(scrolls.size() + 1);
        int mainSlot = crimsonLayout() ? 13 : allSlots[0];

        layout.slot(mainSlot, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            boolean isPaper = p.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);
            boolean unlocked = island.isAvailable() && island.isUnlocked(p);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<8>/warp {}", island.getWarpCommandName()));
            lore.add(Text.literal(" "));

            lore.addAll(Text.of("<7><wrap:30>{}</wrap>", island.getDescription().apply(true)).lines());
            lore.add(Text.literal(" "));

            if (island.getAssociatedSkill() != null) {
                lore.add(Text.of("<7>Main Skill: <b>{}", island.getAssociatedSkill()));
                lore.add(Text.of("<7>Island tier: <e>{}", StringUtility.getAsRomanNumeral(island.getIslandTier())));
                lore.add(Text.literal(" "));
            }

            if (!island.isAvailable()) {
                lore.add(Text.of("<c>Not in SkyBlock yet!"));
            } else {
                lore.add(Text.of(unlocked ? "<e>Click to warp!" : "<c>Warp not unlocked!"));
            }

            Text name = island.getDescriptiveName();
            if (isPaper) {
                return ItemStacks.item(unlocked ? Material.PAPER : Material.BEDROCK, 1, name, lore);
            } else {
                return ItemStacks.head(unlocked ? island.getTexture() : "da99b05b9a1db4d29b5e673d77ae54a77eab66818586035c8a2005aeb810602a", 1, name, lore);
            }
        }, (click, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            if (!island.isAvailable()) {
                p.sendMessage(TravelScrollIslands.NOT_AVAILABLE_MESSAGE);
                return;
            }
            if (!island.isUnlocked(p)) {
                p.sendMessage("<c>You haven't unlocked this fast travel destination!");
                return;
            }
            Text blocked = island.entryBlockReason(p);
            if (blocked != null) {
                p.sendMessage(blocked);
                return;
            }
            p.closeInventory();
            p.sendMessage("<7>Warping you to {}<7>...", island.getDescriptiveName());
            p.asProxyPlayer().transferToWithIndication(island.getServerType()).thenRun(() -> {
                p.asProxyPlayer().sendMessage("<7>You have been warped to {}<7>!", island.getDescriptiveName());
            });
        });

        int[] slots = crimsonLayout() ? FEW_SLOTS : java.util.Arrays.copyOfRange(allSlots, 1, allSlots.length);
        for (int i = 0; i < scrolls.size() && i < slots.length; i++) {
            TravelScrollType scroll = scrolls.get(i);
            int slot = slots[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean isUnlocked = p.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.USED_SCROLLS, DatapointStringList.class)
                        .getValue()
                        .contains(scroll.getInternalName());
                boolean isPaper = p.getToggles().get(DatapointToggles.Toggles.ToggleType.PAPER_ICONS);

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<8>/warp {}", scroll.getInternalName()));
                lore.add(Text.literal(" "));

                lore.addAll(Text.of("<7><wrap:50>{}</wrap>", scroll.getDescription()).lines());
                lore.add(Text.literal(" "));

                if (!island.isAvailable()) {
                    lore.add(Text.of("<c>Not in SkyBlock yet!"));
                } else if (!isUnlocked) {
                    ScrollUnlockReason unlockReason = scroll.getUnlockReason();
                    lore.add(unlockReason.getTitleReason());
                    lore.add(unlockReason.getSubReason());
                    lore.add(Text.literal(" "));
                    lore.add(Text.of("<c>Warp not unlocked!"));
                } else {
                    lore.add(Text.of("<e>Click to warp!"));
                }

                Text name = scroll.getDisplayName();
                if (isPaper) {
                    return ItemStacks.item(isUnlocked ? Material.PAPER : Material.BEDROCK, 1, name, lore);
                } else {
                    return ItemStacks.head(
                            isUnlocked ? scroll.getHeadTexture() : "da99b05b9a1db4d29b5e673d77ae54a77eab66818586035c8a2005aeb810602a",
                            1, name, lore);
                }
            }, (click, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean isUnlocked = p.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.USED_SCROLLS, DatapointStringList.class)
                        .getValue()
                        .contains(scroll.getInternalName());

                if (!island.isAvailable()) {
                    p.sendMessage(TravelScrollIslands.NOT_AVAILABLE_MESSAGE);
                    return;
                }
                if (!isUnlocked) {
                    p.sendMessage("<c>You haven't unlocked this fast travel destination!");
                    return;
                }
                Text blocked = island.entryBlockReason(p);
                if (blocked != null) {
                    p.sendMessage(blocked);
                    return;
                }

                p.closeInventory();
                p.sendMessage("<7>Warping you to {}<7>...", scroll.getDisplayName());
                ScrollWarp.travel(p, scroll, island.getServerType());
            });
        }
    }
}
