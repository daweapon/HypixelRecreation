package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra.GUICragSelectTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/** Crag trades his contact for a Bezal Shard (wiki Crag, 2025 shard update). Calling him opens Select Tier. */
public class AbiphoneCrag extends AbiphoneNPC {
    private static final String GIVEN = "kuudra:crag_contact";

    public AbiphoneCrag() {
        super("crag", "<c>Crag", "Lets you select a <c>Kuudra <7>tier.");
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (DatapointCrimsonIsle.of(player).getNumber(GIVEN) > 0) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "abiphone").thenRun(() -> NPCOption.sendOption(player, "crag", false, List.of(
                new NPCOption.Option("give", NamedTextColor.GREEN, true, "GIVE ITEM", (_) -> give(player, slot)))));
    }

    private void give(SkyBlockPlayer player, int slot) {
        if (DatapointCrimsonIsle.of(player).getNumber(GIVEN) > 0 || dialogue().isInDialogue(player)) return;
        if (player.getAmountInInventory(ItemType.SHARD_BEZAL) < 1) {
            dialogue().setDialogue(player, "missing");
            return;
        }
        player.removeItemFromPlayer(ItemType.SHARD_BEZAL, 1);
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(GIVEN, 1L));
        dialogue().setDialogue(player, "given").thenRun(() -> super.onAdd(player, slot));
    }

    @Override
    public void onCall(HypixelPlayer player) {
        player.openView(new GUICragSelectTier());
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("ff94b7e38d8a2d4049e7b1eb43fca78ab424c56980ec4b8debcba10347a726e7", Text.empty(), List.of());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("abiphone").lines(
                        "<b>✆<f> Huh? you want to add my contact to your Abiphone?",
                        "<b>✆<f> I guess we can come to an agreement.",
                        "<b>✆<f> I'm far too weak to take on Kuudra myself, but maybe one day...",
                        "<b>✆<f> Bring me a <e>Bezal Shard<7>, and I'll give you my contact."
                ).build(),
                DialogueSet.builder().key("missing").lines(
                        "<b>✆ <f>You don't have what I need! Nice try!"
                ).build(),
                DialogueSet.builder().key("given").lines(
                        "<b>✆<f> Ah, that is what I asked for. Thanks.",
                        "<b>✆<f> Here is my contact. Give me a call if you wish to take on the one they call 'Kuudra.'"
                ).build()
        };
    }
}
