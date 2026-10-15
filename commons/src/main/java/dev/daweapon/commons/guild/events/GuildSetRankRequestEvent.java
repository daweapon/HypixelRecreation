package dev.daweapon.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: directly sets a member's rank to a named rank.
public class GuildSetRankRequestEvent extends GuildEvent {
    @NotNull
    private UUID setter;
    @NotNull
    private UUID target;
    @NotNull
    private String rankName;

    public GuildSetRankRequestEvent(@NotNull UUID setter, @NotNull UUID target, @NotNull String rankName) {
        super(null);
        this.setter = setter;
        this.target = target;
        this.rankName = rankName;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(setter, target);
    }

}
