package dev.daweapon.type.hub.gui.fann;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetTraining.PetTrainingData;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetTraining.Session;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.pettraining.PetTrainingService;
import dev.daweapon.type.skyblockgeneric.pettraining.PetTrainingType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GUIFannPetTraining extends StatelessView {
    private static final int[] TRAINING_SLOTS = {11, 13, 15};
    private static final long OWL_PRICE = 2_200_000;
    private static final String OWL_TEXTURE = "da3216da54e7368fb40b721239ad95e07ef4f97d93f1c42ff319bab9a53882af";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Pet Training", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        for (int slot = 0; slot < PetTrainingService.SLOT_COUNT; slot++) {
            int trainingSlot = slot;
            layout.autoUpdating(TRAINING_SLOTS[slot],
                    (s, c) -> renderSlot(player, trainingSlot),
                    (click, c) -> clickSlot(player, trainingSlot, c),
                    Duration.ofSeconds(1));
        }

        layout.slot(31, ItemStacks.head(OWL_TEXTURE, """
                <7>[Lvl 100] <6>Owl
                <8>Taming Pet

                <6>Training Refunds
                <7>The more coins you spend on Fann's
                <7>Sessions, the less coins they will
                <7>cost in the future. <8>(Max 5% off).

                <6>Efficient Trainer
                <7>Makes training sessions at Fann
                <7>more efficient when added into a
                <7>session.

                <7>Increased EXP: <b>+10% EXP

                <6>Fast Learner
                <7>Passively grants <3>+5☯ Taming Wisdom<7>.

                <7>Cost
                <6>2,200,000 Coins

                <e>Click to purchase!"""), (click, c) -> buyOwl(player));

        int taming = PetTrainingService.tamingLevel(player);
        layout.slot(48, ItemStacks.item(Material.POLAR_BEAR_SPAWN_EGG, """
                <a>Extra Free Training
                <7>Fann's Free Training session gets
                <7>increasingly better the higher the
                <a>Taming Level <7>of her customers.

                <7>Taming Level: <a>{}
                <7>Free Training: <b>{:,} EXP/day""", taming, (long) PetTrainingType.FREE.xpPerDay(taming)));
    }

    private static void buyOwl(SkyBlockPlayer player) {
        if (player.getCoins() < OWL_PRICE) {
            player.sendMessage("<c>You don't have enough coins!");
            return;
        }
        player.removeCoins(OWL_PRICE);
        SkyBlockItem owl = new SkyBlockItem(ItemType.OWL_PET);
        owl.getAttributeHandler().setRarity(Rarity.LEGENDARY);
        player.addAndUpdateItem(owl);
        player.playSuccessSound();
        player.sendMessage("<a>You bought an <6>Owl Pet<a>!");
    }

    private static ItemStack.Builder renderSlot(SkyBlockPlayer player, int slot) {
        PetTrainingData data = PetTrainingService.data(player);
        String title = "Training Slot #" + (slot + 1);

        if (!PetTrainingService.isSlotUnlocked(data, slot)) {
            if (slot == 1) {
                return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, """
                        <c>{}
                        <8>Locked

                        <7>Unlock this slot by feeding <6>Hootie
                        <9>Great Carrot Candy<7>! ({}/{})""", title, data.getGreatCarrotCandiesFed(),
                        PetTrainingService.GREAT_CARROT_CANDIES_FOR_SLOT_TWO);
            }
            return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, """
                    <c>{}
                    <8>Locked

                    <7>Unlock this slot by feeding <6>Hootie
                    <6>Ultimate Carrot Candy<7>!""", title);
        }

        Session session = data.getSessions().get(slot);
        if (session == null) {
            return ItemStacks.item(Material.WHITE_STAINED_GLASS_PANE, """
                    <c>{}
                    <8>Empty

                    <7>This slot is currently empty!
                    <8>You can level up pets by training
                    <8>them here.

                    <e>Click to start a session!""", title);
        }

        long now = System.currentTimeMillis();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        lore.add(Text.of("<7>Session: " + session.type().getColor() + "{} Training", session.type().getDisplayName()));
        lore.add(Text.of("<7>EXP Earned: <b>{:,}", (long) PetTrainingService.xpEarned(session, now)));
        if (PetTrainingService.isFinished(session, now)) {
            lore.add(Text.of("<a>Training complete!"));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to claim!"));
        } else {
            lore.add(Text.of("<7>Time Left: <e>{:time}", session.endsAt() - now));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to view!"));
        }
        SkyBlockItem pet = session.pet();
        return ItemStacks.appendLore(PlayerItemUpdater.playerUpdate(player, pet.getItemStack()), lore);
    }

    private static void clickSlot(SkyBlockPlayer player, int slot, ViewContext ctx) {
        PetTrainingData data = PetTrainingService.data(player);
        if (!PetTrainingService.isSlotUnlocked(data, slot)) return;

        Session session = data.getSessions().get(slot);
        if (session == null) {
            ctx.push(new GUIFannTrainingSlot(slot), GUIFannTrainingSlot.State.empty());
            return;
        }
        if (PetTrainingService.isFinished(session, System.currentTimeMillis())) {
            SkyBlockItem pet = PetTrainingService.finish(player, slot);
            player.addAndUpdateItem(pet);
            player.playSuccessSound();
            player.sendMessage("<a>Your {} <a>has finished training!", pet.getDisplayNameText(player));
            ctx.session(DefaultState.class).refresh();
            return;
        }
        ctx.push(new GUIFannSession(slot));
    }
}
