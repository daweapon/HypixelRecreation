package dev.daweapon.type.skyblockgeneric.data.datapoints;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftPity;
import org.json.JSONObject;

public class DatapointMineshaftPity extends SkyBlockDatapoint<DatapointMineshaftPity.PityData> {
	private static final Serializer<PityData> serializer = new Serializer<>() {
		@Override
		public String serialize(PityData value) {
			JSONObject json = new JSONObject();
			json.put("counter", value.counter);
			json.put("exitedAt", value.exitedAt);
			json.put("exitedServer", value.exitedServer == null ? "" : value.exitedServer);
			json.put("shaftsEntered", value.shaftsEntered);
			return json.toString();
		}

		@Override
		public PityData deserialize(String json) {
			PityData data = new PityData();
			if (json == null || json.isEmpty()) return data;
			try {
				JSONObject obj = new JSONObject(json);
				data.counter = obj.optInt("counter", MineshaftPity.START);
				data.exitedAt = obj.optLong("exitedAt", 0L);
				data.exitedServer = obj.optString("exitedServer", "").isEmpty() ? null : obj.getString("exitedServer");
				data.shaftsEntered = obj.optInt("shaftsEntered", 0);
			} catch (Exception ignored) {
			}
			return data;
		}

		@Override
		public PityData clone(PityData value) {
			PityData cloned = new PityData();
			cloned.counter = value.counter;
			cloned.exitedAt = value.exitedAt;
			cloned.exitedServer = value.exitedServer;
			cloned.shaftsEntered = value.shaftsEntered;
			return cloned;
		}
	};

	public DatapointMineshaftPity(String key, PityData value) {
		super(key, value, serializer);
	}

	public DatapointMineshaftPity(String key) {
		super(key, new PityData(), serializer);
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class PityData {
		private int counter = MineshaftPity.START;
		private long exitedAt;
		private String exitedServer;
		private int shaftsEntered;
	}
}
