package dev.daweapon.type.generic.entity.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.entity.npc.walk.InstanceWalkGrid;
import dev.daweapon.type.generic.entity.npc.walk.NPCPathfinder;
import dev.daweapon.type.generic.entity.npc.walk.NPCRelocation;
import dev.daweapon.type.generic.entity.npc.walk.WalkRoute;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleSupplier;

/**
 * Moves one of the per-player NPC entities through an ordered set of waypoints.
 */
public final class NPCMovementController {
    /**
     * The vanilla base movement-speed attribute for a walking player.
     */
    public static final double WALKING_SPEED = 0.1;
    private static final int SEARCH_TIMEOUT_TICKS = 200;
    private static final ExecutorService PATH_SEARCHES = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().daemon().name("npc-path-search").factory());

    private final EntityCreature entity;
    private long movementId;
    private CompletableFuture<Void> currentMovement;
    private List<Pos> waypoints = List.of();
    private int waypointIndex;
    private boolean previousNoGravity;

    public NPCMovementController(EntityCreature entity) {
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    /**
     * Replaces any current route and walks through each supplied point in order.
     * The returned future completes after the NPC reaches the final point.
     */
    public synchronized CompletableFuture<Void> walkPath(List<Pos> points) {
        Objects.requireNonNull(points, "points");
        List<Pos> route = List.copyOf(points);

        cancelCurrentMovement();
        if (route.isEmpty()) return CompletableFuture.completedFuture(null);

        long id = ++movementId;
        this.waypoints = route;
        this.waypointIndex = 0;
        this.currentMovement = new CompletableFuture<>();
        this.previousNoGravity = entity.hasNoGravity();
        CompletableFuture<Void> movement = this.currentMovement;

        entity.setNoGravity(false);
        entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(WALKING_SPEED);
        moveToNextWaypoint(id);
        return movement;
    }

    /**
     * Moves the NPC to {@code destination} along a searched path at a walking pace, placing it there
     * instead once {@code viewerDistance} says it is out of the viewer's sight or the path runs out
     * before the destination. The returned future completes once the NPC is at the destination.
     */
    public synchronized CompletableFuture<Void> relocate(Pos destination, NPCRelocation mode, DoubleSupplier viewerDistance) {
        Objects.requireNonNull(destination, "destination");
        cancelCurrentMovement();
        Instance instance = entity.getInstance();
        if (instance == null || entity.isRemoved() || mode == NPCRelocation.TELEPORT) {
            if (instance != null && !entity.isRemoved()) entity.teleport(destination);
            return CompletableFuture.completedFuture(null);
        }

        long id = ++movementId;
        CompletableFuture<Void> movement = new CompletableFuture<>();
        this.currentMovement = movement;
        this.previousNoGravity = entity.hasNoGravity();
        entity.setNoGravity(true);

        Pos start = entity.getPosition();
        CompletableFuture<NPCPathfinder.Path> search = CompletableFuture.supplyAsync(
                () -> NPCPathfinder.find(new InstanceWalkGrid(instance), start, destination, mode.nodeBudget()), PATH_SEARCHES);
        Glide glide = new Glide(id, search, destination, mode.blocksPerTick(), viewerDistance);
        entity.scheduler().submitTask(glide::tick);
        return movement;
    }

    private final class Glide {
        private final long id;
        private final CompletableFuture<NPCPathfinder.Path> search;
        private final Pos destination;
        private final double blocksPerTick;
        private final DoubleSupplier viewerDistance;
        private WalkRoute route;
        private boolean reachesDestination;
        private double walked;
        private int waited;

        private Glide(long id, CompletableFuture<NPCPathfinder.Path> search, Pos destination, double blocksPerTick,
                      DoubleSupplier viewerDistance) {
            this.id = id;
            this.search = search;
            this.destination = destination;
            this.blocksPerTick = blocksPerTick;
            this.viewerDistance = viewerDistance;
        }

        private TaskSchedule tick() {
            synchronized (NPCMovementController.this) {
                if (id != movementId || currentMovement == null || currentMovement.isDone()) return TaskSchedule.stop();
                if (entity.isRemoved() || entity.getInstance() == null) {
                    failMovement(new IllegalStateException("NPC was removed while walking"));
                    return TaskSchedule.stop();
                }
                if (route == null && !startRoute()) {
                    if (++waited < SEARCH_TIMEOUT_TICKS) return TaskSchedule.nextTick();
                    return arrive(true);
                }
                walked += blocksPerTick;
                boolean finished = walked >= route.length();
                if (NPCRelocation.vanishes(viewerDistance.getAsDouble(), reachesDestination, finished)) return arrive(true);
                if (finished) return arrive(false);
                entity.refreshPosition(route.at(walked, entity.getPosition().yaw()));
                return TaskSchedule.nextTick();
            }
        }

        private boolean startRoute() {
            if (!search.isDone()) return false;
            NPCPathfinder.Path path = search.exceptionally(_ -> null).join();
            if (path == null) {
                route = new WalkRoute(List.of(entity.getPosition().asVec()), destination);
                reachesDestination = false;
                return true;
            }
            route = new WalkRoute(path.points(), destination);
            reachesDestination = path.complete();
            return true;
        }

        private TaskSchedule arrive(boolean teleport) {
            if (!teleport) {
                entity.refreshPosition(destination);
                finishMovement();
                return TaskSchedule.stop();
            }
            entity.teleport(destination).whenComplete((_, _) -> {
                synchronized (NPCMovementController.this) {
                    if (id == movementId && currentMovement != null && !currentMovement.isDone()) finishMovement();
                }
            });
            return TaskSchedule.stop();
        }
    }

    /**
     * Stops the active route at the NPC's current position.
     */
    public synchronized void stop() {
        cancelCurrentMovement();
        movementId++;
    }

    public synchronized boolean isMoving() {
        return currentMovement != null && !currentMovement.isDone();
    }

    private void moveToNextWaypoint(long id) {
        if (id != movementId || currentMovement == null || currentMovement.isDone()) return;
        if (waypointIndex >= waypoints.size()) {
            finishMovement();
            return;
        }
        if (entity.getInstance() == null || entity.isRemoved()) {
            failMovement(new IllegalStateException("Cannot move an NPC that is not spawned"));
            return;
        }

        Pos target = waypoints.get(waypointIndex);
        if (entity.getPosition().sameBlock(target)) {
            reachWaypoint(id, target);
            return;
        }

        try {
            if (!entity.getInstance().isChunkLoaded(target)) {
                entity.getInstance().loadChunk(target).join();
            }

            AtomicBoolean callbackRan = new AtomicBoolean();
            boolean pathStarted = entity.getNavigator().setPathTo(target, 0.1, () -> {
                callbackRan.set(true);
                synchronized (NPCMovementController.this) {
                    reachWaypoint(id, target);
                }
            });

            if (!pathStarted && !callbackRan.get()) {
                failMovement(new IllegalStateException("No path found to NPC waypoint " + target));
            }
        } catch (RuntimeException exception) {
            failMovement(exception);
        }
    }

    private void reachWaypoint(long id, Pos target) {
        if (id != movementId || currentMovement == null || currentMovement.isDone()) return;
        if (!entity.getPosition().sameBlock(target)) {
            failMovement(new IllegalStateException("NPC could not reach waypoint " + target));
            return;
        }

        // Navigator stops within the target block. Preserve the exact coordinate and view
        // supplied by mission/scene authors before continuing to the next waypoint.
        entity.refreshPosition(target);
        waypointIndex++;
        moveToNextWaypoint(id);
    }

    private void finishMovement() {
        entity.getNavigator().reset();
        entity.setNoGravity(previousNoGravity);
        CompletableFuture<Void> completed = currentMovement;
        clearRoute();
        completed.complete(null);
    }

    private void failMovement(Throwable throwable) {
        entity.getNavigator().reset();
        entity.setNoGravity(previousNoGravity);
        CompletableFuture<Void> failed = currentMovement;
        clearRoute();
        if (failed != null) failed.completeExceptionally(throwable);
    }

    private void cancelCurrentMovement() {
        if (currentMovement == null || currentMovement.isDone()) return;

        entity.getNavigator().reset();
        entity.setNoGravity(previousNoGravity);
        CompletableFuture<Void> cancelled = currentMovement;
        clearRoute();
        cancelled.completeExceptionally(new CancellationException("NPC movement was replaced or stopped"));
    }

    private void clearRoute() {
        currentMovement = null;
        waypoints = List.of();
        waypointIndex = 0;
    }
}
