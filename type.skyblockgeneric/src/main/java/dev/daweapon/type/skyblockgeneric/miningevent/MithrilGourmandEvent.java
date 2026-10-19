package dev.daweapon.type.skyblockgeneric.miningevent;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class MithrilGourmandEvent extends MiningEvent {
    private static final List<String> COMPLAINTS = List.of(
            "I FORGOT TO BRING A SNACK", "0/10, WOULD NOT RECOMMEND, TAKING TOO LONG", "WHAT ARE YOU GUYS WAITING FOR",
            "I AM LEAVING A BAD RATING FOR THIS PLACE ON MINEADVISOR", "I WANT TO SPEAK TO YOUR MANAGER", "THEY LOOK SO GOOD",
            "I MIGHT JUST STAY HERE FOREVER THEN", "DO YOU EVEN KNOW WHO I AM", "MY FATHER WILL HEAR ABOUT THIS",
            "WHY IS THIS TAKING SO LONG", "I AM RATING THIS ESTABLISHMENT ONE STAR ON DWELP",
            "I DON'T HAVE ALL DAY, I ONLY WANT THE BEST LOOKING MITHRIL PIECES!", "MY MEALS SHOULD HAVE ALREADY BEEN READY",
            "I AM NOT HAPPY WITH HOW LONG THIS IS TAKING", "I AM NOT OKAY WITH THIS",
            "YOU SHOULD ALL BE ASHAMED OF YOURSELVES", "FEED ME");
    private static final List<String> WRONG_ITEM = List.of(
            "THIS ISN'T WHAT I ASKED FOR! BRING TASTY MITHRIL TO ME NOW!",
            "DISGUSTING! I'M GONNA GET YOU FIRED FOR FEEDING ME THIS GARBAGE!", "EWWW WHAT IS THIS?");
    private static final long COMPLAINT_MIN_MILLIS = 20_000;
    private static final long COMPLAINT_MAX_MILLIS = 35_000;
    private static final double COMPLAINT_RANGE = 40;

    private final String eventId = UUID.randomUUID().toString();
    private final Map<UUID, Integer> donated = new HashMap<>();
    private MiningEventConfig.Site site;
    private int goal;
    private int total;
    private long nextComplaintAt;
    private boolean teleportButtonShown;
    private boolean goalReached;

    MithrilGourmandEvent(MiningEventZone zone) {
        super(MiningEventType.MITHRIL_GOURMAND, zone);
    }

    public static @Nullable MithrilGourmandEvent active() {
        return MiningEvents.current().filter(MithrilGourmandEvent.class::isInstance).map(MithrilGourmandEvent.class::cast).orElse(null);
    }

    public static Pos donPosition() {
        MiningEventConfig config = MiningEventConfig.get();
        var raw = config.section("mithril_gourmand", "don_position");
        return new Pos(num(raw.get("x")), num(raw.get("y")), num(raw.get("z")), (float) num(raw.get("yaw")), 0);
    }

    @Override
    protected void onStart() {
        MiningEventConfig config = MiningEventConfig.get();
        site = EventSites.pick(config.sites("mithril_gourmand", "sites"));
        int min = config.integer(100, "mithril_gourmand", "goal_min");
        int max = config.integer(200, "mithril_gourmand", "goal_max");
        goal = min + ThreadLocalRandom.current().nextInt(max - min + 1);
        nextComplaintAt = System.currentTimeMillis() + COMPLAINT_MIN_MILLIS;
        MiningEventMessages.banner(type(), "MITHRIL GOURMAND STARTED!", "Active Mining Event", List.of(
                "<f>Don Expresso wants some Tasty Mithril from the <b>" + site.name() + "<f>.",
                "<f>Bring <b>" + min + "-" + max + " Tasty Mithril <f>to him at the <b>Dwarven Village<f>!",
                "<f>Bring <b>" + config.integer(10, "mithril_gourmand", "personal_threshold") + " <f>personally and you'll receive extra rewards.",
                " ",
                "<hover:'<e>Teleport to Old Man Garry'><click:run:'/tptogarry'><e><l>Go to " + site.name() + " (or talk to Garry)</click></hover>"));
        say("I HUNGER");
        say("BRING ME EDIBLE MITHRIL");
    }

    @Override
    public void tick(long now) {
        if (now >= nextComplaintAt && !goalReached) {
            say(COMPLAINTS.get(ThreadLocalRandom.current().nextInt(COMPLAINTS.size())));
            nextComplaintAt = now + COMPLAINT_MIN_MILLIS + ThreadLocalRandom.current().nextLong(COMPLAINT_MAX_MILLIS - COMPLAINT_MIN_MILLIS);
        }
        long buttonSeconds = (long) MiningEventConfig.get().number(20, "mithril_gourmand", "teleport_button_seconds");
        if (goalReached && !teleportButtonShown && millisLeft() <= buttonSeconds * 1000) {
            teleportButtonShown = true;
            MiningEvents.broadcast("<hover:'<e>Teleport to Don Expresso'><click:run:'/tptodonexpresso'><b><l>[Teleport to Don Expresso]</l></click></hover>");
        }
    }

    @Override
    protected void onEnd(boolean cancelled) {
        MiningEventConfig config = MiningEventConfig.get();
        int threshold = config.integer(10, "mithril_gourmand", "personal_threshold");
        boolean success = total >= goal;
        MiningEventMessages.banner(type(), "MITHRIL GOURMAND ENDED!", null, List.of(
                success ? "<f>Don Expresso was fed <b>" + total + " Tasty Mithril <f>and fell asleep."
                        : "<f>Don Expresso only got <b>" + total + "<f> of the <b>" + goal + " Tasty Mithril <f>he wanted."));
        say(success ? "I THINK I MIGHT HAVE ROCK POISONING, I NEED TO LAY DOWN!" : "I DON'T FEEL SO GOOD...");
        for (SkyBlockPlayer player : MiningEvents.players()) {
            int count = donated.getOrDefault(player.getUuid(), 0);
            if (count <= 0) {
                player.sendMessage("<7>You didn't participate in this event, so you got no rewards.");
                continue;
            }
            String key = (count >= threshold ? "high" : "low") + "_" + (success ? "success" : "fail");
            MiningEventRewards.grant(player, config.tier("rewards", "mithril_gourmand", key), count);
        }
    }

    @Override
    public @Nullable Pos warpLocation() {
        return EventSites.center(HypixelConst.getInstanceContainer(), site);
    }

    public String siteName() {
        return site.name();
    }

    public String eventId() {
        return eventId;
    }

    public boolean isAtSite(Point point) {
        double dx = point.x() - site.x();
        double dz = point.z() - site.z();
        return dx * dx + dz * dz <= site.radius() * site.radius();
    }

    public void rollDrop(SkyBlockPlayer player, Point block, List<SkyBlockItem> drops) {
        if (goalReached || !isAtSite(block)) return;
        double chance = MiningEventConfig.get().number(30, "mithril_gourmand", "tasty_chance_percent");
        if (!LootRoll.roll(player.getUuid(), ItemType.MITHRIL_GOURMAND, chance)) return;
        drops.add(tasty(1));
    }

    public SkyBlockItem tasty(int amount) {
        SkyBlockItem item = new SkyBlockItem(ItemType.MITHRIL_GOURMAND, amount);
        item.getAttributeHandler().setMiningEventId(eventId);
        return item;
    }

    public void feed(SkyBlockPlayer player) {
        int fresh = 0;
        int stale = 0;
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != ItemType.MITHRIL_GOURMAND) continue;
            if (item.getAttributeHandler().getMiningEventId().equals(eventId)) fresh += stack.amount();
            else stale += stack.amount();
            player.getInventory().setItemStack(slot, ItemStack.AIR);
        }
        if (stale > 0) {
            player.sendMessage("<e>[NPC] <b>Don Expresso<f>: THIS OLD MITHRIL IS STALE! DON'T EVER FEED THIS TO ME AGAIN!");
            player.sendMessage("<c>" + stale + " Tasty Mithril pieces were removed from a different event!");
        }
        if (fresh == 0) {
            if (stale == 0) player.sendMessage("<e>[NPC] <b>Don Expresso<f>: " + WRONG_ITEM.get(ThreadLocalRandom.current().nextInt(WRONG_ITEM.size())));
            return;
        }
        donated.merge(player.getUuid(), fresh, Integer::sum);
        total += fresh;
        markParticipant(player);
        player.getAchievementHandler().addProgressByTrigger("skyblock.a_good_review_trigger", 1);
        player.sendMessage("<e>[NPC] <b>Don Expresso<f>: YUM! <7>You fed him <b>{} Tasty Mithril<7>.", StringUtility.commaify(fresh));
        if (!goalReached && total >= goal) {
            goalReached = true;
            shortenTo(System.currentTimeMillis() + (long) (MiningEventConfig.get().number(60, "mithril_gourmand", "early_finish_seconds") * 1000));
            MiningEvents.broadcast("<7><i>Don Expresso is leaving in 1 minute! Make sure to feed him everything you have before he leaves!");
        }
    }

    private void say(String line) {
        Pos don = donPosition();
        for (SkyBlockPlayer player : MiningEvents.players()) {
            if (player.getPosition().distance(don) <= COMPLAINT_RANGE) player.sendMessage("<e>[NPC] <b>Don Expresso<f>: " + line);
        }
    }

    private static double num(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0;
    }
}
