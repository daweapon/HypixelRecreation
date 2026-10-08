package net.swofty.type.skyblockgeneric.gifts;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The aging Bottle of Jyrre: +1 Intelligence per hour online in the inventory, up to +300, at which
 * point it evolves into a Refined Bottle of Jyrre. Drinking either grants its Intelligence for 60
 * minutes; a Refined Bottle also grants a permanent +1 Intelligence, five times per profile.
 */
public final class BottleOfJyrre {
    public static final int MAX_BONUS = 300;
    public static final int MINUTES_PER_INTELLIGENCE = 60;
    public static final int REFINED_LIMIT = 5;
    public static final double REFINED_XP_EACH = 2;
    private static final long BUFF_MS = 60L * 60 * 1000;
    private static final String BUFF_NAME = "Bottle of Jyrre";
    private static final int INVENTORY_SLOTS = 36;

    private BottleOfJyrre() {
    }

    public static int bonusFor(long agedMinutes) {
        return (int) Math.min(MAX_BONUS, Math.max(0, agedMinutes) / MINUTES_PER_INTELLIGENCE);
    }

    public static boolean hasEvolved(long agedMinutes) {
        return bonusFor(agedMinutes) >= MAX_BONUS;
    }

    public static void registerLore() {
        LoreRegistry.register("BOTTLE_OF_JYRRE", new LoreConfig(BottleOfJyrre::bottleLore, null));
    }

    public static void registerInteractables() {
        InteractableRegistry.register("BOTTLE_OF_JYRRE", InteractableItemConfig.builder()
                .rightClickHandler(BottleOfJyrre::drink).build());
        InteractableRegistry.register("REFINED_BOTTLE_OF_JYRRE", InteractableItemConfig.builder()
                .rightClickHandler(BottleOfJyrre::drinkRefined).build());
    }

    public static void startAging() {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                        SkyBlockGenericLoader.getLoadedPlayers().forEach(BottleOfJyrre::age))
                .repeat(TaskSchedule.minutes(1))
                .schedule();
    }

    public static void restore(SkyBlockPlayer player) {
        long expiration = buffExpiration(player);
        if (expiration > System.currentTimeMillis()) apply(player, buffIntelligence(player), expiration);
    }

    public static int refinedConsumed(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.REFINED_JYRRE_CONSUMED, DatapointInteger.class).getValue();
    }

    public static void setRefinedConsumed(SkyBlockPlayer player, int consumed) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.REFINED_JYRRE_CONSUMED, DatapointInteger.class).setValue(consumed);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.REFINED_JYRRE, consumed * REFINED_XP_EACH);
    }

    public static ItemStatistics permanentStatistics(SkyBlockPlayer player) {
        int consumed = refinedConsumed(player);
        if (consumed == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.INTELLIGENCE, (double) consumed).build();
    }

    private static void age(SkyBlockPlayer player) {
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != ItemType.NEW_BOTTLE_OF_JYRRE) continue;

            long aged = item.getAttributeHandler().getJyrreAgeMinutes() + 1;
            if (hasEvolved(aged)) {
                SkyBlockItem refined = new SkyBlockItem(ItemType.REFINED_BOTTLE_OF_JYRRE);
                refined.setAmount(item.getAmount());
                player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, refined.getItemStack()).build());
                player.sendMessage("<d><l>EVOLUTION! <d>Your <c>Bottle of Jyrre <d>has evolved!");
                continue;
            }
            item.getAttributeHandler().setJyrreAgeMinutes(aged);
            player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
        }
    }

    private static void drink(SkyBlockPlayer player, SkyBlockItem item) {
        if (!startBuff(player, bonusFor(item.getAttributeHandler().getJyrreAgeMinutes()))) return;
        consumeHeld(player, item);
    }

    private static void drinkRefined(SkyBlockPlayer player, SkyBlockItem item) {
        int consumed = refinedConsumed(player);
        if (consumed >= REFINED_LIMIT) {
            player.sendMessage("<c>You have already consumed the maximum amount of Refined Bottles of Jyrre!");
            return;
        }
        if (!startBuff(player, MAX_BONUS)) return;
        setRefinedConsumed(player, consumed + 1);
        consumeHeld(player, item);
        player.sendMessage("<a>You gained a permanent <b>+1✎ Intelligence<a>! <7>(" + (consumed + 1) + "/" + REFINED_LIMIT + ")");
    }

    private static boolean startBuff(SkyBlockPlayer player, int intelligence) {
        if (buffExpiration(player) > System.currentTimeMillis()) {
            player.sendMessage("<c>You already have an active Bottle of Jyrre!");
            return false;
        }
        long expiration = System.currentTimeMillis() + BUFF_MS;
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.JYRRE_BUFF_EXPIRATION, DatapointLong.class).setValue(expiration);
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.JYRRE_BUFF_INTELLIGENCE, DatapointInteger.class).setValue(intelligence);
        apply(player, intelligence, expiration);
        player.sendMessage("<a><l>GULP! <a>You gained <b>+" + intelligence + "✎ Intelligence <a>for <a>60m<a>!");
        return true;
    }

    private static void apply(SkyBlockPlayer player, int intelligence, long expiration) {
        player.getStatistics().removeTemporaryStatistics(statistic -> BUFF_NAME.equals(statistic.getDisplayName()));
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(ItemStatistic.INTELLIGENCE, (double) intelligence).build())
                .withExpirationInMs(expiration - System.currentTimeMillis())
                .withDisplayName(BUFF_NAME)
                .withDisplayColor("<c>")
                .build());
    }

    private static void consumeHeld(SkyBlockPlayer player, SkyBlockItem item) {
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
    }

    private static long buffExpiration(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.JYRRE_BUFF_EXPIRATION, DatapointLong.class).getValue();
    }

    private static int buffIntelligence(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.JYRRE_BUFF_INTELLIGENCE, DatapointInteger.class).getValue();
    }

    private static List<String> bottleLore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        return List.of(
                "<7>Consume to boost your <b>",
                "<b>Intelligence <7>for <a>60m<7>.",
                "",
                "<7>Keep this item in your inventory to",
                "<7>increase the bonus up to <b>+" + MAX_BONUS,
                "<b>Intelligence<7>, at which point the item",
                "<c><o>evolves<7>!",
                "",
                "<7>Current Bonus: <b>+" + bonusFor(item.getAttributeHandler().getJyrreAgeMinutes()) + " Intelligence");
    }
}
