package dev.daweapon.commons.guild;

import lombok.NoArgsConstructor;

@NoArgsConstructor
// Base class for guild-system events sent back to a server as a response/notification, as opposed to a client request.
public abstract class GuildResponseEvent extends GuildEvent {
    public GuildResponseEvent(GuildData guild) {
        super(guild);
    }
}
