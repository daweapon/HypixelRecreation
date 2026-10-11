package net.swofty.type.skyblockgeneric.forge;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM.ForgeProcess;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;

public final class ForgeService {
    public static final int MAX_SLOTS = 7;
    private static final double MAX_REDUCTION_PERCENT = 90;

    private ForgeService() {
    }

    public static int slots(SkyBlockPlayer player) {
        return Math.min(MAX_SLOTS, HotmService.forgeSlots(player));
    }

    public static List<ForgeProcess> processes(SkyBlockPlayer player) {
        return HotmService.data(player).getForgeProcesses();
    }

    public static ForgeProcess processIn(SkyBlockPlayer player, int slot) {
        return processes(player).stream().filter(process -> process.slot() == slot).findFirst().orElse(null);
    }

    public static double reductionPercent(SkyBlockPlayer player) {
        double percent = HotmService.perkValue(player, "quick_forge") + ElectionPerks.bonus(SkyBlockMayor.Perk.MOLTEN_FORGE);
        return Math.min(MAX_REDUCTION_PERCENT, percent);
    }

    public static long durationMillis(SkyBlockPlayer player, ForgeRecipe recipe) {
        return Math.max(1_000L, Math.round(recipe.seconds() * 1000D * (1D - reductionPercent(player) / 100D)));
    }

    public static boolean isFinished(ForgeProcess process) {
        return process.endsAt() <= System.currentTimeMillis();
    }

    public static long remainingMillis(ForgeProcess process) {
        return Math.max(0, process.endsAt() - System.currentTimeMillis());
    }

    public static String reasonUnavailable(SkyBlockPlayer player, ForgeRecipe recipe) {
        if (!recipe.available()) return "<c>This recipe isn't available on this server yet.";
        if (HotmService.data(player).getTier() < recipe.hotm()) return "<c>Requires Heart of the Mountain Tier " + recipe.hotm() + ".";
        return null;
    }

    public static int firstFreeSlot(SkyBlockPlayer player) {
        for (int slot = 1; slot <= slots(player); slot++) if (processIn(player, slot) == null) return slot;
        return -1;
    }

    public static String start(SkyBlockPlayer player, ForgeRecipe recipe) {
        String unavailable = reasonUnavailable(player, recipe);
        if (unavailable != null) return unavailable;
        int slot = firstFreeSlot(player);
        if (slot < 0) return "<c>All of your forge slots are in use!";
        if (player.getCoins() < recipe.coins()) return "<c>You don't have enough coins to forge this!";
        if (!player.removeItemsFromPlayer(recipe.inputTypes())) return "<c>You don't have the ingredients to forge this!";
        if (recipe.coins() > 0) player.removeCoins(recipe.coins());

        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        data.getForgeProcesses().add(new ForgeProcess(slot, recipe.id(), System.currentTimeMillis() + durationMillis(player, recipe)));
        data.getForgeProcesses().sort(Comparator.comparingInt(ForgeProcess::slot));
        HotmService.save(player, data);
        return null;
    }

    public static String claim(SkyBlockPlayer player, int slot) {
        ForgeProcess process = processIn(player, slot);
        if (process == null) return "<c>There is nothing in this forge slot.";
        if (!isFinished(process)) return "<c>This item is still being forged!";
        ForgeRecipe recipe = ForgeRegistry.get().recipe(process.recipeId());
        if (recipe == null || recipe.output() == null) return "<c>This forge item can't be collected right now.";
        if (!player.hasEmptySlots(1)) return "<c>Your inventory is full!";

        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        data.getForgeProcesses().removeIf(existing -> existing.slot() == slot);
        HotmService.save(player, data);
        player.addAndUpdateItem(recipe.output(), recipe.amount());
        return null;
    }

    public static int finishSlot(SkyBlockPlayer player, int slot) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        ForgeProcess process = processIn(player, slot);
        if (process == null) return 0;
        data.getForgeProcesses().removeIf(existing -> existing.slot() == slot);
        data.getForgeProcesses().add(new ForgeProcess(slot, process.recipeId(), System.currentTimeMillis()));
        data.getForgeProcesses().sort(Comparator.comparingInt(ForgeProcess::slot));
        HotmService.save(player, data);
        return 1;
    }

    public static int finishAll(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        long now = System.currentTimeMillis();
        List<ForgeProcess> finished = data.getForgeProcesses().stream().map(process -> new ForgeProcess(process.slot(), process.recipeId(), now)).toList();
        data.getForgeProcesses().clear();
        data.getForgeProcesses().addAll(finished);
        HotmService.save(player, data);
        return finished.size();
    }
}
