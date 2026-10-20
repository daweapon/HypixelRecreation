package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn.GUIStarlyn;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public abstract class StarlynAbiphone extends AbiphoneNPC {
    private final StarlynSister sister;

    protected StarlynAbiphone(StarlynSister sister, String description) {
        super(sister.key(), sister.npcName(), description);
        this.sister = sister;
        StarlynContests.start();
    }

    protected StarlynSister sister() {
        return sister;
    }

    protected abstract boolean canGiveContact(SkyBlockPlayer player);

    protected abstract String[] contactBefore();

    protected abstract String[] contactAfter();

    protected String[] refusal() {
        return new String[0];
    }

    @Override
    public void onAdd(SkyBlockPlayer player, int slot) {
        if (dialogue().isInDialogue(player)) return;
        if (!canGiveContact(player)) {
            dialogue().setDialogue(player, "refusal");
            return;
        }
        dialogue().setDialogue(player, "before").thenRun(() -> {
            super.onAdd(player, slot);
            dialogue().setDialogue(player, "after");
        });
    }

    @Override
    public void onCall(HypixelPlayer player) {
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        if (dialogue().isInDialogue(skyBlockPlayer)) return;
        if (StarlynContests.data(skyBlockPlayer, sister).claimable().isEmpty()) {
            skyBlockPlayer.openView(new GUIStarlyn(sister));
            return;
        }
        dialogue().setDialogue(skyBlockPlayer, "results").thenRun(() -> skyBlockPlayer.openView(new GUIStarlyn(sister)));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        List<DialogueSet> sets = new java.util.ArrayList<>(List.of(
                DialogueSet.builder().key("before").lines(contactBefore()).build(),
                DialogueSet.builder().key("after").lines(contactAfter()).build()));
        String[] refusal = refusal();
        if (refusal.length > 0) sets.add(DialogueSet.builder().key("refusal").lines(refusal).build());
        StarlynProfile.Entry latest = StarlynContests.data((SkyBlockPlayer) player, sister).claimable().stream()
                .max(Comparator.comparingLong(StarlynProfile.Entry::getDay)).orElse(null);
        if (latest != null) {
            sets.add(DialogueSet.builder().key("results").lines(resultLines(latest)).build());
        }
        return sets.toArray(DialogueSet[]::new);
    }

    String[] resultLines(StarlynProfile.Entry entry) {
        return new String[]{
                "You reached the " + entry.getBracket().formatted() + " <r><f>Bracket in my contest!",
                "You earned a total of <b>" + StringUtility.commaify(entry.score()) + " <f>points!",
                "Come see me at " + sister.getIsland() + " <f>or <6><l>CLICK HERE <r><f>to claim your rewards!"};
    }

    @Override
    public ItemStack.Builder getIcon() {
        return ItemStacks.head(sister.getHeadTexture(), Text.empty(), List.of());
    }
}
