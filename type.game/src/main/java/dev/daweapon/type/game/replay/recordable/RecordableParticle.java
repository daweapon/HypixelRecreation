package dev.daweapon.type.game.replay.recordable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import dev.daweapon.commons.replay.protocol.ReplayDataReader;
import dev.daweapon.commons.replay.protocol.ReplayDataWriter;

import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
public class RecordableParticle extends AbstractRecordable {
	private byte[] data;

	public RecordableParticle(byte[] data) {
		this.data = data;
	}

	@Override
	public RecordableType getType() {
		return RecordableType.PARTICLE;
	}

	@Override
	public void write(ReplayDataWriter writer) throws IOException {
		writer.writeBytes(data);
	}

	@Override
	public void read(ReplayDataReader reader) throws IOException {
		this.data = reader.readBytes();
	}
}
