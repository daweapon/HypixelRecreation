package dev.daweapon.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: demotes a guild member to the next rank down.
public class GuildDemoteRequestEvent extends GuildEvent {
    @NotNull
    private UUID demoter;
    @NotNull
    private UUID target;

    public GuildDemoteRequestEvent(@NotNull UUID demoter, @NotNull UUID target) {
        super(null);
        this.demoter = demoter;
        this.target = target;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(demoter, target);
    }

}
