package dev.daweapon.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: mutes a member (or the whole guild) in guild chat for a duration.
public class GuildMuteRequestEvent extends GuildEvent {
    @NotNull
    private UUID muter;
    private String target; // TODO: UUID
    private long duration;

    public GuildMuteRequestEvent(@NotNull UUID muter, String target, long duration) {
        super(null);
        this.muter = muter;
        this.target = target;
        this.duration = duration;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(muter);
    }

}
