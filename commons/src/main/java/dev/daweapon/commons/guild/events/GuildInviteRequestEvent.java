package dev.daweapon.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: a member invited another player to the guild.
public class GuildInviteRequestEvent extends GuildEvent {
    private @NotNull UUID inviter;
    private @NotNull UUID invitee;

    public GuildInviteRequestEvent(@NotNull UUID inviter, @NotNull UUID invitee) {
        super(null);
        this.inviter = inviter;
        this.invitee = invitee;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(inviter, invitee);
    }

}
