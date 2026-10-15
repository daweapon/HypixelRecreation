package dev.daweapon.type.prototypelobby.parkour;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.data.datapoints.DatapointParkourData;
import dev.daweapon.type.lobby.parkour.Parkour;

import java.util.List;

// Defines the parkour course layout for the Prototype Lobby.
public class PrototypeLobbyParkour implements Parkour {

	@Override
	public DatapointParkourData.ParkourType getId() {
		return DatapointParkourData.ParkourType.PROTOTYPE_LOBBY;
	}

	@Override
	public Pos getStartLocation() {
		return new Pos(2.5, 77, 42.5, -135, 0);
	}

	@Override
	public List<Point> getCheckpoints() {
		return List.of(
				new BlockVec(3, 77, 41),
				new BlockVec(40, 91, -10),
				new BlockVec(-38, 94, -25),
				new BlockVec(-7, 81, 38)
		);
	}
}
