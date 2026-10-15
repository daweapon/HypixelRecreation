package dev.daweapon.type.skyblockgeneric.crimson.questline;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Crimson Isle Abiphone contact. Holding an Abiphone and clicking the NPC adds the contact only
 * once {@link #refusal} has nothing to say; calls go through {@link #call}.
 */
public abstract class CrimsonContact extends AbiphoneNPC {
    private final String speaker;
    private final String headTexture;

    protected CrimsonContact(String id, String speaker, String description, String headTexture) {
        super(id, speaker, description);
        this.speaker = speaker;
        this.headTexture = headTexture;
    }

    /** Lines the NPC says instead of handing over its contact, or null when the contact can be added. */
    protected abstract @Nullable String[] refusal(SkyBlockPlayer player);

    protected abstract void call(SkyBlockPlayer player);

    /** Whether the player has earned this contact, so it can be added (or restored by Ziri). */
    public boolean isEarned(SkyBlockPlayer player) {
        return refusal(player) == null;
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        String[] refusal = refusal(player);
        if (refusal != null) {
            CrimsonScene.create().phone().say(speaker, refusal).play(player);
            return;
        }
        super.onAdd(player, slot);
    }

    @Override
    public void onCall(HypixelPlayer player) {
        if (CrimsonScene.isPlaying(player)) return;
        call((SkyBlockPlayer) player);
    }

    protected void reply(SkyBlockPlayer player, String... lines) {
        CrimsonScene.create().phone().say(speaker, lines).play(player);
    }

    protected void replyRandom(SkyBlockPlayer player, String... options) {
        reply(player, options[ThreadLocalRandom.current().nextInt(options.length)]);
    }

    protected void noAnswer(SkyBlockPlayer player) {
        player.sendMessage("<c>✆ No answer.");
    }

    public String speaker() {
        return speaker;
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head(headTexture, Text.empty(), List.of());
    }
}
