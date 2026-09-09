package net.swofty.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.swofty.commons.guild.GuildData;
import net.swofty.commons.guild.GuildResponseEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: confirms a member's mute state (muted or unmuted) was changed.
public class GuildMuteChangedResponseEvent extends GuildResponseEvent {
    private UUID muter;
    private String target;
    private long duration;
    private boolean unmute;

    public GuildMuteChangedResponseEvent(GuildData guild, UUID muter, String target, long duration, boolean unmute) {
        super(guild);
        this.muter = muter;
        this.target = target;
        this.duration = duration;
        this.unmute = unmute;
    }

}
