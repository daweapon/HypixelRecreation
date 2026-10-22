package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingShardShopView;
import dev.daweapon.type.skyblockgeneric.hunting.npc.ShardContactAbiphone;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class AbiphoneKiara extends ShardContactAbiphone {
    public AbiphoneKiara() {
        super("kiara", "<b>Kiara", "Opens her shop of <a>Reptile <7>and <b>Amphibian <7>Shards with one extra daily stock.", "L4");
    }

    @Override
    protected boolean hasOfCourseStep() {
        return true;
    }

    @Override
    public void onCall(HypixelPlayer player) {
        player.openView(HuntingShardShopView.kiara());
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head("8af610d5c4aa82943cfbb47b437adb84e0f4e3781dbfed70a7f18e60d778c56f", Text.empty(), List.of());
    }

    @Override
    protected DialogueSet[] stages() {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "Oh hey, I've seen you around.",
                        "I've been trying to get my Abiphone updated with more local contacts.",
                        "But I don't just hand out my number to anyone. You've got to earn it.").build(),
                DialogueSet.builder().key("task").lines(
                        "There's this creature I've been dying to track. It shifts colors, blends in, vanishes like mist.",
                        "They say if you're lucky, you might find one in <2>Tree Gifts<f>... a <6>Chameleon Shard<f>.",
                        "Bring me one. If you can do that, maybe you're worth talking to.").build(),
                DialogueSet.builder().key("missing").lines("You don't have what I need! Nice try!").build(),
                DialogueSet.builder().key("given").lines(
                        "You actually found one? You weren't kidding around.",
                        "Look at those colors... even holding it feels like it's trying to disappear.",
                        "Alright, fair's fair. I'm adding you to my Abiphone.",
                        "Just don't flood me with calls.").build()
        };
    }
}
