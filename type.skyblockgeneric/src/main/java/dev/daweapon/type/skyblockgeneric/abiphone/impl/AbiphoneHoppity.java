package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityAbiphoneOffer;
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityCalls;
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityEggService;
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityPayment;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;

public class AbiphoneHoppity extends AbiphoneNPC {
    public static final String ID = "hoppity";
    private static final String TEXTURE = "b79e7f3341b672d9de6564cbaca052a6a723ea466a2e66af35ba1ba855f0d692";
    private final Map<UUID, HoppityAbiphoneOffer.Offer> pending = new ConcurrentHashMap<>();

    public AbiphoneHoppity() {
        super(ID, "<a>Hoppity", "Calls you when he has a new <a>Chocolate Rabbit <7>for sale.");
        HoppityCalls.start();
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (player.getToggles().get(ToggleType.HAS_GIVEN_HOPPITY_STEAMED_FISH)) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "abiphone").thenRun(() -> NPCOption.sendOption(player, "hoppity", false, List.of(
                new NPCOption.Option("give", NamedTextColor.GREEN, true, "GIVE ITEM", _ -> give(player, slot)))));
    }

    private void give(SkyBlockPlayer player, int slot) {
        if (player.getToggles().get(ToggleType.HAS_GIVEN_HOPPITY_STEAMED_FISH) || dialogue().isInDialogue(player)) return;
        if (player.getAmountInInventory(ItemType.STEAMED_CHOCOLATE_FISH) < 1) {
            dialogue().setDialogue(player, "missing");
            return;
        }
        player.removeItemFromPlayer(ItemType.STEAMED_CHOCOLATE_FISH, 1);
        player.getToggles().set(ToggleType.HAS_GIVEN_HOPPITY_STEAMED_FISH, true);
        dialogue().setDialogue(player, "given").thenRun(() -> super.onAdd(player, slot));
    }

    @Override
    public void onCall(HypixelPlayer player) {
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        HoppityAbiphoneOffer.Offer offer = pending.get(player.getUuid());
        if (offer == null) {
            player.sendMessage("<c>Hoppity has no new Chocolate Rabbits for you right now.");
            return;
        }
        present(skyBlockPlayer, offer);
    }

    public void incomingCall(SkyBlockPlayer player) {
        if (!hasContact(player) || dialogue().isInDialogue(player) || pending.containsKey(player.getUuid())) return;
        boolean ownsAbi = ChocolateFactoryHelper.getData(player).hasFoundRabbit(ChocolateRabbit.ABI);
        HoppityAbiphoneOffer.Offer offer = HoppityAbiphoneOffer.roll(player.getUuid(), ownsAbi, ThreadLocalRandom.current());
        pending.put(player.getUuid(), offer);
        ring(player);
        present(player, offer);
    }

    private void present(SkyBlockPlayer player, HoppityAbiphoneOffer.Offer offer) {
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "offer").thenRun(() -> {
            player.sendMessage("<7>Hoppity is offering {} <7>for <6>{} Coins<7>.", offer.rabbit().getFormattedName(),
                    StringUtility.commaify(offer.price()));
            NPCOption.sendOption(player, "hoppity", false, List.of(
                    new NPCOption.Option("accept", NamedTextColor.GREEN, true, "ACCEPT", _ -> accept(player)),
                    new NPCOption.Option("decline", NamedTextColor.RED, true, "DECLINE", _ -> decline(player))));
        });
    }

    private void accept(SkyBlockPlayer player) {
        if (dialogue().isInDialogue(player)) return;
        HoppityAbiphoneOffer.Offer offer = pending.get(player.getUuid());
        if (offer == null) return;
        if (!HoppityPayment.canAfford(player, offer.price())) {
            player.sendMessage("<c>You cannot afford this rabbit!");
            return;
        }
        if (!pending.remove(player.getUuid(), offer)) return;
        if (!HoppityPayment.pay(player, offer.price())) {
            pending.put(player.getUuid(), offer);
            player.sendMessage("<c>You cannot afford this rabbit!");
            return;
        }
        HoppityEggService.award(player, offer.rabbit());
        dialogue().setDialogue(player, "accepted");
    }

    private void decline(SkyBlockPlayer player) {
        if (dialogue().isInDialogue(player) || pending.remove(player.getUuid()) == null) return;
        dialogue().setDialogue(player, "declined");
    }

    private void ring(SkyBlockPlayer player) {
        player.sendMessage("<b>✆ <a>Hoppity <f>is calling...");
        for (int pulse = 0; pulse < 3; pulse++) {
            ScheduleUtility.delay(() -> player.playSound(Sound.sound(Key.key("block.note_block.pling"),
                    Sound.Source.PLAYER, 1f, 1.6f), Sound.Emitter.self()), pulse * 8);
        }
    }

    private boolean hasContact(SkyBlockPlayer player) {
        for (SkyBlockItem item : player.getAllInventoryItems()) {
            if (item.hasComponent(AbiphoneComponent.class) && item.getAttributeHandler().hasAbiphoneNPC(this)) return true;
        }
        return false;
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head(TEXTURE, Text.empty(), List.of());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("abiphone").lines("Bring me that Fish...but in steamed form!").build(),
                DialogueSet.builder().key("missing").lines("You don't have what I need! Nice try!").build(),
                DialogueSet.builder().key("given").lines("Thank you so much, here is my contact.",
                        "If I have new rabbit arrivals, I'll let you know!").build(),
                DialogueSet.builder().key("offer").lines("I just got a new Chocolate Rabbit and was wondering if you wanted to buy it.").build(),
                DialogueSet.builder().key("accepted").lines("Nice doing business with you.", "See you around!").build(),
                DialogueSet.builder().key("declined").lines("Alright, maybe next time.").build()
        };
    }
}
