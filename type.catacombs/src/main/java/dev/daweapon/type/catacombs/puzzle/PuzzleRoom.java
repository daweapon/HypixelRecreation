package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleAction;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleController;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleState;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One puzzle room of a run, playing its puzzle in the world. The room's {@link PuzzleController} keeps the puzzle's
 * state; solving it unlocks the reward chest and opening that chest completes the room, failing it fails the room.
 */
public abstract class PuzzleRoom {
    private static final String FAIL = "fail";

    protected final PuzzleContext context;
    protected final PlacedRoom room;
    protected final RoomFrame frame;
    private final PuzzleController controller;

    protected PuzzleRoom(PuzzleContext context, PlacedRoom room, CatacombsPuzzle puzzle) {
        this.context = context;
        this.room = room;
        this.frame = new RoomFrame(room);
        this.controller = CatacombsPuzzleFactory.create(puzzle);
    }

    public abstract void start();

    /** A staff shortcut: plays the puzzle's own solve so its reward chest opens. */
    public abstract void solve(SkyBlockPlayer staff);

    /** Staff-facing lines about the puzzle's current state and solution. */
    public abstract List<String> info();

    public boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        return false;
    }

    public boolean interactEntity(SkyBlockPlayer player, Entity entity) {
        return false;
    }

    public void moved(SkyBlockPlayer player, Pos to, boolean onGround) {
    }

    public boolean arrowHit(SkyBlockPlayer player, int x, int y, int z) {
        return false;
    }

    /** Called with the run's five-tick poll. */
    public void poll() {
    }

    public void dispose() {
    }

    public CatacombsPuzzle puzzle() {
        return controller.puzzle();
    }

    public PlacedRoom room() {
        return room;
    }

    public PuzzleState state() {
        return controller.state();
    }

    public boolean over() {
        return controller.state() == PuzzleState.SOLVED || controller.state() == PuzzleState.FAILED;
    }

    protected final void advance(String objective) {
        controller.accept(PuzzleAction.of(objective, objective));
    }

    protected final void select(String choice) {
        controller.accept(PuzzleAction.of("select", choice));
    }

    /** Fails a failable puzzle for the party; the room turns red and the Skill score loses its puzzle points. */
    public final void fail(@Nullable SkyBlockPlayer player, @Nullable String message) {
        if (over()) return;
        controller.accept(PuzzleAction.of(FAIL, player == null ? "staff" : player.getUsername()));
        if (message != null) context.host().broadcast(message);
        context.failed().accept(room, player);
        failed();
    }

    protected void failed() {
    }

    protected final void completeRoom() {
        context.completed().accept(room);
    }

    protected final PuzzleChest chest(int[] at) {
        return new PuzzleChest(context, at, _ -> completeRoom());
    }

    protected final Instance instance() {
        return context.host().instance();
    }

    protected final void sound(String key, float volume, float pitch, double x, double y, double z) {
        instance().playSound(Sound.sound(Key.key(key), Sound.Source.BLOCK, volume, pitch), x, y, z);
    }

    protected final void later(int ticks, Runnable action) {
        context.host().later(ticks, action);
    }
}
