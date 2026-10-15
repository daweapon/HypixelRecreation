package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.npc.GardenNpcSkins;
import dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUIPesthunter;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class AbiphonePhillip extends AbiphoneNPC {
    public AbiphonePhillip() {
        super("phillip", "<6>Phillip", "Trades <2>Pests <7>for rewards.");
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (Gardens.profile(player).pests().isSqueakyGiven()) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "abiphone").thenRun(() -> NPCOption.sendOption(player, "phillip", false, List.of(
                new NPCOption.Option("give", NamedTextColor.GREEN, true, "GIVE ITEM", (_) -> give(player, slot)))));
    }

    private void give(SkyBlockPlayer player, int slot) {
        GardenProfile profile = Gardens.profile(player);
        if (profile.pests().isSqueakyGiven() || dialogue().isInDialogue(player)) return;
        if (player.getAmountInInventory(ItemType.SQUEAKY_TOY) < 1) {
            dialogue().setDialogue(player, "missing");
            return;
        }
        player.removeItemFromPlayer(ItemType.SQUEAKY_TOY, 1);
        profile.pests().setSqueakyGiven(true);
        Gardens.save(player, profile);
        dialogue().setDialogue(player, "given").thenRun(() -> {
            super.onAdd(player, slot);
            dialogue().setDialogue(player, "farewell");
        });
    }

    @Override
    public void onCall(HypixelPlayer player) {
        player.openView(new GUIPesthunter());
    }

    @Override
    public ItemStack.Builder getIcon() {
        GardenNpcSkins.Skin skin = GardenNpcSkins.of("PESTHUNTER_PHILLIP");
        return ItemStacks.head(new PlayerSkin(skin.texture(), skin.signature()), Text.empty(), List.of());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("abiphone").lines(
                        "<b>✆ <f>Howdy, " + player.getUsername() + "!",
                        "<b>✆ <f>Yer know, standing 'ere all day with nothing to do gets pretty borin' at times.",
                        "<b>✆ <f>If you want my contact, how about you bring me somethin' to keep me occupied, eh?",
                        "<b>✆ <f>A <9>Squeaky Toy <f>would do the trick.",
                        "<b>✆ <f>You hearin' me? Bring me one of those and you can have my contact."
                ).build(),
                DialogueSet.builder().key("missing").lines(
                        "<b>✆ <f>What's all this about, eh?",
                        "<b>✆ <f>Quit yer playin' and bring me a <9>Squeaky Toy <f>or you ain't getting my contact!",
                        "<b>✆ <f>You can sometimes find 'em after vacuuming up a <5>Field Mouse<f>. You can only catch those rare little guys in <9>Pest Traps<f>.",
                        "<b>✆ <f>I heard somebody 'round here sells them. Oh - it's me!"
                ).build(),
                DialogueSet.builder().key("given").lines(
                        "<b>✆ <f>Cheers mate, that works!",
                        "<b>✆ <f>To be honest with ya, though, I have no use for this toy.",
                        "<b>✆ <f>I just ask for it because it gets people buying a bunch of <9>Pest Traps <f>from me in order to go about getting it.",
                        "<b>✆ <f>Waddya call that, a pyramid scheme? Ponzi scheme? I don't know... I just call it bein' me.",
                        "<b>✆ <f>Anyways, here's that contact you was after."
                ).build(),
                DialogueSet.builder().key("farewell").lines(
                        "<b>✆ <f>See ya around, my partner in pesthuntin'!",
                        "<b>✆ <f>...",
                        "<b>✆ <f>Don't tell my wife I said that."
                ).build()
        };
    }
}
