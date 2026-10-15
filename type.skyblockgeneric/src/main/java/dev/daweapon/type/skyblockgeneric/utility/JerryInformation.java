package dev.daweapon.type.skyblockgeneric.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import dev.daweapon.type.generic.entity.hologram.ServerHolograms;

@AllArgsConstructor
@Getter
@Setter
public class JerryInformation {
    private Entity jerry;
    private Pos jerryPosition;
    private ServerHolograms.ExternalHologram hologram;
}
