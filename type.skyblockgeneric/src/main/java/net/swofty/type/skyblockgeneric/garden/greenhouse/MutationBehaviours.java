package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.pest.PestRewards;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUISnake;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class MutationBehaviours {
    private static final MutationBehaviour DEFAULT = new MutationBehaviour() {
    };
    private static final Map<String, MutationBehaviour> REGISTRY = new HashMap<>();
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    public static final int[] SNOOZLING_SLEEP_STAGES = {5, 10, 15};
    public static final int THUNDERLING_CHARGE_PER_STAGE = 2_000;
    public static final int THUNDERLING_MAX_CHARGE = 16_000;
    public static final int[] ALOE_FRAGMENTS = {0, 0, 0, 1, 2, 3, 4, 6, 9, 13, 20, 28, 41, 60, 86, 125, 182, 263, 382, 554, 803, 1164, 1688, 2448, 3549, 5146, 7462};
    public static final int JELLYBEAN_FIRST_HARVEST_STAGE = 12;
    public static final int JELLYBEAN_STAGES_PER_MULTIPLIER = 12;
    public static final int JELLYBEAN_MAX_MULTIPLIER = 10;
    public static final int FLESHTRAP_MAX_HUNGER = 20;
    public static final int FLESHTRAP_START_HUNGER = 10;
    public static final int FLESHTRAP_HUNGER_PER_STAGE = 2;
    public static final double FLESHTRAP_BONUS_CAP = 100;
    public static final double FLESHTRAP_THIRD_DROP_CHANCE = 25;
    public static final int FLESHTRAP_MAX_ITEMS = 3;
    public static final double DEVOURER_ROOT_CHANCE = 0.4;
    public static final double PUFFERCLOUD_DAMAGE_PERCENT = 4;
    public static final int JERRYFLOWER_FEEDINGS = 10;
    private static final String[] SNOOZLING_WAKE = {
            "Fine, I'm up, I'm up.", "Okay, okay, I'm awake! Happy now?", "Alright, Alright. No need to get violent!", "Sheesh, can't a plant catch a nap?"};

    static {
        REGISTRY.put("BLASTBERRY", new Blastberry());
        REGISTRY.put("SNOOZLING", new Snoozling());
        REGISTRY.put("THUNDERLING", new Thunderling());
        REGISTRY.put("ALL_IN_ALOE", new Aloe());
        REGISTRY.put("GLASSCORN", new Glasscorn());
        REGISTRY.put("MAGIC_JELLYBEAN", new Jellybean());
        REGISTRY.put("NOCTILUME", new Noctilume());
        REGISTRY.put("CHEESEBITE", new Cheesebite());
        REGISTRY.put("FLESHTRAP", new Fleshtrap());
        REGISTRY.put("DEVOURER", new Devourer());
        REGISTRY.put("CHORUS_FRUIT", new ChorusFruit());
        REGISTRY.put("ZOMBUD", new Zombud());
        REGISTRY.put("TIMESTALK", new Timestalk());
        REGISTRY.put("PHANTOMLEAF", new Phantomleaf());
        REGISTRY.put("STARTLEVINE", new Startlevine());
        REGISTRY.put("CHLORONITE", new Chloronite());
        REGISTRY.put("PUFFERCLOUD", new Puffercloud());
        REGISTRY.put("THORNSHADE", new Thornshade());
        REGISTRY.put("DO_NOT_EAT_SHROOM", new DoNotEatShroom());
        REGISTRY.put("STOPLIGHT_PETAL", new StoplightPetal());
        REGISTRY.put("PLANTBOY_ADVANCE", new PlantBoy());
        REGISTRY.put("CINDERSHADE", new Dry());
        REGISTRY.put("SOGGYBUD", new Soggybud());
        REGISTRY.put("JERRYFLOWER", new Jerryflower());
    }

    private MutationBehaviours() {
    }

    public static MutationBehaviour of(String id) {
        return REGISTRY.getOrDefault(id, DEFAULT);
    }

    public static boolean waterable(String id) {
        return !(of(id) instanceof Soggybud) && of(id).needsWater();
    }

    public static double chloroniteDrops(double miningFortune) {
        if (miningFortune >= 2_000) return 4;
        if (miningFortune >= 1_000) return 3 + (miningFortune - 1_000) / 1_000;
        if (miningFortune >= 500) return 2 + (miningFortune - 500) / 500;
        if (miningFortune >= 100) return 1 + (miningFortune - 100) / 400;
        return miningFortune / 100;
    }

    public static int aloeFragments(int stage) {
        return stage <= 0 ? 0 : ALOE_FRAGMENTS[Math.min(stage, ALOE_FRAGMENTS.length) - 1];
    }

    public static double aloeResetChance(int stage) {
        return stage <= 3 ? 0 : 3.0 * (stage - 3);
    }

    public static int jellybeanMultiplier(int stage) {
        if (stage < JELLYBEAN_FIRST_HARVEST_STAGE) return 0;
        return Math.min(JELLYBEAN_MAX_MULTIPLIER, 1 + (stage - JELLYBEAN_FIRST_HARVEST_STAGE) / JELLYBEAN_STAGES_PER_MULTIPLIER);
    }

    public static int mealHunger(String itemName) {
        if (!isMeat(itemName)) return 0;
        boolean enchanted = itemName.startsWith("ENCHANTED_");
        boolean cooked = itemName.contains("COOKED") || itemName.contains("GRILLED");
        if (enchanted && cooked) return 6;
        return enchanted ? 4 : 2;
    }

    public static double mealBonus(String itemName) {
        return switch (mealHunger(itemName)) {
            case 6 -> 40;
            case 4 -> 20;
            default -> 0;
        };
    }

    static boolean isMeat(String name) {
        if (name.contains("FOOT") || name.contains("HIDE") || name.contains("FEATHER")) return false;
        for (String keyword : new String[]{"PORK", "BEEF", "MUTTON", "CHICKEN", "RABBIT", "STEAK"}) {
            if (name.contains(keyword)) return true;
        }
        return false;
    }

    private static void crop(MutationBehaviour.Ctx ctx, String name, String message) {
        ctx.player().sendMessage(Text.of("<e>[CROP] <9>{}<f>: {}", name, message));
    }

    static Pos centre(int plot, int anchor, int size) {
        return new Pos(GreenhouseLayout.worldX(plot, anchor % GreenhouseData.SIZE) + size / 2.0, GreenhouseLayout.CROP_Y,
                GreenhouseLayout.worldZ(plot, anchor / GreenhouseData.SIZE) + size / 2.0);
    }

    static Set<Integer> neighbourAnchors(MutationBehaviour.Ctx ctx) {
        Set<Integer> anchors = new LinkedHashSet<>();
        for (int cell : Footprint.ring(ctx.anchor(), ctx.size())) {
            int other = ctx.bed().anchorAt(cell);
            if (other >= 0 && other != ctx.anchor()) anchors.add(other);
        }
        return anchors;
    }

    static boolean inside(MutationBehaviour.Ctx ctx, SkyBlockPlayer player, double margin) {
        int column = ctx.anchor() % GreenhouseData.SIZE;
        int row = ctx.anchor() / GreenhouseData.SIZE;
        double minX = GreenhouseLayout.worldX(ctx.plot(), column) - margin;
        double minZ = GreenhouseLayout.worldZ(ctx.plot(), row) - margin;
        Pos position = player.getPosition();
        return position.x() >= minX && position.x() <= minX + ctx.size() + 2 * margin
                && position.z() >= minZ && position.z() <= minZ + ctx.size() + 2 * margin
                && position.y() >= GreenhouseLayout.FLOOR_Y - 1 && position.y() <= GreenhouseLayout.CROP_Y + 2;
    }

    static boolean cooldown(SkyBlockPlayer player, MutationBehaviour.Ctx ctx, String tag, long millis) {
        String key = player.getUuid() + ":" + ctx.plot() + ":" + ctx.anchor() + ":" + tag;
        long now = System.currentTimeMillis();
        if (COOLDOWNS.getOrDefault(key, 0L) > now) return false;
        COOLDOWNS.put(key, now + millis);
        return true;
    }

    static void trueDamage(SkyBlockPlayer player, double amount) {
        if (amount <= 0 || player.isDead()) return;
        player.setHealth((float) Math.max(1, player.getHealth() - amount));
    }

    static void explode(MutationBehaviour.Ctx ctx, boolean convertTurtellini) {
        for (int other : neighbourAnchors(ctx)) {
            GreenhouseData.Plant plant = ctx.bed().getPlants().get(other);
            if (plant == null || plant.getType().equals("BLASTBERRY")) continue;
            if (convertTurtellini && plant.getType().equals("TURTELLINI")) {
                GreenhouseData.Plant fruit = new GreenhouseData.Plant("SHELLFRUIT", ctx.now());
                fruit.setGrownAt(ctx.now());
                ctx.bed().getPlants().put(other, fruit);
            } else {
                ctx.bed().getPlants().remove(other);
            }
        }
    }

    private static final class Blastberry implements MutationBehaviour {
        @Override
        public boolean ticks(Ctx ctx) {
            return !ctx.plant().grown(ctx.type()) || ctx.plant().state("primed") == 0;
        }

        @Override
        public boolean beforeStage(Ctx ctx) {
            if (!ctx.plant().grown(ctx.type())) return true;
            ctx.plant().setState("primed", 1);
            return false;
        }

        @Override
        public void onStage(Ctx ctx) {
            ctx.plant().setState("primed", 1);
        }

        @Override
        public boolean onUse(Ctx ctx, SkyBlockItem held) {
            if (ctx.plant().state("primed") == 0) {
                ctx.player().sendMessage(Text.of("<c>This Blastberry is not primed yet! It needs a growth stage to prime."));
                return true;
            }
            explode(ctx, true);
            ctx.bed().getPlants().remove(ctx.anchor());
            ctx.player().sendMessage(Text.of("<c>BOOM! <7>The Blastberry exploded!"));
            return true;
        }

        @Override
        public void afterHarvest(Ctx ctx) {
            explode(ctx, false);
        }

        @Override
        public void onDecay(Ctx ctx) {
            explode(ctx, false);
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            return ctx.plant().state("primed") == 0 ? "Not primed" : "Primed";
        }
    }

    private static final class Snoozling implements MutationBehaviour {
        @Override
        public boolean beforeStage(Ctx ctx) {
            return ctx.plant().state("asleep") == 0;
        }

        @Override
        public void onStage(Ctx ctx) {
            for (int stage : SNOOZLING_SLEEP_STAGES) {
                if (ctx.plant().getStage() == stage) ctx.plant().setState("asleep", 1);
            }
        }

        @Override
        public boolean onUse(Ctx ctx, SkyBlockItem held) {
            if (ctx.plant().state("asleep") == 0) return false;
            ctx.plant().setState("asleep", 0);
            crop(ctx, "Snoozling", SNOOZLING_WAKE[ThreadLocalRandom.current().nextInt(SNOOZLING_WAKE.length)]);
            return true;
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            return ctx.plant().state("asleep") == 0 ? null : "Asleep (right-click to wake)";
        }
    }

    private static final class Thunderling implements MutationBehaviour {
        @Override
        public boolean needsWater() {
            return false;
        }

        @Override
        public boolean beforeStage(Ctx ctx) {
            return ctx.plant().state("charge") <= THUNDERLING_MAX_CHARGE;
        }

        @Override
        public void onStage(Ctx ctx) {
            ctx.plant().setState("charge", ctx.plant().state("charge") + THUNDERLING_CHARGE_PER_STAGE);
        }

        @Override
        public boolean onUse(Ctx ctx, SkyBlockItem held) {
            double charge = ctx.plant().state("charge");
            if (charge <= 0) return false;
            ctx.plant().setState("charge", 0);
            if (isEmptyBottle(held)) {
                ctx.player().sendMessage(Text.of("<e>ZOOP! <7>Your {}<7> has absorbed <e>{}<7> charge, calming your Thunderling.", held.getDisplayNameText(), (long) charge));
            } else {
                trueDamage(ctx.player(), ctx.player().getHealth() * 0.3);
                ctx.player().sendMessage(Text.of("<c>OUCH! The Thunderling has discharged all his energy, frying you!"));
            }
            return true;
        }

        @Override
        public Harvest onHarvest(Ctx ctx, SkyBlockItem held) {
            double charge = ctx.plant().state("charge");
            ctx.plant().setState("charge", 0);
            if (charge > 0 && !isEmptyBottle(held)) {
                trueDamage(ctx.player(), ctx.player().getHealth() * 0.3);
                ctx.player().sendMessage(Text.of("<c>OUCH! The Thunderling has discharged all his energy, frying you!"));
            }
            return Harvest.PROCEED;
        }

        private static boolean isEmptyBottle(SkyBlockItem held) {
            if (held.isNA()) return false;
            ItemType type = held.getAttributeHandler().getPotentialType();
            return type != null && type.name().endsWith("_IN_A_BOTTLE_EMPTY");
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            double charge = ctx.plant().state("charge");
            return "Charge " + (long) charge + "/" + THUNDERLING_MAX_CHARGE + (charge > THUNDERLING_MAX_CHARGE ? " (stalled, right-click to discharge)" : "");
        }
    }

    private static final class Aloe implements MutationBehaviour {
        @Override
        public boolean needsWater() {
            return false;
        }

        @Override
        public boolean harvestable(Ctx ctx) {
            return ctx.plant().getStage() >= 1;
        }

        @Override
        public void onStage(Ctx ctx) {
            double chance = aloeResetChance(ctx.plant().getStage());
            if (chance > 0 && ThreadLocalRandom.current().nextDouble() * 100 < chance) {
                ctx.plant().setStage(1);
                ctx.plant().setGrownAt(0);
            }
        }

        @Override
        public List<SkyBlockItem> extraDrops(Ctx ctx) {
            ItemType fragment = ItemType.get("ALL_IN_ALOE_FRAGMENT");
            int amount = aloeFragments(ctx.plant().getStage());
            return fragment == null || amount <= 0 ? List.of() : List.of(new SkyBlockItem(fragment, amount));
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            int stage = ctx.plant().getStage();
            return aloeFragments(stage) + " Fragments, " + (int) aloeResetChance(stage + 1) + "% reset chance next stage";
        }
    }

    private static final class Glasscorn implements MutationBehaviour {
        @Override
        public boolean ticks(Ctx ctx) {
            return true;
        }

        @Override
        public boolean beforeStage(Ctx ctx) {
            if (ctx.plant().getStage() < ctx.type().stages()) return true;
            ctx.plant().setStage(1);
            ctx.plant().setGrownAt(0);
            return false;
        }

        @Override
        public boolean harvestable(Ctx ctx) {
            return ctx.plant().getStage() >= ctx.type().stages() - 1;
        }
    }

    private static final class Jellybean implements MutationBehaviour {
        @Override
        public boolean harvestable(Ctx ctx) {
            return ctx.plant().getStage() >= JELLYBEAN_FIRST_HARVEST_STAGE;
        }

        @Override
        public double dropMultiplier(Ctx ctx) {
            return Math.max(1, jellybeanMultiplier(ctx.plant().getStage()));
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            return "Drop multiplier " + jellybeanMultiplier(ctx.plant().getStage()) + "x";
        }
    }

    private static final class Noctilume implements MutationBehaviour {
        static boolean day() {
            int hour = SkyBlockCalendar.getHour();
            return hour >= 6 && hour < 18;
        }

        @Override
        public boolean beforeStage(Ctx ctx) {
            return ctx.plant().getStage() % 2 == 0 ? day() : !day();
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            return "Needs " + (ctx.plant().getStage() % 2 == 0 ? "day" : "night") + " to advance";
        }
    }

    private static final class Cheesebite implements MutationBehaviour {
        @Override
        public boolean beforeStage(Ctx ctx) {
            return ctx.plant().state("rat") == 0;
        }

        @Override
        public void onStage(Ctx ctx) {
            int stage = ctx.plant().getStage();
            if (stage == 4 || stage == 7) {
                ctx.plant().setState("rat", 1);
                spawnRat(ctx);
            }
        }

        @Override
        public void onTick(Ctx ctx) {
            if (ctx.plant().state("rat") == 0 || ctx.player().getInstance() == null) return;
            if (!GreenhouseCreatures.alive(ctx.player().getInstance(), ctx.plot(), ctx.anchor(), "rat")) spawnRat(ctx);
        }

        private void spawnRat(Ctx ctx) {
            if (ctx.player().getInstance() == null) return;
            GreenhouseCreatures.spawn(ctx.player().getInstance(), ctx.plot(), ctx.anchor(), "rat", new GreenhouseMobs.Rat(),
                    centre(ctx.plot(), ctx.anchor(), 1).add(0.8, 0, 0.8), killer -> {
                        GreenhouseData.Plant plant = ctx.bed().getPlants().get(ctx.anchor());
                        if (plant != null) plant.setState("rat", 0);
                        Gardens.save(killer, ctx.profile());
                        killer.sendMessage(Text.of("<a>You killed the Rat that was eating your Cheesebite!"));
                    });
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            return ctx.plant().state("rat") == 0 ? null : "A Rat is eating it, growth stopped";
        }
    }

    private static final class Fleshtrap implements MutationBehaviour {
        private static double hunger(Ctx ctx) {
            return ctx.plant().state("hungerSet") == 0 ? FLESHTRAP_START_HUNGER : ctx.plant().state("hunger");
        }

        private static void setHunger(Ctx ctx, double value) {
            ctx.plant().setState("hungerSet", 1);
            ctx.plant().setState("hunger", Math.max(0, Math.min(FLESHTRAP_MAX_HUNGER, value)));
        }

        @Override
        public boolean beforeStage(Ctx ctx) {
            return hunger(ctx) > 0;
        }

        @Override
        public void onStage(Ctx ctx) {
            setHunger(ctx, hunger(ctx) - FLESHTRAP_HUNGER_PER_STAGE);
        }

        @Override
        public boolean onUse(Ctx ctx, SkyBlockItem held) {
            if (held.isNA()) return false;
            ItemType type = held.getAttributeHandler().getPotentialType();
            int gain = type == null ? 0 : mealHunger(type.name());
            if (gain <= 0) return false;
            if (hunger(ctx) >= FLESHTRAP_MAX_HUNGER) {
                crop(ctx, "Fleshtrap", "I'm already full!");
                return true;
            }
            ctx.player().removeItemFromPlayer(type, 1);
            setHunger(ctx, hunger(ctx) + gain);
            double bonus = Math.min(FLESHTRAP_BONUS_CAP, ctx.plant().state("bonus") + mealBonus(type.name()));
            ctx.plant().setState("bonus", bonus);
            crop(ctx, "Fleshtrap", "Yum!");
            return true;
        }

        @Override
        public int itemCount(Ctx ctx) {
            double bonus = ctx.plant().state("bonus");
            int count = 1 + (int) (bonus / 100);
            ItemType item = ItemType.get("FLESHTRAP");
            if (item == null) return count;
            if (LootRoll.roll(ctx.player().getUuid(), item, bonus % 100, ThreadLocalRandom.current())) count++;
            if (bonus >= FLESHTRAP_BONUS_CAP && LootRoll.roll(ctx.player().getUuid(), item, FLESHTRAP_THIRD_DROP_CHANCE, ThreadLocalRandom.current())) count++;
            count = Math.min(count, FLESHTRAP_MAX_ITEMS);
            return count;
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            return "Hunger " + (int) hunger(ctx) + "/" + FLESHTRAP_MAX_HUNGER + ", bonus drop +" + (int) ctx.plant().state("bonus") + "%";
        }
    }

    private static final class Devourer implements MutationBehaviour {
        @Override
        public void onStage(Ctx ctx) {
            if (ThreadLocalRandom.current().nextDouble() >= DEVOURER_ROOT_CHANCE) return;
            boolean destroyed = false;
            for (int other : neighbourAnchors(ctx)) {
                GreenhouseData.Plant plant = ctx.bed().getPlants().get(other);
                if (plant == null || plant.getType().equals("DEVOURER")) continue;
                ctx.bed().getPlants().remove(other);
                destroyed = true;
            }
            if (destroyed) ctx.player().sendMessage(Text.of("<c>A Devourer spread its roots and destroyed nearby crops!"));
        }
    }

    private static final class ChorusFruit implements MutationBehaviour {
        @Override
        public boolean needsWater() {
            return false;
        }

        @Override
        public void onStage(Ctx ctx) {
            List<Integer> free = new ArrayList<>();
            for (int slot = 0; slot < GreenhouseData.SLOTS; slot++) {
                if (ctx.bed().getUnlocked()[slot] && !ctx.bed().occupied(slot)) free.add(slot);
            }
            if (free.isEmpty()) return;
            int target = free.get(ThreadLocalRandom.current().nextInt(free.size()));
            ctx.bed().setSurface(ctx.anchor(), Surface.END_STONE);
            ctx.bed().setSurface(target, Surface.END_STONE);
            ctx.bed().getPlants().remove(ctx.anchor());
            ctx.bed().getPlants().put(target, ctx.plant());
        }
    }

    private static final class Zombud implements MutationBehaviour {
        @Override
        public boolean needsWater() {
            return false;
        }

        @Override
        public void afterHarvest(Ctx ctx) {
            ItemType zombud = ItemType.get("ZOMBUD");
            for (int other : neighbourAnchors(ctx)) {
                GreenhouseData.Plant plant = ctx.bed().getPlants().get(other);
                if (plant == null || !plant.getType().equals(GreenhouseConfig.DEAD_PLANT)) continue;
                ctx.bed().getPlants().remove(other);
                if (ctx.player().getInstance() == null || zombud == null) continue;
                new GreenhouseMobs.Zombuddy().spawnAt(ctx.player().getInstance(), centre(ctx.plot(), other, 1),
                        killer -> PestRewards.give(killer, new SkyBlockItem(zombud, 1)));
            }
        }
    }

    private static final class Timestalk implements MutationBehaviour {
        @Override
        public void afterHarvest(Ctx ctx) {
            ItemType timestalk = ItemType.get("TIMESTALK");
            if (ctx.player().getInstance() == null || timestalk == null) return;
            new GreenhouseMobs.TimestalkClone().spawnAt(ctx.player().getInstance(), centre(ctx.plot(), ctx.anchor(), 1).add(1, 0, 1),
                    killer -> PestRewards.give(killer, new SkyBlockItem(timestalk, 1)));
        }
    }

    private static final class Phantomleaf implements MutationBehaviour {
        @Override
        public boolean needsWater() {
            return false;
        }

        @Override
        public Harvest onHarvest(Ctx ctx, SkyBlockItem held) {
            return GreenhouseMinigames.startHide(ctx);
        }
    }

    private static final class Startlevine implements MutationBehaviour {
        static boolean invisible(SkyBlockPlayer player) {
            return player.getActiveEffects().stream().anyMatch(active -> active.potion().effect() == PotionEffect.INVISIBILITY);
        }

        @Override
        public Harvest onHarvest(Ctx ctx, SkyBlockItem held) {
            if (invisible(ctx.player())) return Harvest.PROCEED;
            crop(ctx, "Startlevine", "I'm too scared to be harvested right now...");
            return Harvest.BLOCKED;
        }

        @Override
        public boolean listensNear() {
            return true;
        }

        @Override
        public void onNear(Ctx ctx, SkyBlockPlayer nearby) {
            if (!ctx.plant().grown(ctx.type()) || invisible(nearby) || !inside(ctx, nearby, 2.5)) return;
            if (cooldown(nearby, ctx, "eek", 10_000)) nearby.sendMessage(Text.of("<e>[CROP] <9>Startlevine<f>: Eeek!"));
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            return "Needs Invisibility to harvest";
        }
    }

    private static final class Chloronite implements MutationBehaviour {
        static int breakingPower(SkyBlockPlayer player, SkyBlockItem held) {
            int power = held.isNA() ? 0 : held.getAttributeHandler().getBreakingPower();
            return power + net.swofty.type.skyblockgeneric.hotm.HotmAbilities.breakingPowerBonus(player);
        }

        @Override
        public Harvest onHarvest(Ctx ctx, SkyBlockItem held) {
            if (breakingPower(ctx.player(), held) >= 1) return Harvest.PROCEED;
            ctx.player().sendMessage(Text.of("<c>You need a tool with Breaking Power 1 or higher to harvest this!"));
            return Harvest.BLOCKED;
        }

        @Override
        public int itemCount(Ctx ctx) {
            double fortune = ctx.player().getStatistics().allStatistics().getOverall(ItemStatistic.MINING_FORTUNE);
            double drops = chloroniteDrops(Math.max(0, fortune));
            int count = (int) Math.floor(drops);
            ItemType item = ItemType.get("CHLORONITE");
            double fraction = drops - count;
            if (fraction > 0 && item != null && LootRoll.roll(ctx.player().getUuid(), item, fraction * 100, ThreadLocalRandom.current())) count++;
            return count;
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            double fortune = ctx.player().getStatistics().allStatistics().getOverall(ItemStatistic.MINING_FORTUNE);
            return "Needs Breaking Power 1, guaranteed drops " + (int) Math.floor(chloroniteDrops(Math.max(0, fortune)));
        }
    }

    private static final class Puffercloud implements MutationBehaviour {
        @Override
        public boolean listensNear() {
            return true;
        }

        @Override
        public void onNear(Ctx ctx, SkyBlockPlayer nearby) {
            if (!inside(ctx, nearby, 1) || !cooldown(nearby, ctx, "spore", 3_000)) return;
            trueDamage(nearby, nearby.getMaxHealth() * PUFFERCLOUD_DAMAGE_PERCENT / 100);
            nearby.sendMessage(Text.of("<c>The Puffercloud released spores that hurt you!"));
        }
    }

    private static final class Thornshade implements MutationBehaviour {
        @Override
        public boolean listensNear() {
            return true;
        }

        @Override
        public void onNear(Ctx ctx, SkyBlockPlayer nearby) {
            if (!inside(ctx, nearby, 0) || !cooldown(nearby, ctx, "thorn", 1_000)) return;
            trueDamage(nearby, Math.max(1, nearby.getMaxHealth() * 0.01));
        }
    }

    private static final class DoNotEatShroom implements MutationBehaviour {
        @Override
        public void afterHarvest(Ctx ctx) {
            ctx.player().addEffect(new Potion(PotionEffect.BLINDNESS, (byte) 2, 60));
            ctx.player().addEffect(new Potion(PotionEffect.NAUSEA, (byte) 2, 100));
        }
    }

    private static final class StoplightPetal implements MutationBehaviour {
        @Override
        public Harvest onHarvest(Ctx ctx, SkyBlockItem held) {
            return GreenhouseMinigames.startPunch(ctx);
        }
    }

    private static final class PlantBoy implements MutationBehaviour {
        @Override
        public boolean needsWater() {
            return false;
        }

        @Override
        public Harvest onHarvest(Ctx ctx, SkyBlockItem held) {
            crop(ctx, "PlantBoy Advance", "I doubt you are a real gamer. I challenge you to a game of Snake!");
            ctx.player().openView(new GUISnake(new GreenhouseLayout.Spot(ctx.plot(), ctx.anchor())));
            return Harvest.DEFERRED;
        }
    }

    private static final class Dry implements MutationBehaviour {
        @Override
        public boolean needsWater() {
            return false;
        }
    }

    private static final class Soggybud implements MutationBehaviour {
        @Override
        public void onStage(Ctx ctx) {
            double taken = 0;
            for (int other : neighbourAnchors(ctx)) {
                GreenhouseData.Plant plant = ctx.bed().getPlants().get(other);
                if (plant == null || plant.getType().equals("SOGGYBUD") || plant.getWater() <= 10) continue;
                plant.setWater(plant.getWater() - 10);
                taken += 10;
            }
            if (taken > 0) {
                ctx.plant().setWater(Math.min(GreenhouseRules.MAX_WATER, ctx.plant().getWater() + taken));
                ctx.plant().setLastWateredAt(ctx.now());
            }
        }
    }

    private static final class Jerryflower implements MutationBehaviour {
        @Override
        public boolean needsWater() {
            return false;
        }

        @Override
        public boolean beforeStage(Ctx ctx) {
            return ctx.plant().getStage() != 5 || ctx.plant().state("fed") >= JERRYFLOWER_FEEDINGS;
        }

        @Override
        public boolean onUse(Ctx ctx, SkyBlockItem held) {
            if (held.isNA() || held.getAttributeHandler().getPotentialType() != ItemType.MOVE_JERRY) return false;
            if (ctx.plant().getStage() != 5 || ctx.plant().state("fed") >= JERRYFLOWER_FEEDINGS) return false;
            ctx.player().removeItemFromPlayer(ItemType.MOVE_JERRY, 1);
            ctx.plant().setState("fed", ctx.plant().state("fed") + 1);
            ctx.player().sendMessage(Text.of("<a>You fed the Jerryflower a Move Jerry! <8>({}/{})", (int) ctx.plant().state("fed"), JERRYFLOWER_FEEDINGS));
            return true;
        }

        @Override
        public @Nullable String status(Ctx ctx) {
            return ctx.plant().getStage() == 5 && ctx.plant().state("fed") < JERRYFLOWER_FEEDINGS
                    ? "Waiting for Move Jerry (" + (int) ctx.plant().state("fed") + "/" + JERRYFLOWER_FEEDINGS + ")" : null;
        }
    }
}
