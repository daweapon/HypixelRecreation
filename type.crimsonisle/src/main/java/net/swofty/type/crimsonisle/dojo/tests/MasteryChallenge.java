package net.swofty.type.crimsonisle.dojo.tests;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.dojo.DojoArena;
import net.swofty.type.crimsonisle.dojo.DojoChallenge;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.event.actions.item.ActionUseBow;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.bow.BowRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test of Mastery: wool targets appear on wooden pillars around a small platform and ripen from
 * green (8) to yellow (16) to red (32) before vanishing. Shoot them with the arena bow. Each arrow's flight is
 * traced block by block, so a target of any colour counts whichever face the arrow strikes.
 */
public class MasteryChallenge extends DojoChallenge {
    private static final int PILLARS = 12;
    private static final double RING_RADIUS = 13;
    private static final int MAX_TARGETS = 4;
    private static final int SPAWN_INTERVAL = 18;
    private static final Stage[] STAGES = {
            new Stage(Block.LIME_WOOL, 8, 50),
            new Stage(Block.YELLOW_WOOL, 16, 40),
            new Stage(Block.RED_WOOL, 32, 26)};

    private final List<int[]> pillarTops = new ArrayList<>();
    private final List<Target> targets = new ArrayList<>();
    private final List<Flight> flights = new ArrayList<>();

    public MasteryChallenge(SkyBlockPlayer player) {
        super(player, DojoTest.MASTERY);
    }

    @Override
    protected void build() {
        arena.square(2, Block.POLISHED_ANDESITE);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < PILLARS; index++) {
            double angle = Math.PI * 2 * index / PILLARS + random.nextDouble(-0.15, 0.15);
            double radius = RING_RADIUS + random.nextDouble(-2, 3);
            int x = (int) Math.floor(DojoArena.CENTER_X + Math.cos(angle) * radius);
            int z = (int) Math.floor(DojoArena.CENTER_Z + Math.sin(angle) * radius);
            int top = DojoArena.STAND_Y + random.nextInt(0, 5);
            for (int y = DojoArena.FLOOR_Y; y < top; y++) arena.set(x, y, z, Block.OAK_LOG);
            pillarTops.add(new int[]{x, top, z});
        }
    }

    @Override
    protected Pos spawn() {
        return DojoArena.CENTER;
    }

    @Override
    protected List<ItemStack> kit() {
        ItemStack bow = ItemStacks.item(Material.BOW, """
                <f>Bow

                <7>Shoot the targets before they disappear!""").build();
        return List.of(bow, ItemStack.AIR, ItemStack.AIR, ItemStack.AIR, ItemStack.AIR, ItemStack.AIR,
                ItemStack.AIR, ItemStack.AIR, ItemStack.AIR, ItemStack.of(Material.ARROW));
    }

    @Override
    protected void tick() {
        traceArrows();
        if (targets.size() < MAX_TARGETS && ticks % SPAWN_INTERVAL == 0) spawnTarget();
        Iterator<Target> iterator = targets.iterator();
        while (iterator.hasNext()) {
            Target target = iterator.next();
            if (ticks < target.stageEndsAt) continue;
            target.stage++;
            if (target.stage >= STAGES.length) {
                arena.restore(target.x, target.y, target.z);
                iterator.remove();
                continue;
            }
            target.stageEndsAt = ticks + STAGES[target.stage].duration;
            arena.set(target.x, target.y, target.z, STAGES[target.stage].block);
        }
    }

    @Override
    public void onBowRelease(long useTicks) {
        double power = ActionUseBow.getBowPower(useTicks);
        if (power < 0.1) return;
        ArrowEntityImpl arrow = new ArrowEntityImpl(player, new SkyBlockItem(ItemType.FLINT_ARROW));
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec velocity = BowRegistry.calculateArrowVelocity(eye.pitch(), eye.yaw(), power);
        arrow.setVelocity(velocity);
        arrow.setInstance(DojoArena.instance(), eye.add(eye.direction().mul(0.5)));
        track(arrow);
        flights.add(new Flight(arrow, arrow.getPosition(), velocity.normalize()));
        sound("entity.arrow.shoot", 1f);
    }

    private void traceArrows() {
        Iterator<Flight> iterator = flights.iterator();
        while (iterator.hasNext()) {
            Flight flight = iterator.next();
            if (flight.arrow.isRemoved()) {
                iterator.remove();
                continue;
            }
            Pos now = flight.arrow.getPosition();
            Vec path = now.sub(flight.last).asVec();
            Vec heading = path.length() > 1e-3 ? path.normalize() : flight.heading;
            double length = path.length() + 0.6;
            Target hit = null;
            for (double step = 0; step <= length && hit == null; step += 0.2) {
                hit = targetAt(flight.last.add(heading.mul(step)));
            }
            flight.last = now;
            if (hit == null) continue;
            score(hit);
            flight.arrow.remove();
            iterator.remove();
        }
    }

    private Target targetAt(Point point) {
        for (Target target : targets) {
            if (target.x == point.blockX() && target.y == point.blockY() && target.z == point.blockZ()) return target;
        }
        return null;
    }

    private void score(Target target) {
        addScore(STAGES[target.stage].points);
        arena.restore(target.x, target.y, target.z);
        targets.remove(target);
        sound("entity.experience_orb.pickup", 0.8f + target.stage * 0.3f);
    }

    @Override
    public void onArrowHit(Point block) {
        Target target = targetAt(block);
        if (target != null) score(target);
    }

    private void spawnTarget() {
        List<int[]> free = new ArrayList<>(pillarTops);
        free.removeIf(top -> targets.stream().anyMatch(target -> target.x == top[0] && target.z == top[2]));
        if (free.isEmpty()) return;
        int[] top = free.get(ThreadLocalRandom.current().nextInt(free.size()));
        Target target = new Target(top[0], top[1], top[2], ticks + STAGES[0].duration);
        arena.set(target.x, target.y, target.z, STAGES[0].block);
        targets.add(target);
    }

    private record Stage(Block block, int points, int duration) {
    }

    private static final class Flight {
        private final ArrowEntityImpl arrow;
        private final Vec heading;
        private Pos last;

        private Flight(ArrowEntityImpl arrow, Pos last, Vec heading) {
            this.arrow = arrow;
            this.last = last;
            this.heading = heading;
        }
    }

    private static final class Target {
        private final int x;
        private final int y;
        private final int z;
        private int stage;
        private long stageEndsAt;

        private Target(int x, int y, int z, long stageEndsAt) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.stageEndsAt = stageEndsAt;
        }
    }
}
