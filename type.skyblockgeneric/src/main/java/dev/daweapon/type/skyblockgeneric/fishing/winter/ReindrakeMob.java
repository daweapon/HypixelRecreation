package dev.daweapon.type.skyblockgeneric.fishing.winter;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Reindrake: an Ender Dragon that circles above the pond it formed in and climbs when players
 * come close, taking one damage per hit (wiki: Reindrake). Every hit has a 1 in 10 chance to drop
 * a Gift, and whenever someone else gets one the angler gets one too. Its loot goes only to the
 * angler, and everyone who landed a hit gets Bestiary credit.
 *
 * Minestom can't route hits on the client-side dragon parts, so the hittable body is an invisible
 * Ghast and the dragon is a visual that follows it.
 */
public class ReindrakeMob extends WinterSeaCreatureMob {
    private static final Set<ReindrakeMob> ALIVE = ConcurrentHashMap.newKeySet();
    private static final double HOVER_HEIGHT = 6;
    private static final double MAX_HEIGHT = 28;
    private static final double CLIMB_PER_TICK = 0.25;
    private static final double SINK_PER_TICK = 0.08;
    private static final double CIRCLE_RADIUS = 8;
    private static final double ANGLE_PER_TICK = 0.025;
    private static final double PRESSURE_RANGE = 14;

    private final Set<UUID> hitters = new LinkedHashSet<>();
    private @Nullable Entity dragon;
    private @Nullable Pos anchor;
    private double height = HOVER_HEIGHT;
    private double angle;

    public ReindrakeMob(@Nullable SkyBlockPlayer angler) {
        super(WinterSeaCreature.REINDRAKE, angler);
        setNoGravity(true);
        setInvisible(true);
    }

    public static boolean anyAlive() {
        ALIVE.removeIf(mob -> mob.isRemoved() || mob.isDead());
        return !ALIVE.isEmpty();
    }

    public static void announceCatch(SkyBlockPlayer angler, int count) {
        Text message = count > 1
                ? Text.of("<c><l>WOAH! </l><7>{} <c>summoned <4>TWO Reindrakes <c>from the depths!", angler.getUsername())
                : Text.of("<c><l>WOAH! </l><7>{} <c>summoned a <4>Reindrake <c>from the depths!", angler.getUsername());
        SkyBlockGenericLoader.forEachLoadedPlayer(player -> player.sendMessage(message));
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        ALIVE.add(this);
        anchor = getPosition();
        angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        Instance instance = getInstance();
        if (instance == null) return;
        dragon = new Entity(EntityType.ENDER_DRAGON);
        dragon.setNoGravity(true);
        dragon.setInstance(instance, getPosition());
    }

    @Override
    protected void onTick(int ticks) {
        SkyBlockPlayer angler = getAngler();
        if (angler == null || !angler.isOnline() || angler.getInstance() != getInstance()) {
            remove();
            return;
        }
        fly();
    }

    private void fly() {
        if (anchor == null) return;
        height = underPressure()
                ? Math.min(MAX_HEIGHT, height + CLIMB_PER_TICK)
                : Math.max(HOVER_HEIGHT, height - SINK_PER_TICK);
        angle += ANGLE_PER_TICK;
        double x = Math.cos(angle) * CIRCLE_RADIUS;
        double z = Math.sin(angle) * CIRCLE_RADIUS;
        float heading = (float) Math.toDegrees(Math.atan2(-Math.cos(angle), -Math.sin(angle)));
        Pos next = anchor.add(x, height, z).withView(heading, 0);
        teleport(next);
        if (dragon != null && !dragon.isRemoved()) dragon.teleport(next.withYaw(heading + 180));
    }

    private boolean underPressure() {
        Pos position = getPosition();
        Instance instance = getInstance();
        if (instance == null) return false;
        return instance.getPlayers().stream().anyMatch(player -> {
            Pos at = player.getPosition();
            double dx = at.x() - position.x();
            double dz = at.z() - position.z();
            return dx * dx + dz * dz <= PRESSURE_RANGE * PRESSURE_RANGE && at.y() > position.y() - PRESSURE_RANGE;
        });
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (!(damage.getAttacker() instanceof SkyBlockPlayer player)) return false;
        boolean hit = super.damage(new Damage(damage.getType(), damage.getSource(), damage.getAttacker(),
                damage.getSourcePosition(), 1f));
        if (hit) {
            hitters.add(player.getUuid());
            dropGift(player);
        }
        return hit;
    }

    private void dropGift(SkyBlockPlayer player) {
        boolean timeForGiving = WinterGifts.timeForGiving();
        ItemType gift = WinterGifts.rollReindrakeHit(player.getUuid(), timeForGiving, ThreadLocalRandom.current());
        if (gift == null) return;
        giveGift(player, gift);
        SkyBlockPlayer angler = getAngler();
        if (angler != null && angler != player && angler.isOnline()) {
            giveGift(angler, WinterGifts.pickGift(WinterGifts.reindrakeGifts(timeForGiving), ThreadLocalRandom.current()));
        }
    }

    private void giveGift(SkyBlockPlayer player, ItemType gift) {
        SkyBlockItem item = new SkyBlockItem(gift);
        player.addAndUpdateItem(item);
        player.sendMessage(Text.of("<a><l>GIFT! </l><7>The <4>Reindrake <7>dropped a {}<7>!", item.getDisplayNameText()));
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return null;
    }

    @Override
    public void kill() {
        SkyBlockPlayer killer = killer();
        super.kill();
        Instance instance = getInstance();
        if (instance != null) {
            Text defeated = Text.of("<c><l>DEFEATED! </l><c>A <4>Reindrake <c>was slain and dropped all its loot!");
            instance.getPlayers().forEach(player -> player.sendMessage(defeated));
        }
        if (killer != null) {
            awardAnglerLoot();
            creditHitters(killer);
        }
    }

    private void awardAnglerLoot() {
        SkyBlockPlayer angler = getAngler();
        if (angler == null || !angler.isOnline()) return;
        for (WinterDrop drop : getCreature().getDrops()) {
            if (!LootRoll.roll(angler.getUuid(), drop.item(), drop.chancePercent())) continue;
            SkyBlockItem item = createDrop(drop.item(), drop.rollAmount());
            angler.addAndUpdateItem(item);
            if (drop.chancePercent() < 100) {
                angler.sendMessage(Text.of("<6><l>RARE DROP!</l> {}", item.getDisplayNameText()));
            }
        }
    }

    private void creditHitters(SkyBlockPlayer killer) {
        for (UUID uuid : hitters) {
            if (uuid.equals(killer.getUuid())) continue;
            SkyBlockPlayer hitter = SkyBlockGenericLoader.getFromUUID(uuid);
            if (hitter == null) continue;
            hitter.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class)
                    .getValue().increase(this, 1);
        }
    }

    @Override
    public void remove() {
        ALIVE.remove(this);
        if (dragon != null && !dragon.isRemoved()) dragon.remove();
        super.remove();
    }
}
