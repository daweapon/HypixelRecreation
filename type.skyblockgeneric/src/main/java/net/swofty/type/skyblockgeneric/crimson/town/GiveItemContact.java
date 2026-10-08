package net.swofty.type.skyblockgeneric.crimson.town;

import net.kyori.adventure.text.format.NamedTextColor;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonContact;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A contact handed over once for an item: the ask ends in [GIVE ITEM], which takes the item and adds the contact. */
public abstract class GiveItemContact extends CrimsonContact {
    private final String givenKey;

    protected GiveItemContact(String id, String speaker, String description, String headTexture, String givenKey) {
        super(id, speaker, description, headTexture);
        this.givenKey = givenKey;
    }

    protected abstract String[] ask(SkyBlockPlayer player);

    protected abstract String[] missing();

    protected abstract String[] given();

    /** Takes the item from the player, or returns false when they don't carry it. */
    protected abstract boolean take(SkyBlockPlayer player);

    @Override
    protected @Nullable String[] refusal(SkyBlockPlayer player) {
        return null;
    }

    @Override
    public boolean isEarned(SkyBlockPlayer player) {
        return CrimsonSideQuests.is(player, givenKey);
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (CrimsonSideQuests.is(player, givenKey)) {
            super.onAdd(player, slot);
            return;
        }
        if (CrimsonScene.isPlaying(player)) return;
        CrimsonScene.create().phone().say(speaker(), ask(player)).play(player).thenRun(() ->
                NPCOption.sendOption(player, getId(), false, List.of(new NPCOption.Option("give", NamedTextColor.GREEN,
                        true, "GIVE ITEM", _ -> give(player, slot)))));
    }

    private void give(SkyBlockPlayer player, int slot) {
        if (CrimsonSideQuests.is(player, givenKey) || CrimsonScene.isPlaying(player)) return;
        if (!take(player)) {
            CrimsonScene.create().phone().say(speaker(), missing()).play(player);
            return;
        }
        CrimsonSideQuests.flag(player, givenKey);
        CrimsonScene.create().phone().say(speaker(), given()).play(player).thenRun(() -> super.onAdd(player, slot));
    }
}
