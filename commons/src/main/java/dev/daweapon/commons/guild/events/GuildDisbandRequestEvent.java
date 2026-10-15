package dev.daweapon.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: the guild master requested disbanding the guild.
public class GuildDisbandRequestEvent extends GuildEvent {
    @NotNull
    private UUID disbander;

    public GuildDisbandRequestEvent(@NotNull UUID disbander) {
        super(null);
        this.disbander = disbander;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(disbander);
    }

}
