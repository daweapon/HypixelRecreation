package dev.daweapon.type.skyblockgeneric.miningevent;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class MiningEvent {
    private final MiningEventType type;
    private final MiningEventZone zone;
    private final Set<UUID> participants = new HashSet<>();
    private long startedAt;
    private long endsAt;

    protected MiningEvent(MiningEventType type, MiningEventZone zone) {
        this.type = type;
        this.zone = zone;
    }

    public final MiningEventType type() {
        return type;
    }

    public final MiningEventZone zone() {
        return zone;
    }

    public final long startedAt() {
        return startedAt;
    }

    public final long endsAt() {
        return endsAt;
    }

    public final long millisLeft() {
        return Math.max(0, endsAt - System.currentTimeMillis());
    }

    final void begin(long now, long durationMillis) {
        startedAt = now;
        endsAt = now + durationMillis;
        onStart();
    }

    protected final void shortenTo(long newEndsAt) {
        endsAt = Math.min(endsAt, newEndsAt);
    }

    protected final void markParticipant(SkyBlockPlayer player) {
        if (participants.add(player.getUuid())) MiningEvents.participate(player);
    }

    protected final Set<UUID> participants() {
        return participants;
    }

    public boolean isOver(long now) {
        return now >= endsAt;
    }

    public void tick(long now) {
    }

    protected boolean canStart() {
        return true;
    }

    protected void onCancelled() {
    }

    protected abstract void onStart();

    protected abstract void onEnd(boolean cancelled);


    public @Nullable Pos warpLocation() {
        return null;
    }

    public String statusLine() {
        return type.coloredName();
    }
}
