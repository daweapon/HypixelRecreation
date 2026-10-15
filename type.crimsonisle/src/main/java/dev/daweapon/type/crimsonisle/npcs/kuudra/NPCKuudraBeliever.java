package dev.daweapon.type.crimsonisle.npcs.kuudra;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.CoinOffering;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.FollowerStep;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.npc.KuudraFollowerNPC;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Instant;

/**
 * The Kuudra Believer takes coin offerings at the Plhlegblast Pool. A million coins blesses you for
 * Kuudra's Hollow; the exact Wet Napkin sum (or 4,202,022) with a Plhlegblast beside him pays out.
 */
public class NPCKuudraBeliever extends KuudraFollowerNPC {
    private static final Pos POSITION = new Pos(-390.5, 81, -701.5);

    public NPCKuudraBeliever() {
        super("<c>Kuudra Believer", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RjN2I4M2Y4MWU1OGYyYTExNzk4NWM2NjI3NzE4MzNjMzRkYTY4NDQxN2MzYWE3ZTI5NjBiMDEyNzVlMzFlOWEifX19", POSITION);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!KuudraFollowerProgress.has(player, FollowerStep.BELIEVER_MET)) {
            setDialogue(player, "first").thenRun(() -> KuudraFollowerProgress.complete(player, FollowerStep.BELIEVER_MET));
            return;
        }
        if (holdingDecayablePlaque(player)) {
            setDialogue(player, "plaque").thenRun(() -> openOffering(player));
            return;
        }
        openOffering(player);
    }

    private static boolean holdingDecayablePlaque(SkyBlockPlayer player) {
        return KuudraFollowerRules.holding(player, "KUUDRA_CAVITY")
                && !KuudraFollowerProgress.has(player, FollowerStep.DECAY_BELIEVER)
                && KuudraFollowerProgress.plaqueStage(player) != null;
    }

    private void openOffering(SkyBlockPlayer player) {
        new HypixelSignGUI(player).open(new String[]{"Offer coins", "to Kuudra"})
                .thenAccept(input -> handleOffering(player, input == null ? "" : input.trim()));
    }

    private void handleOffering(SkyBlockPlayer player, String input) {
        if (input.equalsIgnoreCase("decay") && holdingDecayablePlaque(player)) {
            if (KuudraFollowerProgress.decay(player, FollowerStep.DECAY_BELIEVER)) setDialogue(player, "decay");
            return;
        }
        CoinOffering offering = CoinOffering.parse(input);
        switch (offering.result()) {
            case UNREADABLE -> player.sendMessage("<c>Couldn't read that number!");
            case NEGATIVE -> player.sendMessage("<c>Invalid number!");
            case VALID -> offer(player, offering.amount());
        }
    }

    private void offer(SkyBlockPlayer player, long amount) {
        if (amount > player.getCoins()) {
            player.sendMessage("<c>You don't have this amount!");
            return;
        }
        player.removeCoins(amount);
        boolean plhlegblast = KuudraFollowerRules.isPlhlegblastNear(player, POSITION);

        if (plhlegblast && amount == KuudraFollowerRules.HERRING_DONATION) {
            setDialogue(player, "favour").thenRun(() -> {
                player.addAndUpdateItem(ItemType.HERRING_THE_FISH);
                KuudraFollowerProgress.complete(player, FollowerStep.HERRING_CLAIMED);
            });
            return;
        }
        if (plhlegblast && hasWetNapkin(player)
                && amount == KuudraFollowerRules.helmetDonation(player.getUsername(), Instant.now())) {
            setDialogue(player, "favour").thenRun(() -> {
                player.addAndUpdateItem(ItemType.KUUDRA_FOLLOWER_HELMET);
                KuudraFollowerProgress.complete(player, FollowerStep.HELMET_CLAIMED);
            });
            return;
        }
        if (amount >= 1_000_000) {
            KuudraFollowerProgress.bless(player);
            setDialogue(player, "million");
        } else if (amount >= 100_000) {
            setDialogue(player, "great");
        } else if (amount >= 1) {
            setDialogue(player, "thanks");
        } else {
            setDialogue(player, "nothing");
        }
    }

    private static boolean hasWetNapkin(SkyBlockPlayer player) {
        return KuudraFollowerProgress.has(player, FollowerStep.WET_NAPKIN) || player.countItem(ItemType.WET_NAPKIN) > 0;
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("first").lines(
                        "Hello adventurer!",
                        "Are you a believer in the return of Kuudra?",
                        "If you are, why don't you offer up some coin to show your belief.",
                        "It is said that Kuudra once blessed this lavapool.",
                        "Maybe Kuudra will show favor upon you."
                ).build(),
                DialogueSet.builder().key("nothing").lines("Disappointing, maybe one day you will believe.").build(),
                DialogueSet.builder().key("thanks").lines("Thank you for your offering. Kuudra will be pleased.").build(),
                DialogueSet.builder().key("great").lines("What a great offering. Kuudra will be pleased.").build(),
                DialogueSet.builder().key("million").lines(
                        "Wow.",
                        "You truly are a believer.",
                        "Kuudra will bless you."
                ).build(),
                DialogueSet.builder().key("favour").lines(
                        "Wow.",
                        "You truly are a believer.",
                        "Kuudra will bless you.",
                        "ADVENTURER",
                        "it seems like Kuudra has shown favour upon you!",
                        "This item surfaced from the lavapool"
                ).build(),
                DialogueSet.builder().key("plaque").lines(
                        "Hello adventurer!",
                        "You must believe in Kuudra's return!",
                        "I know this as you have that intriguing item!",
                        "You don't have to offer some coin...",
                        "Just tell me how to help you!"
                ).build(),
                DialogueSet.builder().key("decay").lines(
                        "Oh so you want me to help you!",
                        "I can definitely do that for you adventurer!",
                        "There, go and hope for his return."
                ).build()
        };
    }
}
