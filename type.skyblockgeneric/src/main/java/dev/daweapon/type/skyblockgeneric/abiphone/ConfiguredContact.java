package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public final class ConfiguredContact extends AbiphoneNPC {
    public record Requirement(ItemType item, int amount) {
    }

    public record Stage(String label, List<String> intro, List<String> missing, List<String> accepted,
                        List<Requirement> items, AbiphoneCheck check, String phrase) {
    }

    public record Definition(String id, List<String> npcs, String head, List<Stage> stages, List<String> given) {
    }

    private final Definition definition;
    private final String head;
    private final String givenFlag;

    public ConfiguredContact(Definition definition, AbiphoneContact entry) {
        super(definition.id(), entry.name(), entry.description());
        this.definition = definition;
        this.head = definition.head() == null || definition.head().isBlank() ? entry.head() : definition.head();
        this.givenFlag = "abiphone." + definition.id() + ".given";
    }

    public Definition definition() {
        return definition;
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, givenFlag)) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        begin(player, slot, progress(player));
    }

    private String stageFlag(int stage) {
        return "abiphone." + definition.id() + ".stage" + stage;
    }

    private int progress(SkyBlockPlayer player) {
        int stage = 0;
        while (stage < definition.stages().size() && ChapterTriggers.done(player, stageFlag(stage))) stage++;
        return stage;
    }

    private void begin(SkyBlockPlayer player, int slot, int stage) {
        if (stage >= definition.stages().size()) {
            finish(player, slot);
            return;
        }
        Stage current = definition.stages().get(stage);
        if (current.intro().isEmpty()) {
            prompt(player, slot, stage, current);
            return;
        }
        dialogue().setDialogue(player, "intro" + stage).thenRun(() -> prompt(player, slot, stage, current));
    }

    private void prompt(SkyBlockPlayer player, int slot, int stage, Stage current) {
        if (current.phrase() != null) {
            ChatAnswers.expect(player, message -> message.equalsIgnoreCase(current.phrase()), () -> complete(player, slot, stage, current));
            return;
        }
        NPCOption.sendOption(player, "contact_" + getId(), false, List.of(new NPCOption.Option(
                "give" + stage, NamedTextColor.GREEN, true, current.label(), _ -> attempt(player, slot, stage, current))));
    }

    private void attempt(SkyBlockPlayer player, int slot, int stage, Stage current) {
        if (ChapterTriggers.done(player, stageFlag(stage)) || dialogue().isInDialogue(player)) return;
        if (!satisfied(player, current)) {
            List<String> failure = current.check() == null ? List.of() : current.check().failureLines(player);
            if (failure.isEmpty()) dialogue().setDialogue(player, "missing" + stage);
            else failure.forEach(line -> sendNPCMessage(player, line));
            return;
        }
        consume(player, current);
        complete(player, slot, stage, current);
    }

    private void complete(SkyBlockPlayer player, int slot, int stage, Stage current) {
        if (ChapterTriggers.done(player, stageFlag(stage))) return;
        ChapterTriggers.fire(player, stageFlag(stage));
        if (current.accepted().isEmpty()) {
            begin(player, slot, stage + 1);
            return;
        }
        dialogue().setDialogue(player, "accepted" + stage).thenRun(() -> begin(player, slot, stage + 1));
    }

    private void finish(SkyBlockPlayer player, int slot) {
        ChapterTriggers.fire(player, givenFlag);
        dialogue().setDialogue(player, "given").thenRun(() -> super.onAdd(player, slot));
    }

    private static boolean satisfied(SkyBlockPlayer player, Stage stage) {
        for (Requirement requirement : stage.items()) {
            if (player.getAmountAnywhere(requirement.item()) < requirement.amount()) return false;
        }
        return stage.check() == null || stage.check().test(player);
    }

    private static void consume(SkyBlockPlayer player, Stage stage) {
        for (Requirement requirement : stage.items()) player.removeItemFromPlayer(requirement.item(), requirement.amount());
        if (stage.check() != null) stage.check().consume(player);
    }

    @Override
    public void onCall(HypixelPlayer player) {
        if (!AbiphoneCallActions.run(getId(), player)) player.sendMessage("<c>✆ No answer.");
    }

    @Override
    public boolean wantsToCall(SkyBlockPlayer player) {
        AbiphoneIncomingHooks.Hook hook = AbiphoneIncomingHooks.get(getId());
        return hook != null && hook.wants().test(player);
    }

    @Override
    public void onIncomingPickUp(SkyBlockPlayer player) {
        AbiphoneIncomingHooks.Hook hook = AbiphoneIncomingHooks.get(getId());
        if (hook == null) onCall(player);
        else hook.pickUp().accept(player);
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        List<DialogueSet> sets = new ArrayList<>();
        for (int i = 0; i < definition.stages().size(); i++) {
            Stage stage = definition.stages().get(i);
            addSet(sets, "intro" + i, stage.intro());
            addSet(sets, "missing" + i, stage.missing());
            addSet(sets, "accepted" + i, stage.accepted());
        }
        addSet(sets, "given", definition.given());
        return sets.toArray(DialogueSet[]::new);
    }

    private static void addSet(List<DialogueSet> sets, String key, List<String> lines) {
        if (!lines.isEmpty()) sets.add(DialogueSet.builder().key(key).lines(lines.toArray(String[]::new)).build());
    }

    @Override
    public ItemStack.Builder getIcon() {
        if (head == null || head.isBlank()) return ItemStack.builder(Material.PLAYER_HEAD);
        return ItemStacks.head(head, Text.empty(), List.of());
    }
}
