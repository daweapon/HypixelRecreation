package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.playtime.ProfilePlaytime;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.function.Consumer;

public final class TyashoiService {
    public static final int COMBAT_REQUIREMENT = 10;
    public static final int PLAYTIME_HOURS_REQUIREMENT = 10;
    public static final String SPEAKER = "<e>[NPC] <5>Tyashoi Alchemist<f>: ";
    private static final int LINE_DELAY_TICKS = 24;

    private static final List<String> REFUSAL = List.of(
            "Hmm.. You do not seem experienced enough for what I require.",
            "Come back to me when you have harmed more than a fly.",
            "Must you need an explanation? I mean Combat Level 10 and 10 of playtime.");
    private static final List<String> INTRO = List.of(
            "The Great Spook...",
            "It's THERE!",
            "At least I think. You can never be too sure until it's really happening...",
            "Something has to be done, otherwise things are going to get way too <5><o>spooky<r><f>...",
            "Last time my assistant was alright.",
            "Was it you?",
            "Hum, I see.",
            "Well, last time my strategy worked really well.",
            "It was a genius plan to be fair.",
            "So I'll go with a similar strategy this year.",
            "There is no time to explain.",
            "Anyways, I'll reward you if you give me the proper ingredients.");

    public enum Result {BOUGHT, MISSING, LOCKED, ALREADY_CLAIMED, REQUIREMENT, EVENT_OVER}

    private TyashoiService() {
    }

    public static boolean meetsRequirements(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= COMBAT_REQUIREMENT
                && ProfilePlaytime.atLeastHours(player, PLAYTIME_HOURS_REQUIREMENT);
    }

    public static void greet(SkyBlockPlayer player, Consumer<SkyBlockPlayer> menu) {
        if (!meetsRequirements(player)) {
            say(player, REFUSAL, 0);
            return;
        }
        if (TyashoiProgress.introduced(player)) {
            menu.accept(player);
            return;
        }
        TyashoiProgress.introduce(player);
        say(player, INTRO, 0);
        schedule(INTRO.size() * LINE_DELAY_TICKS, () -> menu.accept(player));
    }

    public static boolean hasIngredients(SkyBlockPlayer player, TyashoiExperiment experiment) {
        for (TyashoiExperiment.Cost cost : experiment.costs()) {
            if (cost.isCoins()) {
                if (player.getCoins() < cost.coins()) return false;
                continue;
            }
            ItemType type = cost.type();
            if (type == null || player.countItem(type) < cost.amount()) return false;
        }
        return true;
    }

    public static Result attempt(SkyBlockPlayer player, TyashoiExperiment experiment) {
        if (!GreatSpookEvent.isActive()) return Result.EVENT_OVER;
        if (TyashoiProgress.claimed(player, experiment.id())) {
            player.sendMessage("<c>You can only trade for each item once!");
            return Result.ALREADY_CLAIMED;
        }
        if (experiment.requires() != null && !TyashoiProgress.claimed(player, experiment.requires())) {
            say(player, experiment.hint(), 0);
            return Result.REQUIREMENT;
        }
        if (!hasIngredients(player, experiment)) return missing(player, experiment);
        purchase(player, experiment);
        return Result.BOUGHT;
    }

    private static Result missing(SkyBlockPlayer player, TyashoiExperiment experiment) {
        long now = System.currentTimeMillis();
        long lockedUntil = TyashoiProgress.lockedUntil(player, experiment.id());
        boolean lockoutRunning = lockedUntil > now;
        if (lockoutRunning) {
            say(player, List.of("I have nothing more to tell you about that yet.",
                    "Come back in <e>" + formatDuration(lockedUntil - now) + " <r><f>and I may have more info about this ingredient."), 0);
            return Result.LOCKED;
        }
        boolean waitedOut = lockedUntil != 0;
        java.util.ArrayList<String> lines = new java.util.ArrayList<>(experiment.hint());
        if (waitedOut || experiment.lockoutSeconds() == 0) {
            lines.addAll(experiment.info());
        } else {
            long until = now + experiment.lockoutSeconds() * 1000;
            TyashoiProgress.lock(player, experiment.id(), until);
            lines.add("Come back in <e>" + formatDuration(experiment.lockoutSeconds() * 1000)
                    + " <r><f>and I may have more info about this ingredient.");
        }
        say(player, lines, 0);
        return Result.MISSING;
    }

    private static void purchase(SkyBlockPlayer player, TyashoiExperiment experiment) {
        for (TyashoiExperiment.Cost cost : experiment.costs()) {
            if (cost.isCoins()) player.removeCoins(cost.coins());
            else player.removeItemFromPlayer(cost.type(), cost.amount());
        }
        TyashoiProgress.claim(player, experiment.id());
        grant(player, experiment);
        player.sendMessage("<a>You bought <5>" + experiment.name() + "<a>!");
        say(player, experiment.success(), 1);
    }

    private static void grant(SkyBlockPlayer player, TyashoiExperiment experiment) {
        switch (experiment.kind()) {
            case ITEM -> player.addAndUpdateItem(new SkyBlockItem(ItemType.get(experiment.resultItem())));
            case PET -> player.getPetData().addPet(EerieUpgrades.newPet(player));
            case BOOK -> {
                SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
                book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(
                        EnchantmentType.valueOf(experiment.enchantment()), experiment.enchantmentLevel()));
                player.addAndUpdateItem(book);
            }
        }
        if (experiment.eerieUpgrade() != null) EerieUpgrades.upgradeOwned(player);
    }

    public static String formatDuration(long millis) {
        long seconds = millis / 1000;
        return String.format("%dd %02dh %02dm %02ds", seconds / 86400, seconds / 3600 % 24, seconds / 60 % 60, seconds % 60);
    }

    public static void say(SkyBlockPlayer player, List<String> lines, int firstDelayLines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = SPEAKER + lines.get(i);
            int delay = (i + firstDelayLines) * LINE_DELAY_TICKS;
            if (delay == 0) player.sendMessage(line);
            else schedule(delay, () -> player.sendMessage(line));
        }
    }

    private static void schedule(int ticks, Runnable task) {
        MinecraftServer.getSchedulerManager().buildTask(task).delay(TaskSchedule.tick(ticks)).schedule();
    }
}
