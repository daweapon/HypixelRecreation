package net.swofty.type.skyblockgeneric.crimson.kuudra.reward;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Opens a run's chest, at the end of the fight or later from Vesuvius. The Free chest costs nothing; the
 * Paid chest costs the run tier's Kuudra Key, taken only when it opens. Once one chest is open the other
 * is gone.
 */
public final class KuudraChestService {

    public enum OpenResult { OPENED, ALREADY_OPENED, OTHER_OPENED, CANNOT_AFFORD, EXPIRED }

    private KuudraChestService() {
    }

    public static OpenResult open(SkyBlockPlayer player, String runId, KuudraChest chest) {
        synchronized (player) {
            KuudraRun run = KuudraChestStorage.find(player, runId);
            if (run == null) {
                player.sendMessage("<c>These chests have expired!");
                return OpenResult.EXPIRED;
            }
            if (run.opened() == chest) {
                player.sendMessage("<c>This chest has already been opened!");
                return OpenResult.ALREADY_OPENED;
            }
            if (run.opened() != null) {
                player.sendMessage("<c>Can't open another chest!");
                return OpenResult.OTHER_OPENED;
            }
            if (chest == KuudraChest.PAID) {
                ItemType key = ItemType.get(run.tier().getKeyItemId());
                if (key == null || !player.removeItemFromPlayer(key, 1)) {
                    player.sendMessage("<c>You cannot afford this!");
                    return OpenResult.CANNOT_AFFORD;
                }
            }
            KuudraChestStorage.replace(player, run.open(chest));
            announce(player, chest, run);
            run.rewards(chest).forEach(reward -> reward.deliver(player));
            return OpenResult.OPENED;
        }
    }

    public static boolean canAfford(SkyBlockPlayer player, KuudraRun run, KuudraChest chest) {
        if (chest == KuudraChest.FREE) return true;
        ItemType key = ItemType.get(run.tier().getKeyItemId());
        return key != null && player.getAmountInInventory(key) + player.getSackItems().getAmount(key) > 0;
    }

    private static void announce(SkyBlockPlayer player, KuudraChest chest, KuudraRun run) {
        player.sendMessage("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(chest.getColour() + "<l>" + chest.getRewardsHeader());
        run.rewards(chest).forEach(reward -> player.sendMessage(Text.of("  {}", reward.name())));
        player.sendMessage("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
}
