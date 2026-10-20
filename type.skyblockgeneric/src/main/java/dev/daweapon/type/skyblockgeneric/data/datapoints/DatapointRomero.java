package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

public class DatapointRomero extends SkyBlockDatapoint<DatapointRomero.RomeroData> {
	private static final Serializer<RomeroData> serializer = new Serializer<>() {
		@Override
		public String serialize(RomeroData value) {
			return new JSONObject().put("step", value.step).put("delivered", value.delivered).toString();
		}

		@Override
		public RomeroData deserialize(String json) {
			if (json == null || json.isEmpty()) return new RomeroData();
			try {
				JSONObject obj = new JSONObject(json);
				RomeroData data = new RomeroData();
				data.step = obj.optInt("step", 0);
				data.delivered = obj.optBoolean("delivered", false);
				return data;
			} catch (Exception e) {
				return new RomeroData();
			}
		}

		@Override
		public RomeroData clone(RomeroData value) {
			RomeroData cloned = new RomeroData();
			cloned.step = value.step;
			cloned.delivered = value.delivered;
			return cloned;
		}
	};

	public DatapointRomero(String key, RomeroData value) {
		super(key, value, serializer);
	}

	public DatapointRomero(String key) {
		super(key, new RomeroData(), serializer);
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class RomeroData {
		private int step;
		private boolean delivered;
	}
}
