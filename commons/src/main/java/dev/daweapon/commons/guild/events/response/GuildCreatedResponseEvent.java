package dev.daweapon.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildResponseEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: confirms a new guild was created.
public class GuildCreatedResponseEvent extends GuildResponseEvent {
    private UUID creator;

    public GuildCreatedResponseEvent(GuildData guild, UUID creator) {
        super(guild);
        this.creator = creator;
    }

}
