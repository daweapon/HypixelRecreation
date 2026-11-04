package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DarkCacaoTruffle {
    public static final int MAX_BONUS = 30;
    public static final int MINUTES_PER_TENTH = 60;
    public static final int REFINED_LIMIT = 5;
    public static final double REFINED_XP_EACH = 2;
    private static final long BUFF_MS = 60L * 60 * 1000;
    private static final int INVENTORY_SLOTS = 36;

    private DarkCacaoTruffle() {
    }

    public static double bonusFor(long agedMinutes) {
        long tenths = Math.max(0, agedMinutes) / MINUTES_PER_TENTH;
        return Math.min(MAX_BONUS, tenths / 10.0);
    }

    public static boolean hasEvolved(long agedMinutes) {
        return bonusFor(agedMinutes) >= MAX_BONUS;
    }

    public static void registerLore() {
        LoreRegistry.register("DARK_CACAO_TRUFFLE", new LoreConfig(DarkCacaoTruffle::truffleLore, null));
    }

    public static void registerInteractables() {
        InteractableRegistry.register("DARK_CACAO_TRUFFLE", InteractableItemConfig.builder()
                .rightClickHandler(DarkCacaoTruffle::eat).build());
        InteractableRegistry.register("REFINED_DARK_CACAO_TRUFFLE", InteractableItemConfig.builder()
                .rightClickHandler(DarkCacaoTruffle::eatRefined).build());
    }

    public static void startAging() {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                        SkyBlockGenericLoader.forEachLoadedPlayer(DarkCacaoTruffle::age))
                .repeat(TaskSchedule.minutes(1))
                .schedule();
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        double fortune = data.getRefinedTrufflesConsumed();
        if (data.getTruffleBuffExpires() > System.currentTimeMillis()) fortune += data.getTruffleBuffFortune() / 10.0;
        if (fortune <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.COCOA_BEANS_FORTUNE, fortune).build();
    }

    public static void setRefinedConsumed(SkyBlockPlayer player, int consumed) {
        ChocolateFactoryHelper.mutateData(player, data -> data.setRefinedTrufflesConsumed(consumed));
        syncSkyBlockXP(player);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.REFINED_DARK_CACAO_TRUFFLE,
                ChocolateFactoryHelper.getData(player).getRefinedTrufflesConsumed() * REFINED_XP_EACH);
    }

    private static void age(SkyBlockPlayer player) {
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != ItemType.DARK_CACAO_TRUFFLE) continue;

            long aged = item.getAttributeHandler().getJyrreAgeMinutes() + 1;
            if (hasEvolved(aged)) {
                SkyBlockItem refined = new SkyBlockItem(ItemType.REFINED_DARK_CACAO_TRUFFLE);
                refined.setAmount(item.getAmount());
                player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, refined.getItemStack()).build());
                player.sendMessage("<d><l>EVOLUTION! <d>Your <9>Dark Cacao Truffle <d>has evolved!");
                continue;
            }
            item.getAttributeHandler().setJyrreAgeMinutes(aged);
            player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
        }
    }

    private static void eat(SkyBlockPlayer player, SkyBlockItem item) {
        int tenths = (int) Math.round(bonusFor(item.getAttributeHandler().getJyrreAgeMinutes()) * 10);
        if (!startBuff(player, tenths, "<9>Dark Cacao Truffle")) return;
        consumeHeld(player, item);
    }

    private static void eatRefined(SkyBlockPlayer player, SkyBlockItem item) {
        int consumed = ChocolateFactoryHelper.getData(player).getRefinedTrufflesConsumed();
        if (consumed >= REFINED_LIMIT) {
            player.sendMessage("<c>You have already consumed the maximum amount of Refined Dark Cacao Truffles!");
            return;
        }
        if (!startBuff(player, MAX_BONUS * 10, "<6>Refined Dark Cacao Truffle")) return;
        setRefinedConsumed(player, consumed + 1);
        consumeHeld(player, item);
        player.sendMessage("<a>You gained a permanent <6>+1☘ Cocoa Beans Fortune<a>! <7>(" + (consumed + 1) + "/" + REFINED_LIMIT + ")");
    }

    private static boolean startBuff(SkyBlockPlayer player, int tenths, String name) {
        if (ChocolateFactoryHelper.getData(player).getTruffleBuffExpires() > System.currentTimeMillis()) {
            player.sendMessage("<c>You already have an active Dark Cacao Truffle!");
            return false;
        }
        long expiration = System.currentTimeMillis() + BUFF_MS;
        ChocolateFactoryHelper.mutateData(player, data -> {
            data.setTruffleBuffExpires(expiration);
            data.setTruffleBuffFortune(tenths);
        });
        player.sendMessage("<7>You consumed a " + name + " <7>and gained <6>+" + format(tenths / 10.0)
                + " Cocoa Beans Fortune <7>for <a>60m<7>!");
        return true;
    }

    private static void consumeHeld(SkyBlockPlayer player, SkyBlockItem item) {
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
    }

    private static String format(double value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    private static List<String> truffleLore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        return List.of(
                "<7>Consume to boost your <6>☘ Cocoa",
                "<6>Beans Fortune <7>for <a>60m<7>.",
                "",
                "<7>Keep this item in your inventory to",
                "<7>increase the bonus up to <6>+" + MAX_BONUS,
                "<6>Cocoa Beans Fortune<7>, at which point",
                "<7>the item <c><o>evolves<7>!",
                "",
                "<7>Current Bonus: <6>+" + format(bonusFor(item.getAttributeHandler().getJyrreAgeMinutes())) + " Cocoa Beans Fortune");
    }
}
