package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.gui.inventories.essence.GUIFrozenAlex;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Frozen Alex trades his contact for an Enchanted Lava Bucket, as on the wiki's Frozen Alex page.
public class AbiphoneFrozenAlex extends AbiphoneNPC {
    private static final DatapointToggles.Toggles.ToggleType GIVEN = DatapointToggles.Toggles.ToggleType.HAS_GIVEN_FROZEN_ALEX_LAVA_BUCKET;

    public AbiphoneFrozenAlex() {
        super("frozen_alex", "<b>Frozen Alex", "Runs the <d>Ice Essence Shop<7>.");
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (player.getToggles().get(GIVEN)) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "abiphone").thenRun(() -> NPCOption.sendOption(player, "frozen_alex", true, List.of(
                new NPCOption.Option("need", NamedTextColor.GREEN, false, "Sure, what do you need?",
                        (_) -> dialogue().setDialogue(player, "need").thenRun(() -> offerLavaBucket(player, slot))))));
    }

    private void offerLavaBucket(SkyBlockPlayer player, int slot) {
        NPCOption.sendOption(player, "frozen_alex", false, List.of(
                new NPCOption.Option("give", NamedTextColor.DARK_GREEN, true, "GIVE ITEM", (_) -> {
                    if (player.getToggles().get(GIVEN) || dialogue().isInDialogue(player)) return;
                    if (player.getAmountInInventory(ItemType.ENCHANTED_LAVA_BUCKET) < 1) {
                        player.sendMessage("<c>You don't have an <5>Enchanted Lava Bucket<c>!");
                        return;
                    }
                    player.removeItemFromPlayer(ItemType.ENCHANTED_LAVA_BUCKET, 1);
                    player.getToggles().set(GIVEN, true);
                    dialogue().setDialogue(player, "given").thenRun(() -> super.onAdd(player, slot));
                })));
    }

    @Override
    public void onCall(HypixelPlayer player) {
        player.openView(new GUIFrozenAlex());
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("59a9eb1fba56cd4621a5f7f629793cc1d3618087eea36e18a50b5c9adb3d6a87", Text.empty(), List.of());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("abiphone").lines(
                        "Hmphhh, it's absolutely freezing round here!",
                        "I can't feel my legs, toes, or hands!",
                        "Hey, could you get me something to heat me up a bit?",
                        "I'll give you my ...brr... <3>Abiphone Contact<f> in return."
                ).build(),
                DialogueSet.builder().key("need").lines(
                        "Bring me an <5>Enchanted Lava Bucket<f>, please.",
                        "If I dunk it over myself it should get rid of all the cold.",
                        "Brr..."
                ).build(),
                DialogueSet.builder().key("given").lines(
                        "Whew, that's the stuff.",
                        "...",
                        "Now I'm cold again.",
                        "You know, another dozen or so <5>Enchanted Lava Buckets<f> should be enough to thaw me out completely.",
                        "Don't worry though - I'll give that job to whoever asks for my contact next.",
                        "Here's my ...brr... contact..."
                ).build()
        };
    }
}
