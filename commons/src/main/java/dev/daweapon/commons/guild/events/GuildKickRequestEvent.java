package dev.daweapon.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: a member kicked another member from the guild for a given reason.
public class GuildKickRequestEvent extends GuildEvent {
    @NotNull
    private UUID kicker;
    @NotNull
    private UUID target;
    @NotNull
    private String reason;

    public GuildKickRequestEvent(@NotNull UUID kicker, @NotNull UUID target, @NotNull String reason) {
        super(null);
        this.kicker = kicker;
        this.target = target;
        this.reason = reason;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(kicker, target);
    }

}
