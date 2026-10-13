package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class RosewaterFlask {
    public static final String FLASK = "ROSEWATER_FLASK";
    public static final String FILLED = "FILLED_ROSEWATER_FLASK";
    public static final int MAX_BONUS = 4;
    public static final int MINUTES_PER_STAGE = 75 * 60;
    public static final long COOLDOWN_MILLIS = 2L * 60 * 60 * 1000;
    public static final int PERMANENT_LIMIT = 5;
    public static final double FORTUNE_EACH = 1;
    private static final int INVENTORY_SLOTS = 36;

    private RosewaterFlask() {
    }

    public static int bonusFor(long agedMinutes) {
        return (int) Math.min(MAX_BONUS, Math.max(0, agedMinutes) / MINUTES_PER_STAGE);
    }

    public static boolean hasEvolved(long agedMinutes) {
        return bonusFor(agedMinutes) >= MAX_BONUS;
    }

    public static double permanentFortune(int consumed) {
        return Math.min(PERMANENT_LIMIT, Math.max(0, consumed)) * FORTUNE_EACH;
    }

    public static void registerLore() {
        LoreRegistry.register(FLASK, new LoreConfig(RosewaterFlask::flaskLore, null));
    }

    public static void registerInteractables() {
        InteractableRegistry.register(FLASK, InteractableItemConfig.builder().rightClickHandler((player, item) -> use(player, item, false)).build());
        InteractableRegistry.register(FILLED, InteractableItemConfig.builder().rightClickHandler((player, item) -> use(player, item, true)).build());
    }

    public static void startAging() {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                        SkyBlockGenericLoader.forEachLoadedPlayer(RosewaterFlask::age))
                .repeat(TaskSchedule.minutes(1))
                .schedule();
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double fortune = permanentFortune(Gardens.profile(player).greenhouse().getFlasksConsumed());
        if (fortune <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.FARMING_FORTUNE, fortune).build();
    }

    private static void age(SkyBlockPlayer player) {
        ItemType flask = ItemType.get(FLASK);
        ItemType filled = ItemType.get(FILLED);
        if (flask == null || filled == null) return;
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != flask) continue;
            long aged = item.getAttributeHandler().getJyrreAgeMinutes() + 1;
            if (hasEvolved(aged)) {
                SkyBlockItem evolved = new SkyBlockItem(filled);
                evolved.setAmount(item.getAmount());
                player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, evolved.getItemStack()).build());
                player.sendMessage(Text.of("<d><l>EVOLUTION! <d>Your <9>Rosewater Flask <d>has evolved!"));
                continue;
            }
            item.getAttributeHandler().setJyrreAgeMinutes(aged);
            player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
        }
    }

    private static void use(SkyBlockPlayer player, SkyBlockItem item, boolean filled) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        int plot = GardenLayout.plotAt(player.getPosition().x(), player.getPosition().z());
        if (plot == GardenLayout.NONE || !GreenhouseService.isGreenhouse(profile, plot)) {
            player.sendMessage(Text.of("<c>You must be standing in one of your Greenhouse plots to use this!"));
            return;
        }
        long now = System.currentTimeMillis();
        if (data.getFlaskReadyAt() > now) {
            player.sendMessage(Text.of("<c>You must wait <e>{} <c>before using another Rosewater Flask!", span(data.getFlaskReadyAt() - now)));
            return;
        }
        if (filled && data.getFlasksConsumed() >= PERMANENT_LIMIT) {
            player.sendMessage(Text.of("<c>You have already consumed the maximum amount of Filled Rosewater Flasks!"));
            return;
        }
        int stages = filled ? MAX_BONUS : bonusFor(item.getAttributeHandler().getJyrreAgeMinutes());
        if (stages <= 0) {
            player.sendMessage(Text.of("<c>This Rosewater Flask has not aged enough to grow any crops yet!"));
            return;
        }
        data.setFlaskReadyAt(now + COOLDOWN_MILLIS);
        if (filled) data.setFlasksConsumed(data.getFlasksConsumed() + 1);
        Gardens.save(player, profile);
        consumeHeld(player, item);
        int grown = GreenhouseService.growPlot(player, plot, stages);
        player.sendMessage(Text.of("<a>The Rosewater Flask grew <2>{} <a>crops in this Greenhouse by up to <2>{} Growth Stages<a>!", grown, stages));
        if (filled) {
            player.sendMessage(Text.of("<a>You gained a permanent <6>+1 Farming Fortune<a>! <7>({}/{})", data.getFlasksConsumed(), PERMANENT_LIMIT));
        }
    }

    private static void consumeHeld(SkyBlockPlayer player, SkyBlockItem item) {
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
    }

    private static List<String> flaskLore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        return List.of(
                "<7>Consume to grow crops within the",
                "<a>Greenhouse <7>by several <2>Growth",
                "<2>Stages<7>.",
                "",
                "<7>Keep this item in your inventory to",
                "<7>increase the bonus up to <2>" + MAX_BONUS + " Growth",
                "<2>Stages<7>, at which point the item",
                "<c><o>evolves<7>!",
                "",
                "<7>Current Bonus: <2>+" + bonusFor(item.getAttributeHandler().getJyrreAgeMinutes()) + " Growth Stages",
                "",
                "<8>Co-op wide 2h cooldown.");
    }

    private static String span(long millis) {
        long minutes = Math.max(1, millis / 60_000);
        return minutes >= 60 ? minutes / 60 + "h " + minutes % 60 + "m" : minutes + "m";
    }
}
