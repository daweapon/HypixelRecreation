package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ShardContactAbiphone extends AbiphoneNPC {
    private final String shardId;
    private final String givenFlag;

    protected ShardContactAbiphone(String id, String name, String description, String shardId) {
        super(id, name, description);
        this.shardId = shardId;
        this.givenFlag = "abiphone." + id + ".given";
    }

    protected abstract DialogueSet[] stages();

    protected boolean hasOfCourseStep() {
        return false;
    }

    protected @Nullable String reaction() {
        return null;
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, givenFlag)) {
            super.onAdd(player, slot);
            return;
        }
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "intro").thenRun(() -> {
            if (hasOfCourseStep()) {
                option(player, "agree", NamedTextColor.GREEN, false, "Of course.", () -> askForShard(player, slot));
            } else {
                offer(player, slot);
            }
        });
    }

    private void askForShard(SkyBlockPlayer player, int slot) {
        if (dialogue().isInDialogue(player)) return;
        dialogue().setDialogue(player, "task").thenRun(() -> offer(player, slot));
    }

    private void offer(SkyBlockPlayer player, int slot) {
        option(player, "give", NamedTextColor.GREEN, true, "GIVE SHARD", () -> give(player, slot));
    }

    private void give(SkyBlockPlayer player, int slot) {
        if (ChapterTriggers.done(player, givenFlag) || dialogue().isInDialogue(player)) return;
        if (!takeShard(player)) {
            dialogue().setDialogue(player, "missing");
            return;
        }
        ChapterTriggers.fire(player, givenFlag);
        dialogue().setDialogue(player, "given").thenRun(() -> {
            if (reaction() == null) {
                finish(player, slot);
                return;
            }
            option(player, "react", NamedTextColor.GREEN, false, reaction(), () -> {
                if (dialogue().isInDialogue(player)) return;
                dialogue().setDialogue(player, "given-after").thenRun(() -> finish(player, slot));
            });
        });
    }

    private void finish(SkyBlockPlayer player, int slot) {
        super.onAdd(player, slot);
    }

    private boolean takeShard(SkyBlockPlayer player) {
        AttributeDefinition definition = AttributeRegistry.get(shardId);
        if (definition == null) return false;
        ItemType type = AttributeShardComponent.itemType(definition);
        if (!ItemType.ATTRIBUTE_SHARD.equals(type) && player.removeItemFromPlayer(type, 1)) return true;
        return player.getHuntingData().removeShards(shardId, 1);
    }

    private void option(SkyBlockPlayer player, String key, NamedTextColor color, boolean bold, String name, Runnable action) {
        NPCOption.sendOption(player, getId(), !bold, List.of(new NPCOption.Option(key, color, bold, name, _ -> action.run())));
    }

    @Override
    public DialogueSet[] dialogues(dev.daweapon.type.generic.user.HypixelPlayer player) {
        return stages();
    }
}
