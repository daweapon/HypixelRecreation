package dev.daweapon.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: a member left the guild voluntarily.
public class GuildLeaveRequestEvent extends GuildEvent {
    @NotNull
    private UUID leaver;

    public GuildLeaveRequestEvent(@NotNull UUID leaver) {
        super(null);
        this.leaver = leaver;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(leaver);
    }

}
