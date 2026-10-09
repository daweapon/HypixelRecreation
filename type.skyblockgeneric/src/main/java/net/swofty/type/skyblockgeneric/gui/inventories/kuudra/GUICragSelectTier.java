package net.swofty.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.queue.KuudraPartyCheck;
import net.swofty.type.skyblockgeneric.crimson.kuudra.queue.KuudraQueue;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Crag's "Select Tier" (the Burning capture): the five tiers at row 2, columns 3 to 7, then Find a Party, Close and
 * Toggle Auto Ready Up. Clicking a tier queues it straight away.
 */
public class GUICragSelectTier extends StatelessView {
    private static final int FIRST_TIER_SLOT = 11;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Select Tier", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        for (KuudraTier tier : KuudraTier.values()) {
            layout.slot(FIRST_TIER_SLOT + tier.ordinal(),
                    (_, c) -> tierItem((SkyBlockPlayer) c.player(), tier),
                    (_, c) -> {
                        SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                        player.closeInventory();
                        KuudraQueue.queue(player, tier);
                    });
        }
        layout.slot(30, findAParty(), (_, c) -> GUIKuudraPartyFinder.open((SkyBlockPlayer) c.player()));
        Components.close(layout, 31);
        layout.slot(32, (_, c) -> autoReadyUp(KuudraProgress.autoReadyUp((SkyBlockPlayer) c.player())), (_, c) -> {
            KuudraProgress.toggleAutoReadyUp((SkyBlockPlayer) c.player());
            c.session(DefaultState.class).refresh();
        });
    }

    public static ItemStack.Builder tierItem(SkyBlockPlayer player, KuudraTier tier) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Party size: <9>1-{}", KuudraPartyCheck.MAX_PARTY_SIZE));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Boss: <c>Kuudra " + (KuudraProgress.hasCleared(player, tier) ? "<a>✔" : "<c>✖")));
        lore.add(Text.of("<8>Lava Kraken"));
        lore.add(Text.of("<7>Overgrown lava Kraken. The more Sulphur it"));
        lore.add(Text.of("<7>consumes the stronger it becomes."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Requirements:"));
        for (KuudraProgress.Requirement requirement : KuudraProgress.requirements(player, tier)) {
            requirement.lore().forEach(line -> lore.add(Text.of(line)));
        }
        KuudraAvailability.queueLore(player).forEach(line -> lore.add(line.isEmpty() ? Text.empty() : Text.of(line)));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to join!"));
        return ItemStacks.head(tier.getKeyTexture(), Text.of("<c>Kuudra's Hollow <8>- <e>{} Tier", tier.getDisplayName()), lore);
    }

    public static ItemStack.Builder findAParty() {
        return ItemStacks.item(Material.REDSTONE_BLOCK, """
                <a>Find a Party
                <7>Use the Party Finder to join a party
                <7>to fight the Kuudra.

                <c><l>WARNING! <c>Don't click any suspicious
                <c>links or Discord invites. Never share
                <c>your account information!

                <e>Click to browse!""");
    }

    private static ItemStack.Builder autoReadyUp(boolean enabled) {
        return ItemStacks.item(enabled ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS, """
                <a>Toggle Auto Ready Up
                <7>Auto ready up when joining <c>Kuudra's
                <c>Hollow<7> if you have the requirements,
                <7>so you don't have to!

                <7>Currently: %s

                <e>Click to toggle!""".formatted(enabled ? "<a>Enabled" : "<c>Disabled"));
    }
}
