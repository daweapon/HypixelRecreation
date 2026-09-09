package net.swofty.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.swofty.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: joins a named guild directly, without an invite - only succeeds if that guild has open join enabled.
public class GuildJoinRequestEvent extends GuildEvent {
    @NotNull
    private UUID joiner;
    @NotNull
    private String guildName;

    public GuildJoinRequestEvent(@NotNull UUID joiner, @NotNull String guildName) {
        super(null);
        this.joiner = joiner;
        this.guildName = guildName;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(joiner);
    }
}
