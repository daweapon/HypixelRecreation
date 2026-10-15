package dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraChest;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;

import java.util.ArrayList;
import java.util.List;

/** The Contents and Cost lines Hypixel shows on a Kuudra chest and its open button. */
final class KuudraChestLore {
    private KuudraChestLore() {
    }

    static List<Text> contentsAndCost(KuudraRun run, KuudraChest chest) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Contents"));
        run.rewards(chest).forEach(reward -> lore.add(reward.name()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost"));
        lore.add(chest == KuudraChest.FREE ? Text.of("<a>FREE") : Text.of("<5>{}", run.tier().getKeyName()));
        lore.add(Text.empty());
        return lore;
    }

    static Text status(KuudraRun run, KuudraChest chest, String clickLine) {
        if (run.opened() == chest) return Text.of("<c>Already opened!");
        if (run.opened() != null) return Text.of("<c>Can't open another chest!");
        return Text.of(clickLine);
    }
}
