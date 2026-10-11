package net.swofty.type.hub.gui.fann;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.pettraining.PetTrainingDuration;
import net.swofty.type.skyblockgeneric.pettraining.PetTrainingService;
import net.swofty.type.skyblockgeneric.pettraining.PetTrainingType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIFannTrainingSlot implements StatefulView<GUIFannTrainingSlot.State> {
    private static final int[] DURATION_SLOTS = {10, 11, 12, 13};
    private static final int[] TYPE_SLOTS = {37, 38, 39, 40, 41, 42};
    private static final int[] PET_COLUMN = {15, 24, 33};
    private static final int PET_SLOT = 24;

    private final int trainingSlot;

    public GUIFannTrainingSlot(int trainingSlot) {
        this.trainingSlot = trainingSlot;
    }

    public record State(int inventorySlot, SkyBlockItem pet, PetTrainingType type, PetTrainingDuration duration) {
        public static State empty() {
            return new State(-1, null, PetTrainingType.FREE, PetTrainingDuration.FIVE_DAYS);
        }

        State withPet(int slot, SkyBlockItem item) {
            return new State(slot, item, type, duration);
        }

        State withType(PetTrainingType newType) {
            return new State(inventorySlot, pet, newType, duration);
        }

        State withDuration(PetTrainingDuration newDuration) {
            return new State(inventorySlot, pet, type, newDuration);
        }
    }

    @Override
    public State initialState() {
        return State.empty();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Training Slot " + (trainingSlot + 1), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int taming = PetTrainingService.tamingLevel(player);

        layout.slot(20, ItemStacks.item(Material.OAK_BUTTON, """
                <e>Click a pet in your inventory!
                <7>Selects it for training"""));
        layout.slot(25, ItemStacks.item(Material.OAK_BUTTON, """
                <e>Click an Owl Pet in your inventory!
                <7>You don't have to include one, but it always helps!

                <c>The Owl Pet is not available yet!"""));

        for (int slot : PET_COLUMN) {
            layout.slot(slot, ItemStacks.item(Material.RED_STAINED_GLASS_PANE, """
                    <c>No Pet
                    <7>Click a pet in your inventory to the
                    <7>left to begin training!"""));
        }
        if (state.pet() != null) {
            layout.slot(PET_SLOT, PlayerItemUpdater.playerUpdate(player, state.pet().getItemStack()));
        }

        PetTrainingDuration[] durations = PetTrainingDuration.values();
        for (int i = 0; i < durations.length; i++) {
            PetTrainingDuration duration = durations[i];
            layout.slot(DURATION_SLOTS[i], durationItem(duration, state.duration() == duration),
                    (click, c) -> c.session(State.class).update(s -> s.withDuration(duration)));
        }

        PetTrainingType[] types = PetTrainingType.values();
        for (int i = 0; i < types.length; i++) {
            PetTrainingType type = types[i];
            layout.slot(TYPE_SLOTS[i], typeItem(type, taming, state.type() == type),
                    (click, c) -> {
                        if (!type.isUnlocked(taming)) {
                            player.sendMessage("<c>You need Taming {} to use this training!", type.getRequiredTamingLevel());
                            return;
                        }
                        c.session(State.class).update(s -> s.withType(type));
                    });
        }

        layout.slot(50, startItem(player, state, taming), (click, c) -> start(player, c));
    }

    private static ItemStack.Builder durationItem(PetTrainingDuration duration, boolean selected) {
        ItemStack.Builder item = ItemStacks.item(duration.getIcon(),
                "<a>{}\n\n" + (selected ? "<a>Selected!" : "<e>Click to select!"), duration.getDisplayName());
        return selected ? ItemStacks.enchanted(item) : item;
    }

    private static ItemStack.Builder typeItem(PetTrainingType type, int taming, boolean selected) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Pet EXP/day: <b>{:,}", (long) type.xpPerDay(taming)));
        lore.add(Text.of("<7>Cost/day: <6>{:,} Coins", (long) type.getCoinsPerDay()));
        if (type.getBitsPerDay() > 0) lore.add(Text.of("<7>         <b>{:,} Bits", type.getBitsPerDay()));
        lore.add(Text.empty());
        if (!type.isUnlocked(taming)) {
            lore.add(Text.of("<c>Requires Taming {}!", type.getRequiredTamingLevel()));
        } else {
            lore.add(Text.of(selected ? "<a>Selected!" : "<e>Click to select!"));
        }
        ItemStack.Builder item = ItemStacks.item(type.getIcon(), Text.of(type.getColor() + "{} Training", type.getDisplayName()), lore);
        return selected ? ItemStacks.enchanted(item) : item;
    }

    private static ItemStack.Builder startItem(SkyBlockPlayer player, State state, int taming) {
        if (state.pet() == null) {
            return ItemStacks.item(Material.GRAY_DYE, """
                    <c>Start Training
                    <7>Select a pet first!""");
        }
        int days = PetTrainingService.days(state.pet(), state.type().xpPerDay(taming), state.duration());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Training: " + state.type().getColor() + "{}", state.type().getDisplayName()));
        lore.add(Text.of("<7>Duration: <e>{} Day{}", days, days == 1 ? "" : "s"));
        lore.add(Text.of("<7>Total EXP: <b>{:,}", (long) (state.type().xpPerDay(taming) * days)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost"));
        lore.add(Text.of("<6>{:,} Coins", PetTrainingService.coinCost(player, state.type(), days)));
        if (state.type().getBitsPerDay() > 0) lore.add(Text.of("<b>{:,} Bits", state.type().getBitsPerDay() * days));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to start training!"));
        return ItemStacks.item(Material.LIME_DYE, Text.of("<a>Start Training"), lore);
    }

    private void start(SkyBlockPlayer player, ViewContext ctx) {
        State state = ctx.session(State.class).state();
        if (state.pet() == null) return;
        if (PetTrainingService.session(player, trainingSlot) != null) return;

        int taming = PetTrainingService.tamingLevel(player);
        if (!state.type().isUnlocked(taming)) return;

        ItemStack current = player.getInventory().getItemStack(state.inventorySlot());
        if (current.isAir() || !new SkyBlockItem(current).toUnderstandable().serialize()
                .equals(state.pet().toUnderstandable().serialize())) {
            player.sendMessage("<c>That pet is no longer in your inventory!");
            ctx.session(State.class).update(s -> s.withPet(-1, null));
            return;
        }
        if (PetTrainingService.xpToMaxLevel(state.pet()) <= 0) {
            player.sendMessage("<c>This pet is already max level!");
            return;
        }

        int days = PetTrainingService.days(state.pet(), state.type().xpPerDay(taming), state.duration());
        if (player.getCoins() < PetTrainingService.coinCost(player, state.type(), days)) {
            player.sendMessage("<c>You don't have enough coins!");
            return;
        }
        if (player.getBits() < state.type().getBitsPerDay() * days) {
            player.sendMessage("<c>You don't have enough Bits!");
            return;
        }

        player.getInventory().setItemStack(state.inventorySlot(), ItemStack.AIR);
        PetTrainingService.start(player, trainingSlot, state.pet(), state.type(), days);
        player.playSuccessSound();
        player.sendMessage(Text.of("<a>Fann started training your {}<a>!", state.pet().getDisplayNameText(player)));
        ctx.backOrClose();
    }

    @Override
    public boolean onBottomClick(ClickContext<State> click, ViewContext ctx) {
        ItemStack stack = ctx.player().getInventory().getItemStack(click.slot());
        if (stack.isAir()) return true;

        SkyBlockItem item = new SkyBlockItem(stack);
        if (!item.hasComponent(PetComponent.class)) {
            ctx.player().sendMessage("<c>Fann can only train pets!");
            return true;
        }
        ctx.session(State.class).update(s -> s.withPet(click.slot(), item));
        return true;
    }
}
