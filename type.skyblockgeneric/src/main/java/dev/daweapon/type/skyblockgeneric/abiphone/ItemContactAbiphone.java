package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public abstract class ItemContactAbiphone extends AbiphoneNPC {
    public record Step(String option, String dialogueKey) {
    }

    private final String givenFlag;
    private final ItemType item;
    private final int amount;

    protected ItemContactAbiphone(String id, String name, String description, ItemType item, int amount) {
        super(id, name, description);
        this.givenFlag = "abiphone." + id + ".given";
        this.item = item;
        this.amount = amount;
    }

    protected abstract DialogueSet[] stages();

    protected List<Step> steps() {
        return List.of();
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, givenFlag)) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        play(player, slot, "intro", 0);
    }

    private void play(SkyBlockPlayer player, int slot, String key, int nextStep) {
        dialogue().setDialogue(player, key).thenRun(() -> {
            List<Step> steps = steps();
            if (nextStep < steps.size()) {
                Step step = steps.get(nextStep);
                option(player, "step" + nextStep, false, step.option(), () -> {
                    if (!dialogue().isInDialogue(player)) play(player, slot, step.dialogueKey(), nextStep + 1);
                });
                return;
            }
            option(player, "give", true, "GIVE ITEM", () -> give(player, slot));
        });
    }

    private void give(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, givenFlag) || dialogue().isInDialogue(player)) return;
        if (player.getAmountAnywhere(item) < amount || !player.removeItemFromPlayer(item, amount)) {
            dialogue().setDialogue(player, "missing");
            return;
        }
        ChapterTriggers.fire(player, givenFlag);
        dialogue().setDialogue(player, "given").thenRun(() -> super.onAdd(player, slot));
    }

    private void option(SkyBlockPlayer player, String key, boolean bold, String name, Runnable action) {
        NPCOption.sendOption(player, getId(), !bold, List.of(new NPCOption.Option(key, NamedTextColor.GREEN, bold, name, _ -> action.run())));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return stages();
    }
}
