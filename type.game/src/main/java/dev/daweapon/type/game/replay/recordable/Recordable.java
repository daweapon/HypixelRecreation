package dev.daweapon.type.game.replay.recordable;

import dev.daweapon.commons.replay.protocol.ReplayDataReader;
import dev.daweapon.commons.replay.protocol.ReplayDataWriter;

import java.io.IOException;

public interface Recordable {
	RecordableType getType();

	void write(ReplayDataWriter writer) throws IOException;

	void read(ReplayDataReader reader) throws IOException;

	int getTick();

	void setTick(int tick);

	default boolean isEntityState() {
		return false;
	}

	default int getEntityId() {
		return -1;
	}
}
