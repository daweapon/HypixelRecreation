package net.swofty.type.hub.gui.fann;

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
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetTraining.Session;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.pettraining.PetTrainingService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;

public class GUIFannSession extends StatelessView {
    private final int trainingSlot;

    public GUIFannSession(int trainingSlot) {
        this.trainingSlot = trainingSlot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Training Slot " + (trainingSlot + 1), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 30, ctx);
        Components.close(layout, 31);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        layout.autoUpdating(13, (s, c) -> renderPet(player), Duration.ofSeconds(1));
        layout.slot(15, ItemStacks.item(Material.RED_TERRACOTTA, """
                <c>Cancel Training
                <7>Take your pet back now. It keeps the
                <7>EXP it has earned so far, and Fann
                <7>refunds half of the remaining time.

                <e>Click to cancel!"""), (click, c) -> cancel(player, c));
    }

    private ItemStack.Builder renderPet(SkyBlockPlayer player) {
        Session session = PetTrainingService.session(player, trainingSlot);
        if (session == null) return ItemStack.AIR.builder();
        long now = System.currentTimeMillis();
        return ItemStacks.appendLore(PlayerItemUpdater.playerUpdate(player, session.pet().getItemStack()), List.of(
                Text.empty(),
                Text.of("<7>Session: " + session.type().getColor() + "{} Training", session.type().getDisplayName()),
                Text.of("<7>EXP Earned: <b>{:,}", (long) PetTrainingService.xpEarned(session, now)),
                Text.of("<7>Time Left: <e>{:time}", Math.max(0, session.endsAt() - now))));
    }

    private void cancel(SkyBlockPlayer player, ViewContext ctx) {
        if (PetTrainingService.session(player, trainingSlot) == null) return;
        SkyBlockItem pet = PetTrainingService.finish(player, trainingSlot);
        player.addAndUpdateItem(pet);
        player.sendMessage(Text.of("<e>You took your {} <e>back from Fann.", pet.getDisplayNameText(player)));
        ctx.backOrClose();
    }
}
