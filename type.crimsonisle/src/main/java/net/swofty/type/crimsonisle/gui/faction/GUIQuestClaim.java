package net.swofty.type.crimsonisle.gui.faction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The single-item "Click to claim!" menu quest NPCs open (Elle's Maddox Batphone, the Dean's Letter
 * of Recommendation, Alchemage's staff, Mollim's book). The item sits at row 3, column 5.
 */
public class GUIQuestClaim extends StatelessView {
    private final String title;
    private final SkyBlockItem reward;
    private final @Nullable String unavailableReason;
    private final Runnable onClaim;

    public GUIQuestClaim(String title, SkyBlockItem reward, @Nullable String unavailableReason, Runnable onClaim) {
        this.title = title;
        this.reward = reward;
        this.unavailableReason = unavailableReason;
        this.onClaim = onClaim;
    }

    public GUIQuestClaim(String title, SkyBlockItem reward, Runnable onClaim) {
        this(title, reward, null, onClaim);
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(22, (_, c) -> display((SkyBlockPlayer) c.player()), (_, c) -> {
            if (unavailableReason != null) return;
            c.player().closeInventory();
            onClaim.run();
        });
    }

    private ItemStack.Builder display(SkyBlockPlayer player) {
        ItemStack.Builder builder = PlayerItemUpdater.playerUpdate(player, reward.getItemStack());
        List<Component> existing = builder.build().get(DataComponents.LORE);
        List<Component> lore = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        lore.add(Component.empty());
        lore.add(Text.read(unavailableReason != null ? unavailableReason : "<e>Click to claim!").asComponent()
                .decoration(TextDecoration.ITALIC, false));
        return builder.set(DataComponents.LORE, lore);
    }
}
