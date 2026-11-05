package dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraChest;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import net.minestom.server.item.ItemStack;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** A stored Kuudra run as Vesuvius and Croesus list it: tier key head, age, expiry and chest states. */
public final class KuudraRunIcon {
    private KuudraRunIcon() {
    }

    public static ItemStack.Builder icon(KuudraRun run, long now) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Completed <a>{} ago", duration(now - run.completedAt())));
        lore.add(Text.of("<7>Expires in <a>{}", duration(run.expiresAt() - now)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Chests:"));
        for (KuudraChest chest : KuudraChest.values()) {
            String status = run.opened() == chest ? "<a>Opened" : run.isOpened() ? "<8>Gone" : "<e>Unopened";
            lore.add(Text.of(" " + chest.getColour() + chest.getDisplayName() + " <8>- " + status));
        }
        lore.add(Text.empty());
        lore.add(Text.of(run.isOpened() ? "<c>No more chests to open!" : "<e>Click to view chests!"));
        return ItemStacks.head(run.tier().getKeyTexture(), Text.of("<c>Kuudra - {}", run.tier().getDisplayName()), lore);
    }

    public static String duration(long millis) {
        Duration duration = Duration.ofMillis(Math.max(0, millis));
        return duration.toHours() > 0
                ? duration.toHours() + "h " + duration.toMinutesPart() + "m"
                : duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s";
    }
}
