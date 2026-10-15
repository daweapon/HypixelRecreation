package dev.daweapon.commons.guild.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: reports a member's GEXP contribution and whether it came from a win, for guild/daily progress tracking.
public class GuildProgressRequestEvent extends GuildEvent {
    private UUID player;
    private long gexp;
    private boolean win;

    @JsonCreator
    public GuildProgressRequestEvent(@JsonProperty("player") UUID player, @JsonProperty("gexp") long gexp,
                                     @JsonProperty("win") boolean win) {
        super(null);
        this.player = player;
        this.gexp = gexp;
        this.win = win;
    }
}
